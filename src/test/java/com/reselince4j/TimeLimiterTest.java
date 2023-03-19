package com.reselince4j;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeLimiterTest {
    
    @SneakyThrows
    public String slowAction() {
        log.info("slow action please wait :)");
        Thread.sleep(5000L);
        return "Alliano";
    }

    @Test @SneakyThrows
    public void testTimeLimiter() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        /**
         * disini kita membungkus return value dari method slowAction() kedalam
         * object yang bernama Futute<T> karna untuk menajalankan TimeLimmiter itu 
         * kita harus membugkus aksi atau pekerjaan yang akan di eksekusi 
         * kedalam Future<T> atau CompletionStage<T>
         * 
         * utnk pembahasan ExceutorService dan Future dll ini akan di bahas di materi yang lain
         */
        Future<String> future =  executorService.submit(() -> slowAction());
        
        TimeLimiter timeLimiter = TimeLimiter.ofDefaults("timeLimiter");
        Callable<String> callable =  TimeLimiter.decorateFutureSupplier(timeLimiter, () -> future);
        
        callable.call();
    }

    @Test @SneakyThrows
    public void testTimeLimiterConfig() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(() -> slowAction());

        TimeLimiterConfig timeLimiterConifiguration = TimeLimiterConfig.custom()
        /**
         * timeoutDuration, ini maksudnya jikalau dalam 3 detik 
         * eksekusi program tak kunjung selesai maka 
         * timeoutDuration akan meng thorow exception
         */
        .timeoutDuration(Duration.ofSeconds(3))
        /**
         * cancelRunningFuture, ini maksudnya jikalau nanti terjadi exception pada
         * timeoutDuration, maka eksekusi programnya akan terus di lajutkan
         * atau tidak.
         * jika tetap di lanjutkan maka kita bisa set parameter nya true 
         * jika tidak bisa kita set dengan false
         */
        .cancelRunningFuture(true)
        .build();

        TimeLimiter timeLimiter = TimeLimiter.of("timeLimiter", timeLimiterConifiguration);
        Callable<String> callable = TimeLimiter.decorateFutureSupplier(timeLimiter, () -> future);
        callable.call();
    }
}
