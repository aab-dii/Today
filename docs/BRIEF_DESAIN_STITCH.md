# Today — Brief Desain Aplikasi (untuk Stitch AI)

> **Tujuan dokumen:** menggambarkan aplikasi **Today** secara rinci — daftar halaman, isi tiap halaman, komponen, state, dan gaya visual — agar dapat dijadikan acuan saat meminta **Stitch AI (Google)** merancang UI aplikasi ini.
>
> **Cara pakai:** gunakan §1–§4 sebagai *system context*, lalu salin **prompt siap-tempel per layar** di §8 satu per satu ke Stitch (Stitch bekerja paling baik dengan prompt fokus per layar, bukan seluruh aplikasi sekaligus).
>
> Dokumen ini turunan dari [DESAIN_APLIKASI.md](DESAIN_APLIKASI.md) tetapi **(1) memetakan ke implementasi nyata** dan **(2) dirancang sebagai bahan desain**, bukan arsitektur.

---

## 1. Ringkasan Produk

**Nama:** Today
**Tagline:** *"Diary nonton film, serial & anime — hari ini nonton apa?"*
**Platform:** Android (aplikasi mobile, portrait)
**Bahasa UI:** Indonesia (utama), Inggris (cadangan)
**Gaya produk:** personal, hangat, "buku harian" — **bukan** database film generik dan **bukan** jejaring sosial yang ramai.

**Satu kalimat:** Diary nonton yang personal untuk penonton Indonesia — catat film, serial, dan anime beserta mood, teman nonton, dan perubahan penilaian saat menonton ulang — lalu (opsional) bagikan opini ke komunitas.

**Diferensiasi (harus terasa di desain):**
1. **Log-first**, bukan feed-first — layar utama bertanya **"Hari ini nonton apa?"**.
2. **Manusiawi** — yang ditonjolkan adalah perasaan (mood emoji, nonton bareng siapa), bukan sekadar angka rating.
3. **Rewatch bermakna** — penilaian yang berubah antar penontonan ditampilkan sebagai linimasa (mis. "2022 ★3,0 😴 → 2026 ★4,5 😭").

**Persona (untuk rasa visual):**
- **Pencatat rajin** (mahasiswa/pekerja) — catat cepat, lihat statistik & rekap tahunan.
- **Penonton serial/anime** — tracking episode, "lanjutkan menonton".
- **Penonton sosial** — nonton bareng pasangan/teman/keluarga, bagikan opini, circle.

**Prinsip desain:**
- Mencatat tontonan bisa selesai dalam **≤ 3 ketukan**.
- **Offline-first**: diary tetap jalan tanpa internet (katalog & komunitas butuh jaringan).
- Target sentuh ≥ 48 dp, kontras WCAG AA, dukung **mode gelap** (banyak orang mencatat di malam hari).
- Loading pakai **skeleton**, bukan spinner penuh; state kosong selalu punya ilustrasi + 1 kalimat + tombol aksi.

---

## 2. Identitas Visual & Sistem Desain

### 2.1 Mood / arah visual
- Bersih, lapang, tenang — terasa seperti **buku catatan pribadi** yang modern.
- Nuansa malam: palet navy → indigo → slate → lavender (`#070F2B`, `#1B1A55`, `#535C91`, `#9290C3`) — sejuk, elegan, kontemplatif.
- Foto poster jadi bintang; background netral agar poster "menyala".
- Emoji/label mood memberi sentuhan hangat dan personal.
- Hindari: gradien norak, bayangan berat, ikon ramai, warna neon.

### 2.2 Palet warna (tema saat ini)
Palet **navy → indigo → slate → lavender** (malam, kalem, elegan):

| Token | Hex | Peran |
|---|---|---|
| `navy_900` | `#070F2B` | Paling gelap — teks utama / background dark |
| `indigo_700` | `#1B1A55` | Primary (light) / primary container (dark) |
| `slate_500` | `#535C91` | Secondary — aksen, bintang rating (light) |
| `lavender_300` | `#9290C3` | Primary (dark), aksen terang, rating (dark) |

Pemetaan per peran:

| Peran | Light | Dark |
|---|---|---|
| Primary | `#1B1A55` (indigo) | `#9290C3` (lavender) |
| On Primary | `#FFFFFF` | `#1B1A55` |
| Primary Container | `#9290C3` | `#1B1A55` |
| Secondary (bintang rating) | `#535C91` (slate) | `#9290C3` |
| On Secondary | `#FFFFFF` | `#1B1A55` |
| Background / Surface | `#F7F7FC` (hampir putih, rona lavender tipis) | `#070F2B` (navy) / permukaan `#0E1440` |
| Surface Variant | `#E3E2F0` — *placeholder* poster & chip | `#2B3060` |
| Error | M3 default merah | idem |

> **Catatan untuk desainer:** palet ini seluruhnya bernuansa biru sejuk (cool blue). Jaga agar: **poster tetap menonjol** (background netral), **bintang rating = slate/lavender**, **error & hapus = merah**, **spoiler = merah lembut**. Hindari menambah warna neon di luar palet.

### 2.3 Tipografi
- Memakai **Material 3 default** (Roboto). Judul layar `titleLarge`/`displaySmall`, judul seksi `titleMedium` semi-bold, isi `bodyMedium`, meta `bodySmall`/`labelSmall`.
- Nama judul film/serial semi-bold, maksimum 2 baris dengan elipsis.

