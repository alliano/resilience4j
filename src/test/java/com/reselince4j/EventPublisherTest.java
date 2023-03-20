package com.reselince4j;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventPublisherTest {
    
    public String callMe() {
        log.info("Wait.............................");
        throw new RuntimeException("Sorry there is error");
    }

    @Test
    public void testEventPublisher() {
        Retry retry = Retry.ofDefaults("retry");

        /**
         * disini kita menambahkan even publiser ketika ada  
         * sauatu kejadian , misalnya saat error terjadi 
         * maka log ini akan di eksekusi
         */
        retry.getEventPublisher().onError(event -> log.info("Sorry we have some problem, please just a minute"));
        retry.getEventPublisher().onRetry(event -> log.info("We will retry the excecution again"));
        retry.getEventPublisher().onSuccess(event -> log.info("Cograts The excecution is successfuly"));

        try {
            Supplier<String> supplier = Retry.decorateSupplier(retry, () -> callMe());
            supplier.get();
        } catch (Exception e) {
            System.out.println("eksekusi yang gagal walaupun sudah di retry : "+retry.getMetrics().getNumberOfFailedCallsWithRetryAttempt());
            System.out.println("eksekusi yang gagal tampa retry : "+retry.getMetrics().getNumberOfFailedCallsWithoutRetryAttempt());
            System.out.println("eksekusi yang berhasil setelah di retry : "+retry.getMetrics().getNumberOfSuccessfulCallsWithRetryAttempt());
            System.out.println("eksekusi yang berhasil tampa di retry : "+retry.getMetrics().getNumberOfSuccessfulCallsWithoutRetryAttempt());
        }
    }

    @Test
    public void testEvenRegistry() {
        RetryRegistry retryRegistrty = RetryRegistry.ofDefaults();
        retryRegistrty.getEventPublisher().onEntryAdded(event -> {
            log.info("new retry added with name {}", event.getAddedEntry().getName());
            log.info("created at {}", event.getCreationTime());
            log.info("type {}", event.getEventType().name());
        });
        /**
         * jika di eksekusi maka ada 2 event yaang terdeteksi
         * karena jikalau kita membuat object dengan nama  yang sama pada 
         * registry nya itu akan megembalikan object yang sama
         */
        retryRegistrty.retry("service1");
        retryRegistrty.retry("service1");
        retryRegistrty.retry("service2");
    }
}
