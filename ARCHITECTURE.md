# Arsitektur Today

Dokumen ini merangkum bagaimana kode **Today** disusun. Bukan panduan kontribusi,
bukan buku acuan produksi — hanya peta untuk siapa pun yang ingin memahami
struktur repo dalam 5 menit.

## Modul

```
Today/
├── app/       UI (seluruh Jetpack Compose) + shell aplikasi
├── core/      domain + data: katalog TMDB, Room, diary, watchlist, daftar,
│              statistik, rekap, CSV, jadwal rilis
└── social/    domain + data sosial: Firebase Auth + Firestore
               (profil, opini, like, komentar, follow, circle) — tanpa UI
```

Aturan yang dijaga:

- `app` boleh tahu `core` dan `social`.
- `core` **tidak** tahu `core` lain di sekitarnya — tanpa Compose, tanpa Android View,
  tanpa Firebase. Ia bekerja dengan model domain dan sumber data murni.
- `social` juga tanpa UI. Semua tampilan sosial tetap di `app`.
- Skema nilai tergantung kontrak: `settings.gradle.kts` menyertakan `:app`, `:core`, `:social`.

## Lapisan di dalam `core`

```
domain/     model + use case + kontrak repository (interface)
data/       implementasi: Room DAO, Retrofit service, RemoteMediator, codec CSV
di/         pendaftaran Koin (CoreModule)
utils/      kalkulator murni: RecapCalculator, StatsCalculator,
            ReleaseScheduleCalculator, DiaryCsvCodec
```

Pemisahan ini punya tujuan pragmatis: kalkulator dan codec dites tanpa Android.
`RecapCalculator` dan `StatsCalculator` misalnya fungsi murni di atas list, bukan
yang menyentuh Room.

## Alur data

### Katalog (online → lokal → layar)

```
TMDB API → RemoteDataSource (Retrofit)
        → TitleRemoteMediator (Paging)
        → Room (TitleDao)     ← cache, sekali jaringan habis app tetap jalan
        → PagingData<Title> → Compose
```

`RemoteMediator` yang membuat pola ini bukan skrip cache biasa: Paging yang
"menyentuh" network beneran, jadi scroll katalog dan ruang lokal duduk di satu
abstraksi.

### Diary (lokal dulu)

```
UI → DiaryUseCase → DiaryRepository → DiaryLocalDataSource → DiaryDao → Room
```

Semua diary, watchlist, daftar kustom, dan statistik lewat jalur ini. Murni offline:
tidak ada akun, tidak ada internet. Nyatanya ini yang membuat aplikasi tetap
berguna saat mode pesawat.

### Sosial (remote)

```
UI → SocialRepository → SocialDataSource → Firestore / Firebase Auth
```

- **Opini** dibuat dari satu baris entri diary (aksi "Bagikan").
- **Feed** dua agregat: "Untuk Anda" (semua opini publik terbaru) dan "Mengikuti".
- **Like & komentar** memakai optimistic update: state UI berubah lebih dulu,
  lalu server diverifikasi; kalau gagal, kembali.
- **Komentar** punya skema spoiler: teks disembunyikan sampai ditekan.

### Notifikasi rilis

```
WorkManager (periodik harian, ±08:00)
   → ReleaseScheduleUseCase (watchlist × detail TMDB)
   → ReleaseNotificationWorker → kanal "release_schedule"
```

Pilihan sadar: **WorkManager lokal**, bukan FCM. Tidak ada server push yang
dipertahankan, dan jadwal rilis tetap ada walau app tidak dibuka.

## Sisi desain

| Keputusan | Kenapa |
|---|---|
| Offline-first di `core` | Diary nyaris selalu ditulis dalam keadaan sedang menonton; jaringan tidak boleh jadi syarat |
| Tidak ada akun untuk diary | Tembok login di depan sesuatu yang personal mengurangi penggunaan |
| Auth gate hanya untuk sosial | Profil, opini, like, komentar — butuh identitas; catatan sendiri tidak |
| Spoiler mask tap-to-reveal | Membaca opini seharusnya tidak merusak film yang belum kamu tonton |
| Terjemahan API error ke Bahasa Indonesia | Error seperti `NETWORK_ERROR` tidak berarti apa-apa untuk penonton lokal |
| Dark mode default | Kebiasaan menonton lebih banyak malam hari |

## Keamanan & konfigurasi

Semua rahasia dibaca dari file lokal dan tidak pernah masuk repo:

| File | Isi | Status |
|---|---|---|
| `local.properties` | API key TMDB + `BASE_URL` | gitignored |
| `app/google-services.json` | konfigurasi project Firebase | gitignored |
| `firestore.rules` | aturan keamanan Firestore | dikomit (bukan rahasia) |
| `firebase.json` | pemetaan rules → file | dikomit (bukan rahasia) |

Plugin `google-services` diterapkan **kondisional**: tanpa file konfigurasi,
aplikasi tetap ter-compile dan fitur offline tetap jalan. Menjalankan proyek tanpa
konfigurasi apa pun tidak menghasilkan build gagal — hanya fitur yang tidak
tersedia.

## Kualitas

- **142 unit test** tersebar di `:core` (85), `:social` (22), `:app` (35).
- CI GitHub Actions menjalankan unit test + `assembleDebug` di setiap push ke `main`.
- Android lint bersih.

```bash
./gradlew test          # seluruh unit test
./gradlew lint          # Android lint
```
