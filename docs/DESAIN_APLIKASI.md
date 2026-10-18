# Today — Rancangan Halaman, Navigasi & Fitur

> Dokumen perancangan produk (UX/IA). Gunakan sebagai dasar untuk PRD, wireframe, dan roadmap.
> Status: **draft v1**. Tanda ☐ = belum diputuskan/dikerjakan.

---

## 1. Ringkasan Konsep

**Satu kalimat:** Diary nonton yang personal untuk penonton Indonesia — catat film, serial, dan anime beserta mood, teman nonton, dan perubahan penilaian saat menonton ulang — lalu (fase berikutnya) berbagi dengan komunitas.

**Prinsip desain**
1. **Diary dulu, katalog belakangan.** Mencatat tontonan harus bisa dilakukan dalam ≤ 3 ketukan.
2. **Offline-first.** Semua fitur pribadi jalan tanpa internet; hanya katalog TMDB dan komunitas yang butuh jaringan.
3. **Manusiawi.** Fokus pada perasaan (mood, siapa yang nonton bareng), bukan sekadar angka rating.
4. **Bertahap.** Fitur sosial hanya muncul setelah backend siap; sebelumnya aplikasi harus terasa utuh sebagai diary pribadi.

**Target pengguna (persona singkat)**

| Persona | Kebutuhan utama |
|---|---|
| **Pencatat rajin** — mahasiswa/pekerja, nonton 5–10 judul/bulan | Catat cepat, lihat statistik, rekap tahunan |
| **Penonton serial/anime** | Tracking episode, lanjutkan tontonan, watchlist |
| **Penonton sosial** — nonton bareng pasangan/teman/keluarga | Catat "nonton bareng siapa", bagikan opini, lihat ulasan orang lain |

---

## 2. Arsitektur Informasi & Navigasi

### 2.1 Bottom Navigation (5 tab)

```
┌─────────┬─────────┬─────────┬─────────┬─────────┐
│ Beranda │ Jelajah │ Diary   │ Komunitas│ Profil  │
│  🏠     │  🔍     │  📖     │  💬     │  👤     │
└─────────┴─────────┴─────────┴─────────┴─────────┘
                  [ + Catat ]  ← FAB global (muncul di Beranda, Jelajah, Diary)
```

| Tab | Fase | Catatan |
|---|---|---|
| Beranda | MVP | Ringkasan pribadi + rekomendasi |
| Jelajah | MVP | Cari & telusuri katalog TMDB |
| Diary | MVP | Inti produk: linimasa tontonan, watchlist, daftar |
| Komunitas | Fase sosial | **Disembunyikan / diganti "Segera hadir"** sampai backend ada; bottom nav sementara 4 tab |
| Profil | MVP | Statistik, pengaturan, akun |

### 2.2 Peta Layar (Sitemap)

```
Splash
└─ Onboarding (pertama kali saja)
   └─ Main (Bottom Nav)
      ├─ Beranda
      │   ├─ Detail Judul ───────────────┐
      │   ├─ Lihat Semua (daftar judul)   │
      │   └─ Jadwal Rilis                 │
      ├─ Jelajah                          │
      │   ├─ Pencarian                    │
      │   ├─ Hasil per Kategori/Genre     │
      │   └─ Detail Judul ◄───────────────┘
      │        ├─ Form Catat Tontonan (bottom sheet / layar penuh)
      │        ├─ Tambah ke Watchlist / Daftar
      │        ├─ Tracking Episode (serial)
      │        └─ Ulasan Komunitas (fase sosial)
      ├─ Diary
      │   ├─ Tab: Linimasa
      │   │    └─ Detail Entri Diary
      │   │         ├─ Linimasa Rewatch
      │   │         └─ Edit Entri
      │   ├─ Tab: Watchlist
      │   └─ Tab: Daftar
      │        ├─ Detail Daftar
      │        └─ Buat/Edit Daftar
      ├─ Komunitas (fase sosial)
      │   ├─ Feed
      │   ├─ Detail Postingan / Komentar
      │   └─ Profil Pengguna Lain
      └─ Profil
          ├─ Statistik Lengkap
          ├─ Rekap Tahunan
          ├─ Impor / Ekspor CSV
          ├─ Circle (klub nonton via kode undangan) ✅ P4.6.5
          ├─ Pengaturan
          │    ├─ Tampilan (tema, bahasa)
          │    ├─ Backup & Ekspor Data
          │    ├─ Notifikasi (jadwal rilis lokal ✅ P4.6.4)
          │    └─ Tentang & Lisensi (atribusi TMDB)
          └─ Akun (fase sosial: masuk/daftar)
```

