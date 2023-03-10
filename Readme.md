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

#

