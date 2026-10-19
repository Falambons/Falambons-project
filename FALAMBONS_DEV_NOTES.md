# 🚀 FALAMBONS CODE STUDIO — CATATAN PROYEK & ARSITEKTUR

**Pengembang:** Falambons Developer  
**Aplikasi:** Falambons Code Studio  
**Versi Saat Ini:** v2.0 (Build 2)  
**Tanggal Update v2:** 19 Oktober 2026  
**Status Build:** SUKSES (APK v2 Terkompilasi & Siap Dipasang)

---

## 🚀 Pembaruan Utama Versi 2.0 (Changelog v2)
1. **Animasi GIF Bergerak Global:** Foto profil di bilah atas, foto profil di dialog akun, dan wallpaper latar belakang coding kini memutar animasi GIF secara dinamis menggunakan engine `ImageDecoderDecoder` (Android 9+) dan `GifDecoder` (Android 7-8).
2. **Penyimpanan Pengaturan Konsisten (Anti-Reset):** Seluruh preferensi global (tema aktif, mode word-wrap, transparansi wallpaper, sesi login, dll.) kini disimpan secara sinkron menggunakan `.commit()`.
3. **Penyimpanan Aset Privat Lokal (LocalAssetManager):** Wallpaper GIF/gambar dan foto profil yang dipilih pengguna kini disalin permanen ke folder internal aplikasi (`context.filesDir`), sehingga tidak akan pernah hilang/tereset oleh sistem izin Android saat aplikasi ditutup dan dibuka kembali.
4. **Perbaikan Navigasi Tombol Kembali:** Mengeliminasi race condition coroutine penyimpanan saat keluar sehingga aplikasi tidak lagi terkunci di editor teks.
5. **Menu Aksi Navigasi Baru:** Menambahkan tombol "Tutup & Kembali ke Folder" serta "Kembali ke Home Root".

## 📌 Ringkasan Fitur yang Telah Selesai Dibangun

### 1. Desain & Tampilan
- **Latar Belakang Hitam Pekat (Black Mode)** sebagai tampilan utama.
- **3 Tema Global:**
  1. `PURE_DARK`: Hitam AMOLED (`#000000`) hemat daya dan kontras tinggi.
  2. `BLUE_DARK`: Cyber Midnight Navy (`#050B18`) dengan aksen neon cyan.
  3. `RGB`: Cyberpunk Neon Glow dengan gradien pelangi aktif (Pink `#FF007F`, Cyan `#00F0FF`, Lime `#00FF66`).
- **Custom Background Editor:**
  - Mendukung pemasangan wallpaper gambar atau animasi **GIF**.
  - Dilengkapi *slider transparansi* (*opacity* 5% - 85%) agar teks kode tetap mudah dibaca.

### 2. Autentikasi & Penyimpanan Data Offline
- **100% Offline Storage** menggunakan **Room SQLite Database** Android.
- Registrasi dan login offline tanpa ketergantungan server luar.
- Fitur **Foto Profil / Avatar Kustom** (mendukung unggah file gambar/GIF atau pilihan preset avatar cyber).
- Tersedia tombol **Masuk Cepat (Akun Demo / Tamu)** untuk pengujian instan.
- **Auto-Save:**
  - *Debounced Auto-Save (800ms)* saat mengetik.
  - *Lifecycle Auto-Save* otomatis menyimpan saat aplikasi ditutup atau beralih ke latar belakang.
  - *Clean Exit Handling (Bugfix):* Perbaikan siklus `closeEditor` untuk mencegah *race condition* yang sebelumnya membuat layar editor kembali terbuka secara otomatis saat tombol kembali/gesture ditekan. Ditambahkan opsi cepat "Kembali ke Home Root" di menu titik tiga.

### 3. Manajemen Folder & File (CRUD Lengkap)
- Struktur folder bertingkat dengan navigasi **Breadcrumb** (`Root > Folder > Subfolder`).
- **Tag Warna Kustom:** Pilihan warna untuk setiap folder dan file (Cyan, Pink, Green, Amber, Purple, Blue, Red).
- **Favorit (⭐):** Sematkan folder atau file penting ke daftar favorit.
- Pencarian dan filter instan: *Semua*, *Favorit*, *Code File*, *Text .txt*.

### 4. Code & Text Editor Multi-Bahasa
- **Format yang Didukung:** JavaScript (`.js`), CSS (`.css`), C++ (`.cpp`), PHP (`.php`), HTML (`.html`), Python (`.py`), JSON (`.json`), dan Teks (`.txt`).
- **Live Syntax Highlighting:** Pewarnaan kata kunci, string, angka, komentar, tag HTML, variabel PHP (`$var`), dan tanda kurung.
- **Unlimited Baris Panjang:** Toggle beralih antara *Word Wrap* dan *Horizontal Scroll* (baris panjang tidak terpotong).
- **Nomor Baris (*Line Numbers*):** Kolom penomoran baris otomatis di sebelah kiri.

