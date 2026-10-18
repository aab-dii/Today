# Roadmap — Today

> **Status dokumen**: Draft v0.1 (15 Okt 2026)
> Terkait: [PRD](PRD.md) · [Arsitektur](ARSITEKTUR.md)
> Urutan kerja: **P0 → P1 → P2 → P3 → P4**. P0 adalah blocker build; jangan mulai P1 sebelum `:core` & `:app` compile bersih. Penomoran `[Jn]` merujuk ke [Arsitektur §5 — Jebakan kode](ARSITEKTUR.md#5-jebakan-kode-saat-ini).

---

## P0 — Perbaikan blocker (kode harus compile dulu)

> **Status (15 Okt 2026): SELESAI kecuali P0.10 (commit menunggu konfirmasi).** `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL.

- [x] **P0.1 Sinkronkan kontrak domain–data** `[J1]`
  - Samakan `IDramaRepository`, `DramaUseCase`, `DramaInteractor`, `DramaRepository` pada satu kontrak (usulan: `Flow<PagingData<Title>>` untuk katalog + `Flow<List<...>>` untuk favorit).
  - File: `core/.../domain/repository/IDramaRepository.kt`, `core/.../domain/usecase/DramaUseCase.kt`, `core/.../domain/usecase/DramaInteractor.kt`, `core/.../data/DramaRepository.kt`
- [x] **P0.2 Perbaiki LocalDataSource ↔ DAO** `[J2][J3]`
  - Tambahkan `getPagedDramas()` (atau sesuaikan pemanggilan) dan kembalikan tipe yang benar dari `getAllDrama()`.
  - File: `core/.../data/source/local/LocalDataSource.kt`, `core/.../room/DramaDao.kt`
- [x] **P0.3 Perbaiki DataMapper** `[J4][J6]`
  - Selaraskan nama/arity method (`mapEntitiesToDomain`, `mapResponsesToEntities` tanpa argumen page).
  - File: `core/.../utils/DataMapper.kt` + pemanggil di `DramaRepository.kt`, `DramaRemoteMediator.kt`
- [x] **P0.4 Perbaiki RemoteDataSource ↔ RemoteMediator** `[J5]`
  - Samakan signature `getAllDramas` (page param, tipe response) antara keduanya.
  - File: `core/.../remote/RemoteDataSource.kt`, `core/.../data/source/DramaRemoteMediator.kt`, `core/.../network/ApiService.kt`
- [x] **P0.5 Terapkan plugin KSP** `[J10]`
  - Apply `com.google.devtools.ksp` di root (`apply false`) + di module yang memakai Room (`:core`); pastikan `ksp(...)` Room compiler jalan.
  - File: `build.gradle.kts` (root), `core/build.gradle.kts`, `gradle/libs.versions.toml`
- [x] **P0.6 Rapikan Room & paging version** `[J11][J13]`
  - Seimbangkan `room-paging` dengan Room 2.6.1; hapus duplikasi pembuatan DB (pilih satu jalur via Koin; nama db konsisten).
  - File: `gradle/libs.versions.toml`, `core/.../room/DramaDatabase.kt`, `core/di/CoreModule.kt`
- [x] **P0.7 Bersihkan resource & manifest rusak** `[J7][J9][J12]`
  - Hapus/replace referensi `DashboardFragment` & `fragment_detail` di nav graph (dan bottom nav / `AppBarConfiguration` yang menunjuk `navigation_dashboard`); tambah `title_favorite` atau hapus dist manifest; samakan parent tema day/night.
  - File: `app/src/main/res/navigation/mobile_navigation.xml`, `app/.../MainActivity.kt`, `favorite/src/main/res/values/strings.xml`, `app/src/main/res/values-night/themes.xml`
- [x] **P0.8 Rapikan build file favorite** `[J8]`
  - Buat `favorite/proguard-rules.pro` (atau hapus referensinya).
  - File: `favorite/build.gradle.kts`
- [x] **P0.9 Pindahkan secrets ke `local.properties`** `[J14]`
  - `API_KEY`/`BEARER_TOKEN` dibaca dari `local.properties` → `buildConfigField`; jangan commit nilai secret.
  - File: `app/build.gradle.kts`, `core/build.gradle.kts`, `.gitignore`
- [ ] **P0.10 Commit state kerjaan yang tertunda** `[J21]`
  - Seluruh isi kerjaan pivot masih uncommitted (staged+unstaged campur). Buat commit perbaikan P0 secara terpisah dan rapi. **Menunggu konfirmasi user sebelum commit.**
- [x] **P0.11 Verifikasi build** — `./gradlew :app:assembleDebug` hijau (minimal compile; bug perilaku kecil boleh menyusul).

## P1 — Pivot katalog & migrasi Compose (PRD F0.1, F0.8)

> **Status (16 Okt 2026): SELESAI (kecuali catatan P1.5).** `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL. `:core` kini memakai model `Title` + `MediaType` (movie/tv); `:app` seluruhnya Compose; `:favorite` modul biasa.

- [x] **P1.1 Lepas dynamic feature** `[Arsitektur §4]`
  - `:favorite` jadi modul biasa (`com.android.library`); `dynamicFeatures`/`SplitInstallManager`/`Class.forName` dihapus; `favoriteModule` di-load di `MyApplication`.
  - File: `app/build.gradle.kts`, `favorite/build.gradle.kts`, `app/.../MyApplication.kt`
- [x] **P1.2 Susun fondasi Compose** — Kotlin compose plugin, BOM 2024.09.00, material3, `navigation-compose`, `lifecycle-viewmodel-compose`, Coil, `koin-androidx-compose`; ViewBinding dimatikan di `:app`.
  - File: `gradle/libs.versions.toml`, `app/build.gradle.kts`
- [x] **P1.3 Ganti model katalog** — `Drama`/`DramaEntity` → `Title` + `MediaType { MOVIE, TV }`; `TitleEntity` (PK komposit `id`+`media_type`, kolom `is_watching`); DB baru `TitleDatabase` (nama file saat itu `DramaK.db`, kini `Today.db` setelah P3.5).
  - File: `core/.../domain/model/Title.kt`, `core/.../local/entity/TitleEntity.kt`, `core/.../room/TitleDatabase.kt`
- [x] **P1.4 Perluas RemoteDataSource & ApiService** — `discover/movie` + `discover/tv` + `search/multi` + detail movie/tv; filter `originCountry == "KR"` & genre 18 hardcode dihapus; default `page=1`.
  - File: `core/.../network/ApiService.kt`, `core/.../remote/RemoteDataSource.kt`, `core/.../remote/response/TitleResponse.kt`
- [x] **P1.5 Anime** — **sebagian**: anime (genre Animation) muncul alami di hasil discover/search lintas media; filter/label khusus (chip "Anime", origin JP) **ditunda ke P2**.
- [x] **P1.6 Layar Compose di `:app`** — host `MainActivity` (NavHost), layar katalog (paging + retry), search (debounce), detail; `HomeFragment`, `DetailActivity` XML, `DramaAdapter` + `item_list_drama.xml` dihapus.
  - File: `app/.../ui/AppNavHost.kt`, `ui/catalog/`, `ui/search/`, `ui/detail/`, `ui/components/`
- [x] **P1.7 Kategori "sedang ditonton"** — `is_watching` pada `TitleEntity` + toggle di layar detail + badge di kartu. (Diary penuh menyusul di P2.) **→ Digantikan P2.6**: flag dihapus, "sedang ditonton" kini = entri diary berstatus `WATCHING`.
- [x] **P1.8 Buang komponen mati** `[J17][J18][J19]` — `NetworkBoundResource`, `AppExecutors`, `MovieEntity`, `Resource`, `ApiResponse`, `NotificationsFragment`/ViewModel dihapus.
- [x] **P1.9 Verifikasi** — build `:app:assembleDebug` hijau; katalog film+serial+anime ada di satu daftar; search lintas media; detail (genre/runtime/sinopsis); favorit & "sedang ditonton" tersimpan Room; cache offline (Room + RemoteMediator). **Belum diuji di device/emulator.**

> **Catatan jujur P1**: detail belum menampilkan daftar pemeran (cast) — endpoint credits belum ditambah. Anotasi `[Pn]`/pembersihan `libs.versions.toml` sisa (P3.1) menyusul.


## P2 — Diary MVP (PRD F0.2–F0.7)

> **Status (16 Okt 2026): SELESAI.** `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL. Diary, watchlist, daftar kustom, dan stats dasar berjalan **offline-first via Room**. Modul `:favorite` dihapus; modulnya digantikan tab Watchlist Compose. **Smoke test device OK** (terpasang & launch tanpa crash di emulator `Pixel_4`/API 35); penelusuran manual US-01…US-07 + K2–K8 selesai di P2.10.7 (US-08 head-to-head belum — desain belum final).

- [x] **P2.1 Skema Room diary** — entity `DiaryEntryEntity` (snapshot judul + tanggal, rating 0.5–5, mood, catatan, bareng-siapa, status, musim/episode, `rewatch_of`), `WatchlistItemEntity`, `CustomListEntity`/`CustomListEntryEntity`; `DiaryDao`/`WatchlistDao`/`CustomListDao`, `DiaryLocalDataSource`, `IDiaryRepository`/`DiaryRepository`, `DiaryUseCase`/`DiaryInteractor`, `DiaryMapper`, `StatsCalculator`, `GenreCatalog`; `TitleDatabase` v2 + Koin modules diperluas.
  - File: `core/.../domain/model/{DiaryEntry,WatchlistItem,CustomList,Stats}.kt`, `core/.../local/entity/`, `core/.../local/room/{DiaryDao,WatchlistDao,CustomListDao}.kt`, `core/.../local/DiaryLocalDataSource.kt`, `core/.../data/DiaryRepository.kt`, `core/.../domain/{repository,usecase}/Diary*.kt`, `core/.../utils/{DiaryMapper,StatsCalculator,GenreCatalog}.kt`, `core/di/CoreModule.kt`, `app/di/AppModule.kt`
- [x] **P2.2 Layar log-first** — `HomeScreen` menjawab "hari ini nonton apa?": carousel "Sedang ditonton" + FAB "Catat" (cari judul) + linimasa catatan terbaru (US-01, AC1–AC3).
  - File: `app/.../ui/home/HomeScreen.kt`, `ui/home/HomeViewModel.kt`
- [x] **P2.3 Form diary** — `DiaryFormScreen`: rating setengah bintang (tap kiri/kanan), 7 mood preset (keputusan K4), status (sedang/selesai/drop), catatan, label bareng, pemilih tanggal (US-02).
  - File: `app/.../ui/diary/DiaryFormScreen.kt`, `ui/diary/DiaryFormViewModel.kt`, `ui/components/DiaryComponents.kt`
- [x] **P2.4 Rewatch** — aksi "Tonton ulang" → entri baru (`rewatch_of`); linimasa penilaian di layar detail (US-03).
  - File: `app/.../ui/detail/DetailScreen.kt`, `ui/detail/DetailViewModel.kt`
- [x] **P2.5 Tracking episode** — `DiaryForm` menyimpan musim/episode untuk serial + dialog "Progres" di detail (US-04). ~~**Sebagian**: total episode/season belum diambil dari TMDB~~ → **dibereskan P3.7** (total musim/episode TMDB kini diambil & ditampilkan); progres tetap disimpan pada entri berstatus "sedang ditonton".
- [x] **P2.6 Watchlist** — tab Watchlist (grid + buka/`Catat`, hapus, tambah ke daftar) (US-05); **modul `:favorite` dihapus** dan flag `is_favorite`/`is_watching` di `TitleEntity` dibuang (watchlist + diary menggantikannya).
  - File: `app/.../ui/watchlist/WatchlistScreen.kt`, `ui/watchlist/WatchlistViewModel.kt`; dihapus: `favorite/`
- [x] **P2.7 Daftar kustom** — buat/isi/hapus daftar privat; dialog "Tambah ke daftar" reusable dari detail & watchlist (US-06).
  - File: `app/.../ui/lists/CustomListsScreen.kt`, `CustomListDetailScreen.kt`, `AddToListDialog.kt`, `+ViewModel`
- [x] **P2.8 Stats dasar** — total judul/catatan/jam, heatmap 13 minggu, top genre/negara (US-07).
  - File: `app/.../ui/stats/StatsScreen.kt`, `ui/stats/StatsViewModel.kt`
- [x] **P2.9 Verifikasi** — `:app:assembleDebug` hijau; seluruh fitur di atas bekerja offline (Room). **AC US-01…US-08 belum diuji manual di device.**

> **Catatan jujur P2**: ~~(1) `runtimeMinutes` & genre snapshot kosong saat mencatat dari hasil katalog~~ → **dibereskan P3.7**. ~~(2) Negara disimpan sebagai kode ISO (mis. "KR"), belum dipetakan ke nama~~ → **dibereskan P3.7** (`CountryCatalog`). (3) Backend/sosial (US-08) belum ada — sesuai keputusan K3 Fase 0.


## P2.10 — Iterasi sesuai Catatan Keputusan (K1–K8)

> **Sumber**: [Catatan Keputusan](CATATAN_KEPUTUSAN.md) (menang atas `DESAIN_APLIKASI.md` bila bentrok). Urutan mengikuti §4 dokumen itu. Kerjakan **sebelum** uji device.
>
> **Status (16 Okt 2026): K2–K8 SELESAI + uji device selesai.** `:app:assembleDebug` + `:app:installDebug` → BUILD SUCCESSFUL, terpasang & jalan di emulator `Pixel_4` (Android 15/API 35). Penelusuran manual **US-01…US-07 + K2–K8 terverifikasi** di emulator (dump hierarki UI). Ditemukan & **diperbaiki 1 bug kritis** (J22): argumen navigasi ke Detail/DiaryForm/ListDetail dibaca dari `savedStateHandle` yang salah → selalu `null`. **Catatan**: nomor lama "P2.5" (item P2.5 Tracking episode) bentrok — section ini dinomori **P2.10** agar tidak bertabrakan.

- [x] **P2.10.1 (K3) Kolom cari menonjol di Jelajah** — field cari di atas katalog, placeholder *"Apa yang terakhir kamu tonton?"*; tap → layar Cari.
- [x] **P2.10.2 (K2) Baris katalog kurasi** — baris horizontal: Sedang tren (`trending/all/week`), Drama Korea populer (`discover/tv` KR), Anime musim ini (`discover/tv` genre 16 + JP), Film Indonesia (`discover/movie` ID), Tayang sekarang (`movie/now_playing` region ID), Rating tertinggi (`top_rated`). Daftar "Semua" (cache Room) tetap di bawah.
- [x] **P2.10.3 (K4/K5) Susun ulang Detail Judul + tombol sadar-status** — primary selalu **Catat** (**Catat lagi** bila sudah pernah dicatat) → rewatch; watchlist jadi aksi sekunder; simpan catatan otomatis mengeluarkan judul dari watchlist (US-05 AC3). ~~Bagian cast/episodes/similar bertahap~~ → **selesai di P3.7** (cast, total musim/episode, "Mirip dengan ini").
- [x] **P2.10.4 (K6) Tombol trailer** — endpoint `videos` TMDB; buka YouTube via intent (tanpa pemutar tertanam).
- [x] **P2.10.5 (K7) Ikon catat-cepat di kartu judul** — jalur cepat dari katalog & hasil pencarian.
- [x] **P2.10.6 (K8) Catat kilat (bottom sheet)** — judul → bintang → Simpan (≤3 ketukan); form lengkap/edit tetap tersedia.
- [x] **P2.10.7 Uji di device (menyeluruh)** — `:app:installDebug` lolos + smoke test OK. **Terverified di emulator**: US-01 (Home log-first + carousel "Sedang ditonton"), US-02 (DiaryForm: tanggal/nilai/status/7 mood/catatan), US-03 (rewatch via "Catat lagi"/"Tonton ulang" → form terbuka), US-04 (progres episode: dialog Musim/Episode → tersimpan "Sedang ditonton · Musim 1 · Episode 3", prefilled saat dibuka ulang), US-05 (toggle watchlist + auto-keluar saat catat), US-06 (daftar kustom: dialog "Daftar baru" → "Favorit/0 judul"), US-07 (Stats: 2 judul/3 catatan/genre/asal negara). K5 auto-remove watchlist terkonfirmasi. **Bug ditemukan & diperbaiki**: J22 (argumen navigasi `savedStateHandle` di `AppNavHost.kt`). Screen: US-08 head-to-head tetap **belum dicakup** (desain belum final, lihat PRD §9).

> **Q terbuka (dari catatan)**: Q1 layar awal cari vs katalog · Q2 Beranda vs Diary sebagai start · Q3 anime tab vs baris · Q4 nama aplikasi. Belum diputuskan.


## P3 — Perapihan kualitas

> **Status (16 Okt 2026): P3 SELESAI (P3.1–P3.7).** Produk kini bernama **Today**, package **`com.today.app`**. `:app:assembleDebug` → BUILD SUCCESSFUL; `:core:testDebugUnitTest` → **47 test hijau**; `:app:lintDebug` → **0 errors, 72 warnings** (sisa seluruhnya advisory versi dependensi); CI GitHub Actions ditambahkan; audit aksesibilitas (dark mode, content description, touch target) selesai. **Limitasi P2 dibereskan (P3.7)** dan terverifikasi di device.

- [x] **P3.1** Rapikan `gradle/libs.versions.toml` dari versi tak terpakai; sinkronkan tema/resources sisa XML.
  - Hapus **8 entry library tak terpakai** (`lifecycle-runtime-ktx`, `constraintlayout`, `lifecycle-viewmodel-ktx`, `navigation-fragment-ktx`, `navigation-ui-ktx`, `glide`, `recyclerview`, `lifecycle-livedata-ktx`) + **8 `[versions]` yatim** (`lifecycleRuntimeKtx`, `constraintlayout`, `lifecycleViewmodelKtx`, `navigationFragmentKtx`, `navigationUiKtx`, `glide`, `recyclerview`, `lifecycle`). Normalisasi `version.ref`.
  - Hapus `xmlns:tools` tak terpakai di `values/themes.xml` & `values-night/themes.xml`. Resource XML sisa lain sudah bersih (tak ada layout/menu/navigation/dimens).
- [x] **P3.2** Ganti test template dengan unit test nyata: `DataMapper`, use case diary, repository (fake DAO/Retrofit).
  - Hapus **4 test template** (`ExampleUnitTest`/`ExampleInstrumentedTest` di `:app` & `:core`). Tambah `kotlinx-coroutines-test`.
  - **47 unit test** di `:core` (`:core:testDebugUnitTest` → BUILD SUCCESSFUL, 0 gagal): `GenreCatalogTest` (8), `StatsCalculatorTest` (7), `DiaryMapperTest` (8), `DataMapperTest` (10), `CountryCatalogTest` (5), `DiaryRepositoryTest` (9, dengan **fake DAO** in-memory via `FakeDaos.kt`). `DiaryInteractor` (murni delegasi) sengaja tidak diuji.
- [x] **P3.3** CI minimal (GitHub Actions): `assembleDebug` + test.
  - `.github/workflows/ci.yml`: JDK 17 (temurin) + `gradle/actions/setup-gradle`, jalankan `:core:testDebugUnitTest` lalu `:app:assembleDebug` pada push ke `main` & semua PR. Rahasia TMDB opsional (tanpa `local.properties`, key kosong → tetap build).
- [x] **P3.4** Android lint/`ktlint` atau ktor-format; bersihkan warning.
  - `:app:lintDebug`: `AutoboxingStateCreation` (`QuickLogSheet.kt` → `mutableDoubleStateOf`), `RedundantLabel` (hapus `android:label` di `<activity>`), `OldTargetApi` (`targetSdk` 34 → 35) **diperbaiki**.
  - Sisa **72 warning** = `GradleDependency` (66) + `AndroidGradlePluginVersion` (6): murni "versi lebih baru tersedia"; **upgrade dependensi = tugas terpisah** (belum dilakukan, risiko build).
- [x] **P3.5** Putuskan & jalankan rename nama produk + package (keputusan K1/K2 PRD) sebelum distribusi publik.
  - **K1 = Today**, **K2 = `com.today.app`** (core `com.today.app.core`). Rename menyeluruh: direktori package Kotlin (`com/android/dramak` → `com/today/app`), 75 file sumber, `namespace`/`applicationId`, `rootProject.name = "Today"`, label `@string/app_name` = "Today", tema `Theme.Today` + komponen `TodayTheme`, DB `Today.db` (`fallbackToDestructiveMigration` → aman). Hapus dir sisa `com/android`. Build `:app:assembleDebug` hijau + **37 test** `:core` hijau.
- [x] **P3.6** Desain & aksesibilitas: dark mode konsisten, content description gambar, touch target.
  - **Dark mode**: `TodayTheme` kini membedakan skema gelap (primary `Purple200`/onPrimary hitam) dari terang — sebelumnya identik. **Content description**: diaudit — gambar bermakna pakai `titleName`, dekoratif `null`, ikon aksi berlabel (sudah baik). **Touch target**: bintang rating `RatingStar` (40dp) diberi area sentuh ≥ **48dp** via `Box` + `coerceAtLeast(48.dp)` tanpa mengubah ukuran visual.
- [x] **P3.7** Bereskan limitasi P2 (runtime/genre, nama negara, total episode, cast/similar).
  - **(1) Genre + runtime** diisi otomatis saat mencatat dari katalog: `CatalogUseCase` di-inject ke `DiaryFormViewModel`/`QuickLogViewModel` → `enrich()` best-effort dari detail TMDB (form: sebelum simpan; quick-log: simpan dulu offline lalu enrich di latar).
  - **(2) Kode negara ISO → nama** via `CountryCatalog` (baru; mis. "KR" → "Korea Selatan"), dipakai `StatsCalculator`.
  - **(3) Total musim/episode TMDB** (`TitleDetailResponse.number_of_seasons/number_of_episodes`) ditampilkan di detail + petunjuk di dialog "Progres"; runtime serial jadi "N menit/episode".
  - **(4) Cast & rekomendasi** di detail via `append_to_response=credits,recommendations`; seksi "Pemain" (≤10, urut `order`) & "Mirip dengan ini" (LazyRow, dapat diklik → detail lain).
  - **File**: `core/.../utils/CountryCatalog.kt` (baru), `core/.../utils/{GenreCatalog,StatsCalculator,DataMapper}.kt`, `core/.../remote/response/TitleResponse.kt`, `core/.../network/ApiService.kt`, `core/.../domain/model/TitleDetail.kt`, `app/.../ui/detail/{DetailScreen,DetailViewModel}.kt`, `app/.../ui/diary/{DiaryFormViewModel,QuickLogViewModel}.kt`, `app/di/AppModule.kt`. **Terverifikasi device**: detail menampilkan "5 musim · 41 episode", "23 menit/episode", "Pemain", "Mirip dengan ini" (navigasi antar-judul OK); Stats menampilkan "Tiongkok".

## P4 — Sosial & backend (Fase 1 PRD — high level, detail saat mulai)

> **Status (17 Okt 2026): P4.1 (fondasi) + P4.2 (auth & profil UI) + P4.3 (bagikan opini) + P4.4 (feed aktivitas & follow) + P4.5 (like & komentar) + P4.5.1 (profil publik pengguna lain) + P4.6.1 (rekap tahunan) + P4.6.2 (impor/ekspor CSV) + P4.6.3 (jadwal rilis) + P4.6.4 (notifikasi rilis lokal) + P4.6.5 (circle) SELESAI; Firebase AKTIF + Firestore sinkron profil, opini, feed, like, komentar, & circle.** Provider **Firebase** (K3; project `today-1666e`). Modul **`:social`** (data layer, tanpa UI). Test unit: **142 hijau** (`:core` 85 + `:social` 22 + `:app` 35); `:app:assembleDebug` → BUILD SUCCESSFUL; lint `0 errors`. **`app/google-services.json` sudah ada** → plugin google-services **aktif** (gitignored), **Firebase init sukses di device**. **Keputusan auth**: Email & password **+** Google Sign-In. **P4.2**: **halaman auth jadi gate di depan app** (`TodayApp`) — belum masuk → `AuthScreen`, sudah masuk → `AppNavHost`; tab **Profil** (menggantikan Stats) kini profil-only (lihat/edit + keluar) + pemetaan error auth ke Bahasa Indonesia. **P4.3**: tombol **"Bagikan"** per-entri diary di `DetailScreen` → `ShareOpinionDialog` (catatan + toggle spoiler) → `publishOpinion`; layar **"Opini saya"** (`ROUTE_MY_OPINIONS`) dari Profil (lihat + hapus). **P4.4**: layar **Feed Komunitas** (`ROUTE_FEED`) dengan tab "Untuk Anda" (semua opini publik terbaru) & "Mengikuti" (feed opini akun yang di-follow) + aksi follow/unfollow langsung di kartu opini. Entry point via TopAppBar `HomeScreen` & menu `ProfileScreen`. **P4.5**: Like/unlike reaktif dengan optimistic update + rollback error; sistem komentar real-time dengan filter spoiler (tap-to-reveal mask), penghapusan komentar sendiri dengan transaksi atomic `commentCount` decrement, dan bottom sheet Compose interaktif di `FeedScreen` dan `MyOpinionsScreen`. **P4.5.1**: Layar profil publik akun lain (`PublicProfileScreen` / `ROUTE_PUBLIC_PROFILE`) saat nama author diketuk di Feed atau komentar.

- [x] **P4.1** Pilih provider backend (keputusan K3) & buat modul `:social` (data layer remote saja dulu).
  - Modul **`:social`** (`com.today.app.social`): `api(project(":core"))`; Firebase BOM 33.7.0 (`firebase-auth`, `firebase-firestore`) + `kotlinx-coroutines-play-services`; Koin `socialModule`.
  - **Domain**: `UserSession`/`AuthProvider`, `UserProfile`, `Opinion`, `Comment`; repo `AuthRepository` (observeSession, signUp/signIn email, signIn Google via idToken, reset, signOut, delete) & `SocialRepository` (profil, publish/delete opini, feed by authors, like, komentar, follow).
  - **Data**: `AuthDataSource`→`FirebaseAuthDataSource`; `SocialDataSource`→`FirestoreSocialDataSource` (koleksi `users`/`opinions`/`comments`/`likes`/`following`/`followers`; transaksi increment like/comment). DTO `UserDoc`/`OpinionDoc`/`CommentDoc`/`FollowDoc` + `SocialMapper`.
  - **Verifikasi**: build hijau; plugin google-services aktif + **Firebase init sukses** di emulator (logcat). `google-services.json` **gitignored, jangan di-commit**. UI/logika sosial belum diuji end-to-end.
- [x] **P4.2** Auth + profil publik (F1.1) — **UI Compose** sebagai **gate di depan app** + tab **Profil** profil-only.
  - `app/.../ui/AppRoot.kt` (`TodayApp`): observasi `AuthViewModel.state` → `!ready` = splash, `session == null` = `AuthScreen`, else `AppNavHost`. `MainActivity` memanggil `TodayApp()`.
  - `app/.../ui/auth/AuthScreen.kt` + `AuthViewModel.kt`: masuk/daftar email, reset sandi, tombol **Google** (aktif bila `oauth_client`/web client ID ada), pindah dari tab Profil ke gate.
  - `app/.../ui/profile/ProfileScreen.kt` + `ProfileViewModel.kt` (**profil-only**): lihat/edit profil (nama tampilan + bio) + keluar; `ProfileViewModel` di Koin (`AppModule`). Tab Profil di `AppNavHost`/`Routes` (`ROUTE_PROFILE`); `StatsScreen` diberi `onBack`. `ProfileViewModel` **tahan server-mati**: tampilkan profil dari data sesi lebih dulu + `withTimeoutOrNull(15 dtk)` untuk get/upsert; `getProfile` memakai `get(Source.SERVER)` (offline = gagal, **bukan** "dokumen kosong") sehingga fallback sesi tak menimpa dokumen server. Form edit **pre-fill dari profil server**.
  - **Pemetaan error**: `social/.../data/mapper/AuthErrorMapper.kt` (Throwable → pesan Indonesia) + `AuthRepositoryImpl.authCall` membungkus semua error → pesan ramah; ditampilkan inline di layar auth (bukan hanya snackbar).
  - **Verifikasi**: build + lint hijau; **device** end-to-end: gate muncul sebelum app → daftar email **nyata sukses** → Home; restart app langsung ke Home (sesi persisten); tab Profil menampilkan nama tampilan; **Keluar → kembali ke gate**; tombol Google aktif; **sinkron Firestore** terverifikasi (simpan nama unik → force-stop → restart → Profil memuat dari server).
  - ⚠️ Sisa: provider **Google** sudah aktif & sign-in Google **terverifikasi device**. `play-services-auth` ditambahkan di `:app` (GoogleSignIn klasik; migrasi Credential Manager = tugas lanjutan).
- [x] **P4.3** Mekanisme "bagikan opini" dari entri diary (F1.2) — **SELESAI & terverifikasi device**.
  - `social/.../domain/mapper/OpinionMapper.kt`: `fromDiaryEntry(entry, authorUid, authorName, note, spoiler, createdAt)` → `Opinion` (id="", like/comment=0, note di-trim) + `OpinionMapperTest`.
  - `app/.../ui/social/OpinionViewModel.kt`: `myOpinions` = `observeSession().map{uid}.distinctUntilChanged().flatMapLatest{ observeUserOpinions(uid) }`; `publish` (authorName = displayName ?: prefix email ?: "Pengguna"; `withTimeoutOrNull(15 dtk)`), `deleteOpinion`, snackbar message/error + pemetaan error ramah. Didaftarkan di `AppModule`.
  - `app/.../ui/social/ShareOpinionDialog.kt` (header poster+judul+rating/mood, catatan opsional, toggle spoiler), `OpinionComponents.kt` (`OpinionCard`), `MyOpinionsScreen.kt` (rute `ROUTE_MY_OPINIONS`, empty state, konfirmasi hapus).
  - Wiring: aksi **"Bagikan"** per-baris entri di **`DetailScreen`** ("Linimasa tontonan") → dialog; baris **"Opini saya"** di `ProfileScreen`.
  - **Verifikasi**: `:app:testDebugUnitTest` **3 test** baru (publish sukses, fallback nama dari email, tanpa sesi) — total 64 test hijau; `:app:assembleDebug` & `:app:lintDebug` (0 errors). **Device**: buka detail judul → catat entri → "Bagikan" → tulis opini → snackbar → Opini "X" muncul di **Opini saya** (judul + tipe + tanggal + catatan) → hapus → empty state.
- [x] **P4.4** Feed aktivitas + follow (F1.3) — **SELESAI**.
  - `social/.../source/firebase/FirestoreSocialDataSource.kt` + `SocialRepositoryImpl.kt`: query `observeOpinionsByAuthors` (real-time snapshots), `observeAllOpinions`, `observeFollowing`, `setFollowing` (batch write ke `following` & `followers`).
  - `app/.../ui/feed/FeedViewModel.kt`: mode `FOR_YOU` & `FOLLOWING`, `forYou` state, `followingFeed` (menggabungkan postingan user + akun yang diikuti), `followingUids`, fungsi `toggleFollow(authorUid)` dengan guard auth & guard diri sendiri; di-inject di `AppModule`.
  - `app/.../ui/feed/FeedScreen.kt` (`ROUTE_FEED`): TabRow "Untuk Anda" vs "Mengikuti", reactive list `OpinionCard`, tombol "Ikuti"/"Berhenti ikuti" (otomatis disembunyikan untuk postingan sendiri), snackbar error/message, empty state untuk tab kosong / belum mengikuti siapa pun, klik judul → `openDetail`.
  - Wiring navigasi: icon `DynamicFeed` di TopAppBar `HomeScreen` & baris "Feed Komunitas" di `ProfileScreen`.
  - `app/.../ui/social/OpinionComponents.kt`: fungsi `Opinion.toTitle()` dijadikan utilitas publik yang dapat dipakai bersama oleh `FeedScreen` dan `MyOpinionsScreen`.
  - **Verifikasi**: 5 unit test di `FeedViewModelTest` (toggle mode, follow, unfollow, self-follow guard, unauthenticated guard); total test suite **69 unit test hijau**; lint `0 errors`; `:app:assembleDebug` BUILD SUCCESSFUL.
- [x] **P4.5** Like + komentar spoiler-tag (F1.4) — **SELESAI**.
  - **Data layer (`:social`)**: `SocialRepository` & `SocialDataSource` ketambahan `observeIsLiked(opinionId, uid)`, `isLiked(opinionId, uid)`, `deleteComment(opinionId, commentId)`. Pada `FirestoreSocialDataSource`: `observeIsLiked` memantau snapshot subcollection `opinions/{opinionId}/likes/{uid}`; `deleteComment` menjalankan transaksi Firestore untuk menghapus dokumen komentar sekaligus mendestruksi/menurunkan `commentCount` secara atomic; `setLike` menaikkan/menurunkan `likeCount` dan mengelola subcollection `likes/{uid}`.
  - **ViewModel (`:app`)**:
    - `CommentsViewModel`: mengelola stream komentar real-time per opini yang dipilih (`observeComments`), memantau status like opini secara reaktif, mengelola set ID komentar ber-spoiler yang dibuka pengguna (`revealedCommentIds`), validasi auth, pengiriman komentar baru dengan flag spoiler, dan penghapusan komentar milik sendiri.
    - `FeedViewModel`: mendukung pelacakan set opini yang disukai (`likedOpinionIds`), batch check like status untuk opini yang dimuat, serta aksi `toggleLike(opinion)` dengan optimistic state update dan auto-rollback bila operasi jaringan gagal.
    - `AppModule`: registrasi `CommentsViewModel` ke dalam Koin DI.
  - **UI Compose (`:app`)**:
    - `OpinionComponents.kt` (`OpinionCard`): footer aksi sosial baru dengan tombol Like interaktif (ikon hati penuh berwarna merah bila sudah disukai, outline jika belum, beserta hitungan like) dan tombol Komentar (ikon bubble chat beserta hitungan komentar).
    - `CommentsBottomSheet.kt`: bottom sheet Material3 interaktif yang menampilkan header kartu opini konteks, daftar komentar scrollable dengan card penutup spoiler ("*Komentar mengandung spoiler. Ketuk untuk membuka*") yang dapat dibuka/tutup saat diketuk, tombol hapus untuk komentar penulis sendiri, serta input bar teks dengan chip toggle spoiler ("Spoiler") dan tombol kirim.
    - `FeedScreen.kt` & `MyOpinionsScreen.kt`: terintegrasi penuh dengan `CommentsBottomSheet` dan handler toggle like/komentar.
  - **Verifikasi**:
    - 9 unit test baru di `CommentsViewModelTest` + 4 unit test like di `FeedViewModelTest`.
    - Total test suite: **82 unit test hijau** (`:core` 47 + `:social` 14 + `:app` 21).
    - Android Lint: `0 errors`.
    - Build: `./gradlew :app:assembleDebug` BUILD SUCCESSFUL.
- [x] **P4.5.1** Profil publik pengguna lain (F1.1 lanjutan) — **SELESAI**.
  - **Rute & Navigasi**: rute `ROUTE_PUBLIC_PROFILE` ("public_profile") + parameter `KEY_USER_ID`, `KEY_USER_NAME` dan extension `NavHostController.openPublicProfile(userId, userName)`. Terdaftar di `AppNavHost`.
  - **ViewModel (`:app`)**: `PublicProfileViewModel` (`com.today.app.ui.profile`) mengamati stream `observeUserOpinions(targetUid)`, status following pengguna terhadap target via `observeFollowing(myUid)`, fetch profil remote via `getProfile(targetUid)` dengan fallback nama, aksi `toggleFollow()` dengan guard sesi & akun sendiri, serta aksi `toggleLike(opinion)` dengan optimistic update.
  - **UI Compose (`:app`)**:
    - `PublicProfileScreen.kt`: header kartu profil (foto avatar/initial, nama tampilan, bio, status chip "Ini profil Anda" / tombol "Ikuti" / "Mengikuti"), jumlah opini publik, lazy list `OpinionCard` dengan interaksi like dan `CommentsBottomSheet`.
    - `OpinionComponents.kt` & `CommentsBottomSheet.kt`: nama author dapat diklik (`onAuthorClick`), langsung membuka profil publik pengguna tersebut dari Feed Komunitas maupun bottom sheet komentar.
  - **Verifikasi**:
    - 10 unit test di `PublicProfileViewModelTest` (load profile, fallback name, isSelf detection, follow, unfollow, self guard, unauth guard, like, unlike rollback, opinions flow).
    - Total test suite: **121 unit test hijau** (`:core` 75 + `:social` 14 + `:app` 32).
    - Android Lint: `0 errors`.
    - Build: `./gradlew :app:assembleDebug` BUILD SUCCESSFUL.
- [x] **P4.5.2** Verifikasi fungsional menyeluruh di emulator — **SELESAI & terverifikasi device**.
  - ~~**Data Seeder (`DummyDataSeeder.kt`)**: utilitas pengisi data demo lokal (Room: 5 entri diary kaya rating/mood/catatan/progres, 3 watchlist, 2 custom list tematik) dan remote (Firestore: opini publik + komentar berspoiler).~~ **DIHAPUS** (keputusan produk: data dummy cukup di-inject manual saat pengujian, bukan fitur in-app). File `DummyDataSeeder.kt`, aksi menu "Isi Data Dummy (Demo)" di `ProfileScreen`, `ProfileViewModel.seedDummyData()`, dan factory Koin-nya sudah dihapus.
  - **Pengujian Fungsional Menyeluruh di Emulator `Pixel_4` (Android 15)**:
    1. **Home ("Hari ini")**: carousel "Sedang ditonton" (Reply 1988) + linimasa "Catatan terbaru" (Parasite, Spirited Away, Oppenheimer, Reply 1988, Attack on Titan) terisi rapi.
    2. **Watchlist**: grid poster (Demon Slayer, Severance, Interstellar) tampil responsif.
    3. **Daftar Kustom**: daftar "Masterpiece Favorit" & "Tontonan Santai Akhir Pekan" tampil beserta rincian judul di dalam halaman detail daftar.
    4. **Statistik**: kartu ringkasan (7 Judul, 12 Catatan, 18 Jam), heatmap aktivitas 13 minggu, bar progres genre terfavorit, dan persebaran negara asal terhitung akurat.
    5. **Detail Judul**: backdrop, metadata TMDB, overview, daftar pemain (cast), rekomendasi "Mirip dengan ini", dan riwayat catatan personal.
    6. **Feed Komunitas & Like**: kartu opini di tab "Untuk Anda", interaksi Like real-time (`❤️ 1`) ter-update langsung.
    7. **Komentar & Spoiler Filter**: `CommentsBottomSheet` menampilkan stream komentar, input komentar baru dengan spoiler tag, masking spoiler *"Komentar mengandung spoiler. Ketuk untuk membuka"*, tap-to-reveal membuka isi spoiler, dan tombol hapus komentar sendiri menurunkan `commentCount` secara atomic.
    8. **Profil Publik**: tap nama author di feed/komentar membuka `PublicProfileScreen` dengan avatar, bio, badge/tombol follow, dan daftar opini publik.
    9. **Jelajah**: search bar & baris kurasi horizontal (Sedang tren, K-Drama, Anime, Film ID).
- [x] **P4.6.1** Rekap tahunan ("Today Wrapped") — **SELESAI & terverifikasi device**.
  - **`:core`**: model `YearlyRecap`/`RecapTitle` + `RecapCalculator` (filter per tahun; total judul/catatan/jam; rewatch; top genre/negara; mood dominan; judul tersering; rating tertinggi; bulan tersibuk; streak hari beruntun; teman nonton) + `IDiaryRepository`/`DiaryUseCase` (`getRecapYears`, `getRecap`) + `RecapCalculatorTest` (12 test).
  - **`:app`**: `RecapViewModel` + `RecapScreen` (`ROUTE_RECAP`; `HorizontalPager` story-style 8 kartu; pemilih tahun `FilterChip`; indikator titik; berbagi teks via `ACTION_SEND`); baris **"Rekap Tahunan"** di `ProfileScreen`.
  - **Device (Pixel_4, Android 15)**: Profil → Rekap Tahunan → geser kartu (intro → total → genre/negara → mood → judul tersering → rating tertinggi → irama → outro) → "Bagikan rekap" membuka Android chooser dengan teks rekap.
  - Total test suite **105 hijau** (core 59 + social 14 + app 32); lint `0 errors`; build hijau.
- [x] **P4.6.2** Impor / Ekspor CSV (backup & Letterboxd) — **SELESAI & terverifikasi device**.
  - **`:core`**: `utils/Csv.kt` (codec RFC4180: quote/escape/baris) + `utils/DiaryCsvCodec.kt` (`export` header kanonik `titleId,mediaType,titleName,watchedOn,watchedDate,rating,mood,note,watchedWith,status,seasonNumber,episodeNumber`; `parse` mendeteksi format sendiri & **diary Letterboxd** `Date/Name/Year/Rating/Watched Date` → `titleId=0`, `mediaType=MOVIE`; tanggal `yyyy-MM-dd` via `SimpleDateFormat`) + `IDiaryRepository`/`DiaryUseCase` (`exportDiaryCsv`, `importDiaryCsv`) + `CsvTest` (8) + `DiaryCsvCodecTest` (8).
  - **`:app`**: `ImportExportViewModel` + `ImportExportScreen` (`ROUTE_IMPORT_EXPORT`) memakai **Storage Access Framework** (`ActivityResultContracts.CreateDocument("text/csv")` untuk ekspor, `OpenDocument` untuk impor; baca/tulis via `ContentResolver`), Snackbar status; baris **"Impor / Ekspor"** di `ProfileScreen`. `DiaryFormScreen`/`DiaryFormViewModel` dilonggarkan agar entri impor (`titleId=0`) tetap bisa diedit & disimpan.
  - **Device (Pixel_4, Android 15)**: Profil → Impor / Ekspor ("7 catatan") → **Ekspor ke CSV** menyimpan `today-diary-20261017.csv` (7 baris, kutipan koma/format benar) → **Impor dari CSV** `test-import.csv` (2 baris) → jumlah jadi "9 catatan", entri baru tampil di Home → buka entri impor (`titleId=0`) → tombol **Simpan** aktif → simpan sukses kembali ke Home.
  - Total test suite **121 hijau** (core 75 + social 14 + app 32); lint `0 errors`; build hijau.
- [x] **P4.6.3** Jadwal rilis (rilisan mendatang) — **SELESAI & terverifikasi device**.
  - **`:core`**: `TitleDetailResponse` + `EpisodeAirResult` (`next_episode_to_air`), model `NextEpisode` (di `TitleDetail`) & `UpcomingRelease`; `utils/ReleaseScheduleCalculator` (gabung watchlist × detail → episode TV mendatang + film belum rilis, filter ≥ hari ini, urut tanggal, label `S{n} E{n} · judul`) + `ReleaseScheduleUseCase`/`ReleaseScheduleInteractor` (`getUpcomingReleases`); `ReleaseScheduleCalculatorTest` (7 test).
  - **`:app`**: `ReleaseScheduleViewModel` (refresh trigger + error state) + `ReleaseScheduleScreen` (`ROUTE_RELEASE_SCHEDULE`; dikelompokkan "Hari ini"/"Besok"/tanggal, poster + judul + label + tanggal, tombol segarkan, state kosong) + `ReleaseScheduleViewModelTest` (3 test); ikon **"Jadwal Rilis"** (`Icons.Filled.CalendarMonth`) di TopAppBar `HomeScreen`; tap baris → detail judul.
  - **Device (Pixel_4, Android 15)**: Home → ikon Jadwal Rilis → state kosong (watchlist lama tanpa rilisan mendatang) → tambah "The Simpsons" ke watchlist → section **"Besok"** menampilkan `S38 E3 · Treehouse of Horror Presents: The Revengening` (18 Okt 2026) → tap → detail memuat data TMDB (rating 8.0, 38 musim · 807 episode).
  - Total test suite **131 hijau** (core 82 + social 14 + app 35); lint `0 errors`; build hijau.
  - **Catatan**: notifikasi push jadwal rilis (WorkManager + `POST_NOTIFICATIONS`) **belum** — keputusan produk terbuka (PRD §9).
- [x] **P4.6.4** Notifikasi jadwal rilis (lokal, WorkManager) — **SELESAI & terverifikasi device**.
  - **`:core`**: `ReleaseScheduleCalculator.dueSoon(releases, now, windowDays=1)` (filter rilis hari ini s/d akhir besok; dipakai worker) + 3 test.
  - **`:app`**: `notifications/ReleaseNotifications` (channel `release_schedule`, pref `today_notifications`, izin `POST_NOTIFICATIONS` API 33+, `show()`), `ReleaseNotificationWorker` (`CoroutineWorker`: cek enabled+izin, ambil `ReleaseScheduleUseCase` via Koin `GlobalContext`, notif rilis ≤ besok) + `ReleaseNotificationScheduler` (periodik 1 hari, initial delay ke 08:00, `NetworkType.CONNECTED`, unique work `release_schedule_daily`) + `res/drawable/ic_notification.xml`; `MyApplication` buat channel & jadwalkan bila aktif; toggle **"Notifikasi jadwal rilis"** di `ReleaseScheduleScreen` (izin runtime → jadwal/`cancel`).
  - **Device (Pixel_4, Android 15)**: Jadwal Rilis → switch notifikasi → dialog izin → **Allow** → switch `checked=true` + channel `release_schedule` terdaftar + job WorkManager terjadwal; trigger worker → NotificationRecord `id=4201` (channel `release_schedule`, `contentIntent` ke `MainActivity`) tampil.
  - **Diputuskan (eks K8)**: notifikasi **lokal via WorkManager** (tanpa FCM/server).
  - Total test suite **142 hijau** (core 85 + social 22 + app 35); lint `0 errors`; build hijau.
- [x] **P4.6.5** Circle (klub nonton via kode undangan) — **SELESAI & terverifikasi device**.
  - **`:social`**: model `Circle`/`CircleMember` + `CircleDoc`/`CircleMemberDoc` + mapper; `CircleDataSource` + `FirestoreCircleDataSource` (koleksi `circles` + subkoleksi `members`; `createCircle` generate kode 6 karakter, `joinByCode` cari `inviteCode` + `arrayUnion`/`increment`, `leaveCircle` `arrayRemove`/`increment(-1)`, stream `whereArrayContains("memberUids", uid)`); `CircleRepository`/`CircleRepositoryImpl`; wire di `SocialModule`; `firestore.rules` ditambah aturan `circles`/`members` (baca signed-in, tulis anggota sendiri).
  - **`:app`**: `CirclesViewModel` + `CirclesScreen` (`ROUTE_CIRCLES`; daftar circle, FAB Buat, aksi Gabung, dialog nama/kode, error di body) + `CircleDetailViewModel` + `CircleDetailScreen` (`ROUTE_CIRCLE_DETAIL`; kartu kode undangan + bagikan, daftar anggota, feed opini anggota via `observeOpinionsByAuthors`, like, komentar, keluar); baris **"Circle"** (`Icons.Filled.Groups`) di `ProfileScreen`.
  - **Device (Pixel_4, Android 15)**: Profil → Circle → state kosong → **Buat** "NobarJumat" → detail menampilkan kode `9N5Y8L`, 1 anggota, feed opini anggota → **Keluar** → daftar kosong → **Gabung** dengan kode `9N5Y8L` → kembali ke detail (like tersimpan).
  - **Catatan**: chat/watch party & circle publik di luar cakupan (PRD §9).
  - Total test suite **142 hijau** (core 85 + social 22 + app 35); lint `0 errors`; build hijau.
- [ ] **P4.6** Fase 2 (setelah F1 stabil): ~~recap tahunan ✅ (P4.6.1)~~ · ~~import/export CSV ✅ (P4.6.2)~~ · ~~jadwal rilis ✅ (P4.6.3)~~ · ~~notifikasi rilis ✅ (P4.6.4)~~ · ~~circle ✅ (P4.6.5)~~.

## Aturan main roadmap
1. Satu area per commit/PR; perbarui checkbox ini + tabel status [PRD §8](PRD.md#8-status-implementasi-vs-rencana-per-15-oke-2026) setelah selesai.
2. Jangan menambah fitur P2+ selama P0/P1 belum hijau.
3. Item P4 jangan diimplementasi sebelum keputusan backend (K3) diambil.
