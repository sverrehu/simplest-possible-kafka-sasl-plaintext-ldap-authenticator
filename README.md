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
as found in the `docker-compose.yml`-file.

### Kafka integration

The `.jar`-file of this project must be made available on the Kafka
Broker classpath. For my test setup, it will be made automatically available if mounted into the `/usr/share/java/kafka/` directory like this:
```
volumes:
  - ./target/simplest-possible-kafka-sasl-plaintext-ldap-authenticator-1.0-SNAPSHOT.jar:/usr/share/java/kafka/simplest-possible-kafka-sasl-plaintext-ldap-authenticator-1.0-SNAPSHOT.jar:ro
```

TODO...

### Authenticator configuration

TODO...


## The `docker-compose.yaml` file

The `docker-compose.yaml` file in the project root may be used to
set up a very minimal Kafka environment. The file is based on the
original from cp-all-in-one-community demo, with everything except
Zookeeper and the Broker left out. An OpenLDAP container has been
added, and it will be populated from
`src/test/resources/ldap/bootstrap.ldif` to have a user `testuser`
with password `secret`. In addition the instructions from above
have been applied, to use this project's plug-in for authentication
on port 9099.

To run the minimal environment and follow the broker log, run the
following command (initial build step only needed once):
```
$ mvn clean package
$ docker-compose up -d
$ docker-compose logs -f broker
```

In another window, you may perform a test connection using eg. `kafkacat`:

```
$ kafkacat -b broker:9099 -X security.protocol=SASL_PLAINTEXT -X sasl.mechanism=PLAIN  -X sasl.username=testuser -X sasl.password=secret -L
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
