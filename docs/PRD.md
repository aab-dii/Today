# PRD — Today

> **Status dokumen**: Draft v0.1 (15 Okt 2026)
> **Nama produk**: *Today* — **final** (§9 K1). Nama pengembangan lama: "DramaK".
> **Dokumen terkait**: [Arsitektur](ARSITEKTUR.md) · [Roadmap](ROADMAP.md) · [README](../README.md)

---

## 1. Visi

**Today adalah aplikasi Android sosial untuk penonton Indonesia — tempat mencatat apa yang ditonton (film, serial, anime) sebagai diary pribadi yang personal, lalu berbagi opini dengan komunitas.**

Bukan katalog film, bukan sekadar tracker. Jantung produknya adalah **diary nonton yang "manusia"**: bukan cuma bintang dan ulasan, tapi konteks perasaan saat menonton, siapa yang menonton bareng, dan bagaimana penilaianmu berubah saat menonton ulang bertahun-tahun kemudian.

### Problem statement
- Penonton Indonesia mencatat dan mendiskusikan tontonan di tempat terpisah: chat grup (hilang), notes app (tidak terstruktur), atau platform berbahasa Inggris yang komunitasnya jauh (Letterboxd).
- Tracker yang ada (Moviebase, wako, Trakt) fokus pada kelengkapan data, bukan pada *perasaan dan konteks* di balik tontonan.
- Pemain lokal yang ada (TontonYUK, Mokom) berbasis web; belum ada aplikasi Android native yang menempatkan diary sebagai produk utama.

### Prinsip produk
1. **Log-first** — buka app → langsung ditanya "hari ini nonton apa?". Feed sosial (setelah ada) tidak pernah mendahului pencatatan.
2. **Diary-manusia** — setiap entri mendukung konteks: mood, catatan bebas, nonton bareng siapa, rewatch dengan penilaian berbeda.
3. **Satu diary untuk semua tontonan** — film, serial (per-episode), dan anime dalam satu linimasa.
4. **Offline-first** — diary dan watchlist selalu berfungsi tanpa internet. (Sosial baru setelah backend ada — Fase 1.)

## 2. Target pengguna

| Persona | Kebutuhan utama | Pain point hari ini |
|---|---|---|
| **Rina, 25** — penonton K-drama/anime berat | Catat maraton serial, opini per episode, lihat teman menonton apa | Catatan tercecer di chat; Letterboxd tidak mencakup serial |
| **Budi, 22** — mahasiswa penggemar film | Diary refleksi: catatan panjang, rewatch dengan penilaian baru | Notes app tidak terhubung ke data film; tidak ada statistik |

Target geografis awal: **Indonesia**. Bahasa UI: Indonesia.

## 3. Lanskap kompetitif

| Pemain | Bentuk | Cakupan | Kekuatan | Kelemahan (bagi kita) |
|---|---|---|---|---|
| Letterboxd | Web/iOS/Android | Film saja | Komunitas & budaya review terbaik | Bahasa Inggris; tanpa serial/anime; tanpa lokalitas ID |
| TontonYUK | Web (Bandung) | Film + serial | AI discovery, diary, circle, import Letterboxd | Web, bukan app native |
| Mokom | Web | Film | Komunitas lokal, review | Film saja, fitur dangkal |
| Moviebase / wako | Android | Film + serial + anime | Tracker lengkap, Trakt sync | Utilitarian; tanpa komunitas lokal; diary dangkal |
| Trakt / SIMKL | Web + API | Film + serial + anime | Scrobbling otomatis | Bukan produk sosial-diary |

**Celah yang diambil Today**: aplikasi **Android native** untuk penonton Indonesia dengan **diary kontekstual** sebagai diferensiasi inti. Posisi relatif: *Letterboxd-nya Indonesia, tapi mencakup serial & anime, dan diary-nya lebih personal* — mirip strategi sukses Relicário di Brazil.

**Risiko**: TontonYUK/Mokom bisa meluncurkan app mobile; fitur sosial Letterboxd sulit ditandingi resource-wise. Mitigasi: menang di pengalaman native + kedalaman diary, bukan mengejar fitur.

## 4. Ruang lingkup & fase

### Fase 0 — MVP Lokal (target pengembangan saat ini)
Seluruhnya berjalan **offline di perangkat** (Room), tanpa akun.

| # | Fitur | Ringkasan |
|---|---|---|
| F0.1 | Katalog | Jelajahi & cari film, serial, anime (TMDB, multi-region — tanpa filter Korea). Detail: poster, sinopsis, rating komunitas, genre, pemeran. |
| F0.2 | Diary nonton | Catat tontonan: judul, tanggal, rating 0.5–5 bintang, mood (preset), catatan teks, label "nonton bareng", status (sedang ditonton / selesai / di-drop). |
| F0.3 | Rewatch | Menonton ulang membuat entri diary baru dengan tanggal & penilaian sendiri; linimasa menampilkan perubahan penilaian. |
| F0.4 | Tracking serial | Tandai progres episode per musim; entri diary bisa dibuat per-episode atau per-musim. |
| F0.5 | Watchlist | Simpan judul untuk ditonton (pengganti fitur favorite lama). |
| F0.6 | Daftar kustom | Buat daftar tematik ("Film horor 90an", "Wajib tonton bareng pacar"), publik/privat (privat dulu di Fase 0). |
| F0.7 | Stats dasar | Total tontonan, total jam, heatmap kalender sederhana, genre/negara favorit. |
| F0.8 | UX log-first | Layar utama dibuka dengan pertanyaan "hari ini nonton apa?" + aksi cepat catat. |

