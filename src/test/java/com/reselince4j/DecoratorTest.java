package com.reselince4j;

import java.time.Duration;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DecoratorTest {
    
    @SneakyThrows
    public void callMe() {
        log.info("Wait............");
        Thread.sleep(1000L);
    }

    @Test @SneakyThrows
    public void testDecorators() {

        RateLimiter rateLimiter = RateLimiter.of("rateLimiter", RateLimiterConfig.custom()
        .limitForPeriod(10)
        .limitRefreshPeriod(Duration.ofMinutes(1))  
        .build());

        Retry retry = Retry.of("retry", RetryConfig.custom()
        .maxAttempts(10)
        .waitDuration(Duration.ofMillis(10))
        .build());

        Runnable runnable = Decorators.ofRunnable(() -> callMe())
        .withRateLimiter(rateLimiter)
        .withRetry(retry)
        .decorate();

        for (int i = 0; i < 200; i++) {
            new Thread(runnable).start();
        }

        Thread.sleep(10_000L);
    }

    @SneakyThrows
    public String getName(){
        log.info("Wait.....................");
        throw new RuntimeException("sorry there is error");
    }

    @Test @SneakyThrows
    public void testFallback() {

        RateLimiter rateLimiter = RateLimiter.of("rateLimiter", RateLimiterConfig.custom()
        .limitForPeriod(10)
        .limitRefreshPeriod(Duration.ofMinutes(1))
        .build());

        Retry retry = Retry.of("retry", RetryConfig.custom()
        .maxAttempts(10)
        .waitDuration(Duration.ofMillis(10))
        .build());

        Bulkhead bulkhead = Bulkhead.of("bulkhead", BulkheadConfig.custom()
        .maxConcurrentCalls(10)
        .maxWaitDuration(Duration.ofSeconds(1))
        .build());

        Supplier<String> supplier = Decorators.ofSupplier(() -> getName())
        .withBulkhead(bulkhead)
        .withRetry(retry)
        .withRateLimiter(rateLimiter)
        /**
         * jikalau semua modul yang kita tambahakan (rateLimiter, bulkhead, retry) error
         * maka method waitFallback akan di eksekusi
         */
        .withFallback((throwable) -> "Sorry the name that you loking for not found")
        .decorate();
        System.out.println(supplier.get());
    }
}
