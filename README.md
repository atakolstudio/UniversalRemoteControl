# Universal Remote Control

Kotlin + Jetpack Compose ile yazılmış IR + WiFi evrensel kumanda uygulaması.

**Package:** `com.atakolstudio.universalremote`
**Repo:** `atakolstudio/UniversalRemoteControl` (branch: `main`)

## Kurulum

1. Android Studio (Ladybug/Koala veya üstü) ile projeyi açın (`File > Open` → bu klasör).
2. Gradle senkronizasyonunu bekleyin (ilk senkron internet gerektirir: AGP/Kotlin/AndroidX bağımlılıkları indirilecek).
3. `compileSdk`/`targetSdk = 36` (Android 16) seçili — Android Studio SDK Manager'dan bu platformu indirmeniz istenebilir. İndiremiyorsanız `app/build.gradle.kts` içinde geçici olarak 35'e düşürebilirsiniz, kodun geri kalanı değişmeden derlenir.
4. Fiziksel bir cihazda çalıştırın. **IR göndermek için cihazınızda donanım IR blaster olmalı** (emülatörde ve çoğu 2018 sonrası telefonda yoktur) — uygulama bunu algılayıp WiFi'ye yönlendirir veya kullanıcıyı bilgilendirir.

## Mimari

- **UI:** Tamamen Jetpack Compose + Material 3, tek `Activity` (`MainActivity`) + Navigation Compose.
- **DI:** Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`).
- **Veritabanı:** Room — `DeviceEntity`, `IrCodeEntity`, `MacroEntity`/`MacroStepEntity`, `FavoriteEntity`.
- **Tercihler:** Jetpack DataStore (tema, dil, debug modu).
- **Ağ:** OkHttp + Moshi; Samsung SmartView ve LG WebOS için WebSocket tabanlı gönderim, diğer cihazlar için genel HTTP; SSDP/UPnP keşfi için ham `DatagramSocket` kullanımı.
- **IR:** `ConsumerIrManager` sarmalayıcısı (`IrController`) + protokol kodlayıcı (`IrProtocolEncoder`: NEC/Sony SIRC/RC5/RC6).

```
data/
  local/        Room entity/DAO/Database, DataStore, preset seeder
  model/        DTO'lar ve statik marka kataloğu
  repository/   RemoteRepository (komut yönlendirme), BackupManager
di/             Hilt modülleri (Database, Network)
ir/             IrController, IrProtocolEncoder, IrLearningHelper
wifi/           Samsung/LG/Generic senders, SSDP discovery, router
ui/
  theme/        Material3 tema, tipografi
  navigation/   NavHost + rotalar
  screens/      dashboard, adddevice, remote, macro, settings
