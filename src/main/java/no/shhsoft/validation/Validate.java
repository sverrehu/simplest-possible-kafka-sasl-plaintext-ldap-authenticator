package no.shhsoft.validation;

/**
 * NOTE: Heavily trimmed version of Sverre's original utility.
 *
 * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
 */
public final class Validate {

    private Validate() {
    }

    public static <T> T notNull(final T o, final String message) {
        if (o == null) {
            throw new IllegalArgumentException(message);
        }
        return o;
    }

    public static <T> T notNull(final T o) {
        return notNull(o, "Object must not be null");
    }

}
