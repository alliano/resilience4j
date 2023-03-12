package com.reselince4j;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RateLimiterTest {
    
    private final AtomicLong counter = new AtomicLong(0L);

    @Test
    public void testRateLomiter() {

        /**
         * disini kita membuat object RateLimiter dengan konfigurasi default
         * dan kita beri nama rateLimiter
         */
        RateLimiter rateLimiter = RateLimiter.ofDefaults("rateLimiter");

        Runnable runnable = RateLimiter.decorateRunnable(rateLimiter, () -> {
            /**
             * disini tidak akan terjadi error samasekali, karna perlu diingat
             * kita menggunakan konfigurasi defaut dari RateLimiter, 
             * dan default pengaturan pada RateLimiter adalah 50 
             * request per 500ns (nano second), jadi kode dibawah ini tidak meng
             * throw exception samasekali dikarnakan munngkin kode kita dibawah ini
             * tidak melebihi 50 request per 500ns
             */
            for(var i = 0; i < 10_000; i++) {
                long result = this.counter.incrementAndGet();
                log.info("Result: {}", result);
            }
        });
        runnable.run();   
    }

    @Test
    public void testRateLimiterConfing() {
        // membuat object RateLimiterConfig untuk meng konfigurasi RateLimiter kita nanti
        RateLimiterConfig rateLimiterConfiguration = RateLimiterConfig.custom()
        /**
         * limitRefreshPeriod(), ini digunakan untuk, kapan kita merefresh jumlah
         * requst yang telah kita terima, dalam setingan kali ini 
         * tiap 1 menit jumlah requst yang masuk akan di refresh(dibersihkan kembali lagi ke 0)
         * conotohnya, jikalau dalam 1 menit tesebut jumlah requst yang telah diterima
         * sebanyak 80 requst maka 80 requst tersebut akan di refresh atau di clear
         * menjadi 0 request, jadi nanti dalam hitungan RateLimiternya menjadi 0 requst.
         */
        .limitRefreshPeriod(Duration.ofMinutes(1))
        /**
         * limitForPeriod(), ini digunakan berapa banyak requst yang boleh di terima dalam
         * kurun waktu tertentu, dalam contoh kali ini jumlah request yang diizinkan 
         * yaitu 100 requst per 1 menitnya, jikalau dalam 1 menit requstnya lebih dari 100
         * maka limitForPeriod() akan meng throw exception dengan tipe RequstNotPermitted
         */
        .limitForPeriod(100)
        /**
         * timeoutDuration(), ini digunakan untuk membatasi lama eksekusi request nya,
         * dalam contoh kali ini, jikalau requst yang masuk itu saat di eksekusi 
         * memakan waktu lebih dari 3 detik maka timeoutDuration akan meng throw exception
         */
        .timeoutDuration(Duration.ofSeconds(3))
        .build();
        RateLimiter reteLimiter = RateLimiter.of("rateLimiter", rateLimiterConfiguration);

        for(var i = 0; i< 10_000; i++) {
            Runnable runnable = RateLimiter.decorateRunnable(reteLimiter, () -> {
                long result = this.counter.incrementAndGet();
                log.info("Result {}", result);
            });
            runnable.run();
        }
    }

    @Test
    public void testRateLimiterRegistry() {
        // disini kita membutat RateLimiterConfig untuk mengkonfigurasi RateLimiter
        RateLimiterConfig rateLimiterConfiguration = RateLimiterConfig.custom()
        // melakukan reset atau refresh pada counter(penghitung) dari requst yang masuk
        .limitRefreshPeriod(Duration.ofMinutes(1))
        // jumlah requst yang diizinkan masuk per 1 menit nya
        .limitForPeriod(100)
        // jika waktu eksekusi requst nya lebih dari 10 detik maka akan di throw exception
        .timeoutDuration(Duration.ofSeconds(2))
        .build();
        // membuat object RateLimiterRegistry untuk me manage object RateLimiter kita
        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();
        // menambahkan konfigurasi pada RateLimiter
        rateLimiterRegistry.addConfiguration("rateLimiterRegistryConf", rateLimiterConfiguration);
        /**
         * disini kita mambuat object RateLimiter dengan memanfaatkan method rateLimiter() yang menerima
         * 2 parameter yaitu :
         * name -> nama rateLimiter nya
         * configName -> nama object konfigurasi RateLimiter yang sudah kita tambahakan pada RateLimiterRegistry
         * perlu diingat jikalau kita membuat object RateLimiter dengan nama yang sama maka
         * obejct RateLimiter hanya di buat 1x, artinya object rateLimiter1 dan rateLimiter2 itu sama
         */
        RateLimiter rateLimiter1 = rateLimiterRegistry.rateLimiter("rateLimiter","rateLimiterRegistryConf");
        RateLimiter rateLimiter2 = rateLimiterRegistry.rateLimiter("rateLimiter","rateLimiterRegistryConf");

        Assertions.assertSame(rateLimiter1, rateLimiter2);

       for(var i = 0; i < 10_000; i++) {
        Runnable runnable = RateLimiter.decorateRunnable(rateLimiter1, () -> {
            long result = this.counter.incrementAndGet();
            log.info("Result {}", result);
        });
        runnable.run();
       }
    }
}