### Fase 1 — Sosial (butuh backend; provider: keputusan terbuka §9)
| # | Fitur | Ringkasan |
|---|---|---|
| F1.1 | Akun & profil | Daftar/masuk, profil publik (bio, top favorit, stats). |
| F1.2 | Opini publik | Bagikan entri diary sebagai opini/review publik. |
| F1.3 | Feed & follow | Feed aktivitas orang yang di-follow; follow/unfollow. |
| F1.4 | Interaksi | Like, komentar dengan **spoiler tag** (tersembunyi sampai dibuka). |

### Fase 2 — Perluasan
Circle/klub nonton kecil · recap tahunan · import/export (CSV Letterboxd) · notifikasi jadwal rilis · integrasi jadwal tayang platform lokal.

### Di luar lingkup
- Streaming/pemutar video (tidak pernah — hanya katalog & diary).
- Monetisasi (belum dipikirkan di Fase 0–2).
- Web app (Android-first; web hanya jika suatu saat perlu).

## 5. User story & acceptance criteria (Fase 0)

### US-01 Catat tontonan dengan cepat
> Sebagai penonton, aku ingin mencatat tontonan selesai dalam <3 ketukan setelah film berakhir, agar catatan tidak terlupa.

- AC1: Dari layar utama, tap "+" → pilih dari daftar "sedang ditonton" ATAU cari judul → centang "selesai".
- AC2: Entri diary tersimpan dengan tanggal hari ini secara default (bisa diubah).
- AC3: Bekerja penuh offline.

### US-02 Ekspresi penilaian yang personal
> Sebagai penonton, aku ingin menilai bukan hanya bintang tapi juga mencatat perasaanku, agar diary-ku benar-benar mencerminkan pengalamanku.

- AC1: Rating 0.5–5 bintang (step 0.5).
- AC2: Mood dipilih dari preset (mis. "kagum", "terharu", "biasa saja", "kecewa", "senang") — opsional.
- AC3: Catatan teks bebas — opsional.
- AC4: Label "nonton bareng" + teks (mis. "sama teman kos") — opsional.

### US-03 Menonton ulang dengan penilaian berubah
> Sebagai penonton yang suka nonton ulang, aku ingin setiap rewatch jadi entri terpisah sehingga terlihat bagaimana selera-ku berubah.

- AC1: Aksi "tonton ulang" pada judul yang sudah pernah di-diary membuat entri baru dengan tanggal saat ini.
- AC2: Halaman judul menampilkan riwayat penilaian (mis. "2024: ★★★★☆ → 2026: ★★★★★").
- AC3: Tidak ada entri yang ditimpa.

### US-04 Tracking serial per episode
> Sebagai penonton serial, aku ingin menandai episode yang sudah kutonton agar tahu kelanjutanku.

- AC1: Pada detail serial, tandai episode selesai (per musim).
- AC2: Progres tampil ("S2 · E5 dari 12").
- AC3: Penanda "catat sebagai diary" opsional per episode/musim.

### US-05 Watchlist
> Sebagai penonton, aku ingin menyimpan judul yang akan kutonton agar tidak lupa.

- AC1: Tombol tambah ke watchlist di detail/daftar mana pun.
- AC2: Layar watchlist terpisah; bisa diurutkan & dihapus.
- AC3: Status "sedang ditonton" memindahkan judul dari watchlist ke diary aktif.

### US-06 Daftar kustom
> Sebagai penonton, aku ingin membuat daftar tematik milikku sendiri.

- AC1: Buat daftar dengan nama & deskripsi; tambah/hapus judul.
- AC2: Daftar privat di Fase 0 (publik menyusul di Fase 1).
- AC3: Bisa dibagikan sebagai teks/tautan eksternal minimal.

### US-07 Statistik personal
> Sebagai penonton rutin, aku ingin melihat pola tontonanku.

- AC1: Total judul & total jam (estimasi durasi × jumlah).
- AC2: Heatmap kalender (bulan berjalan + tahunan).
- AC3: Top genre & negara asal dari riwayat.
- AC4: Tidak ada fitur premium untuk stats dasar.

### US-08 Katalog universal (bukan cuma drama Korea)
> Sebagai penonton tontonan global, aku ingin mencari film/serial/anime dari negara mana pun.

- AC1: Pencarian lintas movie + TV (termasuk anime via genre Animation).
- AC2: Jelajah populer per region/global; tanpa filter otomatis "Korea saja".
- AC3: Katalog di-cache (Room) agar jelajah ulang cepat & hemat kuota.

