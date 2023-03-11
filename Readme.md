# Resileence4j
Reselince4j adalah liberary yang sangat rigan dan mudah digunakan, yang terinspirasi dari liberary Netflix Hystrix, namun reselince4j didesain unutk java 8 keatas dan pemograman fungsional.
Reselice4j itu ringan, karena liberary ini hanya membutuhkan satu liberary atau reselence4j ini hanya membuatuhkan satu dependency yaitu Vavr(javaslang), tidak membutuhkan Liberary lainya.
Reselince4j menyediakan high level feature untuk meningkatkan kemampuan fungsional interface, lambda expresion, dan method interface. Dan juga reselincer4j sangat mudular.

reference : https://github.com/resilience4j/resilience4j

# Resileence4j Patterens.
Ada beberapa Pttrens pada liberary resilience4j :
* Retry, unutk mengulangi eksekusi yang gagal.
* Circuit Breaker, sementara menolak eksekusi yang memungkinkan gagal.
* Rate Limiter, membatasi eksekusi dalam kurun waktu tertentu.
* Time Limiter, membatasi durasi waktu eksekusi.
* Bulkhed, membatasi eksekusi yang terjadi secara bebarengan/bersamaan.
* Chache, mengingat hasil eksekusi yang sukses.
* Fallback, menyediakan alternatif hasil dari eksekusi yang gagal.

# Manfaat Resilience4j.
Menjadikan applikasi kita lebih tahan terhadap kejadian error yang diluar dugaan.
Dan memasitikan applikasi kita tidak membuat masalah untuk applikasi lain ketika saling terintregasi.

# Menambahakan Liberary Resilience4j.
Untuk menggunakana Resilience4j didalam project kita, kita harus menambahkan dependency Resilience4j nya kedalam file pom.xml yang ada didalam project kita. Berikut ini adalah dependency nya :
``` xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
	<artifactId>resilience4j-all</artifactId>
	<version>2.0.2</version>
</dependency>
```
untuk versi latest nya bisa kunjungi disini : https://mvnrepository.com/artifact/io.github.resilience4j/resilience4j-retry

# Retry.
Retry merupakan module di Resilience4j yang bisa kita gunakan untuk mencoba melakukan eksekusi kode secara berulang dalam jumlah yang sudah kita tentukan.
Penggunakan Retry mempermudah kita ketika akan mengeksekusi kode yang bisa memungkinkan gagal, dan kita ingin mengulanginya lagi tampa harus menggunkan perulangan secara manual.
Implementasi dari modul ini adalah sebuah interface yang bernama Retry.

``` java
	@Test
	void testRetry() {
		// kita membuat object retry dan diberi nama retryTest
		Retry retry = Retry.ofDefaults("retryTest");
		
	}
```
# Execute Retry.
Konsep Resilience4j adalah membungkus functional interface atau lambda yang kita buat, yang secara otomatis akan menghasilkan object lainya yang sudah dibungkus dengan module Resilience4j.
Ketika kita mengeksekusi object yang telah kita buat diatas (retry), maka secara otomatis fitur Resilience4j akan digunakan pada object tersebut.

``` java
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
```

Menggunakan Retry untuk method yang mengembalikan Object 
``` java
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
```

# Retry Configuration
Saat kita membuat Retry menggunakan Retry.ofDefaults(), secara otomatis kita akan menggunakan pengaturan default.
Kadang pada kasus tertentu kita ingin menentukan pengaturan untuk Retry secara manual, contohnya ketika kita ingin menentukan berapakali melakukan retry ketika method gagal.
Untuk meng konfigurasi Retry kita bisa membuat Object RetryConfig sebelum membuat Object Retry.

# Pengaturan Retry
| Pengaturan	 | Default	| Keterangan								|
|----------------|----------|-------------------------------------------|
| maxAttemps	 |	3		| Seberapa banyak Retry Dilakukan			|
| waitDuration	 |	50ms	| Waktu menunggu sebelum melakukan Retry	|
| ignoreException|	empty	| Jenis Error yang tidak akan di Retry		|

untuk lebih lengkapnya bisa kunjungi disini https://resilience4j.readme.io/docs/retry#create-and-configure-retry

example :
``` java
	public String retryConfex(String arg) {
		if(arg.equalsIgnoreCase("Alliano")) {
			log.info(arg + " detected");
			throw new IllegalArgumentException("Alliano detected not Allowed");
		}
		else if(arg.equalsIgnoreCase("Itachi")) {
			log.info(arg + " detected not Allowed");
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
		 * RuntimeException() maka retry akan dilakukan sebanyak
		 * konfigurasi yang kita set (maxAttemps())
		 */
		.retryExceptions(RuntimeException.class)
		.build();
		Retry retry = Retry.of("retryConf", retryConfiguration);
		Supplier<String> supplier = Retry.decorateSupplier(retry, () -> retryConfex("Itachi"));
		supplier.get();
	}
```
# Retry Registry
Saat kita belajar java database, kita mengenal yang namanya database pooling, yaitu tempat untuk menyimpan semua koneksi ke database.
Resilience4j juga memiliki konsep ini, dengan nama Registry.
Registry adalah tempat untuk mentimpan object-object dari Resilience4j.
Dengan menggunakan Registry, kita bisa menggunakan ulang object yang sudah kita buat tampa harus membuat ulang object baru.
Penggunaan Registry adalah salahsatu best practice yang direkomendasikan ketika menggunakan Resilience4j.

``` java
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
```

# Config RetryRegistry
Salahsatu yang menarik di RetryRegistry yaitu kita bisa menambahkan defautl atau menambahakan default config atau menambahkan config yang sama dengan nama Retry nya.
Jika saat kita membuat RetryRegistry kita tidak menyertakan nama config nya, maka itu akan menggunakan default config.

``` java
	@Test
	public void testRegistryConfig() {

		// membuat konfigurasi untuk RetryRegistry
		RetryConfig retryConfiguration = RetryConfig.custom()
		.maxAttempts(5)
		.waitDuration(Duration.ofSeconds(2))
		.build();

		/**
		 * jikalau kita melakukan seperti ini, maka artinya
		 * kita membuat object RetryRegistry dengan konfigurasi
		 * default.
		 * 
		 * untuk menambahakan konfigurasi pada RetryRegistry ada beberapa
		 * cara yaitu dengan menggunakan method of(RetryConfig), atau menggunakan 
		 * method addConfiguration(name, RetryConfig) dengan 2 parameter :
		 * name -> nama konfigurasi kita
		 * RetryConfig -> konfigurasi Retry kita
		 */
		RetryRegistry retryRegistry = RetryRegistry.ofDefaults();
		retryRegistry.addConfiguration("configurationRetry", retryConfiguration);

		Retry retry1 = retryRegistry.retry("retryRegistry");
		Retry retry2 = retryRegistry.retry("retryRegistry");

		Assertions.assertSame(retry1, retry2);

		Runnable runnable = Retry.decorateRunnable(retry2, () -> callMe());

		runnable.run();
	}
```