### 2.3 Aturan Navigasi
- Tombol **Back** selalu kembali ke layar asal (bukan ke Beranda).
- **Detail Judul** bisa dibuka dari mana saja (Beranda, Jelajah, Diary, Daftar, Komunitas) — satu layar, satu *route* (`detail/{mediaType}/{id}`).
- **Form Catat** dibuka sebagai *bottom sheet* penuh agar konteks (judul di belakang) tidak hilang.
- Pindah tab mempertahankan posisi scroll & state tiap tab (`saveState`/`restoreState`).
- Deep link: ☐ `today://title/{type}/{id}` (untuk berbagi nanti).

---

## 3. Rincian Setiap Halaman

### 3.1 Splash & Onboarding
**Tujuan:** menjelaskan nilai produk dengan cepat dan mengatur preferensi awal.

| Layar | Isi |
|---|---|
| Splash | Logo, inisialisasi DB & cek versi |
| Onboarding 1–3 | (1) "Catat tontonanmu", (2) "Simpan mood & teman nonton", (3) "Lacak penilaianmu saat nonton ulang" |
| Preferensi (opsional) | Pilih genre favorit, platform streaming yang dipakai (Netflix, Disney+, Vidio, WeTV, dll.) — dipakai untuk rekomendasi |

Aksi: **Mulai**, **Lewati**. Tidak wajib akun.

---

### 3.2 Beranda
**Tujuan:** pintu masuk harian — "apa yang sedang kutonton & apa selanjutnya?"

**Bagian (urut dari atas):**
1. **Sapaan + ringkasan mingguan** — "Minggu ini kamu menonton 3 judul · 6 jam".
2. **Lanjutkan Menonton** — kartu horizontal serial yang sedang berjalan, dengan progres episode (`S2 E5/10`) dan tombol cepat **+1 episode**.
3. **Watchlist Teratas** — 5 judul terbaru dari watchlist.
4. **Sedang Tren** — dari TMDB (film & serial tren minggu ini).
5. **Rekomendasi untukmu** — berdasar genre favorit & rating tinggi pengguna.
6. **Throwback** — "Setahun lalu kamu menonton …" (memicu rewatch).

**State:** kosong (belum ada tontonan → ajakan "Catat tontonan pertamamu"), offline (bagian TMDB diganti pesan + tombol coba lagi), loading (skeleton).

---

### 3.3 Jelajah
**Tujuan:** menemukan judul.

- **Search bar** (teks, debounce 400 ms) → hasil campuran film/serial/anime, filter chip: `Semua · Film · Serial · Anime`.
- **Kategori/genre** berupa chip & baris horizontal: Populer, Tayang Sekarang, Rating Tertinggi, per genre.
- **Filter lanjutan** (bottom sheet): tahun, genre, negara asal (Korea, Jepang, Indonesia, dll.), bahasa, rating minimum, durasi.
- **Riwayat pencarian** (lokal, bisa dihapus).
- **Kartu judul**: poster, judul, tahun, rating TMDB, ikon status (✓ sudah ditonton / ★ di watchlist).
- Paging tak terbatas (Paging 3 + RemoteMediator).

---

### 3.4 Detail Judul
**Tujuan:** semua info satu judul + semua aksi pribadi.

**Isi:**
- Backdrop + poster, judul, tahun, durasi/jumlah episode, genre, rating TMDB.
- **Status pribadi** (chip): `Belum ditonton · Sedang ditonton · Selesai · Ditinggalkan`.
- **Tombol aksi utama:** `Catat Tontonan` · `+ Watchlist` · `+ Daftar` · `Bagikan`.
- Sinopsis (bisa diperluas).
- **Pemeran & kru** (baris horizontal).
- **Di mana menonton** (penyedia streaming Indonesia dari TMDB watch providers).
- **Tracking Episode** (serial): daftar musim → episode dengan checkbox, "tandai sampai episode ini".
- **Riwayat tontonanmu**: semua entri diary untuk judul ini (rewatch).
- **Mirip dengan ini** (rekomendasi).
- *(Fase sosial)* **Ulasan komunitas** & rata-rata rating pengguna Today.