## 6. Kebutuhan data & integrasi
- **TMDB API** — metadata film/serial (discover, search, detail, gambar). Sudah dipakai di kode saat ini (`core/data/source/remote`).
- **Katalog anime** — TMDB memuat anime sebagai movie/TV (genre Animation, origin JP); filter khusus diterapkan di lapisan data/UI.
- **Backend (Fase 1)** — **Firebase** (Auth + Firestore), diputuskan (§9 K3). Fondasi data ada di modul `:social` (P4.1).

## 7. Kriteria non-fungsional
| Area | Kriteria |
|---|---|
| Offline | Diary, watchlist, daftar, stats berfungsi penuh tanpa jaringan. Katalog menampilkan cache offline. |
| Performa | Scroll daftar mulus (Compose + paging); pencatatan <3 ketukan; app terbuka ke layar log dalam <1 dtk setelah splash. |
| Bahasa | UI bahasa Indonesia (default; i18n menyusul jika perlu). |
| Privasi | Fase 0: semua data pribadi hanya di perangkat. Tidak ada analytics/telemetri tanpa keputusan eksplisit. |
| Platform | minSdk 24, targetSdk/compileSdk 35; Android 7+ . |
| Arsitektur | Clean Architecture + MVVM; UI Jetpack Compose; domain & data tidak bergantung framework UI (lihat [Arsitektur](ARSITEKTUR.md)). |

## 8. Status implementasi vs rencana (per 17 Okt 2026)

> Bagian ini menjaga AI/pembaca tidak menganggap visi di atas sudah terwujud. Per 16 Okt 2026, **P0, P1, P2, dan P3 selesai** — katalog film+serial (multi-region, tanpa filter Korea) + **diary MVP offline-first** sudah ada, seluruh UI Compose. Ditambah **iterasi P2.10 (K2–K8)**: baris katalog kurasi + kolom cari di Jelajah, detail dengan tombol sadar-status, trailer YouTube, dan catat kilat (bottom sheet). **P3 (perapihan)**: unit test `:core` (47), lint bersih, CI, rename produk → **Today** / `com.today.app`, aksesibilitas. Build+install hijau dan lolos smoke test di emulator `Pixel_4` (API 35). **Penelusuran manual device selesai**: US-01…US-07 + K2–K8 terverifikasi. Limitation P2 (genre/runtime, nama negara, total episode, cast/similar) sudah dibereskan. US-08 (head-to-head) belum dicakup karena desainnya belum final.