### 2.4 Bentuk, elevasi, jarak
- Poster: sudut bulat **8–10 dp**; kartu: sudut bulat Material 3 default (12–16 dp).
- Padding layar standar **16 dp** (beberapa layar form 24 dp).
- Kartu list: poster kecil di kiri (56–80 dp lebar), teks di kanan.
- Grid watchlist: 2 kolom, rasio poster **2:3**.
- Bottom navigation Material 3 dengan **5 tab**, ikon + label.

### 2.5 Ikonografi
Ikon Material (`Icons.Filled`) — makna penting:

| Ikon | Arti |
|---|---|
| `Today` | tab Beranda / "Hari ini" |
| `Explore` | tab Jelajah |
| `Bookmark` / `BookmarkBorder` | Watchlist (terisi = sudah di watchlist) |
| `PlaylistPlay` | tab Daftar |
| `Person` | tab Profil |
| `Add` | aksi "Catat" |
| `CalendarMonth` | Jadwal Rilis |
| `DynamicFeed` | Feed Komunitas |
| `Star` / `StarHalf` | rating ½ bintang |
| `Favorite` / `FavoriteBorder` | Suka/like |
| `ChatBubbleOutline` | Komentar |
| `Groups` | Circle / anggota |
| `GroupAdd` | Gabung circle |
| `AutoAwesome` | Rekap Tahunan |
| `BarChart` | Statistik |
| `FileUpload` / `FileDownload` | Impor / Ekspor |
| `Logout` | Keluar |
| `Warning` / `VisibilityOff` | Spoiler / sembunyikan |
| `Celebration` | badge Rewatch |

### 2.6 Komponen bersama (dipakai berulang di banyak layar)
- **TitleCard** — kartu horizontal: poster 80×120, judul (2 baris), label tipe ("Film"/"Serial"), rating + ikon bintang, tombol opsional "Catat cepat".
- **PosterTile** — poster kolom (default 120 dp, badge opsional di sudut, mis. "Lanjut"), judul di bawah.
- **SectionHeader** — judul seksi tebal.
- **RatingDisplay** — 5 bintang teal (penuh/½/kosong) untuk menampilkan nilai.
- **RatingBar** — 5 bintang yang bisa ditekan (kiri = ½, kanan = penuh), untuk mengisi nilai.
- **DiaryRow / DiaryEntryRow** — kartu entri: poster 56×84, judul, tanggal, rating, mood, status, cuplikan catatan.
- **OpinionCard** — kartu opini sosial: nama penulis (+ tombol Ikuti), poster, judul + "Film · tanggal", rating, mood, cuplikan catatan, tag "Mengandung spoiler", baris aksi Suka & Komentar.
- **EmptyState** — teks tengah, tenang, + (idealnya) ilustrasi.
- **ErrorBanner** — pesan ramah + tombol "Coba lagi".
- **LoadingIndicator** — spinner (target: ganti skeleton).

---

## 3. Peta Navigasi

### 3.1 Gerbang masuk (Auth gate)
Aplikasi **selalu** dibuka di gerbang auth:
```
Splash (memuat sesi)  →  [belum masuk] AuthScreen
                      →  [sudah masuk] AppNavHost (isi aplikasi)
```
- Diary bisa dipakai tanpa akun secara konsep, tetapi pada implementasi saat ini **wajib masuk** untuk masuk ke aplikasi.
- Belum masuk → layar **Masuk / Daftar** (email + Google).
- Sudah masuk → aplikasi utama.

### 3.2 Bottom navigation (5 tab)
```
┌───────────┬───────────┬───────────┬───────────┬───────────┐
│ Hari ini  │  Jelajah  │ Watchlist │  Daftar   │  Profil   │
│  Today    │  Explore  │ Bookmark  │ Playlist  │  Person   │
└───────────┴───────────┴───────────┴───────────┴───────────┘
                        [ + Catat ]  ← FAB (di Beranda/Jelajah)
```
| Tab | Isi |
|---|---|
| **Hari ini** | Ringkasan pribadi: "Sedang ditonton" + "Catatan terbaru" |
| **Jelajah** | Cari & telusuri katalog film/serial (TMDB) |
| **Watchlist** | Daftar "mau ditonton" (grid poster) |
| **Daftar** | Koleksi kustom buatan pengguna |
| **Profil** | Akun, statistik, rekap, impor/ekspor, circle, keluar |

Bottom bar **disembunyikan** saat berada di layar turunan (detail, form, dsb.).

### 3.3 Sitemap lengkap
```
Splash
└─ Auth (Masuk / Daftar)  ── (setelah masuk) ──┐
   └─ Home (Bottom Nav)                         │
      ├─ Beranda ("Hari ini nonton apa?")       │
      │   ├─ Jadwal Rilis                        │
      │   ├─ Feed Komunitas                      │
      │   └─ Detail Judul ◄──────────────────────┤ (bisa dari mana saja)
      ├─ Jelajah                                 │
      │   └─ Pencarian                           │
      ├─ Watchlist                               │
      ├─ Daftar                                  │
      │   └─ Detail Daftar                       │
      └─ Profil                                  │
          ├─ Ubah Profil (inline)                │
          ├─ Opini saya                          │
          ├─ Circle ─► Detail Circle             │
          ├─ Statistik                           │
          ├─ Rekap Tahunan                       │
          ├─ Impor / Ekspor                      │
          └─ Keluar                              │

Detail Judul
 ├─ Form Catat Tontonan (layar penuh, bottom sheet untuk quick-log)
 ├─ Dialog "Tambah ke daftar"
 ├─ Dialog "Progres episode" (serial)
 ├─ Dialog "Bagikan opini"
 ├─ Sheet "Komentar"
 └─ Detail Entri (lewat linimasa) ─► Form Ubah / Tonton ulang

Feed / Opini saya / Public Profile / Detail Circle
 └─ Sheet "Komentar"
```

