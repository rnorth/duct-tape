package org.rnorth.circuitbreakers.unreliables;

/**
 * A variation on java.util.function.Supplier which allows checked exceptions to be thrown.
 * @param <T>
 */
public interface UnreliableSupplier<T> {
    T get() throws Exception;
}