| Fitur rencana | Status nyata di kode |
|---|---|
| Katalog film+serial+anime multi-region | ✅ Ada — tab **Jelajah**: baris kurasi (Sedang tren `trending/all/week`, Drama Korea, Anime, Film Indonesia, Tayang sekarang, Rating tertinggi) + daftar "Semua" paged dari cache Room; `discover/movie`/`discover/tv` + `search/multi`. Detail menampilkan **pemain (cast)** & **"Mirip dengan ini"** (`append_to_response=credits,recommendations`), plus **total musim/episode** untuk serial |
| Pencarian & penemuan | ✅ Ada — kolom cari menonjol di Jelajah (placeholder *"Apa yang terakhir kamu tonton?"*) → layar Cari lintas media (K3) |
| Trailer | ✅ Ada — tombol "Tonton trailer" di detail via endpoint `videos` TMDB → buka YouTube (intent) (K6) |
| Catat cepat | ✅ Ada — ikon catat-cepat di kartu judul (katalog & pencarian) + bottom sheet "catat kilat" (judul → bintang → Simpan) (K7/K8) |
| Diary nonton (entri, mood, catatan) | ✅ Ada — `DiaryEntryEntity` + `DiaryDao`/`IDiaryRepository`/`DiaryUseCase`; form Compose (`DiaryFormScreen`) rating ½ bintang, 7 mood preset, catatan, bareng, tanggal. **P2 selesai per 16 Okt 2026** |
| Rewatch & riwayat penilaian | ✅ Ada — aksi "Tonton ulang" (`rewatch_of`) + linimasa penilaian di layar detail |
| Tracking episode | ✅ Ada — simpan musim/episode manual (form serial + dialog "Progres" di detail) pada entri berstatus "sedang ditonton"; **total musim/episode dari TMDB** kini ditampilkan (detail + petunjuk di dialog Progres) |
| Watchlist | ✅ Ada — tab Watchlist Compose (grid, hapus, tambah ke daftar). **Modul `:favorite` + flag `is_favorite`/`is_watching` dihapus di P2.6** |
| Daftar kustom | ✅ Ada — `CustomList` + layar list/detail + dialog "Tambah ke daftar" (dari detail & watchlist) |
| Stats dasar | ✅ Ada — total judul/catatan/jam, heatmap 13 minggu, top genre/negara (`StatsScreen`). **Rekap Tahunan ("Today Wrapped", P4.6.1)** ✅ — layar `RecapScreen` (`ROUTE_RECAP`) berisi kartu geser story-style (`HorizontalPager`) + pemilih tahun + berbagi teks via `ACTION_SEND` (`RecapCalculator`, `RecapViewModel`) |
| Impor / Ekspor CSV | ✅ Ada (P4.6.2) — `Csv` + `DiaryCsvCodec` di `:core` (export/import RFC4180; format sendiri & **diary Letterboxd**). Layar `ImportExportScreen` (`ROUTE_IMPORT_EXPORT`) via Storage Access Framework (`CreateDocument`/`OpenDocument`), baris **"Impor / Ekspor"** di `ProfileScreen`. Entri impor (`titleId=0`) tetap bisa diedit/disimpan |
| Jadwal rilis | ✅ Ada (P4.6.3/P4.6.4) — layar `ReleaseScheduleScreen` (`ROUTE_RELEASE_SCHEDULE`, ikon di TopAppBar Home): episode TV mendatang (`next_episode_to_air` TMDB) + film belum rilis dari **watchlist**, dikelompokkan per hari. **Notifikasi lokal** (P4.6.4) via **WorkManager** (periodik harian ±08:00 + izin `POST_NOTIFICATIONS`, channel `release_schedule`), diaktifkan lewat toggle di layar Jadwal Rilis. Push server (FCM) tidak dipakai (K8) |
| Circle | ✅ Ada (P4.6.5) — klub nonton via **kode undangan**: `Circle`/`CircleMember` (domain) + `FirestoreCircleDataSource` (koleksi `circles` + subkoleksi `members`); layar `CirclesScreen` (`ROUTE_CIRCLES`; buat/gabung, daftar) & `CircleDetailScreen` (`ROUTE_CIRCLE_DETAIL`; kode undangan + bagikan, daftar anggota, feed opini anggota, like/komentar, keluar) dari baris **"Circle"** di Profil. Chat/watch party & circle publik di luar cakupan |
| Sosial (feed/follow/komentar) | ✅ Ada — **Auth gate + profil + bagikan opini + feed/follow + like & komentar spoiler + circle** (P4.2–P4.6.5). **Halaman auth tampil DI DEPAN sebelum masuk app utama** (`TodayApp`), bukan lagi di tab Profil. Tab **Profil** menggantikan tab Stats (Stats dibuka dari Profil) & kini profil (lihat/edit + keluar) + baris **"Opini saya"** + **"Feed Komunitas"** + **"Circle"**. Masuk/daftar email, reset sandi, tombol Google. **Bagikan opini** (F1.2) ✅. **Feed & follow** (F1.3, P4.4) ✅ — layar `FeedScreen` (`ROUTE_FEED`) dengan tab "Untuk Anda" vs "Mengikuti", aksi follow/unfollow per kartu, dibuka dari TopAppBar Home & Profil. **Like & komentar dengan spoiler tag** (F1.4, P4.5) ✅ — like interaktif dengan optimistic update, modal bottom sheet komentar real-time, card spoiler tap-to-reveal, hapus komentar penulis dengan decrement atomik `commentCount`. |
| Backend | ⚙️ **Firebase aktif + auth gate + sinkron profil/opini/circle** (P4.1/P4.2/P4.6) — modul `:social` (Firebase Auth + Firestore) ter-wire di Koin; `app/google-services.json` (gitignored) → plugin google-services aktif. **Gate auth** di `TodayApp`: splash → form auth (bila belum masuk) → app utama. **Email/Password aktif** (sign-up nyata terverifikasi device). **Sign-in Google nyata terverifikasi device** (account picker → Home). **Cloud Firestore API aktif + database dibuat** (`firestore.rules`/`firebase.json`) → **profil tersinkron server** (terverifikasi device: simpan → restart → muat dari server) + **opini publik tersinkron** + **circle tersinkron** (terverifikasi device: buat/gabung/keluar). |

**Yang nyata berfungsi sebagai fondasi**: struktur multi-module Gradle (tanpa dynamic feature), Clean Architecture + Koin, pipeline TMDB (Retrofit + `RemoteDataSource` movie/tv/search), Room + Paging (`TitleRemoteMediator`, offline cache), dan **diary/watchlist/daftar/stats offline-first**.

**Keterbatasan yang sudah dibereskan (16 Okt 2026)**: (1) genre + **runtime** kini diisi otomatis saat mencatat dari katalog (enrichment best-effort dari detail TMDB) → total jam lebih akurat; (2) negara kode ISO **dipetakan ke nama** (`CountryCatalog`, mis. "KR" → "Korea Selatan"); (3) **total musim/episode TMDB** diambil & ditampilkan; (4) detail menampilkan **cast** & rekomendasi.

**Keterbatasan yang tersisa**: (1) **US-08 (head-to-head)** belum (desain belum final); (2) baris kurasi butuh jaringan (belum di-cache seperti daftar "Semua"); (3) enrichment runtime pada catat-katalog butuh jaringan (bila offline, entri tetap tersimpan tanpa runtime).


