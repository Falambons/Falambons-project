# 🛡️ FALAMBONS CODE STUDIO — CATATAN JEJAK ERROR & ATURAN PENCEGAHAN (POST-MORTEM)

**Proyek:** Falambons Code Studio  
**Pengembang:** Falambons Developer  
**Status Terakhir:** SUKSES PENUH (Build 100% Lulus, Unit Test Lulus, APK Terverifikasi)  
**Tujuan Dokumen:** Mencatat riwayat error dan menetapkan aturan wajib agar kesalahan yang sama **TIDAK PERNAH TERULANG**.

---

## 🛑 1. RIWAYAT ERROR & JEJAK PERBAIKAN

### ❌ Error Kasus 1: Dollar Sign ($) pada Kode PHP Terbaca sebagai Kotlin Interpolation
- **Gejala / Log:**
  ```text
  e: file:///app/src/main/java/com/example/data/AppRepository.kt:277:2 Unresolved reference 'response'.
  BUILD FAILED in 3m 38s
  ```
- **Penyebab:**
  Di dalam `AppRepository.kt`, template kode PHP awal dimasukkan ke dalam triple-quoted string Kotlin (`""" ... """`). String berisi variabel `$response = [...]`. Kotlin Compiler menganggap `$response` sebagai variabel Kotlin lokal yang tidak dideklarasikan.
- **Solusi yang Diterapkan:**
  Mengganti `$response` menjadi `${'$'}response` pada setiap sintaks PHP dan shell script di dalam string Kotlin.
- **⚠️ ATURAN MUTLAK (JANGAN DIULANGI):**
  Setiap kali menulis contoh kode PHP, Bash, jQuery, atau Regex di dalam file Kotlin yang menggunakan tanda `$`, **WAJIB** mengekspresikannya sebagai `${'$'}` atau memisahkan ke file asset raw terpisah.

---

### ❌ Error Kasus 2: Layar Terkunci di Papan Editor (Menolak Kembali ke Root)
- **Gejala / Bukti Screenshot:**
  Pengguna menekan tombol kembali (`<-`) atau melakukan gestur *back* pada HP Xiaomi/HyperOS, namun aplikasi seketika membuka kembali file yang sama, seperti "terkunci" di editor.
- **Penyebab Akar (Root Cause Analysis):**
  1. Di dalam `MainViewModel.kt`, fungsi `closeEditor()` memanggil `saveCurrentFileImmediately()`.
  2. `saveCurrentFileImmediately()` meluncurkan *coroutine* asinkron di `viewModelScope`.
  3. `closeEditor()` di thread utama menyetel `_activeEditingFile.value = null` (layar editor tertutup).
  4. Beberapa milidetik kemudian, coroutine simpan selesai dan mengeksekusi:
     ```kotlin
     _activeEditingFile.value = updated // 💥 MEMBANGKITKAN KEMBALI EDITOR!
     ```
  5. Ditambah lagi, di `CodeEditorScreen.kt` terdapat `DisposableEffect(Unit) { onDispose { onSaveImmediately() } }`. Ketika layar dilepas dari komposisi (*unmount*), `onDispose` kembali memanggil simpan, yang kembali mengisi `_activeEditingFile.value` dengan nilai non-null!
  6. Hasilnya: Infinite loop di mana setiap usaha menutup editor langsung membatalkan dirinya sendiri.
- **Solusi yang Diterapkan:**
  1. Menghapus `DisposableEffect { onDispose { ... } }` yang memicu *side-effect* saat unmount.
  2. Di `closeEditor()`, tangkap nilai `fileToSave` dan `textToSave` dalam variabel lokal, batalkan `autoSaveJob`, lalu setel `_activeEditingFile.value = null` secara permanen.
  3. Di `saveCurrentFileImmediately()`, tambahkan guard condition:
     ```kotlin
     if (_activeEditingFile.value?.id == file.id) {
         _activeEditingFile.value = updated
         _saveStatus.value = SaveStatus.SAVED
     }
     ```
  4. Menambahkan menu navigasi darurat eksplisit: **"Tutup & Kembali ke Folder"** dan **"Kembali ke Home Root"** pada bilah menu atas.
