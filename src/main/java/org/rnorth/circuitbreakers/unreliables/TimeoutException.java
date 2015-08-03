package org.rnorth.circuitbreakers.unreliables;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates timeout of an UnreliableSupplier
 */
public class TimeoutException extends RuntimeException {

    public TimeoutException(@NotNull String message, @Nullable Exception exception) {
        super(message, exception);
    }

    public TimeoutException(@NotNull Exception e) {
        super(e);
    }
}
