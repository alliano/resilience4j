package com.reselince4j;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
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

    @Test @SneakyThrows
    public void testSemaphoreConfiguration() {
        BulkheadConfig bulkheadConfiguration = BulkheadConfig.custom()
        /**
         * maxConcurrentCalls, adalah maksimal thread yang boleh
         * dieksekusi dalam satu waktu yang bersamaan, jikalau melebihi 10 
         * maka akan terjadi exception
         */
        .maxConcurrentCalls(5)
        /**
         * maxWaitDuration, adalah maksimala waktu tunggu eksekusi
         * thread, misalnya kita meng set maxConcurrentCalls nya 10 maka
         * dalam 1 waktu tersebut hanya boleh ada 10 thread yang jalan.
         * Setelah 10 thread tersebut jalan maka maxWaitDuration ini akan dijalankan
         * misal pada maxWaitDuration nya kita set 5 detik maka 
         * dalam 10 thread yang jalan tersebut harus selesai dalam 5 detik, kalo nga selesai
         * maka akan terjadi exception, akan terapi eksekusi kodenya tetap dilakukan hingga selesai.
         */
        .maxWaitDuration(Duration.ofSeconds(5))
        .build();
        // membuat object bulkhead dengan menggunakan konfigurasi yang telah kita buat diatas
        Bulkhead bulkhead = Bulkhead.of("bulkheadConfiguration", bulkheadConfiguration);
        for (int i = 0; i < 50; i++) {
            Runnable runnable = Bulkhead.decorateRunnable(bulkhead, () -> slowAction());
            new Thread(runnable).start();
        }    
        Thread.sleep(10_000L);
    }

    @Test @SneakyThrows
    public void testFixThreadPoolConfiguration() {

        ThreadPoolBulkheadConfig fixThreadPoolConfiguration = ThreadPoolBulkheadConfig.custom()
        /**
         * maxThreadPoolSize, ini untuk membatasi maksimal thread yang akan dieksekusi dalam satu waktu
         * jikalau kita set 5 maka artinya jikalau thread yang jalan itu cukup banyak maka thread pool 
         * akan menambahkan jumlah thread yang digunakan hingga mencapai batas maksimum (5)
         */
        .maxThreadPoolSize(5)
        /**
         * coreThreadPoolSize, ini digunakan untuk mengeksekusi thread yang diambil dari queueCapasity
         * jikalau kita set dengan 5 maka artinya dalam 1 waktu akan menjalankan 5 thread
         */
        .coreThreadPoolSize(5)
        /**
         * queueCapacity, ini digunakan untuk memberi maksimal antrian tugas yang akan di jalankan
         * defautnya jikalau kita nga set antrian yang diterima 100
         */
        .queueCapacity(100)
        .build();
        
        ThreadPoolBulkhead threadPoolBulkhead = ThreadPoolBulkhead.of("fixThreadPoolConfig", fixThreadPoolConfiguration);
        
        for(var i = 0; i < 20; i ++) {
            Supplier<CompletionStage<Void>> supplier = ThreadPoolBulkhead.decorateRunnable(threadPoolBulkhead, () -> slowAction());
            supplier.get();
        }
        Thread.sleep(10_000L);
    }

    @Test @SneakyThrows
    public void testSemaphoreRegistry() {
        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
        .maxConcurrentCalls(5)
        .maxWaitDuration(Duration.ofSeconds(6))
        .build();

        BulkheadRegistry bulkheadRegistry = BulkheadRegistry.ofDefaults();
        // menambahakan konfigurasi pada bulkhead registry dengan nama bulkheadConfiguration
        bulkheadRegistry.addConfiguration("bulkheadConfiguration", bulkheadConfig);
        // membuat object Bulkhead dari object BulkheadRegistry dan menggunakan konfigurasi yang 
        // telah kita definisikan diatas
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("bulkheadRegistry", bulkheadConfig);

        for (int i = 0; i < 10; i++) {
            Runnable runnable = Bulkhead.decorateRunnable(bulkhead, () -> slowAction());
            new Thread(runnable).start();
        }
        Thread.sleep(10_000L);
    }

    @Test @SneakyThrows
    public void testFixThreadPoolRegistry() {
        ThreadPoolBulkheadConfig threadPoolBulkheadConf = ThreadPoolBulkheadConfig.custom()
        .maxThreadPoolSize(5)
        .coreThreadPoolSize(5)
        .queueCapacity(100)
        .build();

        ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry = ThreadPoolBulkheadRegistry.ofDefaults();
        // menambahkan konfigurasi pada threadPoolBulkheadRegistry dengan nama threadPoolConfig
        threadPoolBulkheadRegistry.addConfiguration("threadPoolConfig", threadPoolBulkheadConf);
        // membuat object dari threadPoolBulkheadRegistry dengan menggunakan konfigurasi yang telah
        // kita buat diatas
        ThreadPoolBulkhead threadPoolBulkhead = threadPoolBulkheadRegistry.bulkhead("threadPoolBulkhead", threadPoolBulkheadConf);

        for(var i = 0; i < 10; i++){
            Supplier<CompletionStage<Void>> supplier = ThreadPoolBulkhead.decorateRunnable(threadPoolBulkhead, () -> slowAction());
            supplier.get();
        }
        Thread.sleep(10_000L);
    }
}