- **⚠️ ATURAN MUTLAK (JANGAN DIULANGI):**
  - **DILARANG KERAS** memperbarui state navigasi aktif di dalam callback simpan asinkron saat proses penutupan layar (*clean exit*).
  - **DILARANG KERAS** menaruh mutasi state navigasi di dalam blok `onDispose` Jetpack Compose jika state tersebut mengontrol visibilitas Composable itu sendiri.

---

### ❌ Error Kasus 3: Gambar Format GIF Terdiam / Tidak Bergerak (Statis di Profil & Background)
- **Gejala / Bukti Screenshot:**
  Gambar format GIF yang diunggah pengguna untuk foto profil maupun latar belakang editor hanya menampilkan frame pertama (terdiam/statis tanpa animasi).
- **Penyebab Akar (Root Cause Analysis):**
  Library Coil di Android secara default menganggap file gambar sebagai bitmap statis. Meskipun dependensi `coil-gif` ada di `build.gradle.kts`, Coil membutuhkan pendaftaran eksplisit `ImageDecoderDecoder.Factory()` (untuk Android 9+ / API 28+) atau `GifDecoder.Factory()` (untuk API < 28) ke dalam komponen `ImageLoader`. Tanpa decoder ini, animasi tidak pernah di-decode.
- **Solusi yang Diterapkan di Versi 2.0:**
  1. Membuat singleton `GifImageLoader.kt` dengan auto-detection versi Android untuk mendaftarkan `ImageDecoderDecoder` dan `GifDecoder`.
  2. Mengikat `gifLoader` secara global di `MainActivity.onCreate` lewat `Coil.setImageLoader(gifLoader)` dan `CompositionLocalProvider(LocalImageLoader provides gifLoader)`.
  3. Memastikan `AsyncImage` di `UserAvatar` dan `CodeEditorScreen` secara eksplisit menggunakan `GifImageLoader.get(context)`.
- **⚠️ ATURAN MUTLAK (JANGAN DIULANGI):**
  Setiap kali menggunakan `AsyncImage` di Android untuk memuat file GIF pengguna, **WAJIB** menyertakan `GifImageLoader` dengan decoder animasi, dan tidak boleh hanya mengandalkan ImageLoader default Coil.

---

### ❌ Error Kasus 4: Pengaturan Global APK Hilang / Reset ke Default Saat Keluar & Buka Ulang
- **Gejala / Bukti Screenshot:**
  Setelah pengguna keluar dan membuka kembali aplikasi, seluruh pengaturan (tema, wallpaper GIF di editor, foto profil, dll.) kembali ke setelan bawaan (seperti tereset). Pada pengaturan izin Android terlihat bahwa sistem secara otomatis menghapus izin file sementara (*temporary picker grant*).
- **Penyebab Akar (Root Cause Analysis):**
  1. Di `UserPreferences.kt`, seluruh setter memakai `.apply()` yang berjalan secara asinkron di memori. Jika proses aplikasi dihentikan atau di-swipe dari Recent Apps, penulisan ke disk belum sempat selesai.
  2. URI gambar/GIF yang dipilih pengguna dari galeri/picker adalah URI sementara (`content://media/picker/...`). Saat aplikasi ditutup, sistem keamanan Android secara otomatis mencabut izin akses (*permission revoked*), sehingga aplikasi tidak lagi bisa membaca file tersebut saat dibuka kembali.
- **Solusi yang Diterapkan:**
  1. Mengganti seluruh pemanggilan `.apply()` pada `UserPreferences.kt` dengan `.commit()` (sinkron dan langsung tertulis ke flash disk).
  2. Membuat modul **`LocalAssetManager`**: Setiap kali pengguna memilih foto profil atau wallpaper GIF di editor, filenya langsung disalin (*byte-to-byte*) ke folder penyimpanan privat internal aplikasi (`/data/user/0/.../files/internal_wallpapers/` dan `internal_avatars/`). File ini tidak bergantung pada izin galeri luar dan tidak akan pernah terhapus atau tereset oleh Android.
- **⚠️ ATURAN MUTLAK (JANGAN DIULANGI):**
  Jangan pernah menyimpan string URI sementara (`content://media/picker/...`) langsung ke preferensi atau database tanpa menyalin filenya ke penyimpanan privat internal (`context.filesDir`). Selalu gunakan `.commit()` untuk pengaturan global kritis.