---

### 3.5 Form Catat Tontonan (inti produk)
**Prinsip:** cepat (hanya tanggal + rating wajib), tapi kaya jika pengguna mau.

| Field | Jenis | Wajib | Catatan |
|---|---|---|---|
| Judul | otomatis dari Detail / pencarian cepat | ✓ | |
| Tanggal nonton | date picker, default hari ini | ✓ | Bisa pilih "tidak ingat" |
| Rating | bintang ½ (0,5–5) | ✓ | |
| Mood saat menonton | pilih 1–2 emoji/label: 😂 Ceria · 😭 Mewek · 😱 Tegang · 🥰 Hangat · 😴 Bosan · 🤔 Mikir · 😡 Kesal | – | Daftar mood bisa dikembangkan |
| Nonton bareng | chip: Sendiri · Pasangan · Keluarga · Teman · + nama kustom | – | Ingat nama yang pernah dipakai |
| Catatan | teks bebas (multi-baris) | – | Dukung spoiler-tag `||seperti ini||` |
| Tempat/platform | Bioskop · Netflix · … · Lainnya | – | |
| Tandai rewatch | toggle otomatis jika judul sudah pernah dicatat | – | |
| Suka (♥) | toggle | – | Terpisah dari rating |
| Tag | teks bebas (mis. "healing", "nangis di bagian akhir") | – | ☐ fase 2 |

**Aksi:** `Simpan`, `Simpan & tambah ke daftar`, `Batal`. Konfirmasi saat keluar dengan data belum disimpan.

---

### 3.6 Diary
**Tab 1 — Linimasa**
- Daftar entri dikelompokkan per **bulan** (header sticky: "Oktober 2026 · 7 judul").
- Kartu entri: poster kecil, judul, tanggal, rating bintang, ikon mood, badge `Rewatch #2`.
- Filter: tahun, rating, mood, jenis (film/serial/anime), nonton bareng.
- Urut: terbaru / rating / judul.
- Gestur: geser untuk hapus (dengan undo), tekan lama untuk multi-pilih.

**Tab 2 — Watchlist**
- Grid/daftar judul yang ingin ditonton. Urut: terbaru, rating TMDB, prioritas.
- Aksi per item: `Mulai nonton` (→ Form Catat), hapus, pindah ke daftar.
- Fitur: **"Acak tontonan"** (pilih random dari watchlist, filter durasi/genre).
- ☐ Prioritas (Tinggi/Biasa).

**Tab 3 — Daftar**
- Koleksi kustom buatan pengguna ("Drama Korea healing", "Maraton akhir pekan").
- Kartu daftar: kolase 4 poster, nama, jumlah judul.
- FAB `Buat Daftar` → nama, deskripsi, (fase sosial: publik/pribadi), urutkan manual (drag).

---

### 3.7 Detail Entri Diary
- Poster + info judul (tap → Detail Judul).
- Rating, mood, teman nonton, platform, tanggal, catatan lengkap.
- **Linimasa Rewatch**: grafik/daftar perubahan rating & mood antar penontonan
  ("2022 ★3,0 😴 → 2026 ★4,5 😭").
- Aksi: `Edit`, `Catat nonton ulang`, `Bagikan sebagai kartu` (gambar), `Hapus`.

---

### 3.8 Komunitas *(fase sosial — butuh backend & akun)*
- **Feed**: tab `Untuk Kamu` / `Mengikuti`. Postingan = ulasan singkat dari entri diary (poster, rating, mood, cuplikan catatan).
- Interaksi: suka, komentar, simpan, laporkan.
- **Spoiler**: teks tersembunyi dengan tombol "Lihat spoiler".
- **Profil pengguna lain**: bio, statistik publik, diary publik, daftar publik, tombol ikuti.
- **Moderasi**: lapor, blokir, sembunyikan kata.
- Pengguna memilih **per entri**: Pribadi (default) / Publik.

