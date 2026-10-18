# Status Pengembangan — Today

**Catatan kemajuan pengembangan (bukan pengantar produk).** Untuk pengenalan aplikasi, cara setup, dan build, lihat [README](../README.md).

> Dokumen lengkap: [PRD](PRD.md) · [Arsitektur](ARSITEKTUR.md) · [Roadmap](ROADMAP.md) · [Desain Aplikasi](DESAIN_APLIKASI.md) · [Catatan Keputusan](CATATAN_KEPUTUSAN.md)

## Status proyek

Per 17 Okt 2026, **P0–P3 sudah selesai** (produk bernama **Today**, package `com.today.app`), dan **P4.1–P4.5.2 (sosial) + P4.6.1 (rekap tahunan) + P4.6.2 (impor/ekspor CSV) + P4.6.3 (jadwal rilis) + P4.6.4 (notifikasi rilis lokal) + P4.6.5 (circle) selesai**:

- Fondasi (Clean Architecture, Koin, TMDB, Room + Paging) ada di `:core` dan dipakai ulang.
- Seluruh UI kini **Jetpack Compose** (XML/Fragment lama sudah dihapus).
- **Diary MVP sudah ada**: catat tontonan (rating ½ bintang, mood, catatan, nonton bareng), rewatch + linimasa penilaian, tracking episode (dengan total musim/episode TMDB), **watchlist**, **daftar kustom**, dan **stats dasar** — semuanya offline-first (Room).
- Detail judul kini menampilkan **pemain** & rekomendasi **"Mirip dengan ini"**.
- Kode lama `Drama*` dan modul `:favorite` sudah dihapus; katalog kini film + serial (+ anime yang muncul alami).
- **P3 (perapihan) selesai**: unit test `:core` (47), lint bersih, CI GitHub Actions, rename produk/package.
- **P4.1 selesai (fondasi)**: modul **`:social`** (Firebase Auth + Firestore, data layer) ter-wire di Koin; `app/google-services.json` sudah ada (gitignored) → Firebase **aktif** (init sukses di device).
- **P4.2 selesai (auth gate + profil)**: **halaman auth tampil di depan sebelum masuk app** (`TodayApp`) — daftar/masuk email, reset sandi, tombol Google; tab **Profil** (menggantikan Stats) kini profil-only (lihat/edit profil, keluar); error auth dipetakan ke Bahasa Indonesia. Terverifikasi device: sign-up email **nyata sukses** → Home; **sign-in Google nyata sukses** (account picker → Home); sesi persisten; Keluar → kembali ke gate. **Firestore aktif** (Cloud Firestore API + database + `firestore.rules`/`firebase.json`) → **profil tersinkron server** (terverifikasi: simpan → restart → muat).
- **P4.3 selesai (bagikan opini / F1.2)**: aksi **"Bagikan"** per-baris entri diary di detail → dialog (catatan + toggle spoiler) → Firestore; tab **Profil** → baris **"Opini saya"** (rute `ROUTE_MY_OPINIONS`) untuk melihat + menghapus opini sendiri. Terverifikasi device: catat → Bagikan → muncul di "Opini saya" → hapus → empty state.
- **P4.4 selesai (feed aktivitas + follow / F1.3)**: layar **Feed Komunitas** (`ROUTE_FEED`) dengan tab "Untuk Anda" (semua opini publik terbaru) & "Mengikuti" (feed opini akun yang di-follow) + aksi follow/unfollow responsif langsung di kartu opini. Dibuka lewat TopAppBar `HomeScreen` & menu `ProfileScreen`.
- **P4.5 selesai (like + komentar spoiler-tag / F1.4)**: interaksi Like/Unlike dengan optimistic update di Feed & detail komentar; sistem komentar real-time dengan mask spoiler tap-to-reveal ("*Komentar mengandung spoiler. Ketuk untuk membuka*"); input komentar dengan chip toggle spoiler; penghapusan komentar penulis dengan transaksi decrement atomik `commentCount`. Tampil di bottom sheet Compose `CommentsBottomSheet` pada `FeedScreen` dan `MyOpinionsScreen`.
- **P4.5.1 selesai (profil publik pengguna lain / F1.1 lanjutan)**: layar profil publik `PublicProfileScreen` (`ROUTE_PUBLIC_PROFILE`) saat nama author diketuk di kartu feed maupun komentar — menampilkan bio, avatar, status follow/unfollow, dan seluruh opini publik akun lain.
- **P4.6.1 selesai (rekap tahunan / "Today Wrapped")**: baris **"Rekap Tahunan"** di `ProfileScreen` → layar `RecapScreen` (`ROUTE_RECAP`) berisi **kartu geser story-style** (`HorizontalPager`): intro tahun, total judul/catatan/jam, genre & negara favorit, mood dominan, judul tersering, rating tertinggi, irama (bulan tersibuk, streak hari beruntun, teman nonton), dan outro dengan **berbagi teks** (`ACTION_SEND`). Agregasi offline di `:core` (`RecapCalculator`, `YearlyRecap`) + pemilih tahun.
- **P4.6.2 selesai (impor / ekspor CSV)**: baris **"Impor / Ekspor"** di `ProfileScreen` → layar `ImportExportScreen` (`ROUTE_IMPORT_EXPORT`) memakai **Storage Access Framework** (`CreateDocument`/`OpenDocument`). `Csv` + `DiaryCsvCodec` (`:core`) mengekspor diary ke CSV dan mengimpor **format Today** maupun **diary Letterboxd** (`titleId=0`, `mediaType=MOVIE`); entri impor tetap bisa diedit/disimpan.
- **P4.6.3 selesai (jadwal rilis)**: ikon **"Jadwal Rilis"** (`CalendarMonth`) di TopAppBar `HomeScreen` → layar `ReleaseScheduleScreen` (`ROUTE_RELEASE_SCHEDULE`): episode TV mendatang (`next_episode_to_air` TMDB) + film belum rilis dari **watchlist**, dikelompokkan per hari ("Hari ini"/"Besok"/tanggal) dengan poster, label `S{n} E{n} · judul`, dan tombol segarkan. Agregasi offline di `:core` (`ReleaseScheduleCalculator`).
- **P4.6.4 selesai (notifikasi rilis lokal)**: toggle **"Notifikasi jadwal rilis"** di `ReleaseScheduleScreen` → izin `POST_NOTIFICATIONS` → **WorkManager periodik harian** (±08:00, `NetworkType.CONNECTED`, channel `release_schedule`). Worker mengambil rilis ≤ besok via `ReleaseScheduleUseCase` dan memposting notifikasi (tap → buka app). Tanpa FCM/server (**K8**).
- **P4.6.5 selesai (circle)**: baris **"Circle"** di `ProfileScreen` → layar `CirclesScreen` (`ROUTE_CIRCLES`) buat/gabung klub via **kode undangan 6 karakter** + `CircleDetailScreen` (`ROUTE_CIRCLE_DETAIL`) menampilkan kode + bagikan, daftar anggota, feed opini anggota (like/komentar), dan keluar. Data di Firestore (`circles` + subkoleksi `members`, `:social`). Chat/watch party di luar cakupan (**K9**).
- **Sudah diuji di emulator `Pixel_4` (API 35)**: US-01…US-07 + iterasi K terverifikasi; alur auth gate end-to-end (P4.2) + sinkron profil Firestore + bagikan opini (P4.3) + feed/follow (P4.4) + like/komentar (P4.5) + rekap tahunan (P4.6.1) + impor/ekspor CSV (P4.6.2) + jadwal rilis (P4.6.3) + notifikasi rilis (P4.6.4) + circle (P4.6.5) terverifikasi. **142 unit test** (`:core` 85 + `:social` 22 + `:app` 35), lint `0 errors`.
- Selanjutnya: fitur Fase 2 lanjutan (chat/watch party). Lihat [Roadmap](ROADMAP.md).