## 9. Keputusan terbuka
| # | Keputusan | Status | Catatan |
|---|---|---|---|
| K1 | Nama produk final | **Diputuskan** | **Today** (label app `Today`). Nama pengembangan lama "DramaK" tidak dipakai lagi. |
| K2 | Penamaan package/app ID | **Diputuskan** | **`com.today.app`** (namespace + `applicationId`; core `com.today.app.core`). Rename P3.5 selesai 16 Okt 2026. |
| K3 | Provider backend Fase 1 | **Diputuskan** | **Firebase** (Firestore + Auth) untuk P4. |
| K4 | Skala mood | **Diputuskan sementara** | 7 preset di `Mood`: Kagum, Terharu, Senang, Biasa saja, Kecewa, Tegang, Bosan (dipakai di P2.3). Bisa direvisi. |
| K5 | Serial: entri diary per-episode vs per-musim | Terbuka | Usulan: keduanya, default per-musim (US-04) |
| K6 | Metode auth Fase 1 | **Diputuskan** | **Email & password + Google Sign-In** (Firebase Auth). Halaman auth = **gate di depan app** (`TodayApp`; P4.2). **Prasyarat Console**: (1) aktifkan provider **Email/Password** (**sudah aktif** — sign-up nyata OK) & **Google** (**sudah aktif** — sign-in Google nyata OK); (2) daftarkan SHA-1 (`D1:8E:73:…:0D`); (3) **aktifkan Cloud Firestore API + buat database** (**sudah aktif** — sinkron profil terverifikasi) + publish `firestore.rules`; (4) unduh ulang `google-services.json` (`oauth_client` sudah terisi). Error provider mati dipetakan ke pesan Indonesia ("Metode masuk ini belum diaktifkan di Firebase Console."). |
| K7 | Struktur modul sosial | **Diputuskan** | Modul baru **`:social`** (`com.today.app.social`; domain+data, **tanpa UI**). `google-services.json` diterapkan **kondisional** & **gitignored**. |
| K8 | Notifikasi jadwal rilis | **Diputuskan** | **Lokal via WorkManager** (P4.6.4) — `PeriodicWorkRequest` harian (initial delay ±08:00, `NetworkType.CONNECTED`) + izin runtime `POST_NOTIFICATIONS`, channel `release_schedule`; tanpa FCM/server. Integrasi jadwal tayang platform lokal tetap terbuka. |
| K9 | Cakupan Circle | **Diputuskan** | **Klub dasar via kode undangan** (P4.6.5): buat/gabung dengan kode 6 karakter, daftar anggota, feed opini anggota, keluar. **Chat/watch party & circle publik di luar cakupan.** Dokumen `circles` dapat dibaca signed-in agar kode bisa dicari. |

> **Catatan**: keputusan desain/UX terbaru (alur katalog→detail→aksi, katalog kurasi, kolom cari, susunan Detail, trailer, catat kilat) dan pertanyaan terbuka (layar awal, nama) ada di [CATATAN_KEPUTUSAN.md](CATATAN_KEPUTUSAN.md) (K1–K8 + Q1–Q4) dan [DESAIN_APLIKASI.md](DESAIN_APLIKASI.md). Bila bentrok, **CATATAN_KEPUTUSAN menang**. Penomoran `K1–K8` di catatan itu **berbeda** dari `K1–K5` di tabel di atas.

