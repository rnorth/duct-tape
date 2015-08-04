# Circuit Breakers

> Circuit Breakers is a little Java 8 library providing an implementation of the
  [Circuit Breaker pattern](http://martinfowler.com/bliki/CircuitBreaker.html).
  It provides fault tolerant wrappers for code that calls external APIs and unreliable components, including those that
  may fail, time out, or return inconsistent results.

[![Circle CI](https://circleci.com/gh/rnorth/circuitbreakers.svg?style=svg)](https://circleci.com/gh/rnorth/circuitbreakers)

## Table of Contents

<!-- MarkdownTOC autolink=true bracket=round depth=4 -->

- [Use Case](#use-case)
- [Usage summary](#usage-summary)
    - [Circuit breakers](#circuit-breakers)
        - [Circuit Breaker Example](#circuit-breaker-example)
    - [Automatic retry functions](#automatic-retry-functions)
        - [Automatic retry Examples](#automatic-retry-examples)
- [Features/TODOs](#featurestodos)
- [Why not Hystrix?](#why-not-hystrix)
- [Maven dependency](#maven-dependency)
- [License](#license)
- [Contributing](#contributing)
- [Copyright](#copyright)

<!-- /MarkdownTOC -->


## Use Case

From [wikipedia](https://en.wikipedia.org/wiki/Circuit_breaker_design_pattern):

> Assume that your application connects to a database 100 times per second and the database fails. You do not want to have the same error reoccur constantly. You also want to handle the error quickly and gracefully without waiting for TCP connection timeout.
> Generally Circuit Breaker can be used to check the availability of an external service. An external service can be a database server or a web service used by the application.
> Circuit breaker detects failures and prevents the application from trying to perform the action that is doomed to fail (until its safe to retry).

## Usage summary

### Circuit breakers

In this library a breaker exists in one of two states:

* *OK*: the primary action will be invoked. If it fails by throwing an exception, a one-time failure
  action can be called, and the breaker will 'trip' into the BROKEN state.
* *BROKEN*: if provided, a fallback action will be invoked instead of the primary action.

#### Circuit Breaker Example

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

In this example, <1> will definitely be called the first time this piece of code is hit, and will
be called on subsequent invocations as long as the breaker remains in the OK state.

If <1> throws an exception, <2> and <3> will be called immediately, and the breaker will change into
the BROKEN state. Thereafter, every time this code is hit <3> will be called.

### Automatic retry functions

This library also includes utility classes to help with automatic retry where there's an element of unpredictability.

For example:

* connection attempts to networked resources,
* waiting for a file or resource managed by another process to become available
* many kinds of automated testing tasks

It's often best to use events/callbacks to manage these situations, but when these are not available as options we need
a polling approach. These retry functions try to reduce the amount of boilerplate code needed.

The `Unreliables` class caters for calls that may be unreliable:
* `retryUntilSuccess` retries a call that might throw exceptions. The call will be repeated (up to a time limit)
   until it returns a result.
* `retryUntilTrue` retries a call until it returns `true`

The `Inconsistents` class deals with calls that might return a result quickly, but that take some time to stabilize
on a consistent result. A call wrapped with `retryUntilConsistent` will be called until the result is stable (equal) for
a minimum period of time, or until a time limit is hit.

_N.B. these methods do not automatically pause between retries - they'll retry a call as quickly as possible. You'll
sometimes want to include a call to Thread.sleep() to reduce polling frequency. Also, these functions are fairly simple
and not optimized for performance or resource usage yet, so please test before using these under heavy load._

#### Automatic retry Examples

Try to obtain a database connection:

    Connection connection = Unreliables.retryUntilSuccess(30, TimeUnit.SECONDS, () -> {
        return driver.connect(databaseUrl, databaseInfo);
    });

Make sure a Selenium UI element has been updated before continuing:

    Unreliables.retryUntilTrue(3, TimeUnit.SECONDS, () -> {
        return statusLabel.getText().equals("Finished");
    });

Wait until a collection of UI elements has been fully updated by JavaScript code before continuing:

    List<WebElement> listItems = Inconsistents.retryUntilConsistent(300, 1000, TimeUnit.MILLISECONDS, () -> {
        return driver.findElements(By.cssSelector("li.product"));
    });

## Features/TODOs

* Support for calling a void-return `Runnable`, or getting a value from a `Callable` (see `tryDo` and `tryGet` methods)
* Optional automatic reset a given time after the last failure
* Optional holding of state in an external object or Map (aimed at allowing breaker state to be shared across a cluster)
* TODO: Configurable trip after _n_ consecutive failures
* TODO: Configurable trip at _x_% failure rate
* TODO: Configurable logging of breaker state transitions
* TODO: Support passing exceptions to `runOnFirstFailure` for handling
* TODO: Allow configuration of exceptions that should trip the breaker vs exceptions that should be re-thrown

## Why not Hystrix?

[Hystrix](https://github.com/Netflix/Hystrix) is a leading Java fault tolerance library that is undoubtedly more mature,
 more battle-tested and probably more robust fault tolerance library.
However, it is fairly prescriptive, and imposes a structure that might not fit with existing codebases. Circuitbreakers
is intended to be very easy to understand and easy to integrate, with least disruption: integration should not be much
harder than wrapping integration points.

If you need the strength of Hystrix or pre JDK-1.8 support then absolutely go for Hystrix. Otherwise, please at least
consider Circuitbreakers :)

## Maven dependency

    <dependencies>
        <dependency>
            <groupId>org.rnorth.circuitbreakers</groupId>
            <artifactId>circuitbreakers</artifactId>
            <version>1.0.3</version>
        </dependency>
    </dependencies>

## License

See [LICENSE](LICENSE).

## Contributing

* Star the project on Github and help spread the word :)
* [Post an issue](https://github.com/rnorth/circuitbreakers/issues) if you find any bugs
* Contribute improvements or fixes using a [Pull Request](https://github.com/rnorth/circuitbreakers/pulls). If you're going to contribute, thank you! Please just be sure to:
	* discuss with the authors on an issue ticket prior to doing anything big
	* follow the style, naming and structure conventions of the rest of the project
	* make commits atomic and easy to merge
	* verify all tests are passing. Build the project with `mvn clean install` to do this.

## Copyright

Copyright (c) 2014-2015 Richard North.

See [AUTHORS](AUTHORS) for contributors.
