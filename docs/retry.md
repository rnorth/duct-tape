# Automatic retry functions

This library also includes utility classes to help with automatic retry where there's an element of unpredictability.

For example:

* connection attempts to networked resources,
* waiting for a file or resource managed by another process to become available
* many kinds of automated testing tasks

It's often best to use events/callbacks to manage these situations, but when these are not available as options we need
a polling approach. These retry functions try to reduce the amount of boilerplate code needed.

## Calling *unreliable* code

The `Unreliables` class caters for calls that may be unreliable:
* `retryUntilSuccess` retries a call that might throw exceptions. The call will be repeated (up to a time limit)
   until it returns a result.
* `retryUntilTrue` retries a call until it returns `true`

## Calling *inconsistent* code

The `Inconsistents` class deals with calls that might return a result quickly, but that take some time to stabilize
on a consistent result. A call wrapped with `retryUntilConsistent` will be called until the result is stable (equal) for
a minimum period of time, or until a time limit is hit.

## A note about retry frequency

_These methods do not automatically pause between retries - they'll retry a call as quickly as possible. You'll
perhaps want to use a [rate limiter](ratelimiter.md) to reduce polling frequency. Also, these functions are fairly simple
and not optimized for performance or resource usage yet, so please test before using these under heavy load._

## Examples

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