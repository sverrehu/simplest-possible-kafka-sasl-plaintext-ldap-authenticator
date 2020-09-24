package no.shhsoft.ldap;

import no.shhsoft.security.UsernamePasswordAuthenticator;
import no.shhsoft.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;
import java.util.Objects;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class LdapUsernamePasswordAuthenticator
implements UsernamePasswordAuthenticator {

    private static final Logger LOG = LoggerFactory.getLogger(LdapUsernamePasswordAuthenticator.class);

    private final LdapConnectionSpec ldapConnectionSpec;
    private final String usernameToDnFormat;

    public LdapUsernamePasswordAuthenticator(final LdapConnectionSpec ldapConnectionSpec, final String usernameToDnFormat) {
        this.ldapConnectionSpec = Objects.requireNonNull(ldapConnectionSpec);
        this.usernameToDnFormat = Objects.requireNonNull(usernameToDnFormat);
    }

    @Override
    public boolean authenticate(final String username, final char[] password) {
        if (StringUtils.isBlank(username)) {
            return false;
        }
        final String userDn = String.format(usernameToDnFormat, LdapUtils.escape(username));
        return authenticateByDn(userDn, password);
    }

    public boolean authenticateByDn(final String userDn, final char[] password) {
        if (StringUtils.isBlank(userDn) || password == null || password.length == 0) {
            return false;
        }
        final Hashtable<String, Object> env = new Hashtable<>();
        /* As per https://docs.oracle.com/javase/jndi/tutorial/ldap/connect/pool.html,
         * not using connection pooling, since we change the principal of the connection. */
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapConnectionSpec.getUrl());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userDn);
        env.put(Context.SECURITY_CREDENTIALS, password);
        LdapContext context = null;
        try {
            context = new InitialLdapContext(env, null);
            return true;
        } catch (final AuthenticationException e) {
            LOG.info("Authentication failure for user \"" + userDn + "\": " + e.getMessage());
            return false;
        } catch (final NamingException e) {
            throw new UncheckedNamingException(e);
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (final NamingException e) {
                    LOG.warn("Ignoring exception when closing LDAP context.", e);
                }
            }
        }
    }

}
