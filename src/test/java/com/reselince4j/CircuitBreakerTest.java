package com.reselince4j;

import java.time.Duration;
import org.junit.jupiter.api.Test;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
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

    @Test
    public void circuitBreakerConfiguration() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
        /**
         * slidingWindowType(SlidingWindowType.COUNT_BASED), ini artinya
         * circuit breaker akan berkerja dengan cara menghitung
         * request yang dieksekusi.
         * 
         * dan jikalau kita ingin circuit breaker menghitung 
         * request yang dieksekusi itu berdasarkan waktu maka kita bisa
         * menggunakan parameter SlidingWindowType.TIME_BASED pada
         * paramter method slidingWindowType.
         * 
         */
        .slidingWindowType(SlidingWindowType.COUNT_BASED)
        /**
         * falufailureRateThreshold(10f), ini maksudnya jikalau error 
         * rate nya itu lebih dari 10% maka state akan berubah menjadi CLOSE
         * dan tidak menerima request lagi untuk bebebrapa waktu
         */
        .failureRateThreshold(10f)
        /**
         * method ini akan bekerja mengikuti tipe yang telah kita set pada method
         * slidingWindowType(), jikalau kita set paraperter dari method slidingWindowType()
         * adalah SlideingWindowType.COUNT_BASED maka artinya jikalau sudah ada 
         * 10 request yang telah di eksekusi maka circuit brekaer akan mulai
         * menghitung eksekusi yang error dari 10 requst tersebut,
         * 
         * dan jikalau kita set parameter pada slidingWindowType() nya itu
         * SlidingWindowType.TIME_BASED maka ini artinya jikalau
         * circuit breaker sudah menerima requst selama 10 detik maka
         * circuit breaker akan mulai menghitung jumlah eksekusi yang error
         * selama 10 detik tersebut.
         */
        .slidingWindowSize(10)
        /**
         * minimumNumberOfCalls(10), ini artinya jikalau dalam eksekusi 
         * request nya itu terjadi error sebanyak 10 kali maka 
         * circuit breaker langsung mengubah state nya menjadi OPEN
         * dan menolak semua request, method ini biasanya dikombinasikan
         * dengan slidingWindowType dengan patameter TIME_BASED.
         * 
         * misalnya pada slidingWindowType kita set paramternya
         * TIME_BASED maka pada method slidingWIndowSize otomatis
         * akan menghitung eksekusi yang error berdasarkan waktu,
         * misalnya pada method slidingWindowSize nya kita set 20,
         * maka jikalau sudah 20 detik circuit breaker akan mulai
         * menghitung eksekusi error nya, dan pada method
         * minimumNumberOfCalls(10), maka ini kita tidak perduli sudah berapa
         * detik circuit breaker menerima requst, jikalau eksekusi error nya
         * terjadi 10 kali maka state akan diubah menjadi OPEN
         */
        .minimumNumberOfCalls(10)
        /**
         * waitDurationInOpenState(Duration.ofSeconds(5)), ini maksudnya
         * saat circuit breaker berada pada state OPEN, circuit 
         * breaker akan mengunggu selama 5 detik sebelum masuk 
         * ke state HALF_OPEN
         */
        .waitDurationInOpenState(Duration.ofSeconds(5))
        /**
         * permittedNumberOfCallsInHalfOpenState(4), ini maksudnya
         * padasaat circuit breaker berada pada state HALF_OPEN
         * circuit breaker akan menerima beberapa request saja,
         * dalam contoh ini circuit breaker hanya menerima 4 requst saja
         * dan jikalau 4 requst tersebut gagal jumlah eksekusinya lebih dari
         * 10%(sesuai dengan yang kita set pada failureRateThreshold) maka
         * circuit breaker akan kembali ke state OPEN dan menunggu beberapa waktu
         * untuk masuk ke state HALF_OPEN
         */
        .permittedNumberOfCallsInHalfOpenState(4)
        /**
         * maxWaitDurationInHalfOpenState(Duration.ofSeconds(2)), ini artinya
         * jikalau tidak ada requst pada saat di state HALF_OPEN
         * selama 2 detik maka circuit breaker akan kembali ke state OPEN
         * jikalau pada parameter maxWaitDurationInHalfOpenState() kita 
         * set parameter nya dengan 0 maka artinya walaupun tidak ada 
         * requst yang masuk ke circuit breaker maka circuit breaker
         * akan terap berada pada state HALF_OPEN
         */
        .maxWaitDurationInHalfOpenState(Duration.ofSeconds(2))
        /**
         * slowCallDurationThreshold(Duration.ofSeconds(3)), ini artinya
         * jika lama eksekusi kode program nya lebih dari 3 detik maka
         * circuit breaker akan menghitung bahwa itu gagal, Walaupun
         * dalam eksekusi nya tidak ada Exception samasekali
         */
        .slowCallDurationThreshold(Duration.ofSeconds(3))
        /**
         * slowCallRateThreshold(50), ini maksudnya jikalau
         * 50 eksekusi nya itu selama 2(sesuai setingan kita pada method maxWaitDurationInHalfOpenState) 
         * detik tidak selesai maka state akan berubah menajadi CLOSE
         */
        .slowCallRateThreshold(50)
        /**
         * ignoreExceptions(IllegalArgumentException.class), ini maksudnya adalah
         * jikalau dalam eksekudi nya itu terjadi error dengan tipe exception
         * IllegalArgumenException maka itu tidak dianggap sebuah eksekusi
         * yang gagal.
         */
        .ignoreExceptions(IllegalArgumentException.class)
        .build();

        CircuitBreaker circuitBreaker = CircuitBreaker.of("circuitBreaker", circuitBreakerConfig);
        for (int i = 0; i < 200; i++) {
            try {
                Runnable runnable = CircuitBreaker.decorateRunnable(circuitBreaker, this::callMe);
                runnable.run();
            } catch (Exception e) {
                log.error("ERROR {}", e.getMessage());
            }
        }
    }
}
