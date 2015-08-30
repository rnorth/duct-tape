# Rate limiter

Rate limiters allow control over the maximum frequency a block of code will be called.

At present this is simply done by performing a `Thread.sleep()` to achieve a given constant throughput rate, but could
be expanded to other timing techniques in the future.

## Examples

Limit calls to a third party service to a maximum of 2 per second.

    // Create a shared rate limiter object somewhere. The same rate limiter object should be reused at all call sites
    RateLimiter sharedRateLimiter = RateLimiterBuilder.newBuilder()
                                         .withRate(2, TimeUnit.SECONDS)
                                         .withConstantThroughput()
                                         .build();

    // ....

    // Somewhere else (the call site), wrap the call to the third party service in the rate limiter
    result = sharedRateLimiter.getWhenReady(() -> {
        return externalApi.fetchSomethingById(id)
    });

## Key Javadocs

* **[RateLimiterBuilder](http://rnorth.github.io/duct-tape/org/rnorth/ducttape/ratelimits/RateLimiterBuilder.html)**
* **[RateLimiter](http://rnorth.github.io/duct-tape/org/rnorth/ducttape/ratelimits/RateLimiter.html)**