### 3.4 Aturan navigasi
- Tombol **Kembali** selalu kembali ke layar asal.
- **Detail Judul** = satu layar untuk semua konteks.
- **Form Catat** dibuka penuh; **quick-log** (isi rating saja) dibuka sebagai *bottom sheet*.
- Pindah tab mempertahankan posisi scroll tiap tab.

---

## 4. Daftar Halaman & Isi Detail

> Legenda: **✅** sudah ada di aplikasi · **◇** rencana (belum dibangun) — tetap disertakan agar desain siap ke depan.
>
> Setiap halaman: **Tujuan · Isi (atas→bawah) · Aksi · State**.

---

### 4.1 Onboarding / Splash
**Tujuan:** memuat sesi & (rencana) memperkenalkan nilai produk.
- Splash: logo "Today" + indikator.
- ◇ Onboarding 1–3: (1) "Catat tontonanmu" (2) "Simpan mood & teman nonton" (3) "Lacak penilaian saat nonton ulang". Tombol **Mulai** / **Lewati**.
- ◇ Preferensi: pilih genre favorit & platform streaming (Netflix, Disney+, Vidio, WeTV, dst.).

---

### 4.2 AuthScreen — Masuk / Daftar ✅
**Tujuan:** pintu masuk akun.
**Isi (atas→bawah):**
1. Judul besar **"Today"** (warna primary) + kalimat: *"Diary nonton film, serial, dan anime. Masuk untuk menyimpan opini publik & mengikuti penonton lain."*
2. Dua **FilterChip**: **"Masuk"** / **"Daftar"** (pilih mode).
3. (mode Daftar) Field **"Nama tampilan"**.
4. Field **"Email"**.
5. Field **"Kata sandi"** (tersembunyi).
6. Tombol utama **"Masuk"** / **"Daftar"** (aktif bila email terisi & sandi ≥ 6).
7. Teks error merah (bila gagal, dipetakan ke Bahasa Indonesia).
8. Teks tombol **"Lupa kata sandi?"** (mode Masuk) → dialog reset.
9. Pemisah "— atau —".
10. Tombol **"Masuk dengan Google"**.
**Dialog:** **"Reset kata sandi"** — teks *"Kami akan mengirim tautan reset ke emailmu."*, field Email, tombol **"Kirim"**/**"Batal"**.
**State:** tombol utama bisa *disabled*; overlay spinner saat proses; error inline.

---

### 4.3 Beranda — "Hari ini nonton apa?" ✅
**Tujuan:** pintu masuk harian.
**TopAppBar:** judul **"Hari ini nonton apa?"**; aksi ikon **Jadwal Rilis** (`CalendarMonth`) & **Feed Komunitas** (`DynamicFeed`).
**Isi (atas→bawah):**
1. **"Sedang ditonton"** — baris horizontal `PosterTile` (badge **"Lanjut"**) untuk serial/film yang berstatus sedang ditonton; tekan → buka catatan cepat.
2. **"Catatan terbaru"** — daftar kartu entri diary (`DiaryRow`): poster, judul, label tipe (Film/Serial), tanggal nonton, rating bintang, emoji/mood, status ("Sedang ditonton" / "Di-drop"), cuplikan catatan (2 baris).
**FAB:** **"Catat"** (tombol melar dengan ikon `+`) → buka Pencarian.
**State:** kosong → *"Belum ada catatan.\nTekan \"Catat\" untuk mulai mencatat tontonanmu."*
**◇ Rencana Beranda (untuk desain yang lebih kaya):** sapaan + ringkasan mingguan ("Minggu ini 3 judul · 6 jam"); **Lanjutkan Menonton** dengan progres `S2 E5/10` + tombol cepat **+1 episode**; **Watchlist teratas**; **Sedang Tren** (TMDB); **Rekomendasi untukmu**; **Throwback** ("Setahun lalu kamu menonton …").

---

### 4.4 Jelajah (Katalog) ✅
**TopAppBar:** judul **"Jelajah"**.
**Isi (atas→bawah):**
1. **Bilah cari** bergaya field: ikon `Search` + placeholder **"Apa yang terakhir kamu tonton?"** → buka layar Pencarian.
2. **Baris katalog dinamis** (per seksi): judul seksi + `LazyRow` `PosterTile` (mis. "Populer", "Tayang Sekarang", "Rating Tertinggi", per genre).
3. Judul seksi **"Semua"**.
4. Daftar vertikal `TitleCard` dengan paging.
**Aksi:** tap poster/kartu → Detail; tombol "Catat cepat" → quick-log.
**State:** loading spinner, error + "Coba lagi", empty.

---

### 4.5 Pencarian ✅
**TopAppBar:** judul **"Cari judul"** + tombol Kembali.
**Isi:**
1. `OutlinedTextField` placeholder **"Cari film, serial, anime…"** (debounce).
2. Daftar hasil `TitleCard`.
**State:** idle → *"Ketik minimal N karakter."*; kosong → *"Tidak ada hasil untuk \"…\"."*; error + "Coba lagi".
**◇ Rencana:** filter chip `Semua · Film · Serial · Anime`, filter lanjutan (tahun/genre/negara/rating), riwayat pencarian.
**Aksi:** tap hasil → Detail; "Catat cepat" → quick-log.

---

### 4.6 Detail Judul ✅
**TopAppBar:** judul = nama judul; aksi: **Bookmark** (`"Tambah ke watchlist"` / `"Hapus dari watchlist"`) + menu **"Tambah ke daftar"**.
**Isi (atas→bawah):**
1. **Backdrop** besar (200 dp) + poster.
2. Judul (headline).
3. Baris meta: tipe (Film/Serial) · tanggal rilis · rating bintang + angka.
4. **Tombol aksi:** **"Catat"** atau **"Catat lagi"** (bila sudah ada entri); (serial) tombol **"Progres"**.
5. Baris status (bila sedang ditonton): *"Sedang ditonton · Musim N · Episode N"*.
6. Genre (" • "), durasi ("120 menit" / "45 menit/episode"), jumlah musim & episode, **sinopsis** (bisa diperluas).
7. Tombol **"Tonton trailer"** (jika tersedia).
8. Seksi **"Pemain"** (baris horizontal).
9. Seksi **"Mirip dengan ini"** (baris poster).
10. Seksi **"Linimasa tontonan"** — kartu entri: tanggal, badge Rewatch, status, rating, "Mood: …", catatan, "Nonton bareng: …", tombol **"Bagikan"** & **"Tonton ulang"**.
**Dialog:** **"Progres episode"** (field Musim & Episode + total; **Simpan**/**Batal**); **"Tambah ke daftar"**; **"Bagikan opini"**.
**State:** loading; gagal (sinopsis cadangan + pesan); kosong → *"Belum ada catatan. Yuk mulai catat tontonanmu."*
**◇ Rencana:** **"Di mana menonton"** (penyedia streaming), **ulasan komunitas**.

