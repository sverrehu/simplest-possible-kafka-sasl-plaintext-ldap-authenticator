# Simplest Possible Kafka SASL_PLAINTEXT LDAP Authenticator

A Kafka `AuthenticateCallbackHandler` that uses a directory (LDAP/Active
Directory) to verify a username and a plain-text password.

Based on LDAP code I wrote back in 2006. The Kafka integration is work
in progress, and not production-ready.

## Build requirements

* JDK 8+
* Maven 3
* Docker (only for the `LdapUsernamePasswordAuthenticatorIntegrationTest`.
  `@Ignore` this test if you don't have Docker.)

## Configuration
  
Configuration is done using Kafka properties: Either
`dot.separated.properties`, or `KAFKA_ENVIRONMENT_VARIABLES`. To use
environment variables, capitalize every letter of the original property,
replace dots with underscores, and prefix with `KAFKA_`.

During development I use Confluent's
[cp-all-in-one-community demo](https://github.com/confluentinc/cp-all-in-one/tree/5.5.1-post/cp-all-in-one-community)
for my Kafka environment, so all examples will use environment variables
as found in the `docker-compose.yaml`-file.

### Kafka integration

The `.jar`-file of this project must be made available on the Kafka
Broker classpath. For my test setup, it will be made automatically
available if mounted into the `/usr/share/java/kafka/` directory
like this (requires the project to be built first
(`mvn clean package`)):
```
volumes:
  - ./target/simplest-possible-kafka-sasl-plaintext-ldap-authenticator-1.0-SNAPSHOT.jar:/usr/share/java/kafka/simplest-possible-kafka-sasl-plaintext-ldap-authenticator-1.0-SNAPSHOT.jar:ro
```
Then set up a binding and a listener for `SASL_PLAINTEXT`, eg:
```
KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: ... ,SASLPLAINTEXT:SASL_PLAINTEXT
KAFKA_ADVERTISED_LISTENERS: ... ,SASLPLAINTEXT://broker:9099
```
Tell Kafka to enable SASL, and to use our class to handle the protocol
binding created above:
```
KAFKA_LISTENER_NAME_SASLPLAINTEXT_PLAIN_SASL_SERVER_CALLBACK_HANDLER_CLASS: no.shhsoft.kafka.auth.LdapAuthenticateCallbackHandler
KAFKA_SASL_ENABLED_MECHANISMS: PLAIN
KAFKA_LISTENER_NAME_SASLPLAINTEXT_PLAIN_SASL_JAAS_CONFIG: |
  org.apache.kafka.common.security.plain.PlainLoginModule required ;
```

### Authenticator configuration

The authenticator plug-in expects some configuration parameters to tell
it how to connect to and handle the LDAP Directory:
```
KAFKA_AUTHN_LDAP_HOST: openldap
KAFKA_AUTHN_LDAP_PORT: 389
KAFKA_AUTHN_LDAP_BASE_DN: dc=example,dc=com
KAFKA_AUTHN_LDAP_USERNAME_TO_DN_FORMAT: "cn=%s,ou=People,dc=example,dc=com"
```
LDAPS (TLS) is assumed if the port is 636. For all other ports, plain-text
LDAP is assumed. If using LDAPS with a self-signed certificate, the Broker JVM
must be told to trust your certificates. How to do that is beyond the scope
of this README.

The final parameter, `ldap.username.to.dn.format`, specifies how the incoming
username should be transformed to match whatever the directory expects as
part of a bind operation. The `%s` combination will be replaced by a properly
escaped version of what the user provided. On Active Directory this string
should often be specified as just `"%s"`, since the directory authenticates
using just the username without matching a full DN.

## The `docker-compose.yaml` file

The `docker-compose.yaml` file in the project root may be used to
set up a very minimal Kafka environment. The file is based on the
original from cp-all-in-one-community demo, with everything except
Zookeeper and the Broker left out. An OpenLDAP container has been
added, and it will be populated from
`src/test/resources/ldap/bootstrap.ldif` to have a user `testuser`
with password `secret`. In addition, the instructions from above
have been applied, to use this project's plug-in for authentication
on port 9099.

To run the minimal environment and follow the broker log, run the
following command (initial build step only needed once):
```
$ mvn clean package
$ docker-compose up -d
$ docker-compose logs -f broker
```
In another window, you may perform a test connection using
eg. `kafkacat`:

```
$ kafkacat -b broker:9099 -X security.protocol=SASL_PLAINTEXT -X sasl.mechanism=PLAIN -X sasl.username=testuser -X sasl.password=secret -L
Metadata for all topics (from broker 1: sasl_plaintext://broker:9099/1):
 1 brokers:
  broker 1 at broker:9099 (controller)
 1 topics:
  topic "__confluent.support.metrics" with 1 partitions:
    partition 0, leader 1, replicas: 1, isrs: 1
```
The Broker log should state something similar to the following:
```
[2020-09-22 12:10:06,817] INFO User "testuser" authenticated. (no.shhsoft.kafka.auth.LdapAuthenticateCallbackHandler)
```
If you attempt to authenticate using a user unknown to the directory,
the results will be different:
```
$ kafkacat -b broker:9099 -X security.protocol=SASL_PLAINTEXT -X sasl.mechanism=PLAIN -X sasl.username=foo -X sasl.password=bar -L
% ERROR: Failed to acquire metadata: Local: Broker transport failure
```
And for the Broker log:
```
[2020-09-22 12:26:30,459] INFO Authentication failure for user "cn=foo,ou=People,dc=example,dc=com": [LDAP: error code 49 - Invalid Credentials] (no.shhsoft.ldap.LdapUsernamePasswordAuthenticator)
[2020-09-22 12:26:30,459] WARN Authentication failed for user "foo". (no.shhsoft.kafka.auth.LdapAuthenticateCallbackHandler)
```
Connecting without SASL will yield a different result:
```
$ kafkacat -b broker:9099 -L
% ERROR: Failed to acquire metadata: Local: Timed out
```
with logs:
```
[2020-09-22 12:50:54,731] INFO [SocketServer brokerId=1] Failed authentication with /172.27.0.1 (Unexpected Kafka request of type METADATA during SASL handshake.) (org.apache.kafka.common.network.Selector)
```

## Misc. notes

* Since all this is based on plain-text passwords, you will want to
  run it over SSL/TLS.
* This is SASL_PLAIN, meaning that both the Kafka broker and the
  application the user wants to use will get access to the user's
  password before passing it on to the directory server where the
  authentication is taking place. In many environments this is
  considered not acceptable: Look for something like OAUTH or SAML
  instead.
* There is no caching of the authentication result, but it is trivial to
  implement if needed. If you do, please do not use the plain-text
  password as part of the cache key, but pass it through a hashing
  function first.