### 5. Fast Coding Toolbar & Auto Copy-Paste
- Baris simbol cepat: `{ } ( ) [ ] < > ; : " ' = + - * / & | ! ? $ .` dan `TAB` (4 spasi).
- Template snippet instan sesuai format bahasa aktif.
- **Mode Auto Copy-Paste:** Tombol 1-tap *Copy Semua*, 1-tap *Paste*, serta chip riwayat *Clipboard Buffer*.

### 6. Floating Code Pad (Overlay Melayang)
- Mini-pad kode mengambang (*draggable*) yang dapat digeser ke posisi mana pun di layar.
- Mendukung minimize menjadi pill kecil atau ekspansi penuh untuk coretan catatan cepat.

### 7. Custom QR Code & Barcode Generator
- Meng-compile isi kode atau teks menjadi **QR Code resolusi tinggi** (ZXing Engine).
- Pilihan warna kustom (Cyan Neon, Pink, Lime, Gold, White).
- Kustomisasi teks logo tengah (misal: label `FLB` atau inisial kustom).
- Tombol langsung untuk membagikan gambar QR Code (`.png`).

### 8. Ekspor & Impor File
- **Ekspor PDF:** Menghasilkan dokumen PDF rapi dengan penomoran baris dan footer pengembang.
- **Impor File:** Impor file dari penyimpanan Android ke dalam folder aplikasi.
- **Bagikan File/Teks:** Berbagi ke aplikasi lain lewat Android FileProvider.

### 9. Identitas Pengembang
- Label resmi **`Credit by : falambons developer`** tersemat pada antarmuka, profil, hasil ekspor PDF, dan QR generator.

---

## 📂 Struktur File Utama Kode Sumber

```text
/
├── app/
│   ├── src/main/java/com/example/
│   │   ├── MainActivity.kt                  # Activity utama & lifecycle observer
│   │   ├── data/
│   │   │   ├── AppDatabase.kt               # Room SQLite Database
│   │   │   ├── AppDao.kt                    # Data Access Object (User, Folder, File)
│   │   │   ├── AppRepository.kt             # Repository & Seeding Demo Files
│   │   │   ├── UserEntity.kt                # Model User Offline
│   │   │   ├── FolderEntity.kt              # Model Folder (Warna & Favorit)
│   │   │   ├── FileEntity.kt                # Model File (Multi-ekstensi & Konten)
│   │   │   └── UserPreferences.kt           # SharedPrefs (Tema, Wrap, Wallpaper)
│   │   ├── syntax/
│   │   │   ├── SyntaxHighlighter.kt         # Engine pewarna sintaks live
│   │   │   └── SnippetProvider.kt           # Template kode cepat & simbol
│   │   ├── util/
│   │   │   ├── PdfExporter.kt               # Generator PDF dengan nomor baris
│   │   │   ├── QrCodeGenerator.kt           # Generator QR Code kustom
│   │   │   └── FileShareHelper.kt           # Android FileProvider share helper
│   │   ├── ui/
│   │   │   ├── MainViewModel.kt             # State management & auto-save
│   │   │   ├── theme/
│   │   │   │   ├── Color.kt                 # Palet Pure Dark, Blue Dark, & RGB
│   │   │   │   └── Theme.kt                 # M3 Dynamic Scheme Switcher
│   │   │   ├── screens/
│   │   │   │   ├── AuthScreen.kt            # Login & Register offline
│   │   │   │   ├── FolderExplorerScreen.kt  # File & folder manager
│   │   │   │   └── CodeEditorScreen.kt      # Editor kode layar penuh
│   │   │   └── components/
│   │   │       ├── AvatarPicker.kt          # Komponen avatar & preset
│   │   │       ├── CustomBackgroundDialog.kt# Dialog wallpaper background editor
│   │   │       ├── FloatingCodePad.kt       # Widget melayang draggable
│   │   │       ├── ProfileDialog.kt         # Dialog profil & ganti foto
│   │   │       └── QrCodeDialog.kt          # Dialog custom QR code
├── FalambonsCode-v2-debug.apk               # File APK v2 hasil kompilasi terbaru
├── FalambonsCode-debug.apk                  # File APK debug
├── index.html                               # Halaman web unduh APK
├── DEBUG_LOG_AND_RULES.md                   # Catatan jejak error & aturan anti-regresi
└── FALAMBONS_DEV_NOTES.md                   # Catatan arsip ini
```

---

## 🔮 Rencana Pengembangan Selanjutnya (Roadmap Ide)
1. **Eksekusi / Compiler Ringan:** Menambahkan web preview langsung untuk HTML/CSS/JS di dalam aplikasi.
2. **Git Versioning Lokal:** Riwayat versi file (*history undo/redo* lintas sesi).
3. **Penyimpanan Terenkripsi (Encrypted Database):** SQLCipher untuk mengunci folder rahasia dengan PIN/sidik jari.
4. **Ekstensi Bahasa Tambahan:** Dukungan sintaks untuk Kotlin, Rust, Go, dan Dart.

---
*Arsip tersimpan dengan aman. Kapan pun Anda kembali, proyek siap dilanjutkan!*
