package com.reselince4j;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BulkheadTest {
    
    private final AtomicLong counter = new AtomicLong(0L);

    /**
     * annotasi @SneakyThrows ini kita gunakan untuk 
     * menghandle exception yang dilemparkan,
     * jadi jikalau kita menggunakan @SneakyThrows, kita tidak perlu
     * mendeklarasikan throws exception pada mehtod kita ataupun
     * didalam body menthod kita
     */
    @SneakyThrows
    public void slowAction() {
        long result = this.counter.incrementAndGet();
        log.info("Slow act : {}", result);
        Thread.sleep(5_000L);
    }

    @Test 
    public void testSemaphore() {
        /**
         * disini kita membuat Object Bulkhead dengan nama bulkhead
         * dan menggunakan konfigurasi default,
         * by default jikalau kita membuat bulkhead degan cara
         * Bulkhead.ofDefaults(name), ini akan membuat bulkhead dengan 
         * tipe semaphore
         */
        Bulkhead bulkhead = Bulkhead.ofDefaults("bulkhead");
        for(var i = 0; i < 1000; i++) {
            Runnable runnable = Bulkhead.decorateRunnable(bulkhead, () -> slowAction());
            /**
             * ini kita tiap interasi akan membuat thread baru, artinya
             * kita akan membuat palarel excution kode program (asycnronus)
             * dan pada interasi ke 25 exception akan terjadi (BulkheadFullException),
             * karena jikalau kita menggunakan konfigurasi default dari Bulkhead
             * jumlah concurrent excution yang diizinkan adalah 25, jikalau jumlah 
             * concurrent execution nya lebih dari 25 maka akan terjadi exception
             */
            new Thread(runnable).start();
        }
    }

    @Test
    public void testFixThreadPool() {
        /**
         * membuat object BulkheadThreadPool dengan nama bulkheadThreadPool dan menggunakan 
         * konfigurasi default
         */
        ThreadPoolBulkhead bulkheadThreadPool = ThreadPoolBulkhead.ofDefaults("bulkheadThreadPool");

        for(var i = 0; i < 100; i++) {
            /**
             * disini tiap iterasi akan membuat thread baru
             * (Thread nya otomatis dibuatkan didalam decorateRunnable dan juga 
             * otomatis di jalanakan)
             * 
             * disini kana terjadi error ketika jumlah excecution palarel nya
             * lebih dari core processor komputer yang dipakai
             */
            Supplier<CompletionStage<Void>> supplier = ThreadPoolBulkhead.decorateRunnable(bulkheadThreadPool, () -> slowAction());
            supplier.get();
        }
    }
}
