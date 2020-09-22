package no.shhsoft.ldap;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
final class LdapContainerUtils {

    static final boolean USE_LDAPS = false;
    static final String LDAP_DOMAIN = "example.com";
    static final String LDAP_BASE_DN = "dc=example,dc=com";
    static final String LDAP_ADMIN_DN = "cn=admin," + LDAP_BASE_DN;
    static final char[] LDAP_ADMIN_PASSWORD = "admin".toCharArray();
    static final String EXISTING_USERNAME = "testuser";
    static final String EXISTING_RDN = "cn=" + EXISTING_USERNAME + ",ou=People";
    static final char[] EXISTING_USER_PASSWORD = "secret".toCharArray();
    static final String USERNAME_TO_DN_FORMAT = "cn=%s,ou=People," + LDAP_BASE_DN;

    private LdapContainerUtils() {
    }

    /* osixia will autogenerate
     * dn: {{ LDAP_BASE_DN }}
     * dn: cn=admin,{{ LDAP_BASE_DN }} */
    static GenericContainer<?> createContainer() {
        return new GenericContainer<>(DockerImageName.parse("osixia/openldap:1.4.0"))
               .withClasspathResourceMapping("/ldap/bootstrap.ldif", "/container/service/slapd/assets/config/bootstrap/ldif/50-bootstrap.ldif", BindMode.READ_ONLY)
               .withEnv("LDAP_DOMAIN", LDAP_DOMAIN)
               .withEnv("LDAP_BASE_DN", LDAP_BASE_DN)
               .withEnv("LDAP_ADMIN_PASSWORD", new String(LDAP_ADMIN_PASSWORD))
               .withEnv("LDAP_TLS_VERIFY_CLIENT", "never")
               .withExposedPorts(389, 636)
               .withCommand("--copy-service");
    }

    static LdapConnectionSpec getLdapConnectionSpec(final GenericContainer<?> ldapContainer) {
        return new LdapConnectionSpec(ldapContainer.getHost(), ldapContainer.getMappedPort(LdapContainerUtils.USE_LDAPS ? 636 : 389), USE_LDAPS, LDAP_BASE_DN);
    }

}