---

### 3.9 Profil
**Atas:** avatar, nama, bio singkat, tombol `Edit` (fase sosial: jumlah pengikut).

**Statistik Dasar (MVP)**
- Total judul, total jam tonton, rata-rata rating, rewatch terbanyak.
- Grafik **tontonan per bulan** (bar).
- Distribusi **rating** (histogram 0,5–5).
- Distribusi **mood** & **genre** (donut/bar).
- **Teman nonton** teratas.
- Rekor: streak hari menonton, bulan paling produktif.

**Rekap Tahunan ("Today Wrapped")** — ✅ P4.6.1: kartu geser (story-style) yang bisa dibagikan.

**Jadwal Rilis** — ✅ P4.6.3: episode TV mendatang (`next_episode_to_air` TMDB) + film belum rilis dari watchlist, dikelompokkan per hari ("Hari ini"/"Besok"/tanggal) dengan poster, label `S{n} E{n} · judul`, dan tombol segarkan. **Notifikasi lokal** ✅ P4.6.4: toggle di layar ini → WorkManager periodik harian (±08:00) + izin `POST_NOTIFICATIONS`, channel `release_schedule` (tanpa FCM).

**Pengaturan**
| Grup | Opsi |
|---|---|
| Tampilan | Tema (terang/gelap/ikuti sistem), bahasa (ID/EN), warna dinamis |
| Data | Backup & pulihkan (file lokal), ekspor CSV/JSON, impor dari Letterboxd/CSV ✅ P4.6.2 (SAF `CreateDocument`/`OpenDocument`) |
| Notifikasi | Pengingat catat ("Semalam nonton apa?"), episode baru serial di watchlist ☐ |
| Privasi | Default visibilitas entri (fase sosial) |
| Akun | Masuk/daftar, hapus akun (fase sosial) |
| Tentang | Versi, lisensi, **atribusi TMDB wajib**, kebijakan privasi |

---

## 4. Daftar Fitur & Prioritas

Legenda: **P0** wajib MVP · **P1** penting · **P2** nanti · **S** fase sosial

| # | Fitur | Prioritas | Catatan |
|---|---|---|---|
| 1 | Katalog film & serial (TMDB), pencarian, filter | P0 | ✅ sudah ada (core) |
| 2 | Catat tontonan: rating ½ bintang, mood, catatan, nonton bareng | P0 | ✅ |
| 3 | Rewatch + linimasa penilaian | P0 | ✅ |
| 4 | Tracking episode manual | P0 | ✅ |
| 5 | Watchlist | P0 | ✅ |
| 6 | Daftar kustom | P0 | ✅ |
| 7 | Statistik dasar | P0 | ✅ |
| 8 | Onboarding & pengaturan tema | P1 | |
| 9 | Beranda: Lanjutkan Menonton, Throwback | P1 | |
| 10 | Backup/ekspor/impor data | P1 | Penting untuk kepercayaan pengguna — ✅ CSV P4.6.2 |
| 11 | Penyedia streaming ("di mana menonton") | P1 | TMDB watch providers |
| 12 | Berbagi kartu diary sebagai gambar | P1 | Pertumbuhan organik tanpa backend |
| 13 | Pengingat & notifikasi episode baru | P2 | Jadwal rilis in-app ✅ P4.6.3; notifikasi **lokal** ✅ P4.6.4 (WorkManager); push server ☐ |
| 14 | Rekap tahunan | P2 | ✅ P4.6.1 |
| 15 | Widget homescreen (Lanjutkan menonton) | P2 | |
| 16 | Akun & sinkron cloud | S | |
| 17 | Feed, ikuti, suka, komentar | S | |
| 18 | Ulasan publik & rating komunitas | S | |
| 19 | Daftar publik & kolaboratif | S | |
| 20 | Moderasi & pelaporan | S | Wajib sebelum rilis sosial |
| 21 | Circle (klub nonton privat via kode undangan) | S | ✅ P4.6.5 (klub dasar: buat/gabung, feed anggota, keluar; chat/watch party ☐) |

---

## 5. Alur Pengguna Utama

**A. Catat tontonan cepat (target ≤ 3 ketukan)**
```
Tap [+ Catat] → ketik judul → pilih hasil → atur bintang → Simpan
```

