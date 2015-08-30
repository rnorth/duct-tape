# Circuit breakers

From [Martin Fowler](http://martinfowler.com/bliki/CircuitBreaker.html):

> The basic idea behind the circuit breaker is very simple. You wrap a protected function call in a circuit breaker
object, which monitors for failures. Once the failures reach a certain threshold, the circuit breaker trips, and all
further calls to the circuit breaker return with an error, without the protected call being made at all. Usually you'll
also want some kind of monitor alert if the circuit breaker trips.

## Usage summary

In this library a breaker exists in one of two states:

* `OK`: the primary action will be invoked. If it fails by throwing an exception, a one-time failure
  action can be called, and the breaker will 'trip' into the `BROKEN` state.
* `BROKEN`: if provided, a fallback action will be invoked instead of the primary action.

## Example

    // Creating a new breaker instance
    breaker = BreakerBuilder.newBuilder()
                            .build();

    // Using the breaker instance to perform a task which could potentially fail
    breaker.tryDo(() -> {
        someUnreliableService.sendMessage(message);    // <1>
    }, () -> {
        LOGGER.error("Service failed!");               // <2>
    }, () -> {
        fallbackQueue.add(message);                    // <3>
    });

In this example, **1** will definitely be called the first time this piece of code is hit, and will
be called on subsequent invocations as long as the breaker remains in the `OK` state.

If **1** throws an exception, **2** and **3** will be called immediately, and the breaker will change into
the `BROKEN` state. Thereafter, every time this code is hit **3** will be called.

## Key Javadocs

* **[BreakerBuilder](http://rnorth.github.io/duct-tape/org/rnorth/ducttape/circuitbreakers/BreakerBuilder.html)**
* **[Breaker](http://rnorth.github.io/duct-tape/org/rnorth/ducttape/circuitbreakers/Breaker.html)**

## Features/TODOs

* Support for calling a void-return `Runnable`, or getting a value from a `Callable` (see `tryDo` and `tryGet` methods)
* Optional automatic reset a given time after the last failure ([javadocs](http://rnorth.github.io/duct-tape/org/rnorth/ducttape/circuitbreakers/BreakerBuilder.html#autoResetAfter-long-java.util.concurrent.TimeUnit-))
* Optional holding of state in an external object or Map (aimed at allowing breaker state to be shared across a cluster - [javadocs](http://rnorth.github.io/duct-tape/org/rnorth/ducttape/circuitbreakers/BreakerBuilder.html#storeStateIn-java.util.concurrent.ConcurrentMap-java.lang.String-))
* TODO: Configurable trip after _n_ consecutive failures
* TODO: Configurable trip at _x_% failure rate
* TODO: Configurable logging of breaker state transitions
* TODO: Support passing exceptions to `runOnFirstFailure` for handling
* TODO: Allow configuration of exceptions that should trip the breaker vs exceptions that should be re-thrown