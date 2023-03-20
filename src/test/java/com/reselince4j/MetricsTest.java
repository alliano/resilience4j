package com.reselince4j;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.github.resilience4j.retry.Retry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricsTest {

    @SneakyThrows
    public String sayHello() {
        log.info("error");
        throw new RuntimeException("Sorry thre is error");
    }

    @Test
    public void retryMetrics() {
        Retry retry = Retry.ofDefaults("retry");

        try {
            Supplier<String> supplier = Retry.decorateSupplier(retry, () -> sayHello());
            supplier.get();
        } catch (Exception e) {
            System.out.println("eksekusi yang gagal walaupun sudah di retry : "+retry.getMetrics().getNumberOfFailedCallsWithRetryAttempt());
            System.out.println("eksekusi yang gagal tampa retry : "+retry.getMetrics().getNumberOfFailedCallsWithoutRetryAttempt());
            System.out.println("eksekusi yang berhasil setelah di retry : "+retry.getMetrics().getNumberOfSuccessfulCallsWithRetryAttempt());
            System.out.println("eksekusi yang berhasil tampa di retry : "+retry.getMetrics().getNumberOfSuccessfulCallsWithoutRetryAttempt());
        }
    }
}
