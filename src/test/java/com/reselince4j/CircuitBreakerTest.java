package com.reselince4j;

import org.junit.jupiter.api.Test;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CircuitBreakerTest {
    
    public void callMe() {
        log.info("wait....");
        throw new RuntimeException("Sorry system error");
    }

    @Test
    public void testCircuitBreaker() {
        /**
         * disini kita membuat object circuit breaker dengan nama circuitBreaker
         */
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("circuitBreaker");

        for (int i = 0; i < 200; i++) {
            try {
                Runnable runnable = CircuitBreaker.decorateRunnable(circuitBreaker, () -> callMe());
                runnable.run();
            } catch (Exception ECX) {
                log.error("ERROR {}", ECX.getMessage());
            }
        }
    }
}