## 🏆 2. JEJAK KEBERHASILAN (SUCCESS TRACE)

Semua poin spesifikasi pengguna telah berhasil direalisasikan dan diuji secara menyeluruh:

| No | Fitur | Status | Catatan Teknis |
|---|---|---|---|
| 1 | **Latar Belakang Hitam & 3 Tema Global** | ✅ Berhasil | Pure Dark (`#000000`), Blue Dark (`#050B18`), Cyber RGB Glow (`#08080E`). Beralih 1-tap via top bar. |
| 2 | **Login & Daftar Offline Android Storage** | ✅ Berhasil | Menggunakan Room SQLite internal, terisolasi per akun offline, lengkap dengan akun demo cepat. |
| 3 | **Foto Profil & Avatar (Mendukung Gambar & GIF)** | ✅ Berhasil | Image picker dari memori HP + 6 pilihan preset avatar developer. |
| 4 | **Manajemen Root Folder & File (CRUD)** | ✅ Berhasil | Navigasi breadcrumbs, buat folder/file, ubah nama, hapus, tag warna (7 warna), dan tanda bintang (⭐). |
| 5 | **Dukungan Coding Lengkap** | ✅ Berhasil | JS, CSS, C++, PHP, HTML, TXT, Python, JSON dengan VisualTransformation syntax coloring. |
| 6 | **Unlimited Baris Panjang** | ✅ Berhasil | Toggle horizontal scroll bebas tanpa terpotong vs mode word-wrap. |
| 7 | **Fast Coding & Auto Copy-Paste Bar** | ✅ Berhasil | Baris simbol cepat `{ } ( ) [ ] < > ; : " ' = + - * / & | ! ? $ .`, tombol TAB 4-spasi, Copy Semua, Paste, dan Buffer Riwayat. |
| 8 | **Custom Background Gambar & GIF di Editor** | ✅ Berhasil | Coil AsyncImage di belakang area ketik dengan slider transparansi (*opacity*). |
| 9 | **Auto-Save Otomatis** | ✅ Berhasil | Debounce 800ms saat mengetik + lifecycle onPause/onStop Android. |
| 10 | **Custom QR Code & Barcode Generator** | ✅ Berhasil | ZXing engine dengan kustomisasi warna neon dan teks logo tengah (`FLB`). Bisa langsung dibagikan. |
| 11 | **Ekspor PDF & Berbagi ke Perangkat** | ✅ Berhasil | PDF formatted dengan penomoran baris rapi via native Android PdfDocument & FileProvider. |
| 12 | **Floating Code Pad (Overlay Melayang)** | ✅ Berhasil | Widget draggable melayang di atas layar dengan fungsi minimize/expand. |
| 13 | **Credit Wajib Pengembang** | ✅ Berhasil | Tersemat permanen: `Credit by : falambons developer`. |
| 14 | **Perbaikan Navigasi Back / Root** | ✅ Berhasil | Tombol panah kembali, gestur HP Android, dan tombol menu "Kembali ke Home Root" berjalan 100% mulus. |

---

## 📦 3. ARSIP FILE BUILD & DISTRIBUSI

- **File APK v2 Siap Pakai:** `FalambonsCode-v2-debug.apk` dan `FalambonsCode-debug.apk` (Root direktori, ukuran ~23 MB).
- **Halaman Web Unduhan:** `index.html` (Melalui port 3000 pada container).
- **Tautan Unduh Langsung v2:** `https://ais-dev-nbdpom2r43a7ibbvzaf5eq-732879384466.asia-northeast1.run.app/FalambonsCode-v2-debug.apk`
- **File Konfigurasi Utama:**
  - `app/src/main/java/com/example/ui/MainViewModel.kt`
  - `app/src/main/java/com/example/ui/screens/CodeEditorScreen.kt`
  - `app/src/main/java/com/example/ui/screens/FolderExplorerScreen.kt`
  - `app/src/main/java/com/example/data/AppRepository.kt`

---
*Dokumen ini adalah referensi penguncian agar seluruh arsitektur stabil dan bebas regresi saat dikembangkan lebih lanjut di masa mendatang.*
