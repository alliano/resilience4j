package com.reselince4j;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
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

}
