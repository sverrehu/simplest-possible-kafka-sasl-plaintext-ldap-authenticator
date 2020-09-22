package no.shhsoft.ldap;

/**
 * NOTE: Heavily trimmed version of Sverre's original utility.
 *
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class LdapUtils {

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private LdapUtils() {
    }

    public static String escape(final String s) {
        /* See RFC 2253, section 2.4 */
        final StringBuilder sb = new StringBuilder();
        final int len = s.length();
        for (int q = 0; q < len; q++) {
            final int c = s.charAt(q);
            boolean doEscape = false;
            if (q == 0 && (c == ' ' || c == '#')) {
                doEscape = true;
            } else if (q == len - 1 && c == ' ') {
                doEscape = true;
            } else if (",+\"\\<>;".indexOf(c) >= 0) {
                doEscape = true;
            } else if (c < 32 || c > 126) {
                /* The standard actually allows values outside this range, but since we are allowed
                 * to escape anything, we do it just to avoid potential problems. */
                /* Update 2007-04-24: only escape the low ones. */
                if (c < 32) {
                    doEscape = true;
                }
            }
            if (doEscape) {
                sb.append('\\');
                if (" #,+\"\\<>;".indexOf(c) >= 0) {
                    sb.append((char) c);
                } else {
                    if (c > 255) {
                        sb.append(HEX_CHARS[(c >> 12) & 0xf]);
                        sb.append(HEX_CHARS[(c >> 8) & 0xf]);
                        sb.append('\\');
                    }
                    sb.append(HEX_CHARS[(c >> 4) & 0xf]);
                    sb.append(HEX_CHARS[c & 0xf]);
                }
            } else {
                sb.append((char) c);
            }
        }
        return sb.toString();
    }

}