---

### 4.7 Form Catat Tontonan / Entri Diary ✅
**TopAppBar:** judul dinamis **"Catat tontonan"** / **"Ubah catatan"** / **"Tonton ulang"**; aksi **Hapus** (saat mengubah).
**Isi (atas→bawah):**
1. Header poster kecil + nama judul.
2. **"Tanggal nonton"** — tombol dengan ikon kalender (default hari ini) → pemilih tanggal.
3. **"Nilai"** — `RatingBar` ½ bintang.
4. **"Status"** — chip: Belum ditonton · Sedang ditonton · Selesai · Ditinggalkan (label).
5. (serial) **"Progres episode"** — field **"Musim"** & **"Episode"**.
6. **"Mood"** — grid chip 3 kolom, mis.: 😂 Ceria · 😭 Mewek · 😱 Tegang · 🥰 Hangat · 😴 Bosan · 🤔 Mikir · 😡 Kesal.
7. Field **"Catatan (opsional)"** (multibaris).
8. Field **"Nonton bareng siapa? (opsional)"**.
9. Tombol **"Simpan"** (melar).
**Dialog:** pemilih tanggal (**"Pilih"**/**"Batal"**).
**◇ Rencana:** tempat/platform (Bioskop, Netflix, …), toggle Suka (♥), tag, dukungan spoiler `||…||`.

---

### 4.8 Quick-Log (bottom sheet) ✅
**Tujuan:** catat super cepat.
**Isi:**
1. Header poster kecil + nama judul.
2. Teks **"Kasih nilai berapa?"**.
3. `RatingBar`.
4. Baris aksi: teks **"Isi lengkap"** + tombol **"Simpan"** (aktif bila rating > 0).
Buka sebagai *ModalBottomSheet* dari katalog.

---

### 4.9 Watchlist ✅
**TopAppBar:** judul **"Watchlist"**.
**Isi:** grid **2 kolom** poster (2:3, judul di bawah). Overlay ikon **"Opsi"** tiap poster → menu **"Tambah ke daftar"** / **"Hapus dari watchlist"**.
**State:** kosong → *"Watchlist kosong.\nTandai judul dengan ikon bookmark di halaman detail."*
**Aksi:** tap poster → isi catatan untuk judul itu.
**◇ Rencana:** tombol **"Acak tontonan"**, prioritas (Tinggi/Biasa), urut.

---

### 4.10 Daftar (koleksi kustom) ✅
**TopAppBar:** judul **"Daftar"**.
**Isi:** daftar kartu: nama daftar (tebal), deskripsi opsional, **"N judul"**, ikon **Hapus**.
**FAB:** **"Buat daftar"** (`+`).
**Dialog:** **"Daftar baru"** — field **"Nama daftar"** + **"Deskripsi (opsional)"**; **Buat**/**Batal**.
**State:** kosong → *"Belum ada daftar.\nBuat daftar seperti \"Film Korea 2024\"."*
**Aksi:** tap kartu → Detail Daftar.

### 4.11 Detail Daftar ✅
**TopAppBar:** judul = nama daftar + Kembali.
**Isi:** daftar baris: poster kecil, judul, label tipe, ikon **Hapus dari daftar**.
**State:** kosong → *"Belum ada judul di daftar ini."*
**◇ Rencana:** urutkan manual (drag), visibilitas publik/pribadi.

### 4.12 Dialog "Tambah ke daftar" ✅
**Isi:** daftar baris dengan **Checkbox** + nama daftar (bisa multi-pilih); bila belum ada daftar → *"Belum ada daftar. Buat daftar baru di bawah."*; field **"Daftar baru"**. Tombol **Simpan**/**Batal**.

---

### 4.13 Statistik ✅
**TopAppBar:** judul **"Stats"** (+ Kembali).
**Isi (atas→bawah):**
1. **Ringkasan** — 3 kartu: **"Judul"**, **"Catatan"**, **"Jam"**.
2. **"Aktivitas 13 minggu terakhir"** — heatmap (13 kolom × 7 baris).
3. **"Genre favorit"** — bar list.
4. **"Asal negara"** — bar list.
**State:** kosong → *"Belum ada data.\nMulai catat tontonan untuk melihat statistikmu."*
**◇ Rencana:** histogram rating, distribusi mood, teman nonton teratas, rekor streak.

---

### 4.14 Rekap Tahunan ("Today Wrapped") ✅
**TopAppBar:** judul **"Rekap Tahunan"** + Kembali.
**Isi:** pemilih tahun (chip, bila > 1 tahun) + **kartu geser story-style** (`HorizontalPager`) + indikator titik.
**Urutan kartu:**
1. **INTRO** — *"Rekap Tontonan"*, angka tahun besar, *"Inilah cerita menontonmu sepanjang tahun."*, petunjuk *"Geser untuk melihat →"*.
2. **TOTALS** — *"Tahun ini kamu menonton"* + jumlah judul berbeda + mini: catatan, jam, (nonton ulang).
3. **GENRES** — *"Genre favoritmu"* + bar + *"Asal negara"*.
4. **MOOD** — *"Perasaanmu paling sering"* + emoji + label mood besar.
5. **TOP_TITLE** — *"Paling sering ditonton"* + poster + *"Ditonton N kali"*.
6. **HIGHEST_RATED** — *"Rating tertinggi"* + judul + bintang + angka.
7. **RHYTHM** — *"Irama menontonmu"* + bulan tersibuk + streak + teman nonton teratas.
8. **OUTRO** — *"Sampai jumpa di rekap berikutnya"* + tombol **"Bagikan rekap"** (bagi teks).
**State:** loading; kosong → *"Belum ada tontonan yang tercatat tahun ini.\nMulai catat tontonanmu untuk membuka rekap."*

---

### 4.15 Jadwal Rilis ✅
**TopAppBar:** judul **"Jadwal Rilis"** + Kembali + aksi **"Segarkan"**.
**Isi (atas→bawah):**
1. **Kartu toggle "Notifikasi jadwal rilis"** — subjudul *"Pengingat episode/film mendatang (sekitar pukul 08.00)"* + **Switch** (bila dinyalakan → minta izin notifikasi).
2. Daftar rilis dikelompokkan per hari: header **"Hari ini"** / **"Besok"** / tanggal; kartu: poster, nama judul, detail episode/film, tanggal (warna primary).
**State:** loading; error; kosong → *"Belum ada jadwal rilis mendatang.\nTambahkan film atau serial ke Watchlist untuk memantau rilisan berikutnya."*
**Aksi:** tap kartu → Detail Judul.

---

### 4.16 Impor / Ekspor ✅
**TopAppBar:** judul **"Impor / Ekspor"** + Kembali.
**Isi (atas→bawah):**
1. Intro: *"Cadangkan diary-mu sebagai berkas CSV, atau pulihkan dari cadangan / impor dari Letterboxd."*
2. Kartu **"Diary saat ini"** + **"N catatan"**.
3. Tombol **"Ekspor ke CSV"** (ikon unduh).
4. Tombol **"Impor dari CSV"** (ikon unggah).
5. Catatan kaki kecil soal format yang didukung (Today & Letterboxd).
**Interaksi:** membuka pemilih berkas sistem (Storage Access Framework). Pesan sukses/gagal lewat snackbar.

---

### 4.17 Feed Komunitas ✅
**TopAppBar:** judul **"Feed Komunitas"** + Kembali.
**Isi (atas→bawah):**
1. **Tab:** **"Untuk Anda"** / **"Mengikuti"**.
2. Indikator progres tipis saat memuat.
3. Daftar kartu opini (`OpinionCard`): nama penulis (+ tombol **"Ikuti"**/**"Berhenti ikuti"**), poster, judul + "Film · tanggal", rating, mood, cuplikan catatan, tag **"Mengandung spoiler"**, baris aksi **Suka** & **Komentar**.
**State kosong:**
- Untuk Anda → *"Belum ada opini publik yang dibagikan.\nJadilah yang pertama membagikan opini dari catatan tontonanmu!"*
- Mengikuti (belum ikut siapa pun) → *"Kamu belum mengikuti siapa pun.\nTemukan opini menarik di tab \"Untuk Anda\" dan ikuti akun lain!"*
- Mengikuti (tidak ada post baru) → *"Belum ada ulasan baru dari akun yang kamu ikuti."*
**Aksi:** tap kartu → Detail Judul; tap nama penulis → Public Profile; ikon komentar → sheet komentar.

