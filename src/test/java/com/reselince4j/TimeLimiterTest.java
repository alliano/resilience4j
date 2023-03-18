package com.reselince4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import io.github.resilience4j.timelimiter.TimeLimiter;
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
}
