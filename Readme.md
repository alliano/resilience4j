# Resilience4j
Reselince4j adalah liberary yang sangat rigan dan mudah digunakan, yang terinspirasi dari liberary Netflix Hystrix, namun reselince4j didesain unutk java 8 keatas dan pemograman fungsional.
Reselice4j itu ringan, karena liberary ini hanya membutuhkan satu liberary atau reselence4j ini hanya membuatuhkan satu dependency yaitu Vavr(javaslang), tidak membutuhkan Liberary lainya.
Reselince4j menyediakan high level feature untuk meningkatkan kemampuan fungsional interface, lambda expresion, dan method interface. Dan juga reselincer4j sangat mudular.

reference :
- https://github.com/resilience4j/resilience4j
- https://resilience4j.readme.io/docs/getting-started

# Resilience4j Patterens.
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
# Rate Limiter
Merupakan module di Resilience4j yang bisa kita gunakan untuk membatasi jumlah eksekusi pada waktu tertentu.
Rate Limiter sering digunakan ketika kita tidak ingin terlalu banyak request yang yang diterima untuk menjalankan senuah code program, dengan demikian kita bisa memastikan program kita tidak terbebani terlalu berat.
Jikalau jumlah Request melebihi jumlah yang sudah kita tentukan maka Rate Limiter akan meng throw error exception dengan tipe exception RequestNotPermited.

contoh :

``` java
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
```
# Configure RateLimiter

Saat kita membuat RateLimiter.ofDefault(), ini artinya kita membuat object RateLimiter dengan konfigurasi default, berikut default konfigurasi RateLimiter :
|	Config Property		|	Default Value		|	Description																		|
|-----------------------|-----------------------|-----------------------------------------------------------------------------------|
|	timeOutDuration		|	5[s] lima detik		|	Waktu maksimal menunggu Rate Limiter											|
|	limitRefreshPeriod	|	500[ns] nano second	|	Durasi refresh, setelah mencapai waktu refresh, hitungan limit akan kembali ke 0|
|	limitForPeriod		|	50[s] 50 detik		|	Jumlah yang diperbooehkan dalam kurun waktu refresh								|

contoh : 
``` java
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
```

# RateLimiterRegistry
Sama seperti RetryRegistry, RateLimiter juga memiliki Registry untuk melakukan management object RateLimiter.
Dan sebaik nya, ketika membuat appikasi, kita menggunakan Registry untuk melakukan management object RateLimiter.

contoh penggunakan ReteLimiterRegistry :
``` java
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
```

# Bulkhead
Resilience4j modul unutk menjaga jumlah eksekusi concurrent menggunakan Bulkhead.
Terdapat dua implementasi Bulkhead yang kita bisa pakai di Resilience4j :
- semaphore, makdsudnya dalam waktu yang bersamaan tidak boleh menjalankan thread lebih dari yang ditentukan.
- fix Threadpool, artinya dalam waktu yang bersamaan tidak boleh menjalankan thread melebihi jumlah core yang dimiliki oleh processor kalian.
Jika Bulkhead sudah penuh, maka Bulkhead akan meng throw exception dengan tipe BulkheadFullException.

example for semaphore :
``` java
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
```

example for fix Threadpool :
``` java
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
             * lebih dari core processor yang dimiki komputer kalian
             * untuk mengetahui berapa core yang dimiliki processor kalian, bisa
             * jalankan kode berikut ini 
             * log.info(String.valueOf(Runtime.getRuntime().availableProcessors()));
             */
            Supplier<CompletionStage<Void>> supplier = ThreadPoolBulkhead.decorateRunnable(bulkheadThreadPool, () -> slowAction());
            supplier.get();
        }
    }
```

# Bulkhead Configuration.
Seperti module sebelumnya, kita juga bisa mengkonfigurasi pengaturan bulkhead.
Akan tetapi pengaturanya harus disesuaikan dengan implementasi bulkhead yang kita gunakan, baik itu semaphore atau Fix ThreadPool.

configuration default semaphore Bulkhead :
|   config property     |   default value   |   description                                                |
|-----------------------|-------------------|--------------------------------------------------------------|
|   maxConcurrentCalls  |         25        |   maksimal eksekusi prgram secara palarel yang diperbolehkan.|
|   maxWaitDuration     |         0         |   maksimal waktu menunggu durasi saat eksekusi bulkhead.     |

contoh :
``` java
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
```

# configuration default Fix ThreadPool
|   configuration property  |   default value                               |   description                                                    |
|---------------------------|-----------------------------------------------|------------------------------------------------------------------|
|   maxThreadPoolSize       |   Runtime.getRuntime.availableProcessors()    |   maksimal thread yang boleh berada pada scope pool              |
|   coreThreadPoolSize      |   Runtime.getRuntime.availableProcessors()-1  |   minimal thread awal yang terdapat pada scope pool              |
|   queueCapacity           |                       100                     |   kapasitas antrian                                              |
|   keepAliveDuration       |                       20[ms]                  |   lama thread hidup jika tidak bekerja (thread tidak digunakan)  |