---

### 4.18 Opini Saya ✅
**TopAppBar:** judul **"Opini saya"** + Kembali.
**Isi:** daftar `OpinionCard` milik sendiri, dengan ikon **Hapus opini**.
**Dialog:** **"Hapus opini?"** — *"\"<judul>\" akan dihapus dari opini publik."* → **Hapus**/**Batal**.
**State:** kosong → *"Belum ada opini. Buka catatan tontonanmu, lalu pilih \"Bagikan\"."*

### 4.19 Dialog "Bagikan Opini" ✅
**Isi:** header poster + judul + rating + mood; catatan *"Opini ini akan tampil publik dengan namamu."*; field **"Opini (opsional)"** (terisi cuplikan catatan); checkbox **"Mengandung spoiler"**. Tombol **"Bagikan"** (spinner saat proses) / **"Batal"**.

### 4.20 Sheet Komentar ✅
**Isi (atas→bawah):**
1. Header: poster kecil + judul opini + *"Opini oleh <nama>"* + rating.
2. Area komentar (daftar; nama penulis, tanggal, hapus bila milik sendiri).
   - Komentar berspoiler tersembunyi → kartu merah dengan ikon mata tertutup + *"Komentar mengandung spoiler. Ketuk untuk membuka."*; setelah dibuka ada tag **"Spoiler"** + **"Sembunyikan"**.
3. Input: chip **"Spoiler"** (ikon peringatan) + field **"Tulis komentar..."** + tombol kirim.
**State:** kosong → *"Belum ada komentar.\nJadilah yang pertama berkomentar!"*; spinner saat mengirim.

---

### 4.21 Public Profile (profil pengguna lain) ✅
**TopAppBar:** judul = nama pengguna + Kembali.
**Isi (atas→bawah):**
1. **Header profil** — avatar bulat besar, nama, bio; tombol **"Ikuti"**/**"Mengikuti"** (atau chip **"Ini profil Anda"** bila diri sendiri).
2. **"Opini Publik (N)"**.
3. Daftar `OpinionCard`.
**State:** kosong → *"Pengguna ini belum membagikan opini publik."*