Jangan menganggap fitur di PRD sudah terwujud sebelum tabel [status implementasi](PRD.md#8-status-implementasi-vs-rencana-per-15-oke-2026) menyatakan demikian.

## Tech stack

| Area | Teknologi |
|---|---|
| Bahasa | Kotlin 2.0.0 |
| Build | Gradle 8.7, AGP 8.6.0, version catalog |
| UI | Jetpack Compose + Material 3 |
| Navigasi | navigation-compose |
| DI | Koin 3.5.6 + koin-androidx-compose |
| Network | Retrofit 2.11 + OkHttp + Gson |
| Database | Room 2.6.1 + Paging (`RemoteMediator`) |
| Sumber data | [TMDB API](https://www.themoviedb.org/documentation/api) |
| Backend (Fase 1) | Firebase (Auth + Firestore) |
| Background | WorkManager 2.9.1 (notifikasi rilis lokal) |
| Async | Coroutines + Flow |

Rincian: [ARSITEKTUR.md](ARSITEKTUR.md).

## Struktur module

```
Today/
├── app/       # shell + SELURUH UI Compose (home, jelajah, detail, diary, watchlist, daftar, stats, recap, sosial)
├── core/      # domain + data: katalog TMDB, Room, diary/watchlist/daftar/stats/recap/CSV/jadwal, use case, model
└── social/    # domain + data sosial (Firebase Auth + Firestore: profil/opini/like/komentar/follow) — TANPA UI
```

`settings.gradle.kts` menyertakan `:app`, `:core`, dan `:social` (modul `:favorite` sudah dihapus di P2.6).

## Prasyarat

- Android Studio (AGP 8.6) dengan JDK 17+
- Akun [TMDB](https://www.themoviedb.org/signup) + API key
- Perangkat/emulator Android 7.0+ (minSdk 24)

## Cara menjalankan

1. **Clone & buka** project di Android Studio; biarkan Gradle sync.

2. **Siapkan API key TMDB** di `local.properties` (root project):
   ```properties
   TMDB_API_KEY=isi_api_key_mu_di_sini
   BEARER_TOKEN=isi_bearer_token_mu_di_sini
   ```
   *(Secrets dibaca `core/build.gradle.kts` dari `local.properties`; file ini gitignored — jangan commit.)*

3. *(Opsional — fitur sosial/backend)* Taruh `google-services.json` di folder `app/` dari project Firebase-mu. **Wajib gitignored, jangan di-commit.** Tanpa file ini build tetap hijau, hanya Firebase yang tidak aktif (plugin diterapkan kondisional).

4. **Jalankan** konfigurasi `app` ke emulator/perangkat.

> Diary, watchlist, daftar kustom, dan stats bekerja **offline** (Room). Katalog butuh internet saat pertama kali memuat.

## Cara build

```bash
./gradlew :app:assembleDebug      # build debug APK
./gradlew test                    # unit test
./gradlew lint                    # Android lint
```

## Berkontribusi / melanjutkan pengembangan

1. Baca [PRD](PRD.md) untuk visi & fitur, [Arsitektur](ARSITEKTUR.md) untuk struktur kode dan daftar "jebakan" yang sudah diketahui.
2. Kerjakan item sesuai urutan [Roadmap](ROADMAP.md) (P0–P3 ✅ selesai; **P4 sedang berjalan** — P4.1 fondasi `:social` + P4.2 auth/profil + P4.3 bagikan opini + P4.4 feed + follow + P4.5 like/komentar + P4.5.1 profil publik + P4.6.1 rekap tahunan + P4.6.2 impor/ekspor CSV + P4.6.3 jadwal rilis + P4.6.4 notifikasi rilis + P4.6.5 circle selesai).
3. Setelah menyelesaikan fitur, perbarui checkbox di Roadmap dan tabel status di PRD §8.

## Lisensi

Belum ditentukan.
