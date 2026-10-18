# AGENTS.md — Konteks AI untuk proyek Today

File ini adalah **pintu masuk konteks**. Baca file ini + dokumen yang dirujuk sebelum mengubah kode. Tujuannya: mencegah AI berasumsi keliru tentang isi proyek.

## Ringkasan produk
**Today** (nama final; nama pengembangan lama "DramaK") — aplikasi Android sosial untuk penonton Indonesia: **diary nonton film + serial + anime** dengan konteks personal (mood, nonton bareng, rewatch dengan penilaian berubah), lalu opini/komunitas (Fase 1, butuh backend). UX inti: **log-first** — "hari ini nonton apa?" mendahului feed sosial.

> **Penting**: visi di atas masih **belum lengkap di kode**. P0–P3 selesai (katalog film+serial + diary MVP, seluruh UI Compose) dan **P4.1–P4.5.2 ada** (auth gate, profil, bagikan opini, feed & follow, like & komentar spoiler-tag, profil publik pengguna lain) + **P4.6.1 (rekap tahunan "Today Wrapped")** + **P4.6.2 (impor/ekspor CSV)** + **P4.6.3 (jadwal rilis)** + **P4.6.4 (notifikasi rilis lokal)** + **P4.6.5 (circle)**; fitur Fase 2 lainnya (chat/watch party) belum. Selalu cek tabel status: [docs/PRD.md §8](docs/PRD.md#8-status-implementasi-vs-rencana-per-15-oke-2026).

## Dokumen sumber (baca sebelum bertindak besar)
| File | Isi |
|---|---|
| [docs/PRD.md](docs/PRD.md) | Visi, fitur per fase, user story + acceptance criteria, keputusan terbuka |
| [docs/ARSITEKTUR.md](docs/ARSITEKTUR.md) | Stack, module graph, alur data, **daftar jebakan kode (J1–J22)**, strategi refactor |
| [docs/ROADMAP.md](docs/ROADMAP.md) | Urutan kerja P0–P4 dengan rujukan file |
| [docs/DESAIN_APLIKASI.md](docs/DESAIN_APLIKASI.md) | Rancangan UX/IA: sitemap, rincian per halaman, state, prioritas fitur |
| [docs/CATATAN_KEPUTUSAN.md](docs/CATATAN_KEPUTUSAN.md) | Keputusan desain terbaru (K1–K8) + pertanyaan terbuka (Q1–Q4) |
| [README.md](README.md) | Pengenalan aplikasi, fitur, setup, build, tech stack |
| [docs/PROGRES.md](docs/PROGRES.md) | Catatan kemajuan implementasi per fase (status terbaru) |

> **Presedensi dokumen**: bila `CATATAN_KEPUTUSAN.md` bertentangan dengan `DESAIN_APLIKASI.md`, **catatan keputusan yang menang** (lebih baru). Keputusan yang sudah final lalu diturunkan ke PRD/Roadmap.

> **Catatan repo**: seluruh `docs/` bersifat lokal (**gitignored**) — tetap ada di mesin, tapi tidak di-commit. Yang publik di repo hanya `README.md` (pengantar produk) dan `AGENTS.md` ini.

## Fakta cepat
- **Module**: `:app` (shell + seluruh UI Compose), `:core` (domain+data: TMDB+Room+Paging+diary/watchlist/daftar/stats, Koin), `:social` (**baru di P4.1** — domain+data sosial Firebase: auth/profil/opini/like/komentar/follow; **tanpa UI**). Modul `:favorite` **sudah dihapus** (dulu dynamic feature).
- **Stack nyata**: Kotlin 2.0, AGP 8.6, Koin, Retrofit+Gson, Room+Paging, Coil, **Jetpack Compose** (Material3, navigation-compose, koin-compose).
- **Stack target**: UI **Jetpack Compose** (Material3, navigation-compose, Coil, koin-compose); LiveData → StateFlow; domain/data tetap di `:core`.
- **Backend**: **Firebase** (PRD §9 K3; project `today-1666e`). Fondasi **P4.1–P4.5.2 ada**: modul `:social` (Firebase Auth + Firestore, data layer, **tanpa UI**) ter-wire di Koin; **halaman auth = gate di depan app** (`ui/AppRoot.kt` → `TodayApp`: belum masuk = `AuthScreen`, sudah masuk = `AppNavHost`); tab **Profil** (ke-5, menggantikan Stats) kini **profil-only** = lihat/edit profil + keluar + baris **"Opini saya"** + **"Feed Komunitas"**. `app/google-services.json` **sudah ada** (gitignored) → plugin google-services **aktif**, Firebase init sukses. `oauth_client` **sudah terisi** → tombol Google aktif; **Email/Password sudah aktif** (sign-up nyata sukses di device). **Sign-in Google nyata terverifikasi device** (account picker → Home, Profil tampil nama/email Google). **Cloud Firestore API sudah aktif + database dibuat** (`firestore.rules`/`firebase.json`) → **profil tersinkron server** (terverifikasi: simpan → restart → muat) + **opini publik tersinkron** (terverifikasi: detail → Bagikan → Opini saya → hapus). **Feed aktivitas & follow (P4.4)**: layar `FeedScreen` (`ROUTE_FEED`) dengan tab "Untuk Anda" & "Mengikuti" + aksi follow/unfollow responsif. **Like & komentar spoiler-tag (P4.5)**: interaksi Like/Unlike dengan optimistic update di Feed & detail; stream komentar real-time di `CommentsBottomSheet`, spoiler tap-to-reveal mask, input komentar dengan spoiler toggle, hapus komentar sendiri dengan decrement atomic `commentCount`. **Profil publik pengguna lain (P4.5.1)**: layar `PublicProfileScreen` (`ROUTE_PUBLIC_PROFILE`) saat nama author diketuk di kartu opini atau komentar. **Uji Komprehensif (P4.5.2)**: seluruh layar (Home, Jelajah, Watchlist, Daftar, Statistik, Detail, Feed, Komentar, Profil) terverifikasi fungsional dan responsif di emulator device Android 15; **seeder data dummy (`DummyDataSeeder`) sudah DIHAPUS dari app** (fitur "Isi Data Dummy (Demo)" di Profil tak ada lagi) — data demo cukup di-inject manual saat pengujian. **Rekap Tahunan (P4.6.1)**: `RecapCalculator` (`:core`) + `RecapScreen` (`ROUTE_RECAP`, `HorizontalPager` story-style, pemilih tahun, berbagi `ACTION_SEND`), dibuka dari baris "Rekap Tahunan" di `ProfileScreen`. **Impor/Ekspor CSV (P4.6.2)**: `Csv` + `DiaryCsvCodec` (`:core`; format Today & diary Letterboxd) + `ImportExportScreen` (`ROUTE_IMPORT_EXPORT`, Storage Access Framework `CreateDocument`/`OpenDocument`), dibuka dari baris "Impor / Ekspor" di `ProfileScreen`; entri impor `titleId=0` tetap bisa diedit/disimpan. **Jadwal Rilis (P4.6.3)**: `ReleaseScheduleCalculator` + `ReleaseScheduleUseCase` (`:core`) + `ReleaseScheduleScreen` (`ROUTE_RELEASE_SCHEDULE`, ikon `CalendarMonth` di TopAppBar `HomeScreen`), menggabungkan watchlist × detail TMDB (`next_episode_to_air` TV + `release_date` film) → episode/film mendatang per hari; **notifikasi push belum** (K8 terbuka). **Notifikasi Rilis (P4.6.4)**: WorkManager periodik harian (±08:00) + izin `POST_NOTIFICATIONS` + channel `release_schedule` (toggle di `ReleaseScheduleScreen`; keputusan **K8: lokal WorkManager**). **Circle (P4.6.5)**: `FirestoreCircleDataSource` (koleksi `circles` + subkoleksi `members`; kode undangan 6 karakter) + `CirclesScreen`/`CircleDetailScreen` (`ROUTE_CIRCLES`/`ROUTE_CIRCLE_DETAIL`) dari baris **"Circle"** di Profil (keputusan **K9: klub dasar via kode undangan**). MVP Fase 0 tetap **offline-first, Room-based, tanpa akun** (diary tak butuh login).
- Package: **`com.today.app`** (namespace + `applicationId`; core `com.today.app.core`). Rename K1/K2 sudah dijalankan (P3.5); jangan ganti lagi sembarangan.
- Secrets (API key TMDB) dibaca dari `local.properties` (gitignored) di `core/build.gradle.kts`; **jangan commit nilai**. Tanpa file → key kosong, build tetap jalan.

## Aturan main
1. **Jangan menganggap fitur PRD sudah ada.** Diary, watchlist, daftar, stats, sosial, Compose — semua statusnya di PRD §8. Kalau PRD bilang ❌, itu belum ada.
2. **Cek [Arsitektur §5](docs/ARSITEKTUR.md#5-jebakan-kode-saat-ini) sebelum refactor `:core`** — ada inkonsistensi kontrak (J1–J6) yang akan merusak compile kalau disentuh asal-asalan.
3. **Ikuti urutan Roadmap.** P0 (blocker compile) → P1 (pivot katalog + Compose) → P2 (diary) → P3 (kualitas) → P4 (sosial). Jangan melompat.
4. **UI baru = Compose only.** Dilarang menambah XML layout baru, Fragment baru, atau ViewBinding untuk layar baru.
5. **`:core` tanpa UI framework** — jangan taruh Compose/Android view di domain/data layer.
6. Setelah menyelesaikan fitur: centang item [Roadmap](docs/ROADMAP.md) + perbarui tabel [PRD §8](docs/PRD.md#8-status-implementasi-vs-rencana-per-15-oke-2026).
7. Keputusan produk yang masih terbuka (nama, backend, dll): jangan putuskan diam-diam — daftarkan ke user (PRD §9).

## Saat memulai sesi
- Perubahan kecil/bugfix: cukup baca file yang disentuh + bagian terkait di dokumen.
- Fitur baru / refactor besar: baca PRD (fitur + AC) → Arsitektur (pola & jebakan) → Roadmap (item & urutan) → baru tulis kode.