---

### 4.22 Circle ✅
**TopAppBar:** judul **"Circle"** + Kembali + aksi ikon **"Gabung circle"** (`GroupAdd`).
**FAB:** **"Buat circle"**.
**Isi:** kartu circle: ikon `Groups`, nama circle, *"Oleh <pemilik> · N anggota"*.
**Dialog:**
- **"Buat circle"** — field **"Nama circle"**, tombol **Buat**.
- **"Gabung circle"** — field **"Kode undangan"**, tombol **Gabung**.
**State:** loading; kosong/error → *"Belum ikut circle apa pun.\nBuat circle baru atau gabung dengan kode undangan temanmu."* (atau pesan error server).

### 4.23 Detail Circle ✅
**TopAppBar:** judul = nama circle + Kembali + aksi **"Bagikan kode"** (`Share`).
**Isi (atas→bawah):**
1. **Kartu kode undangan** — label **"Kode undangan"**, kode besar (warna primary), **"N anggota"**, tombol **"Bagikan kode"**.
2. **"Anggota (N)"** — daftar nama (pemilik ditandai "(pemilik)"; jika kosong → *"Belum ada anggota lain."*).
3. **"Opini anggota"** — daftar `OpinionCard` (suka & komentar); jika kosong → *"Belum ada opini dari anggota circle ini."*
4. Tombol **"Keluar dari circle"**.
**Dialog:** **"Keluar dari circle?"** — *"Kamu akan berhenti melihat opini anggota circle ini."* → **Keluar**/**Batal**.
**State:** loading; *"Circle tidak ditemukan."*.

---

### 4.24 Profil ✅
**TopAppBar:** judul **"Profil"**.
**Isi (atas→bawah):**
1. **Header** — avatar bulat, nama tampilan, email.
2. (mode lihat) bio + tombol **"Ubah profil"**.
3. (mode ubah) field **"Nama tampilan"** + **"Bio"** + tombol **"Simpan"**/**"Batal"**.
4. **Kartu menu** (baris ikon + judul + subjudul):
   - **Feed Komunitas** — *"Opini publik & akun yang kamu ikuti"*
   - **Opini saya** — *"Opini yang kamu bagikan"*
   - **Circle** — *"Klub nonton dengan teman via kode undangan"*
   - **Statistik** — *"Ringkasan tontonanmu"*
   - **Rekap Tahunan** — *"Ringkasan tontonan tahunanmu (wrapped)"*
   - **Impor / Ekspor** — *"Cadangkan atau pulihkan diary (CSV)"*
   - **Keluar** — *"Keluarkan akun dari perangkat ini"*
**State:** overlay spinner saat menyimpan/keluar; pesan via snackbar.
**◇ Rencana:** tombol Pengaturan (Tema, Bahasa, Notifikasi, Tentang & atribusi TMDB), jumlah pengikut.

---

## 5. Alur Pengguna Utama

**A. Catat tontonan cepat (≤ 3 ketukan)**
```
FAB "Catat" → ketik judul → pilih hasil → (quick-log) atur bintang → Simpan
```

**B. Dari penemuan ke diary**
```
Jelajah → cari/pilih judul → Detail → "+ Watchlist"
Watchlist → tap poster → Form Catat → Simpan
```

**C. Tracking serial**
```
Detail/Beranda → "Progres" → set Musim & Episode → Simpan
```

**D. Nonton ulang**
```
Detail Entri → "Tonton ulang" → Form (rating lama jadi pembanding) → Simpan
→ "Linimasa tontonan" diperbarui
```

