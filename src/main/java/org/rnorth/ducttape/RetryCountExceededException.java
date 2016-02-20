package org.rnorth.ducttape;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates repeated failure of an UnreliableSupplier
 */
public class RetryCountExceededException extends RuntimeException {

    public RetryCountExceededException(@NotNull String message, @NotNull Exception exception) {
        super(message, exception);
    }
}
