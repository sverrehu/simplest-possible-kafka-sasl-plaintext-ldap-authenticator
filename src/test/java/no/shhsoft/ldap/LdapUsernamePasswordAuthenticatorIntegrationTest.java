package no.shhsoft.ldap;

import org.junit.ClassRule;
import org.junit.Test;
import org.zapodot.junit.ldap.EmbeddedLdapRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class LdapUsernamePasswordAuthenticatorIntegrationTest {

    @ClassRule
    public static final EmbeddedLdapRule LDAP_RULE = EmbeddedLdapUtils.createEmbeddedLdapRule();

    @Test
    public void shouldAcceptValidUserDnAndPassword() {
        final LdapUsernamePasswordAuthenticator authenticator = getAuthenticator();
        assertTrue(authenticator.authenticateByDn(EmbeddedLdapUtils.LDAP_ADMIN_DN, EmbeddedLdapUtils.LDAP_ADMIN_PASSWORD));
        assertTrue(authenticator.authenticateByDn(EmbeddedLdapUtils.EXISTING_RDN + "," + EmbeddedLdapUtils.LDAP_BASE_DN, EmbeddedLdapUtils.EXISTING_USER_PASSWORD));
    }

    @Test
    public void shouldAcceptValidUsernameAndPassword() {
        final LdapUsernamePasswordAuthenticator authenticator = getAuthenticator();
        assertTrue(authenticator.authenticate(EmbeddedLdapUtils.EXISTING_USERNAME, EmbeddedLdapUtils.EXISTING_USER_PASSWORD));
    }

    @Test
    public void shouldDenyEmptyUserDnOrPassword() {
        final LdapUsernamePasswordAuthenticator authenticator = getAuthenticator();
        assertFalse(authenticator.authenticateByDn(EmbeddedLdapUtils.LDAP_ADMIN_DN, null));
        assertFalse(authenticator.authenticateByDn(EmbeddedLdapUtils.LDAP_ADMIN_DN, "".toCharArray()));
        assertFalse(authenticator.authenticateByDn(null, EmbeddedLdapUtils.LDAP_ADMIN_PASSWORD));
        assertFalse(authenticator.authenticateByDn("", EmbeddedLdapUtils.LDAP_ADMIN_PASSWORD));
        assertFalse(authenticator.authenticateByDn(null, null));
        assertFalse(authenticator.authenticateByDn("", "".toCharArray()));
    }

    private LdapUsernamePasswordAuthenticator getAuthenticator() {
        return new LdapUsernamePasswordAuthenticator(EmbeddedLdapUtils.getLdapConnectionSpec(LDAP_RULE), EmbeddedLdapUtils.USERNAME_TO_DN_FORMAT);
    }

}