**E. Bagikan opini (sosial)**
```
Detail Judul → entri → "Bagikan" → (opsional catatan + spoiler) → Bagikan
→ tampil di Feed Komunitas & Profil publik
```

**F. Interaksi komunitas**
```
Feed → Suka / Komentar → (spoiler disembunyikan, ketuk untuk buka)
Feed → tap nama penulis → Public Profile → Ikuti
```

**G. Circle**
```
Profil → Circle → "Buat circle" (dapat kode) ATAU "Gabung circle" (masukkan kode)
→ Detail Circle → bagikan kode → lihat opini anggota
```

---

## 6. State yang Harus Dirancang (tiap layar)

| State | Perilaku desain |
|---|---|
| **Loading** | Skeleton/shimmer (idealnya), bukan spinner penuh. |
| **Kosong** | Ilustrasi lembut + 1 kalimat ajakan + tombol aksi utama. |
| **Error jaringan** | Pesan ramah + tombol **"Coba lagi"**; data lokal tetap tampil. |
| **Offline** | Banner tipis "Mode offline"; fitur diary tetap aktif. |
| **Spoiler** | Teks tersembunyi + masker "Ketuk untuk membuka". |
| **Busy/aksen** | Overlay tipis + spinner; tombol *disabled*. |

**Aksesibilitas:** target sentuh ≥ 48 dp · kontras WCAG AA · `contentDescription` untuk poster & ikon mood · rating bintang bisa dioperasikan TalkBack · tema gelap penuh.

---

## 7. Catatan untuk Desainer (opsional)

- Aplikasi ini menampilkan **banyak poster**; pastikan ada *placeholder* abu yang tenang saat gambar lambat/gagal.
- Elemen pembeda yang layak dibuat "ikonik": **kartu entri diary**, **linimasa rewatch**, **emoji mood**, dan **kartu story Rekap Tahunan**.
- Nada bahasa: ramah, hangat, tidak formal ("Hari ini nonton apa?", "Nonton bareng siapa?").
- Untuk Stitch: sebutkan **ukuran layar Android portrait (mis. 412×915 dp)** dan minta **light + dark** untuk layar inti.

---

## 8. Prompt Siap-Tempel untuk Stitch AI

> **Tips:** mulai dengan **master prompt** (8.1), lalu lanjutkan per layar (8.2 dst.). Sebut setiap kali: "Android mobile app, portrait 412×915, Material 3, bahasa Indonesia, tampilkan light mode."

### 8.1 Master prompt (konteks aplikasi)
```
Design a mobile app UI for "Today", an Indonesian personal movie & TV diary app.
Style: clean, warm, calm like a modern personal notebook at night. Posters are the
hero, neutral background, cool navy-to-lavender palette — navy #070F2B, indigo
#1B1A55, slate #535C91, lavender #9290C3 (primary #1B1A55, star ratings #535C91),
rounded 12-16dp cards, Material 3, Roboto, show light mode. Android portrait.
Bottom navigation with 5 tabs: "Hari ini" (Today icon), "Jelajah" (Explore),
"Watchlist" (Bookmark), "Daftar" (Playlist), "Profil" (Person).
Friendly, casual Indonesian copy. Show empty states as soft illustrations with a
short sentence and one action button.
```

### 8.2 Auth (Masuk / Daftar) — layar turunan §4.2
```
Auth screen titled "Today" (big purple). Subtext: "Diary nonton film, serial, dan
anime. Masuk untuk menyimpan opini publik & mengikuti penonton lain." Two chips:
"Masuk" / "Daftar". Fields "Email", "Kata sandi" (and "Nama tampilan" when Daftar).
Full-width primary button "Masuk"/"Daftar". Link "Lupa kata sandi?".
Divider "atau". Outlined button "Masuk dengan Google".
```

### 8.3 Beranda — layar turunan §4.3
```
Home screen. TopAppBar title "Hari ini nonton apa?" with two action icons:
calendar (Jadwal Rilis) and feed (Feed Komunitas). Below: section "Sedang ditonton"
as a horizontal row of poster cards with a small "Lanjut" badge. Then section
"Catatan terbaru": vertical list of diary cards — small poster, title, type label
(Film/Serial), date, star rating, mood emoji, optional status text, and a 2-line
note excerpt. Add an extended FAB labeled "Catat" with a plus icon.
```

### 8.4 Jelajah + Pencarian — §4.4, §4.5
```
Catalog screen titled "Jelajah". Top: a rounded search field with placeholder
"Apa yang terakhir kamu tonton?". Then horizontal section rows of poster tiles
("Populer", "Tayang Sekarang", "Rating Tertinggi", per genre), and a vertical list
section "Semua" of title cards (poster 80x120, title, type label, TMDB rating with
teal star). Search screen titled "Cari judul" with back arrow and a text field
placeholder "Cari film, serial, anime…" showing a list of title cards.
```

### 8.5 Detail Judul — §4.6
```
Title detail screen. Large backdrop image with poster. Title, then meta row:
type (Film/Serial) · release date · teal star rating. Action buttons: primary
"Catat" and a tonal button "Progres". Show "Sedang ditonton · Musim 2 · Episode 5"
status line. Sections: synopsis, "Pemain" (horizontal cast row), "Mirip dengan ini"
(horizontal poster row), and "Linimasa tontonan" — list of diary cards each with
date, rewatch badge, rating, "Mood:", note, "Nonton bareng:", and small text
buttons "Bagikan" and "Tonton ulang". Top bar has a bookmark icon and an overflow
menu "Tambah ke daftar".
```

