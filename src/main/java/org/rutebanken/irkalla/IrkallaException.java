package org.rutebanken.irkalla;

/**
 *
 */
public class IrkallaException extends RuntimeException {

    public IrkallaException(String message) {
        super(message);
    }

    public IrkallaException(String message, Throwable cause) {
        super(message, cause);
    }
}