**B. Dari penemuan ke diary**
```
Jelajah → cari/pilih judul → Detail → "+ Watchlist"
        ... (nanti) ...
Watchlist → "Mulai nonton" → Form Catat → Simpan
```

**C. Tracking serial**
```
Beranda → "Lanjutkan Menonton" → +1 episode
Setelah episode terakhir → prompt "Selesai! Beri rating?" → Form Catat
```

**D. Nonton ulang**
```
Detail Entri → "Catat nonton ulang" → Form (rating awal tampil sebagai pembanding)
→ Linimasa Rewatch diperbarui
```

**E. (Sosial) Bagikan opini**
```
Detail Entri → toggle "Publik" → muncul di feed pengikut
```

---

## 6. Model Data Konseptual

```
Media            (tmdbId, type: movie|tv, judul, poster, genre[], tahun, durasi, ...)
DiaryEntry       (id, mediaId, tanggal, rating, mood[], nontonBareng[], catatan,
                  platform, suka, isRewatch, rewatchKe, visibilitas)
EpisodeProgress  (mediaId, musim, episode, ditontonPada)
WatchlistItem    (mediaId, ditambahkanPada, prioritas?)
CustomList       (id, nama, deskripsi, visibilitas)
CustomListItem   (listId, mediaId, urutan)
Companion        (id, nama)   ← untuk saran "nonton bareng"
UserPrefs        (tema, bahasa, genreFavorit[], platform[])
--- fase sosial ---
User, Follow, Post/Review, Like, Comment, Report
```

---

## 7. Spesifikasi Pelengkap

### State yang harus dirancang di tiap layar
| State | Perilaku |
|---|---|
| Loading | Skeleton, bukan spinner penuh |
| Kosong | Ilustrasi + 1 kalimat + tombol aksi |
| Error jaringan | Pesan ramah + `Coba lagi`; data lokal tetap tampil |
| Offline | Banner tipis "Mode offline"; fitur diary tetap aktif |

### Aksesibilitas & lokalisasi
- Bahasa utama Indonesia, EN sebagai cadangan; tanggal & angka mengikuti locale.
- Target sentuh ≥ 48 dp, kontras sesuai WCAG AA, `contentDescription` untuk poster & ikon mood, bintang rating bisa dioperasikan dengan TalkBack.
- Dukungan tema gelap (pengguna biasa membuka diary malam hari).

### Metrik keberhasilan (usulan)
- ≥ 60% pengguna baru mencatat ≥ 1 tontonan di sesi pertama.
- Median waktu mencatat < 30 detik.
- Retensi D7 ≥ 25%, D30 ≥ 12%.
- ≥ 20% entri memakai mood/nonton bareng (bukti diferensiasi produk).

---

## 8. Urutan Pengerjaan yang Disarankan

1. **Rapikan MVP** yang sudah ada: Onboarding, tema, Beranda (Lanjutkan Menonton, Throwback), Detail Entri.
2. **Kepercayaan data:** backup/ekspor/impor.
3. **Pertumbuhan tanpa backend:** berbagi kartu diary, "di mana menonton".
4. **Retensi:** notifikasi, widget, rekap tahunan.
5. **Fase sosial:** akun → sinkron cloud → profil publik → feed → moderasi.

---

## 9. Keputusan Terbuka

- ✅ **Nama final** aplikasi: **Today** (diputuskan 16 Okt 2026; K1/K2 PRD).
- ☐ Anime: tab/filter khusus, atau cukup lewat genre/negara asal?
- ☐ Rating komunitas digabung dengan TMDB, atau dipisah?
- ☐ Model bisnis: gratis penuh, atau fitur premium (statistik lanjutan, backup cloud, tema)?
- ☐ Komunitas: feed global atau hanya dari yang diikuti (lebih mudah dimoderasi)?
- ☐ Sumber data cadangan jika kuota/lisensi TMDB bermasalah (mis. AniList untuk anime)?
- ☐ Impor dari layanan lain (Letterboxd, MyAnimeList, Trakt) — prioritas?
- ☐ Kebijakan privasi & persetujuan data sebelum fitur akun dirilis (UU PDP).
