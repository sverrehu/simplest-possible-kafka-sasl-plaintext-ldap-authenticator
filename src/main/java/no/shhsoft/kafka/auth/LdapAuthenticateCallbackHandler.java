package no.shhsoft.kafka.auth;

import no.shhsoft.ldap.LdapUsernamePasswordAuthenticator;
import no.shhsoft.ldap.LdapConnectionSpec;
import no.shhsoft.security.UsernamePasswordAuthenticator;
import no.shhsoft.validation.Validate;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.plain.PlainAuthenticateCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class LdapAuthenticateCallbackHandler
implements AuthenticateCallbackHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LdapAuthenticateCallbackHandler.class);
    private static final String CONFIG_LDAP_HOST = "authn.ldap.host";
    private static final String CONFIG_LDAP_PORT = "authn.ldap.port";
    private static final String CONFIG_LDAP_BASE_DN = "authn.ldap.base.dn";
    private static final String CONFIG_LDAP_USERNAME_TO_DN_FORMAT = "authn.ldap.username.to.dn.format";
    private static final String SASL_PLAIN = "PLAIN";
    private UsernamePasswordAuthenticator authenticator;
    private final UsernamePasswordAuthenticatorFactory usernamePasswordAuthenticatorFactory;

    public interface UsernamePasswordAuthenticatorFactory {

        UsernamePasswordAuthenticator create(LdapConnectionSpec spec, String usernameToDnFormat);

    }

    public LdapAuthenticateCallbackHandler() {
        usernamePasswordAuthenticatorFactory = LdapUsernamePasswordAuthenticator::new;
    }

    public LdapAuthenticateCallbackHandler(final UsernamePasswordAuthenticatorFactory usernamePasswordAuthenticatorFactory) {
        this.usernamePasswordAuthenticatorFactory = Validate.notNull(usernamePasswordAuthenticatorFactory);
    }

    @Override
    public void configure(final Map<String, ?> configs, String saslMechanism, List<AppConfigurationEntry> jaasConfigEntries) {
        if (!SASL_PLAIN.equals(saslMechanism)) {
            throw new IllegalArgumentException("Only SASL mechanism \"" + SASL_PLAIN + "\" is supported.");
        }
        configure(configs);
    }

    private void configure(final Map<String, ?> configs) {
        final String host = getRequiredStringProperty(configs, CONFIG_LDAP_HOST);
        final int port = getRequiredIntProperty(configs, CONFIG_LDAP_PORT);
        final String baseDn = getRequiredStringProperty(configs, CONFIG_LDAP_BASE_DN);
        final String usernameToDnFormat = getRequiredStringProperty(configs, CONFIG_LDAP_USERNAME_TO_DN_FORMAT);
        authenticator = usernamePasswordAuthenticatorFactory.create(new LdapConnectionSpec(host, port, port == 636, baseDn), usernameToDnFormat);
        LOG.info("Configured.");
    }

    private int getRequiredIntProperty(final Map<String, ?> configs, final String name) {
        final String stringValue = getRequiredStringProperty(configs, name);
        try {
            return Integer.parseInt(stringValue);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Value must be numeric in configuration property \"" + name + "\".");
        }
    }

    private String getRequiredStringProperty(final Map<String, ?> configs, final String name) {
        final Object value = configs.get(name);
        if (value == null) {
            throw new IllegalArgumentException("Missing required configuration property \"" + name + "\".");
        }
        return value.toString();
    }

    @Override
    public void close() {
        LOG.info("Closed.");
    }

    @Override
    public void handle(final Callback[] callbacks)
    throws UnsupportedCallbackException {
        if (authenticator == null) {
            throw new IllegalStateException("Handler not properly configured.");
        }
        String username = null;
        PlainAuthenticateCallback plainAuthenticateCallback = null;
        for (final Callback callback : callbacks) {
            if (callback instanceof NameCallback) {
                username = ((NameCallback) callback).getDefaultName();
            } else if (callback instanceof PlainAuthenticateCallback) {
                plainAuthenticateCallback = (PlainAuthenticateCallback) callback;
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
        if (username == null) {
            throw new IllegalStateException("Expected NameCallback was not found.");
        }
        if (plainAuthenticateCallback == null) {
            throw new IllegalStateException("Expected PlainAuthenticationCallback was not found.");
        }
        final boolean authenticated = authenticator.authenticate(username, plainAuthenticateCallback.password());
        if (authenticated) {
            LOG.info("User \"" + username + "\" authenticated.");
        } else {
            LOG.warn("Authentication failed for user \"" + username + "\".");
        }
        plainAuthenticateCallback.authenticated(authenticated);
    }

}
