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
Broker classpath. 

TODO...

### Authenticator configuration

TODO...