## 10. Riwayat revisi
| Versi | Tanggal | Perubahan |
|---|---|---|
| 0.1 | 15 Okt 2026 | Draft awal — visi pivot dari katalog drama ke sosial-diary film/serial/anime |
| 0.2 | 16 Okt 2026 | P0+P1 selesai (pivot katalog film+serial, migrasi UI Compose, modul `:favorite` jadi modul biasa); status §8 diperbarui |
| 0.3 | 16 Okt 2026 | P2 selesai (diary MVP offline-first: entri/mood/catatan, rewatch, tracking episode manual, watchlist, daftar kustom, stats); modul `:favorite` dihapus; K4 mood diputuskan sementara |
| 0.4 | 16 Okt 2026 | Iterasi P2.10 (K2–K8): baris katalog kurasi + kolom cari, detail tombol sadar-status + trailer, catat-cepat (ikon + bottom sheet); build+install hijau, smoke test device OK |
| 0.5 | 16 Okt 2026 | Uji device menyeluruh selesai (US-01…US-07 + K2–K8 terverifikasi); bug navigasi kritis ditemukan & diperbaiki (J22 di ARSITEKTUR §5); US-08 head-to-head belum dicakup |
| 0.6 | 16 Okt 2026 | Perapihan kualitas P3.1/P3.2/P3.3/P3.4/P3.6: bersihkan `libs.versions.toml` (8 library + 8 versi tak terpakai) & XML tema; **37 unit test** `:core` (ganti 4 test template) via fake DAO; **CI GitHub Actions**; lint `0 errors` (perbaiki `AutoboxingStateCreation`/`RedundantLabel`/`OldTargetApi`); aksesibilitas (dark mode dibedakan, touch target bintang ≥48dp). **P3.5 (rename) belum** (butuh keputusan K1/K2) |
| 0.7 | 16 Okt 2026 | **K1/K2/K3 diputuskan**: nama produk **Today**, package **`com.today.app`**, backend **Firebase**. **P3.5 rename selesai**: package Kotlin, namespace, `applicationId`, root project, label `Today`, tema `Theme.Today`/`TodayTheme`, DB `Today.db`; build+37 test hijau |
| 0.8 | 16 Okt 2026 | **Limitasi P2 dibereskan** (terverifikasi device): runtime+genre di-enrich saat catat dari katalog; kode negara ISO→nama (`CountryCatalog`); total musim/episode TMDB di detail + dialog Progres; **cast** & **rekomendasi "Mirip dengan ini"** di detail (`append_to_response=credits,recommendations`). 47 unit test `:core` hijau |
| 0.9 | 16 Okt 2026 | **P4.1 (fondasi sosial) selesai**: modul **`:social`** (domain+data Firebase Auth + Firestore — profil/opini/like/komentar/follow) ter-wire di Koin; plugin google-services **kondisional** & `google-services.json` **gitignored**; keputusan **K6** (auth: email+Google) & **K7** (modul `:social`). 6 unit test `:social` + build hijau. UI/sosial (P4.2+) belum |
| 0.10 | 16 Okt 2026 | **Firebase diaktifkan**: `app/google-services.json` (project `today-1666e`) ditaruh → plugin google-services aktif (`processDebugGoogleServices`), **Firebase init sukses di device** (logcat). `oauth_client` masih kosong → Google Sign-In menunggu registrasi SHA-1 |
| 0.11 | 16 Okt 2026 | **P4.2 (auth + profil) selesai**: tab **Profil** menggantikan tab Stats (Stats dibuka dari Profil); layar auth Compose (masuk/daftar email, reset sandi, tombol Google nonaktif sampai `oauth_client` ada) + profil lihat/edit + keluar; `ProfileViewModel` + `:social` mapper error auth → pesan Indonesia (`AuthErrorMapper`). 59 unit test (core 47 + social 12); lint `0 errors`; build hijau. **Terverifikasi device**: form auth, toggle masuk/daftar, validasi, dan pesan error local. Sign-up nyata & Google menunggu provider/OAuth client diaktifkan di Console |
| 0.12 | 16 Okt 2026 | **Auth jadi gate di depan app** (lanjutan P4.2): `TodayApp` (`ui/AppRoot.kt`) observasi sesi → splash / `AuthScreen` / `AppNavHost`; `AuthViewModel`+`AuthScreen` baru; tab **Profil** kini profil-only. **Terverifikasi device**: gate muncul sebelum app; **sign-up email nyata sukses** (provider Email/Password aktif) → masuk Home; sesi persisten; Keluar → kembali ke gate; tombol Google aktif (`oauth_client` terisi). `ProfileViewModel` dibuat tahan saat server mati (timeout 6 dtk + fallback dari data sesi). ⚠️ **Cloud Firestore API belum diaktifkan** di project `today-1666e` → profil belum tersinkron server |
| 0.13 | 16 Okt 2026 | **Sinkron profil Firestore terverifikasi end-to-end.** Cloud Firestore API **diaktifkan** + database dibuat + `firestore.rules`/`firebase.json` ditambahkan (ruleset users/following/followers/opinions/likes/comments). Perbaikan: `FirestoreSocialDataSource.getProfile` kini `get(Source.SERVER)` → kegagalan baca saat offline **tidak** lagi dianggap "dokumen kosong" (mencegah fallback sesi menimpa dokumen server); `REMOTE_TIMEOUT_MS` 6→**15 dtk**; form edit profil **pre-fill dari profil server** (bukan nama sesi). **Terverifikasi device** (cold-boot emulator): Simpan nama unik → force-stop → restart → Profil memuat nilai dari Firestore. 59 test hijau; lint `0 errors`; build hijau |
| 0.14 | 16 Okt 2026 | **Sign-in Google nyata terverifikasi device**: provider Google diaktifkan di Console → tombol "Masuk dengan Google" → account picker → masuk Home; Profil menampilkan nama/email Google ("Lagu Gweh"/`gwehlagu@gmail.com`). Prasyarat Console (K6) kini **lengkap**: Email/Password ✅, Google ✅, Firestore ✅. Sisa roadmap Fase 1: P4.3 (bagikan opini dari diary) → P4.4 (feed + follow) → P4.5 (like + komentar) |
| 0.15 | 16 Okt 2026 | **P4.3 (bagikan opini / F1.2) selesai & terverifikasi device.** Baru: `OpinionMapper` (`:social`) — entri diary → `Opinion` snapshot (id baru, catatan opsional, spoiler) + unit test; `OpinionViewModel` (`:app`, daftar `myOpinions` via `observeUserOpinions` + `publish`/`deleteOpinion`, timeout 15 dtk, snackbar, error ramah) di Koin; `ShareOpinionDialog` (poster + rating/mood + catatan + toggle spoiler), `OpinionCard` + `MyOpinionsScreen` (rute `ROUTE_MY_OPINIONS`, empty state, konfirmasi hapus); aksi **"Bagikan"** per-baris di `DetailScreen` ("Linimasa tontonan"); baris **"Opini saya"** di `ProfileScreen`. **Device**: detail → catat entri → Bagikan → opini muncul di "Opini saya" → hapus → empty state. 64 unit test (core 47 + social 14 + app 3); lint `0 errors`; build hijau |
| 0.16 | 16 Okt 2026 | **P4.4 (feed aktivitas + follow / F1.3) selesai.** Baru: `FeedViewModel` (`:app`, mode `FOR_YOU` vs `FOLLOWING`, real-time streams `forYou`/`followingFeed`/`followingUids`, toggle follow dengan guard auth & self); `FeedScreen` (`ROUTE_FEED`, TabRow, reactive list `OpinionCard`, empty states, tombol follow/unfollow kontekstual); navigasi dibuka dari TopAppBar `HomeScreen` & menu `ProfileScreen`; `OpinionComponents.toTitle` dijadikan utilitas publik bersama; 5 unit test baru (`FeedViewModelTest`). Total test suite **69 unit test hijau**; lint `0 errors`; `:app:assembleDebug` sukses |
| 0.17 | 17 Okt 2026 | **P4.5 (like + komentar spoiler-tag / F1.4) selesai.** Baru: `SocialRepository` & `FirestoreSocialDataSource` menambahkan `observeIsLiked`, `isLiked`, `deleteComment` (transaksional decrement `commentCount`); `CommentsViewModel` (`:app`) stream komentar real-time, toggle mask spoiler per komentar, input komentar baru dengan spoiler toggle, hapus komentar; `FeedViewModel` pelacakan `likedOpinionIds` & `toggleLike` dengan optimistic update + failure rollback; UI Compose `CommentsBottomSheet` (Material3 bottom sheet, header konteks, tap-to-reveal spoiler card, delete action, input bar), `OpinionCard` footer like/komentar, integrasi di `FeedScreen` & `MyOpinionsScreen`. 13 test baru (`CommentsViewModelTest` 9 + `FeedViewModelTest` 4); total test suite **82 unit test hijau**; lint `0 errors`; build sukses |
| 0.18 | 17 Okt 2026 | **P4.5.1 (profil publik pengguna lain / F1.1 lanjutan) selesai.** Layar `PublicProfileScreen` (`ROUTE_PUBLIC_PROFILE` / "public_profile") menampilkan bio, avatar, status follow/unfollow, dan seluruh opini publik akun lain saat nama author diketuk di kartu `FeedScreen` atau `CommentsBottomSheet`. `PublicProfileViewModel` mengamati `observeUserOpinions`, status following, dan status like. 10 test baru di `PublicProfileViewModelTest`. Total test suite **92 unit test hijau**; lint `0 errors`; build sukses |
| 0.19 | 17 Okt 2026 | **Seeder Data Dummy & Uji Fungsional Lengkap di Device.** Pembuatan `DummyDataSeeder` (populasi Room: diary, watchlist, daftar kustom; Firestore: opini & komentar). Seluruh fitur inti & sosial teruji dan terverifikasi live di emulator: Home (linimasa & carousel aktif), Watchlist, Custom Lists, Stats (heatmap, genre, negara), Jelajah, Feed Komunitas, Like reaktif, Komentar spoiler tag & mask tap-to-reveal, hapus komentar atomik, dan Profil Publik pengguna lain |
| 0.20 | 17 Okt 2026 | **P4.6.1 (Rekap Tahunan / "Today Wrapped") selesai & terverifikasi device.** Baru di `:core`: model `YearlyRecap`/`RecapTitle` + `RecapCalculator` (filter per tahun, total judul/catatan/jam, rewatch, top genre/negara, mood dominan, judul tersering, rating tertinggi, bulan tersibuk, streak hari beruntun, teman nonton) + kontrak `IDiaryRepository`/`DiaryUseCase` (`getRecapYears`, `getRecap`) + `RecapCalculatorTest` (12 test). Baru di `:app`: `RecapViewModel` + `RecapScreen` (`ROUTE_RECAP`, `HorizontalPager` story-style 8 kartu, pemilih tahun `FilterChip`, indikator titik, berbagi via `ACTION_SEND`), baris **"Rekap Tahunan"** di `ProfileScreen`. **Device (Pixel_4, Android 15)**: Profil → Rekap Tahunan → geser kartu (intro/total/genre/mood/judul tersering/rating tertinggi/irama/outro) → tombol "Bagikan rekap" membuka Android chooser dengan teks rekap. Total test suite **105 hijau** (core 59 + social 14 + app 32); lint `0 errors`; build hijau |
| 0.21 | 17 Okt 2026 | **P4.6.2 (Impor / Ekspor CSV) selesai & terverifikasi device.** Baru di `:core`: `utils/Csv.kt` (codec RFC4180) + `utils/DiaryCsvCodec.kt` (ekspor header kanonik & impor format sendiri **+ diary Letterboxd**; `titleId=0`/`mediaType=MOVIE` untuk Letterboxd) + kontrak `IDiaryRepository`/`DiaryUseCase` (`exportDiaryCsv`, `importDiaryCsv`) + `CsvTest` (8) + `DiaryCsvCodecTest` (8). Baru di `:app`: `ImportExportViewModel` + `ImportExportScreen` (`ROUTE_IMPORT_EXPORT`) memakai **Storage Access Framework** (`CreateDocument`/`OpenDocument`), baris **"Impor / Ekspor"** di `ProfileScreen`; `DiaryFormScreen`/`DiaryFormViewModel` dilonggarkan agar entri impor `titleId=0` bisa diedit/disimpan. **Device (Pixel_4, Android 15)**: ekspor `today-diary-20261017.csv` (7 baris, kutipan koma benar) → impor `test-import.csv` (2 baris) → "9 catatan" → entri baru tampil di Home → buka entri impor → **Simpan** aktif & sukses. Total test suite **121 hijau** (core 75 + social 14 + app 32); lint `0 errors`; build hijau |
| 0.22 | 17 Okt 2026 | **P4.6.3 (Jadwal rilis) selesai & terverifikasi device.** Baru di `:core`: `EpisodeAirResult`/`next_episode_to_air` di `TitleDetailResponse`, model `NextEpisode` (di `TitleDetail`) & `UpcomingRelease`; `ReleaseScheduleCalculator` (gabung watchlist × detail → episode TV mendatang + film belum rilis, filter ≥ hari ini, urut tanggal, label `S{n} E{n} · judul`) + `ReleaseScheduleUseCase`/`ReleaseScheduleInteractor` (`getUpcomingReleases`) + `ReleaseScheduleCalculatorTest` (7 test). Baru di `:app`: `ReleaseScheduleViewModel` + `ReleaseScheduleScreen` (`ROUTE_RELEASE_SCHEDULE`, kelompok "Hari ini"/"Besok"/tanggal, segarkan, state kosong) + `ReleaseScheduleViewModelTest` (3 test); ikon **"Jadwal Rilis"** (`Icons.Filled.CalendarMonth`) di TopAppBar `HomeScreen`. **Device (Pixel_4, Android 15)**: state kosong → tambah "The Simpsons" → **"Besok"** `S38 E3 · Treehouse of Horror Presents: The Revengening` → tap → detail (rating 8.0). **Notifikasi push menunggu keputusan produk (PRD §9).** Total test suite **131 hijau** (core 82 + social 14 + app 35); lint `0 errors`; build hijau |
| 0.23 | 17 Okt 2026 | **P4.6.4 (Notifikasi rilis lokal) + P4.6.5 (Circle) selesai & terverifikasi device.** *Notifikasi*: WorkManager 2.9.1; `ReleaseScheduleCalculator.dueSoon` (+3 test); `notifications/ReleaseNotifications` (channel `release_schedule`, izin `POST_NOTIFICATIONS`, `show`), `ReleaseNotificationWorker`, `ReleaseNotificationScheduler` (periodik harian ±08:00, network constraint), `ic_notification.xml`; `MyApplication` buat channel + jadwalkan; toggle di `ReleaseScheduleScreen`. **K8 diputuskan: lokal WorkManager.** *Circle*: `:social` `Circle`/`CircleMember` + `FirestoreCircleDataSource` (koleksi `circles` + subkoleksi `members`; kode 6 karakter; `whereArrayContains`) + `CircleRepository` (5 test) + mapper (+3 test) + `firestore.rules` `circles`/`members`; `:app` `CirclesScreen`/`CirclesViewModel` + `CircleDetailScreen`/`CircleDetailViewModel` (`ROUTE_CIRCLES`/`ROUTE_CIRCLE_DETAIL`) + baris "Circle" di Profil. **K9 diputuskan: klub dasar via kode undangan.** **Device (Pixel_4, Android 15)**: toggle notifikasi → izin → job terjadwal → worker → NotificationRecord `id=4201`; Circle buat "NobarJumat" (kode `9N5Y8L`) → feed anggota → keluar → gabung ulang via kode. Total test suite **142 hijau** (core 85 + social 22 + app 35); lint `0 errors`; build hijau |
| 0.24 | 18 Okt 2026 | **Perapihan konsistensi UI + hapus fitur demo.** (1) **Format tanggal diseragamkan** (`ui/components/Formats.kt`): list/kartu/komentar pakai `d MMM yyyy` ("18 Okt 2026"), halaman **Detail** pakai nama bulan penuh (`d MMMM yyyy` → "18 Oktober 2026"); tanggal ISO TMDB (`release_date`, bisa `yyyy-MM-dd`/`yyyy-MM`/`yyyy`) kini diformat (`formatIsoDate`/`formatIsoDateLong`) alih-alih ditampilkan mentah; header Jadwal Rilis tetap "Hari ini"/"Besok" lalu absolut (TMDB tak menyediakan jam rilis). (2) **Tinggi judul kartu poster dikunci** (`TodayPosterBox` + grid `WatchlistScreen` pakai `minLines = 2`) → jarak ke seksi berikutnya tak lagi naik-turun mengikuti panjang judul. (3) **Fitur "Isi Data Dummy (Demo)" dihapus**: `DummyDataSeeder.kt`, `ProfileViewModel.seedDummyData()`, baris menu di `ProfileScreen`, dan factory Koin-nya dihilangkan — data demo cukup di-inject manual saat pengujian. Total test suite **142 hijau**; lint `0 errors`; build hijau |
