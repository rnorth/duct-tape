package org.rnorth.ducttape;

/**
 * Simple Preconditions check implementation.
 */
public class Preconditions {

    /**
     * Check that a given condition is true. Will throw an IllegalArgumentException otherwise.
     * @param message message to display if the precondition check fails
     * @param condition the result of evaluating the condition
     */
    public static void check(String message, boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException("Precondition failed: " + message);
        }
    }
}
