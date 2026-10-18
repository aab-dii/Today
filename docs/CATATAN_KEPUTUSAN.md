# Today — Catatan Keputusan Desain

> Hasil brainstorming tentang layar awal, katalog, detail judul, dan alur mencatat.
> Pelengkap dari `DESAIN_APLIKASI.md`. Jika ada yang bertentangan, **catatan ini yang lebih baru**.
> Terakhir diperbarui: 16 Okt 2026

Legenda: ✅ diputuskan, kerjakan sekarang · ⏸ ditunda (jangan dikerjakan dulu) · ❓ belum diputuskan

---

## 1. Keputusan yang Dikerjakan Sekarang

### ✅ K1 — Alur dasar: Katalog → Detail → Aksi
Pengguna melihat katalog, mengetuk judul, masuk ke Detail Judul, lalu memilih aksi (Catat / Watchlist / Daftar).
**Alasan:** pola standar aplikasi sejenis (Letterboxd, Trakt), mudah dipahami, dan komponen katalog + detail sudah ada di `:core`.

### ✅ K2 — Katalog dikurasi untuk penonton Indonesia, bukan hanya "Trending"
TMDB trending bersifat global dan condong ke Hollywood, sementara target pengguna banyak menonton drama Korea, anime, dan film lokal.

Baris katalog yang dipakai di tab Jelajah:

| Baris | Sumber / filter TMDB |
|---|---|
| Sedang tren minggu ini | `trending/all/week` |
| Drama Korea populer | discover `tv`, `with_origin_country=KR`, sort popularity |
| Anime musim ini | discover `tv`, `with_genres=16`, `with_origin_country=JP` |
| Film Indonesia | discover `movie`, `with_origin_country=ID` |
| Tayang sekarang di bioskop | `movie/now_playing`, `region=ID` |
| Rating tertinggi | `top_rated` |

### ✅ K3 — Kolom cari harus menonjol, di atas katalog
**Alasan:** kebanyakan orang mencatat judul yang *sudah* mereka tonton, dan judul itu jarang muncul di trending. Katalog melayani orang yang mencari tontonan; kolom cari melayani orang yang ingin mencatat.
Teks placeholder: **"Apa yang terakhir kamu tonton?"**

### ✅ K4 — Susunan Halaman Detail Judul
Urutan dari atas ke bawah, agar tombol utama tidak tenggelam:

1. Backdrop + poster, judul, tahun, durasi / jumlah musim, rating TMDB, genre
2. **Tombol aksi** (lihat K5)
3. Sinopsis (dapat diperluas)
4. Tombol **"Tonton trailer"** (lihat K6)
5. Episode & musim (hanya serial, dimuat saat bagian dibuka)
6. Pemeran
7. Judul mirip

Penyedia streaming ("di mana menonton") ditambahkan setelah susunan ini stabil.

### ✅ K5 — Tombol aksi sadar status
| Kondisi judul | Tombol utama | Tombol sekunder |
|---|---|---|
| Belum di mana-mana | **Catat** | + Watchlist, + Daftar |
| Ada di watchlist | **Catat** (otomatis keluar dari watchlist setelah disimpan) | ✓ Di watchlist (ketuk untuk hapus) |
| Sudah pernah dicatat | **Catat lagi** (rewatch) | Lihat entri |

Tombol utama selalu **Catat**, karena itu pembeda produk. Watchlist adalah aksi sekunder.

### ✅ K6 — Trailer: sederhana dulu
Hanya tombol yang membuka YouTube lewat intent (kunci video dari endpoint `videos` TMDB). Tidak ada pemutar tertanam pada versi awal.
**Alasan:** pemutar tertanam menambah dependensi dan kompleksitas, sementara trailer bukan inti diary.

### ✅ K7 — Jalan pintas mencatat dari kartu judul
Tiap kartu judul di katalog dan hasil pencarian memiliki ikon ✓ kecil yang langsung membuka form catat. Halaman detail tetap menjadi jalur utama; ikon ini untuk pengguna yang sudah tahu persis apa yang mereka tonton.

### ✅ K8 — Dua cara mencatat
- **Catat kilat:** pilih judul → bintang → Simpan. Mood, teman nonton, dan catatan dilengkapi belakangan lewat Detail Entri.
- **Catat lengkap:** form penuh (tanggal, rating, mood, nonton bareng, catatan, platform).

Form dibuka sebagai *bottom sheet*, bukan layar baru.

---

## 2. Keputusan yang Ditunda ⏸

| Item | Alasan ditunda | Dipikirkan lagi saat |
|---|---|---|
| Pemutar trailer tertanam | Tidak inti; menambah dependensi | Ada bukti trailer sering dibuka |
| Rekomendasi personal di Beranda | Butuh cukup data diary pengguna | Pengguna rata-rata punya ≥ 20 entri |
| Penyedia streaming | Perlu dicek kelengkapan data untuk Indonesia | Detail Judul sudah stabil |
| Fitur sosial & backend | Mahal; baru berharga jika ada pengguna aktif | Diary MVP dipakai nyata oleh sekelompok penguji |
| Tag bebas pada entri | Bukan kebutuhan awal | Setelah mood + nonton bareng terbukti dipakai |

---

## 3. Pertanyaan Terbuka ❓

### ❓ Q1 — Layar awal: cari dulu atau katalog dulu?
Ini keputusan terpenting yang belum final. Kuncinya satu pertanyaan:

> **Apakah pengguna lebih sering mencatat sesudah menonton, atau saat sedang mencari tontonan?**

- Jika **sesudah menonton** → kolom cari menjadi elemen paling dominan, katalog di bawahnya.
- Jika **mencari tontonan** → katalog dengan baris kurasi menjadi pusat, kolom cari tetap di atas.

K3 menetapkan kolom cari selalu menonjol; yang belum diputuskan hanyalah seberapa dominan dibanding katalog.
**Cara memutuskan:** tunjukkan dua versi ke 5–10 calon pengguna, atau pantau perilaku di build uji.

### ❓ Q2 — Beranda atau Diary sebagai layar awal pengguna lama?
Condong ke **Beranda** selama ada "Lanjutkan Menonton". Jika mayoritas pengguna lebih banyak menonton film daripada serial, Diary bisa lebih masuk akal.

### ❓ Q3 — Anime: tab khusus atau cukup baris di katalog?
Saat ini hanya baris "Anime musim ini" (K2). Perlu data penggunaan sebelum memberi tab sendiri.

### ✅ Q4 — Nama aplikasi
**Diputuskan (16 Okt 2026)**: nama final **Today**; package/app ID **`com.today.app`** (rename P3.5 sudah dijalankan). "DramaK" hanya nama pengembangan lama.

---

## 4. Langkah Berikutnya (urutan kerja)

1. Menjalankan aplikasi di emulator/perangkat dan mencatat semua yang rusak (README: *belum diuji di device*).
2. Menerapkan K2 (baris katalog kurasi) dan K3 (kolom cari menonjol) di tab Jelajah.
3. Menyusun ulang Detail Judul sesuai K4 dan K5.
4. Menambah tombol trailer (K6) dan ikon catat cepat di kartu (K7).
5. Membuat Catat kilat (K8) dan menguji kecepatannya: target ≤ 3 ketukan.
6. Mengumpulkan umpan balik 5–10 penguji untuk menjawab Q1 dan Q2.