### 8.6 Form Catat Tontonan + Quick-log — §4.7, §4.8
```
Diary entry form titled "Catat tontonan". Top: small poster + title. Then label
"Tanggal nonton" with a date button. Label "Nilai" with a 5-star half-star rating
bar (teal). Label "Status" as chips (Belum ditonton, Sedang ditonton, Selesai,
Ditinggalkan). (For series) "Progres episode" with "Musim"/"Episode" fields.
Label "Mood" as a 3-column grid of emoji chips (😄 Ceria, 😢 Mewek, 😨 Tegang,
🥰 Hangat, 😴 Bosan, 🤔 Mikir, 😡 Kesal). Multiline field "Catatan (opsional)" and
field "Nonton bareng siapa? (opsional)". Full-width primary button "Simpan".
Also design a compact bottom sheet for quick logging: poster + title, "Kasih nilai
berapa?", star rating, and buttons "Isi lengkap" + "Simpan".
```

### 8.7 Watchlist + Daftar — §4.9–§4.12
```
Watchlist screen titled "Watchlist": 2-column grid of 2:3 poster cards with title
below and an overflow menu (Tambah ke daftar / Hapus dari watchlist). Empty state:
"Watchlist kosong. Tandai judul dengan ikon bookmark di halaman detail."
Lists screen titled "Daftar": cards with name, optional description and "N judul",
plus a FAB "Buat daftar". Show a "Daftar baru" dialog with "Nama daftar" and
"Deskripsi (opsional)".
```

### 8.8 Statistik + Rekap Tahunan — §4.13, §4.14
```
Stats screen titled "Stats": three summary cards ("Judul", "Catatan", "Jam"), a
contribution heatmap titled "Aktivitas 13 minggu terakhir", and bar lists
"Genre favorit" and "Asal negara".
Recap screen "Rekap Tahunan": a story-style swipeable card deck (like Spotify
Wrapped) with page dots and a year selector: intro "Rekap Tontonan 2026",
"Tahun ini kamu menonton N judul", "Genre favoritmu", "Perasaanmu paling sering",
"Paling sering ditonton", "Rating tertinggi", "Irama menontonmu", and a final page
with a "Bagikan rekap" button.
```

### 8.9 Jadwal Rilis + Impor/Ekspor — §4.15, §4.16
```
Release schedule screen "Jadwal Rilis" with a back arrow and refresh icon. Top: a
toggle card "Notifikasi jadwal rilis" with subtitle "Pengingat episode/film
mendatang (sekitar pukul 08.00)" and a switch. Then list grouped by day with
headers "Hari ini" / "Besok" / date, each row a poster thumbnail, title, episode/
movie detail and date in primary color.
Import/Export screen "Impor / Ekspor": intro text about CSV backup, a card
"Diary saat ini · N catatan", a primary button "Ekspor ke CSV" and an outlined
button "Impor dari CSV", plus a small footnote about supported formats.
```

### 8.10 Feed + Profil Publik + Komentar — §4.17–§4.21
```
Community feed titled "Feed Komunitas" with tabs "Untuk Anda" / "Mengikuti" and a
list of opinion cards. Each opinion card: author name with a "Ikuti" button, small
poster, title + "Film · date", star rating, "Mood:", note excerpt, optional red
tag "Mengandung spoiler", and a bottom row with "Suka" and "Komentar".
Comments bottom sheet: header with poster + title + "Opini oleh <name>", a comment
list, a spoiler toggle chip, a "Tulis komentar..." field and send button. Hidden
spoiler comments appear as a red-tinted card "Komentar mengandung spoiler. Ketuk
untuk membuka."
Public profile titled with the user name: avatar, name, bio, a "Ikuti"/"Mengikuti"
button, and a section "Opini Publik (N)" listing opinion cards.
```

### 8.11 Circle — §4.22, §4.23
```
Circles screen titled "Circle" with a back arrow and a "Gabung circle" action icon
and a FAB "Buat circle". List of circle cards: group icon, circle name, and
"Oleh <owner> · N anggota". Dialogs: "Buat circle" with field "Nama circle";
"Gabung circle" with field "Kode undangan".
Circle detail screen: an invite-code card with label "Kode undangan", a big code,
"N anggota" and a "Bagikan kode" button; section "Anggota (N)" listing member
names; section "Opini anggota" with opinion cards; and a full-width outlined button
"Keluar dari circle".
```

### 8.12 Profil — §4.24
```
Profile screen titled "Profil": avatar, display name and email. A card with menu
rows (icon + title + subtitle): "Feed Komunitas", "Opini saya", "Circle",
"Statistik", "Rekap Tahunan", "Impor / Ekspor", and
"Keluar". Also show an inline edit state with fields "Nama tampilan" and "Bio" and
"Simpan"/"Batal" buttons.
```

---

## 9. Rujukan
- [PRD.md](PRD.md) — visi, fitur per fase, acceptance criteria.
- [DESAIN_APLIKASI.md](DESAIN_APLIKASI.md) — rancangan UX/IA lengkap + rencana fitur.
- [CATATAN_KEPUTUSAN.md](CATATAN_KEPUTUSAN.md) — keputusan desain (K1–K8) & pertanyaan terbuka.
- [ARSITEKTUR.md](ARSITEKTUR.md) — stack & pola teknis.
