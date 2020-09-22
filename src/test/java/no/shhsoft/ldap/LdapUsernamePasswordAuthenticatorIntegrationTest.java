package no.shhsoft.ldap;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class LdapUsernamePasswordAuthenticatorIntegrationTest {

    @ClassRule
    public static final GenericContainer<?> LDAP_CONTAINER = LdapContainerUtils.createContainer();

    @Test
    public void shouldAcceptValidUserDnAndPassword() {
        final LdapUsernamePasswordAuthenticator authenticator = getAuthenticator();
        assertTrue(authenticator.authenticateByDn(LdapContainerUtils.LDAP_ADMIN_DN, LdapContainerUtils.LDAP_ADMIN_PASSWORD));
        assertTrue(authenticator.authenticateByDn(LdapContainerUtils.EXISTING_RDN + "," + LdapContainerUtils.LDAP_BASE_DN, LdapContainerUtils.EXISTING_USER_PASSWORD));
    }

    @Test
    public void shouldAcceptValidUsernameAndPassword() {
        final LdapUsernamePasswordAuthenticator authenticator = getAuthenticator();
        assertTrue(authenticator.authenticate(LdapContainerUtils.EXISTING_USERNAME, LdapContainerUtils.EXISTING_USER_PASSWORD));
    }

    @Test
    public void shouldDenyEmptyUserDnOrPassword() {
        final LdapUsernamePasswordAuthenticator authenticator = getAuthenticator();
        assertFalse(authenticator.authenticateByDn(LdapContainerUtils.LDAP_ADMIN_DN, null));
        assertFalse(authenticator.authenticateByDn(LdapContainerUtils.LDAP_ADMIN_DN, "".toCharArray()));
        assertFalse(authenticator.authenticateByDn(null, LdapContainerUtils.LDAP_ADMIN_PASSWORD));
        assertFalse(authenticator.authenticateByDn("", LdapContainerUtils.LDAP_ADMIN_PASSWORD));
        assertFalse(authenticator.authenticateByDn(null, null));
        assertFalse(authenticator.authenticateByDn("", "".toCharArray()));
    }

    private LdapUsernamePasswordAuthenticator getAuthenticator() {
        return new LdapUsernamePasswordAuthenticator(LdapContainerUtils.getLdapConnectionSpec(LDAP_CONTAINER), LdapContainerUtils.USERNAME_TO_DN_FORMAT);
    }

}
