# Sakti

Sakti adalah aplikasi Android berbasis Kotlin yang menggabungkan Jetpack Compose dan komponen View tradisional (ViewBinding). Aplikasi ini menyediakan alur autentikasi (Splash → Login) serta modul terpisah untuk pengguna akhir (Enduser) dan teknisi (Teknisi). Aplikasi juga mendukung pemrosesan gambar (kamera/galeri), penyimpanan lokal dengan Room, dan komunikasi jaringan melalui Retrofit + OkHttp.

---

## Daftar Isi
- [Ringkasan](#ringkasan)
- [Fitur Utama](#fitur-utama)
- [Arsitektur & Teknologi](#arsitektur--teknologi)
- [Struktur Aplikasi (sekenario singkat)](#struktur-aplikasi-sekenario-singkat)
- [Persiapan & Prasyarat](#persiapan--prasyarat)
- [Cara Build & Jalankan](#cara-build--jalankan)
- [Pengujian](#pengujian)
- [Kontribusi](#kontribusi)
- [Catatan Tambahan](#catatan-tambahan)
- [Lisensi](#lisensi)

---

## Ringkasan
Aplikasi ini ditulis 100% menggunakan Kotlin. Nama package utama: `com.example.saktinocompose`. Aplikasi ditargetkan untuk Android (minSdk 24, target/compileSdk 36) dan menggunakan Gradle Kotlin DSL. Tema aplikasi menunjukkan dukungan untuk UI tanpa Compose di beberapa bagian, tetapi project sudah mengaktifkan Compose (`buildFeatures.compose = true`) sehingga kemungkinan kombinasi Compose + ViewBinding digunakan.

---

## Fitur Utama
- Splash screen sebagai entry point aplikasi.
- Halaman Login untuk autentikasi pengguna.
- Modul Teknisi dan Modul Enduser (aktivitas terpisah).
- Dukungan kamera dan pemilihan gambar dari galeri.
- FileProvider untuk berbagi file internal dengan komponen lain.
- Penyimpanan lokal menggunakan Room Database.
- Komunikasi HTTP/REST menggunakan Retrofit + OkHttp (dengan Gson sebagai converter).
- Pemrograman asinkron menggunakan Kotlin Coroutines.
- Kompatibilitas Jetpack Compose + ViewBinding.

---

## Arsitektur & Teknologi
- Bahasa: Kotlin (100%).
- Build: Gradle Kotlin DSL (file `build.gradle.kts`, `settings.gradle.kts`).
- Android:
  - compileSdk = 36, targetSdk = 36, minSdk = 24
  - AndroidManifest utama di `app/src/main/AndroidManifest.xml`
  - Application class: `.MyApplication`
- UI:
  - Jetpack Compose (Compose BOM dan dependency Compose tersedia)
  - ViewBinding diaktifkan untuk layout tradisional
- Database:
  - Room (version 2.7.0) dengan KSP (`ksp("androidx.room:room-compiler:$room_version")`)
- Jaringan:
  - Retrofit 3.0.0 + converter-gson
  - OkHttp 5.3.2 + logging-interceptor
  - Gson 2.13.2
- Coroutines:
  - kotlinx-coroutines (android & core) versi 1.10.2
- Lainnya:
  - Coil atau library image-loading kemungkinan disiapkan (komentar di build file)
  - KSP plugin terpasang (`com.google.devtools.ksp`)
- Gradle Wrapper disertakan (`gradlew`, `gradlew.bat`)

---

## Struktur Aplikasi (sekenario singkat)
Berdasarkan AndroidManifest:
- MyApplication — class Application kustom.
- SplashActivity — launcher activity.
- LoginActivity — layar login.
- TeknisiActivity — layar khusus teknisi.
- EnduserActivity — layar khusus pengguna akhir.
- FileProvider dikonfigurasi (`@xml/file_paths`) untuk akses file internal.
- Permission yang dideklarasikan: CAMERA, READ_EXTERNAL_STORAGE / READ_MEDIA_IMAGES, INTERNET, ACCESS_NETWORK_STATE.

---

## Persiapan & Prasyarat
- Java JDK 11 (project menggunakan target JVM 11)
- Android Studio (disarankan versi yang kompatibel dengan compileSdk 36)
- Android SDK sesuai (API 36)
- Gradle wrapper (jalankan via `./gradlew` atau `gradlew.bat`)

---

## Cara Build & Jalankan

1. Clone repository:
```bash
git clone https://github.com/nrhdyt3012/sakti.git
cd sakti
```

2. Build debug (menggunakan Gradle wrapper):
```bash
./gradlew assembleDebug
```

3. Jalankan di emulator/perangkat (melalui Android Studio) atau pasang APK:
```bash
./gradlew installDebug
```
(atau buka project di Android Studio dan run `app`)

4. Jika ingin menjalankan task Gradle lainnya:
- Build release:
```bash
./gradlew assembleRelease
```

Catatan: Nama aplikasi dan applicationId saat ini `com.example.saktinocompose` — ganti jika diperlukan di `app/build.gradle.kts`.

---

## Pengujian
- Unit tests (JVM):
```bash
./gradlew test
```
- Instrumentation tests (Android):
```bash
./gradlew connectedAndroidTest
```

---

## Kontribusi
Terima kasih atas minat kontribusi! Langkah umum:
1. Fork repository
2. Buat branch fitur: `git checkout -b feat/nama-fitur`
3. Commit perubahan: `git commit -m "Menambahkan: ..."`
4. Push ke fork: `git push origin feat/nama-fitur`
5. Buat Pull Request ke branch `main`

Tambahkan deskripsi perubahan, cara menguji, dan screenshot bila perlu.

---

## Catatan Tambahan & Petunjuk Pengembangan
- Room memerlukan annotation processing KSP; pastikan KSP aktif dan build bersih bila melakukan perubahan pada entitas/DAO.
- Periksa konfigurasi permission runtime untuk CAMERA dan READ_MEDIA_IMAGES / READ_EXTERNAL_STORAGE pada runtime (Android 13+ perbedaan permission).
- Jika menambahkan network calls, gunakan interceptor logging OkHttp untuk debugging.
- Jika ingin pindah sepenuhnya ke Compose, pertimbangkan mengganti tema `Theme.SaktiNoCompose` dan membersihkan layout/view binding yang tidak lagi diperlukan.

---

## Hasil Pemindaian Repo
Saya memindai file build dan manifest untuk menyesuaikan README. Hasil pemindaian file/code mungkin tidak menampilkan semua file karena batasan pencarian API — untuk melihat riwayat commit atau file lengkap, silakan buka:
https://github.com/nrhdyt3012/sakti/commits?per_page=5  
atau telusuri repository di GitHub.

---

## Lisensi
Belum ada file LICENSE di repo. Jika Anda ingin, saya dapat:
- Menambahkan lisensi MIT/Apache-2.0 (pilih salah satu), atau
- Menambahkan file LICENSE sesuai pilihan Anda.

---

Jika Anda setuju, saya dapat:
- (A) Meng-commit README.md ini langsung ke repository (membuat commit atau PR), atau
- (B) Memindai lebih jauh (mis. seluruh `app/src` untuk menyusun README yang lebih rinci tentang arsitektur code, modul, dan contoh penggunaan) lalu memperbarui README.  
Pilih A atau B, atau beri instruksi lain.  
