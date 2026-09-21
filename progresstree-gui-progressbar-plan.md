# Plan Update: GUI Rework — ProgressTree
### Progress Bar, Tata Letak & Sistem Pagination

**Status:** Draft
**Terkait:** [PRD ProgressTree](./progresstree-prd.md) — tambahan di luar scope rebrand/technical cleanup, murni polish & upgrade GUI

---

## 1. Masalah pada Tampilan Saat Ini

- Progress bar: `[████████░░] 80%` — karakter block Unicode polos, warna solid tanpa gradasi, cuma 10 segmen, kesannya generik/placeholder.
- Tata letak: slot milestone di-hardcode di config (`10,11,12,13,14,15,16,19,21,23,25,28,30,32,34`) — susunannya nggak jelas kebaca sebagai "pohon" (nggak ada garis/koneksi visual antar milestone), dan cuma nampung 15 milestone. Kalau milestone-nya nambah dari 15, nggak ada cara buat nampilin sisanya — nggak ada next page.

---

## 2. Opsi Pendekatan Progress Bar

### Opsi A — Quick Win: Perbaiki Bar Berbasis Teks (lore/chat)
Tetap teks, tapi ganti karakter jadi `▰`/`▱`, pakai gradient warna RGB (Adventure/MiniMessage) alih-alih warna solid, naikkan resolusi ke 20 segmen, dan tampilkan angka mentah di sebelah persen (`1.234 / 5.000 blok — 24%`).
**Effort:** Rendah. **Dependency:** Tidak ada.

### Opsi B — Physical Bar di dalam GUI (slot item)
Baris item di GUI yang terisi progresif pakai stained glass pane warna beda + 1 item marker posisi. Native ke Minecraft, nggak butuh resource pack.
**Effort:** Sedang. **Dependency:** Tidak ada.

### Opsi C — Custom Font / Resource Pack (bitmap bar)
Bar digambar sebagai gambar pixel via custom font resource pack. Paling halus, tapi butuh pipeline resource pack aktif di server.
**Effort:** Tinggi. **Dependency:** Resource pack aktif.

**Rekomendasi:** kombinasi A (lore/chat) + B (dalam GUI). C jadi fase berikutnya kalau network udah/mau pakai resource pack custom.

---

## 3. Rombak Tata Letak GUI

Tujuan: bikin susunan milestone kebaca sebagai pohon beneran (bukan icon yang keliatan random nyebar), dan konsisten di semua halaman.

- **Bentuk tree yang jelas** — susun milestone melebar dari atas ke bawah (misal 1 node di baris atas → 3 node di baris berikutnya → makin melebar), pakai slot filler (glass pane gelap) buat ngasih kesan "cabang" di antara node yang berdekatan.
- **Baris navigasi tetap** — baris paling bawah GUI (bukan bagian dari area milestone) direservasi khusus buat: tombol Previous Page, info/stats pemain, indikator halaman ("Halaman 2/4"), tombol Close/Info, tombol Next Page. Baris ini sama di semua halaman.
- **Template yang reusable** — daripada nge-hardcode slot per milestone kayak sekarang, definisikan satu "template" pola tree yang dipakai ulang tiap halaman, terus milestone tinggal diisi ke situ sesuai urutan.

*(Mockup visual tata letak barunya ada di respons chat.)*

---

## 4. Sistem Pagination / Multi-Page

Tujuan: nggak ada lagi batas 15 milestone — kalau nambah, otomatis kebentuk halaman baru.

- Total halaman dihitung otomatis: `total_milestone ÷ slot_per_halaman` (dibulatin ke atas).
- Tombol Next/Previous: kalau udah di halaman terakhir, tombol Next di-nonaktifin/diubah jadi ikon abu-abu (bukan dihilangin, biar player tau itu halaman terakhir). Sama buat Previous di halaman pertama.
- Indikator halaman nunjukin posisi sekarang, misal "Halaman 2 / 4".
- Urutan milestone di tiap halaman harus konsisten (misal berdasarkan urutan definisi di config, atau tipe+threshold) biar nggak acak tiap kali GUI dibuka.
- Kalau player klaim reward dari halaman 2, GUI re-render tetap di halaman 2 — jangan kebalik ke halaman 1.

### Perubahan config yang dibutuhkan
- `milestone-slots` (list statis) diganti jadi `gui.layout-template` (pola slot yang dipakai berulang tiap halaman).
- Section baru `gui.navigation` buat slot & icon tombol prev/next/page-indicator/close.

---

## 5. Rencana Implementasi

1. **Refactor render logic** — pisahkan progress bar jadi `ProgressBarRenderer`, dan GUI page jadi objek `GuiPage` (index halaman, daftar milestone di halaman itu, method render) — dipakai bareng biar nggak duplikat logic.
2. **Kerjain Opsi A (bar teks)** dulu — paling cepat kelihatan hasilnya.
3. **Implementasi template tree + baris navigasi** sesuai desain di bagian 3.
4. **Implementasi pagination** — hitung total halaman, handle klik next/previous, jaga state halaman aktif per player selama GUI kebuka.
5. **Update parsing config** ke schema baru (`layout-template`, `navigation`), kasih warning/migration note kalau masih ketemu format `milestone-slots` lama.
6. **Testing edge case** — 0 milestone, pas 1 halaman penuh, banyak halaman, klaim dari halaman tengah, GUI reopen setelah pindah halaman.
7. **Preview ke staff/player** sebelum final — terutama buat warna gradient bar dan kejelasan bentuk tree-nya.

---

## 6. Non-Goals

- Tidak mengubah mekanik milestone atau choice-based reward.
- Tidak masuk ke pipeline resource pack (Opsi C) kecuali diputuskan lanjut nanti.
- Tidak nambah fitur jump-to-page atau search/filter milestone — itu enhancement terpisah kalau dibutuhkan ke depannya.

---

## 7. Pertanyaan Terbuka

- Warna brand ProgressTree buat gradient bar itu apa? (belum ada palet resmi)
- Realm-realm AlwiNation/Minegens udah ada yang pakai resource pack custom, atau ini infra baru kalau lanjut ke Opsi C?
- Urutan milestone antar halaman mau berdasarkan apa — urutan config, tipe, atau threshold progress?