util/           Quick Settings Tile, App Widget provider
```

## Ele alınan özellik listesi

- Dashboard: grid halinde cihaz kartları (isim, marka/model, bağlantı türü).
- Cihaz ekleme: 20+ marka x TV/Klima/STB/Fan/Soundbar/Projeksiyon önceden tanımlı IR kod tablosu (`assets/preset_ir_codes.json`, 604 satır), WiFi cihazlar için IP/MAC + protokol seçimi.
- IR gönderim: `ConsumerIrManager` ile gerçek gönderim, IR donanımı yoksa açık uyarı.
- WiFi gönderim: Samsung SmartView (WebSocket), LG WebOS (SSAP WebSocket), genel HTTP, SSDP keşif altyapısı.
- Manuel IR kod girişi: kumanda ekranında bir tuşa **uzun basarak** hex kod atama/override.
- Favoriler, Makrolar: Room'a kayıtlı, birden fazla cihaz+fonksiyon adımını sırayla çalıştıran makro sistemi ("Film Modu" vb.).
- Tema: Material 3 dinamik renk + açık/koyu/sistem, DataStore'da kalıcı.
- Çoklu dil: TR (varsayılan) + EN, Android 13+ "Uygulama dili" ayarına entegre (`AppCompatDelegate.setApplicationLocales`).
- Ayarlar: yedekleme/geri yükleme (JSON, SAF üzerinden dosya seçici ile), debug modu anahtarı.
- Quick Settings Tile ve ana ekran widget'ı: en son kullanılan cihaz için hızlı güç/ses erişimi.
- Erişilebilirlik: `contentDescription`'lar, büyük dokunma alanları (min 48dp aspect grid hücreleri), TalkBack ile okunabilir etiketler.

## Dürüstçe belirtilmesi gereken sınırlamalar

Bu bir iskelet/temel sürümdür, üretime hazır bitmiş bir ürün değildir. Aşağıdaki noktalar bilerek basitleştirilmiştir:

1. **IR "öğrenme" (learning) gerçek anlamda yoktur.** Android'de üçüncü parti uygulamalara açık, standart bir IR *alma* API'si yok (`ConsumerIrManager` sadece gönderir). Bu yüzden "eski kumandayı telefona tutup öğret" akışı platformda genel olarak mümkün değil; `ir/IrLearningHelper.kt` bunu açıkça belgeliyor ve uygulama bunun yerine manuel kod girişi sunuyor.
2. **Önceden tanımlı IR kodları temsili/placeholder'dır.** `preset_ir_codes.json` içindeki hex kodlar gerçek üretici kod tablolarından değil, protokol formatına uygun şekilde programatik olarak üretildi. Gerçek cihazlarda çalışmaları garanti değildir — üretime almadan önce doğrulanmış bir IR kod veritabanıyla (örn. lisanslı bir sağlayıcı veya kendi ölçümleriniz) değiştirilmelidir. Klima (AC) kodları özellikle çoğu markada NEC'ten çok daha uzun "raw" darbe dizileri gerektirir; buradaki AC girişleri `RAW` protokolüyle işaretli ama gerçek zaman tabloları içermiyor.
3. **LG WebOS SSAP entegrasyonu basitleştirilmiştir.** Gerçek SSAP el sıkışması (pairing, izin istemleri, `client-key` kalıcılığı, `ssap://com.webos.service.networkinput/...` pointer-socket akışı) daha ayrıntılıdır; burada tek istekle en yaygın tuş gönderimini gösteren minimal bir örnek var.
4. **Samsung SmartView token yönetimi eksik.** İlk bağlantıda TV ekranında çıkan eşleştirme isteğini onayladıktan sonra dönen token'ın `DeviceEntity.authToken` alanına kalıcı yazılması (repository'de bir güncelleme adımı) production için eklenmelidir.
5. **SSDP keşif sonuçları henüz Add Device ekranına bağlanmadı.** `SsdpDiscoveryService` çalışır durumda ama UI'da "Ağda tara" butonu/akışı bu iterasyona dahil edilmedi.
6. **AGP/Kotlin sürümleri** bu dosyaların yazıldığı an itibarıyla kararlı olarak mevcut olanlardır (AGP 8.6.1 / Kotlin 2.0.21); "AGP 9.x" gibi henüz yayınlanmamış sürümler yerine çalışır durumda olan en güncel kararlı sürümler seçildi. `compileSdk/targetSdk = 36` bırakıldı; SDK Manager'da mevcut değilse Android Studio güncelleme isteyecektir.
7. **Gradle wrapper JAR dosyası eklenmedi** (`gradlew`/`gradlew.bat` script'leri bu paylaşımda yok) — Android Studio'da açtığınızda "Gradle wrapper oluştur" istemi otomatik gelir, ya da `gradle wrapper` komutunu kendi makinenizde çalıştırabilirsiniz.

## Yol haritası (öneriler)

- Gerçek/lisanslı bir IR kod veritabanı entegrasyonu.
- SSDP tarama sonuçlarının Add Device ekranına bağlanması.
- Samsung/LG token'larının kalıcı saklanması ve otomatik yeniden bağlanma.
- Widget'ı Glance (Compose tabanlı widget API) ile yeniden yazmak.
- Enstrümantasyon/birim testleri (Room DAO testleri, IrProtocolEncoder testleri).
