package com.reselince4j;

import java.time.Duration;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class Reselince4jApplicationTests {

	public void callMe() {
		log.info("Try call me");
		throw new IllegalArgumentException("There is error");
	}

	@Test
	void testRetry() {
		// kita membuat object retry dan diberi nama retryTest
		Retry retry = Retry.ofDefaults("retryTest");
		/**
		 * tampa menggunakan lambda (not recomended)
		 *  */ 
		Runnable runnable = Retry.decorateRunnable(retry, new Runnable() {
					public void run(){
						callMe();
					}
				});
			runnable.run();
		
		/**
		 * menggunakan lanbda (recekomended)
		 * kode ini jikalau gagal akan dijalankan ulang, by default jikalau 
		 * gagal akan di ulangi 3x
		 * 
		 * disini kita menggunakan method decorateRunnable(retry, () -> callMe());
		 * maka ini akan mengembalikan object Runnable, method decorateRunnable()
		 * ini kita gunakan untuk method yang tidak mengembalikan apa apa, contohnya 
		 * adalah method callMe() yang kita buat diatas
		 *  */ 
		Runnable runnable2 = Retry.decorateRunnable(retry, () -> callMe());
		runnable2.run();
	}

	public String createSupplier() {
		log.info("Suplier called");
		throw new IllegalArgumentException("There is error");
	}

	@Test
	public void testRetrySupplier() {
		// membuat objec retry dengan nama supplier
		Retry retry = Retry.ofDefaults("supplier");
		/**
		 * disini kita akan menmanggil method yang mengembalikan object
		 * makan kita bisa menggunakan decorateSupplier(retry, Supplier)
		 */
		Supplier<String> supplier = Retry.decorateSupplier(retry, () -> createSupplier());

		// untuk menjalankan supplier nya kita gunakan method get();
		supplier.get();
	}

	public String retryConfex(String arg) {
		if(arg.equalsIgnoreCase("Alliano")) {
			log.info(arg + " detected");
			throw new IllegalArgumentException("Alliano detected");
		}
		else if(arg.equalsIgnoreCase("Itachi")) {
			log.info(arg + " detected");
			throw new RuntimeException("Itachi detected");
		}
		return "OK";
	}

	@Test
	public void retryConfiguration() {
		
		RetryConfig retryConfiguration = RetryConfig.custom()
		.maxAttempts(5)
		.waitDuration(Duration.ofSeconds(2))
		/**
		 * ignoreException() ini berarti jikalau ada error dengan tipe 
		 * IllegalArgumentException maka error akan tetap di 
		 * throw (akan tetap di lemparkan) dan
		 * tidak akan di retry lagi (hanya di eksekusi satu kali)
		 *  */ 
		.ignoreExceptions(IllegalArgumentException.class)
		/**
		 * retryException(), ini berarti jikala terjadi error dengan tipe
		 * RuntimeException() maka retry dari eksekusi kode akan dilakukan
		 */
		.retryExceptions(RuntimeException.class)
		.build();
		Retry retry = Retry.of("retryConf", retryConfiguration);
		Supplier<String> supplier = Retry.decorateSupplier(retry, () -> retryConfex("Itachi"));
		supplier.get();
	}

	@Test
	public void testRetryRegistry() {
		/**
		 * ini kita membuat object retryRegistry degan menggunakan
		 * konfigurasi default
		 */
		RetryRegistry retryRegistry = RetryRegistry.ofDefaults();

		/**
		 * saat kita melakukan retryRegistry.retry("retryRegistry");
		 * ini artinya kita membuat object retry dengan nama retryRegistry
		 * dan ketika kita melakukan hal tersebut lebih dari 1x
		 * maka RetryRegistry tidak akan membuat ulang object
		 * melainkan akan mengembalikan object yang sama
		 * jadi variabel retry1 dan retry2 itu adalah object yang sama
		 */
		Retry retry1 = retryRegistry.retry("retryRegistry");
		Retry retry2 = retryRegistry.retry("retryRegistry");

		Assertions.assertSame(retry1, retry2);
	}
}
