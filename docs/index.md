# Duct Tape

> Duct Tape is a little Java 8 library providing fault tolerance support for code that calls external APIs and
  unreliable components, including those that may fail, time out, or return inconsistent results.

[![Circle CI](https://circleci.com/gh/rnorth/duct-tape.svg?style=svg)](https://circleci.com/gh/rnorth/duct-tape)

This library was formerly named `circuitbreakers`, but given the inclusion of more general purpose fault tolerance
features, it was renamed.

## Features

* **[Circuit breaker](circuitbreaker.md) pattern implementation:** graceful isolation of an external component after a
  failure occurs
* **[Automatic retry functions](retry.md):** try/retry a block of code until it succeeds, returns true, or returns a
  consistent result for long enough.
* **[Timeout functions](timeout.md):** quick and easy wrappers for code to automatically limit execution time
* **[Rate limiter](ratelimiter.md) implementation:** limit how often a block of code can be called

## Example

This example combines several of the above features:

    // Shared instances (e.g. fields on a singleton object)
    circuitBreaker = BreakerBuilder.newBuilder().build();
    rateLimiter = RateLimiterBuilder.newBuilder()
                                    .withRate(20, TimeUnit.SECONDS)
                                    .withConstantThroughput()
                                    .build();

    // ...

    // Try and get the result but trip a circuit breaker if the component is dead:
    Result result = circuitBreaker.tryGet(() -> {
        // Retry the call for up to 2s if an exception is thrown
        return Unreliables.retryUntilSuccess(2, TimeUnit.SECONDS, () -> {
            // Limit calls to a max rate of 20 per second
            return rateLimiter.getWhenReady(() -> {
                // Limit each call to 100ms
                return Timeouts.getWithTimeout(100, TimeUnit.MILLISECONDS, () -> {
                    // Actually call the external service/API/unreliable component
                    return exampleService.getValue("Hello World");
                });
            });
        });
    }, () -> {
        // Report the first time a failure occurs (probably fire off a monitoring alert IRL)
        LOGGER.error("Circuit breaker was tripped");
    }, () -> {
        // Provide a default value if the circuit breaker is tripped
        return DEFAULT_VALUE;
    });


## Why not Hystrix?

[Hystrix](https://github.com/Netflix/Hystrix) is a leading Java fault tolerance library that is undoubtedly more mature,
 more battle-tested and probably more robust.
However, it is fairly prescriptive, and imposes a structure that might not fit with existing codebases. Duct Tape
is intended to be very easy to understand and easy to integrate, with least disruption: integration should not be much
harder than wrapping integration points.

If you need the strength of Hystrix or pre JDK-1.8 support then absolutely go for Hystrix. Otherwise, please at least
consider Duct Tape :)

## Maven dependency

    <dependencies>
        <dependency>
            <groupId>org.rnorth.duct-tape</groupId>
            <artifactId>duct-tape</artifactId>
            <version>1.0.4</version>
        </dependency>
    </dependencies>

## License

See [LICENSE](LICENSE).

## Contributing

* Star the project on Github and help spread the word :)
* [Post an issue](https://github.com/rnorth/duct-tape/issues) if you find any bugs
* Contribute improvements or fixes using a [Pull Request](https://github.com/rnorth/duct-tape/pulls). If you're going to contribute, thank you! Please just be sure to:
	* discuss with the authors on an issue ticket prior to doing anything big
	* follow the style, naming and structure conventions of the rest of the project
	* make commits atomic and easy to merge
	* verify all tests are passing. Build the project with `mvn clean install` to do this.

## Copyright

Copyright (c) 2014-2015 Richard North.

See [AUTHORS](AUTHORS) for contributors.
