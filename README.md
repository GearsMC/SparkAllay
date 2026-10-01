<h1 align="center">
	<img
		alt="spark"
		src="https://i.imgur.com/ykHn9vx.png">
</h1>

# spark-extra-platforms

This repository contains implementations of [spark](https://github.com/lucko/spark) for additional platforms.

These releases do not receive the same level of support as the main spark plugins/mods. They are provided as-is, and may be out of date or contain bugs. If you run into problems, please open an issue on GitHub and/or raise a pull request to fix!

## GearsMC: spark-allay

Bu fork yalnizca `spark-allay` modulunu derler ve GearsMC'nin Allay'ine (API `0.29.0-SNAPSHOT`, Java 25) gore ayarlidir.

**Derleme**

1. Allay checkout'unda: `./gradlew :api:publishToMavenLocal`
2. Burada: `./gradlew :spark-allay:shadowJar`
3. `spark-allay/build/libs/spark-*-allay.jar` dosyasini sunucunun `plugins/` klasorune at.

**Turkce kisayol: `/performans` (kisaca `/perf`)**

| Komut | Ne yapar |
| --- | --- |
| `/performans durum` | TPS, CPU ve bellek ozeti |
| `/performans profil [saniye]` | Tum thread'leri profiller (varsayilan 120 sn, 30-1800; uyuyan thread'ler haric), sonunda rapor linki verir |
| `/performans durdur` | Profili erken bitirir ve linki verir |
| `/performans iptal` | Profili rapor olusturmadan iptal eder |
| `/performans bellek` | Bellek ozeti (link) |
| `/performans gc` | Cop toplayici istatistikleri |
| `/performans rapor` | Genel saglik raporu (link) |
| `/performans izle` | Ana thread tick izlemeyi ac/kapa |
| `/performans ping [oyuncu]` | Oyuncu pingleri |

Spark'in kendi ciktilari ve web goruntuleyicisi Ingilizcedir. Allay'de dunyalar kendi
thread'lerinde doner; TPS ana (scheduler) thread'ini gosterir. Dunya kaynakli lag icin
`/performans profil` kullanin. Gelismis kullanim icin `/spark` aynen duruyor.

#### Useful Links
* [**Website**](https://spark.lucko.me/) - browse the project homepage
* [**Documentation**](https://spark.lucko.me/docs) - read documentation and usage guides
* [**Downloads**](https://ci.lucko.me/job/spark-extra-platforms/) - latest plugin/mod downloads

## License

spark is free & open source. It is released under the terms of the GNU GPLv3 license. Please see [`LICENSE.txt`](LICENSE.txt) for more information. 
