# Timeout functions

The timeout functions provide a very simple way to ensure that a given block of code does not exceed a given execution
time.

The [Timeouts](http://rnorth.github.io/duct-tape/org/rnorth/ducttape/timeouts/Timeouts.html)
class allows dispatching a synchronous call that does not return a result ([doWithTimeout](http://rnorth.github.io/duct-tape/org/rnorth/ducttape/timeouts/Timeouts.html#doWithTimeout-int-java.util.concurrent.TimeUnit-java.lang.Runnable-))
 or a synchronous call that returns a result ([getWithTimeout](http://rnorth.github.io/duct-tape/org/rnorth/ducttape/timeouts/Timeouts.html#getWithTimeout-int-java.util.concurrent.TimeUnit-java.util.concurrent.Callable-)).

## Examples

Try to invoke an external service and return a result, but time out if it takes more than 5 seconds.

    try {
        result = Timeouts.getWithTimeout(5, TimeUnit.SECONDS, () -> {
            return myExternalService.doSomething();
        });
    } catch (TimeoutException e) {
        // handle failure - e.g. use a cached value, report an error, etc
    }

Try to invoke an external service without returning a result, but time out if it takes more than 30 seconds.

    Timeouts.doWithTimeout(30, TimeUnit.SECONDS, () -> {
        myExternalService.doSomething();
    });

## Key Javadocs

* **[Timeouts](http://rnorth.github.io/duct-tape/org/rnorth/ducttape/timeouts/Timeouts.html)**