contoh :
``` java
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
        .queueCapacity(1)
        .build();
        
        ThreadPoolBulkhead threadPoolBulkhead = ThreadPoolBulkhead.of("fixThreadPoolConfig", fixThreadPoolConfiguration);
        
        for(var i = 0; i < 20; i ++) {
            /**
             * sebelum program di eksekusi, semua pekerjaan akan masuk di antrian/queue terlebih dahulu, dan kapasitas
             * antrianya itu sesuai dengan yang kita set pada ThreadPoolBulkheadConfig dengan method queueCapacity
             * dan beberapa thread akan dijalankan sejumlah dengan yang kita set pada mehtod coreThreadPoolSize
             * */
            Supplier<CompletionStage<Void>> supplier = ThreadPoolBulkhead.decorateRunnable(threadPoolBulkhead, () -> slowAction());
            supplier.get();
        }
        Thread.sleep(10_000L);
    }
```
# BulkheadRegistry
Sama dengan module lainya, bulkhead juga memiliki registry.
Baik itu semaphore maupun Fix ThreadPool Bulkhead.


contoh semaphore registry :
``` java
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
```
contoh fix threadpoolRegistry :
``` java
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
```

# TimeLimiter
TimeLimiter merupakan module di resilience4j yang digunakan untuk membatasi durasi dari eksekusi kode program.
Dengan TimeLimiter, kita bisa menentukan berapa maksimnal durasi eksekusi sebuah kode program, jikalau eksekusi kode programnya melebihi yang telah ditentukan, maka eksekusi tersebut akan dibatalkan dan akan terjadi exception.
Untuk menggunakan TimeLimiter kita membutuhkan eksekusi dalam bentuk Future atau CompletableFuture.

contoh : 
``` java
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
```

# TimeLimiterConfig
Secara default, TimeLimiter akan mengunggu lama waktu eksekusi suatu kode program selama 1 detik, jikalau dalam waktu 1 detik eksekusi tidak selesai maka TimeLimiter akan melemparkan Exception.
Kita bisa mengubah konfigurasi dari TimeLimiter dengan menggunakan TimeLimiterConfig.

configuration property TimeLimiter
|   config property     |   Default value   |   keterangan                                                      |
|-----------------------|-------------------|-------------------------------------------------------------------|
|   timeOutDuration     |       1[s]        |   lama waktu tunggu eksekusi program sampai dianggap timeout      |
|   cancelRunningFuture |       true        |   apakah eksekusi future dibatalkan jika terjadi Exception TimeOut|

contoh :
``` java
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
```

# TimeLimiterRegistry
Sama dengan module lainya, TimeLimiter juga memiliki Registry untuk mengelola dan menyimpan Object dari TimeLimiter.

contoh penggunakan TimeLimiterConfig : 
``` java
    @Test @SneakyThrows
    public void testTimeLimiterRegistry() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(() -> slowAction());

        TimeLimiterConfig timeLimiterConfiguration = TimeLimiterConfig.custom()
        .timeoutDuration(Duration.ofSeconds(10))
        .cancelRunningFuture(true)
        .build();
        /**
         * disini kita membuat Object TimeLimiterRegistry dengan nama kofigurasi timeLimiterRegistryconf
         */
        TimeLimiterRegistry timeLimiterRegistry = TimeLimiterRegistry.ofDefaults();
        timeLimiterRegistry.addConfiguration("timeLimiterRegistryConf", timeLimiterConfiguration);

        /**
         * disini kita membuat Object TimeLimter dengan  nama timeLimiter dan
         * menggunakan konfigurasi yang telah kita deklarasikan
         * diatas
         */
        TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter("timeLimiter", "timeLimiterRegistryConf");
        Callable<String> callable = TimeLimiter.decorateFutureSupplier(timeLimiter, () -> future);
        callable.call();
    }
```

# CircuitBreaker
Circuit Breaker adalalah implementasi dari finite state machine, dengan tiga normal state :
- CLOSED,
- HALF_OPEN,  

dan dengan dua special state :
- DISABLED,
- FORCE_OPEN,  
Saat kita meggunakan Circuit Breaker kita bisa memilih cara kerja circuit breaker berdasarkan waktu atau hitungan, unutk lebih detail nya sebagai berikut :
- CircuiBreaker berdasarkan hitungan, ini artinya circuit breaker akan menghitung data yaang masuk berdasarkan jumlah N eksekusi terakhir.
    + maksudnya adalah, N adalah jumlah eksekusi yang terakhir dilakukan oleh circuit breaker, misal N nya itu kita set 20, maka circuit breaker akan menghitung 20 eksekusi terkahir.
- CircuitBreaker berdasarkan batas waktu, ini artinya circuit breaker akan menghitung jumlah eksekusi dalam N detik terakhir.
    + maksudnya adalh, N adalah lama waktu eksekusi terakhir, misalnya N nya kita set menjadi 10 detik berarti circuit breaker akan menghitung jumlah eksekusi 10 detik terakhir.

[alt](https://github.com/alliano/resilience4j/blob/master/src/main/resources/img/circuitbreaker.jpg)