# PRD: Category-Based GUI — ProgressTree
### Hub Kategori, Tree per Kategori & Navigasi Bertingkat

**Status:** Draft
**Owner:** ALtss (Syaaddd)
**Versi target:** 2.1.0 (non-breaking; config lama tetap jalan)
**Terkait:** [PRD ProgressTree](./progresstree-prd.md) · [Plan Update GUI Rework](./progresstree-gui-plan.md)

---

## 1. Latar Belakang

Setelah v2.0.0, plugin punya 7 tipe milestone × sampai 7 level, plus JOIN dan COMMUNITY_PLAYTIME. Total milestone gampang tembus 30–43+. Plan GUI Rework sebelumnya menyelesaikan masalah *bentuk tree* dan *pagination*, tapi semua milestone masih tercampur dalam satu alur halaman: PlayTime, Break, Place, Kill, dan Join berbaur, dipisah cuma oleh nomor halaman.

Masalahnya:

- Pemain nggak bisa langsung "loncat" ke jenis progres yang mereka peduliin (misal cuma mau lihat milestone mining).
- Pagination bersifat linear dan nggak punya makna — halaman 3 isinya apa, pemain harus buka dulu.
- Reward yang siap diklaim bisa terselip di halaman lain tanpa petunjuk.
- Makin banyak milestone ditambah admin, makin berantakan.

Update ini mengubah struktur GUI dari **satu daftar bergulir** menjadi **hub kategori → tree per kategori**.

---

## 2. Tujuan (Goals)

1. Pemain bisa menemukan milestone yang relevan dalam maksimal 2 klik dari `/pt`.
2. Setiap kategori punya tree sendiri yang rapi dan konsisten dengan desain tree di Plan GUI Rework.
3. Pemain langsung tahu **kategori mana yang punya reward siap klaim** tanpa membuka satu per satu.
4. Admin bisa mengatur kategori lewat config (ikon, nama, urutan, isi) tanpa ubah kode.
5. Config v2.0.0 yang ada tetap valid — tanpa `category:` eksplisit, milestone otomatis masuk kategori sesuai `type`.

## 3. Non-Goals

- Tidak mengubah mekanik milestone, tipe milestone, atau choice-based reward.
- Tidak menambah tipe milestone baru.
- Tidak ada search / filter teks / jump-to-page.
- Tidak masuk ke resource pack (Opsi C di Plan GUI Rework).
- Tidak mengubah skema database (kategori murni konsep tampilan, bukan data pemain).

---

## 4. Konsep Utama

### 4.1 Alur Navigasi

```
/pt
 └─ HUB KATEGORI (menu utama)
      ├─ klik kategori → TREE KATEGORI (halaman 1..N)
      │                    ├─ klik milestone → layar pilih reward (choice screen)
      │                    ├─ Prev / Next page (hanya kalau kategori > 1 halaman)
      │                    └─ tombol Back → kembali ke HUB
      └─ Close
```

### 4.2 Pemetaan Kategori Bawaan (Default)

Ada dua opsi pengelompokan. **Rekomendasi: Opsi 1.**

**Opsi 1 — 1 tipe = 1 kategori (7 kategori)**

| Kategori | Tipe | Ikon default |
|---|---|---|
| Playtime | `PLAYTIME` | `CLOCK` |
| Mining | `BLOCK_BREAK` | `DIAMOND_PICKAXE` |
| Building | `BLOCK_PLACE` | `BRICKS` |
| Hunting | `MOB_KILL` | `ZOMBIE_HEAD` |
| Combat (PvP) | `PLAYER_KILL` | `IRON_SWORD` |
| Loyalty | `JOIN` | `CAMPFIRE` |
| Community | `COMMUNITY_PLAYTIME` | `BEACON` |

Kelebihan: 7 kategori pas muat dalam **satu baris hub (slot 10–16)** dan tiap kategori (7 level) pas muat dalam **satu halaman tree**, jadi pagination praktis nggak kepakai untuk config bawaan.

**Opsi 2 — Super-kategori (4 grup)**

| Grup | Isi |
|---|---|
| Aktivitas | PLAYTIME, JOIN |
| Gathering | BLOCK_BREAK, BLOCK_PLACE |
| Combat | MOB_KILL, PLAYER_KILL |
| Community | COMMUNITY_PLAYTIME |

Kelebihan: hub lebih ringkas. Kekurangan: tiap grup isinya 14+ milestone → butuh pagination lagi, dan campuran dua tipe dalam satu tree lebih susah dibaca. Bisa dicapai admin sendiri lewat custom category (lihat 4.3), jadi nggak perlu jadi default.

### 4.3 Custom Category

Field `category:` opsional di tiap milestone. Kalau kosong → kategori default dari `type`. Kalau diisi → milestone masuk kategori itu, dan kategori custom otomatis muncul di hub sesuai definisinya di `gui.categories`. Ini memungkinkan admin bikin, misalnya, kategori "Event" atau "Rank-Up" tanpa update plugin.

---

## 5. Desain GUI

### 5.1 Hub Kategori (3–4 baris)

```
Baris 1:  ░ ░ ░ ░ ░ ░ ░ ░ ░       ← border/filler
Baris 2:  ░ [Play][Mine][Build][Hunt][PvP][Loyal][Comm] ░
Baris 3:  ░ ░ ░ ░ ░ ░ ░ ░ ░
Baris 4:  ░ ░ ░ [Stats] ░ [Close] ░ ░ ░
```

Setiap ikon kategori menampilkan di lore:

- Nama + deskripsi singkat kategori
- **Ringkasan klaim:** `Diklaim: 3 / 7`
- **Progress ke milestone berikutnya** (pakai `ProgressBarRenderer` dari Plan GUI Rework): `Next: 10.000 blok ▰▰▰▰▰▰▱▱▱▱ 62%`
- **Status badge:**
  - 🟢 `N reward siap diklaim!` → item diberi **enchant glow** + nama highlight
  - 🟡 Sedang berjalan
  - ✅ `Semua selesai` (kategori tuntas)
  - 🔒 Belum ada milestone terbuka (jarang, opsional)
- Hint klik: `Klik untuk membuka`

Ikon **Stats** (kepala pemain) merangkum total: total klaim, total milestone, kategori terlengkap.

### 5.2 Tree Kategori

Memakai template tree dari Plan GUI Rework (1 node → 3 node → melebar, filler glass pane sebagai cabang), dengan tambahan:

- **Judul GUI dinamis:** `&8ProgressTree » &bMining` (nama kategori masuk judul).
- **Baris navigasi bawah** (tetap sama di semua halaman):
  `[◀ Prev] [Back ke Hub] [Info: Mining 3/7 · Hal 1/1] [Close] [Next ▶]`
- Tombol **Back ke Hub** selalu ada, terpisah dari Prev/Next.
- Kalau kategori hanya 1 halaman, Prev/Next jadi ikon abu-abu (perilaku sama dengan Plan GUI Rework).
- Urutan node: **berdasarkan `amount` naik** dalam kategori (level 1 di puncak tree → level tertinggi di bawah), bukan urutan definisi config — jadi kalau admin nambah milestone di tengah file, posisinya tetap logis.

### 5.3 Layar Pilih Reward

Tidak berubah secara fungsional. Tambahan kecil: tombol **Back** kembali ke **halaman & kategori asal**, bukan ke halaman 1.

### 5.4 Kategori Kosong

Kategori tanpa milestone (misal admin menghapus semua milestone COMMUNITY) **disembunyikan otomatis** dari hub, bukan ditampilkan kosong. Opsi `hide-empty: true` di config (default true).

---

## 6. Saran Tambahan (Di Luar Permintaan, Tapi Direkomendasikan)

Urut dari yang paling worth-it:

1. **Indikator "siap klaim" di hub** — sudah masuk desain 5.1. Ini fitur paling berdampak untuk retensi: pemain nggak perlu buka semua kategori buat cek klaim.
2. **Ingat state terakhir** — simpan kategori + halaman terakhir yang dibuka per pemain (in-memory, hilang saat logout) supaya setelah klaim, GUI re-render di tempat yang sama. Ini melanjutkan aturan "klaim di halaman 2 tetap di halaman 2" dari Plan GUI Rework, sekarang mencakup kategori.
3. **Placeholder baru per kategori** — contoh `%progresstree_claimable_mining%` dan `%progresstree_claimable_total%` untuk scoreboard/hologram/tab ("🎁 3 reward menunggu!").
4. **Notifikasi berbasis kategori** — pesan notif milestone selesai menyebut kategori: `Milestone Mining selesai! Buka /pt untuk klaim.` Tetap lewat jalur `ProgressUpdateEvent` yang sudah ada dan idempoten per (player, milestone) — jangan bikin jalur notifikasi baru.
5. **Permission per kategori (opsional)** — `progresstree.category.<id>` untuk menyembunyikan kategori tertentu per rank/event. Default: semua orang boleh, jadi nggak breaking.
6. **Shortcut command** — `/pt <kategori>` buka langsung tree kategori itu (lewati hub), plus tab-complete. Cocok untuk NPC/menu server lain (DeluxeMenus tinggal jalankan command).
7. **Progress bar di hub memakai satu gaya** — pakai renderer yang sama dengan lore milestone supaya konsisten, gradient mengikuti warna kategori.

Yang **sengaja tidak disarankan** sekarang: tab/filter status (semua/siap klaim/selesai). Menarik, tapi menambah state GUI dan masuk kategori "fitur baru"; lebih baik ditunda sampai hub kategori terbukti dipakai.

---

## 7. Perubahan Config

### 7.1 Section baru `gui.categories`

```yaml
gui:
  hub:
    title: "&8ProgressTree"
    rows: 4
    hide-empty: true
    filler: BLACK_STAINED_GLASS_PANE
    # slot ikon kategori mengikuti urutan `order`
    category-slots: [10, 11, 12, 13, 14, 15, 16]

  categories:
    playtime:
      name: "&eplaytime"
      icon: CLOCK
      color: "&e"
      order: 1
      description:
        - "&7Bermain lebih lama, dapat lebih banyak."
      types: [PLAYTIME]          # tipe yang otomatis masuk kategori ini
    mining:
      name: "&bMining"
      icon: DIAMOND_PICKAXE
      color: "&b"
      order: 2
      description:
        - "&7Hancurkan blok, kumpulkan hadiah."
      types: [BLOCK_BREAK]
    building:
      name: "&6Building"
      icon: BRICKS
      color: "&6"
      order: 3
      types: [BLOCK_PLACE]
    hunting:
      name: "&aHunting"
      icon: ZOMBIE_HEAD
      color: "&a"
      order: 4
      types: [MOB_KILL]
    combat:
      name: "&cCombat"
      icon: IRON_SWORD
      color: "&c"
      order: 5
      types: [PLAYER_KILL]
    loyalty:
      name: "&dLoyalty"
      icon: CAMPFIRE
      color: "&d"
      order: 6
      types: [JOIN]
    community:
      name: "&5Community"
      icon: BEACON
      color: "&5"
      order: 7
      types: [COMMUNITY_PLAYTIME]
```

### 7.2 Override per milestone (opsional)

```yaml
milestones:
  Mining_Event_1:
    type: BLOCK_BREAK
    category: event          # override: masuk kategori custom "event"
    amount: 5000
    ...
```

### 7.3 Aturan resolusi kategori

1. Kalau milestone punya `category:` → pakai itu (harus ada di `gui.categories`, kalau tidak → warning di log + fallback ke kategori dari `type`).
2. Kalau tidak → cari kategori yang `types:`-nya memuat `type` milestone itu.
3. Kalau tetap tidak ketemu → masuk kategori fallback `other` ("Lainnya") supaya nggak ada milestone yang hilang dari GUI.

### 7.4 Kompatibilitas

- Config v2.0.0 **tanpa** section `gui.categories` → plugin memakai pemetaan bawaan (Opsi 1) dan menulis section default saat first boot/`reload`, tanpa menimpa perubahan admin.
- `layout-template` dan `navigation` dari Plan GUI Rework tetap dipakai; ditambah key `back-button` di `gui.navigation`.
- Log satu kali saat startup kalau ketemu `milestone-slots` lama (sudah direncanakan di Plan GUI Rework).

---

## 8. Arsitektur & Perubahan Kode

Melanjutkan refactor `ProgressBarRenderer` / `GuiPage` dari Plan GUI Rework:

| Komponen | Peran |
|---|---|
| `Category` (model) | id, nama, ikon, warna, order, daftar tipe, daftar milestone |
| `CategoryRegistry` | Load & validasi `gui.categories`, resolusi kategori milestone (aturan 7.3), dibangun ulang saat `/pt reload` |
| `CategorySummary` | Hitung per pemain: total, diklaim, siap klaim, milestone berikutnya + progress. Dihitung on-demand dari data yang sudah ada di cache. |
| `HubGui` | Render hub dari `CategoryRegistry` + `CategorySummary` |
| `GuiPage` (existing) | Ditambah field `categoryId`; pagination dihitung **per kategori** |
| `GuiSession` | State in-memory per pemain: layar aktif (hub/tree/choice), kategori, halaman |
| Click handler | Route klik berdasarkan `GuiSession`, bukan mencocokkan judul inventory (lebih aman dari bug title-matching di v1.0.2) |

Catatan penting:

- `CategorySummary` **tidak boleh** query database di main thread. Ambil dari cache pemain yang sudah di-load async; kalau data masih loading, hub menampilkan state "memuat…" (pakai message key `data-loading` yang sudah ada).
- Ringkasan direfresh saat GUI dibuka dan saat klaim selesai; nggak perlu polling.

---

## 9. Rencana Implementasi

| Fase | Isi |
|---|---|
| 1. Model & config | `Category`, `CategoryRegistry`, parsing `gui.categories`, resolusi kategori, validasi + log jelas untuk config salah |
| 2. Summary | `CategorySummary` (klaim, siap klaim, next milestone, progress) |
| 3. Hub GUI | `HubGui`, badge, glow untuk kategori siap klaim |
| 4. Tree per kategori | Sambungkan `GuiPage` ke kategori, sorting by amount, judul dinamis, tombol Back |
| 5. Session state | `GuiSession`, ingat kategori+halaman, re-render setelah klaim |
| 6. Ekstra | Placeholder claimable, `/pt <kategori>`, tab-complete, notif berkategori, permission per kategori |
| 7. Testing & preview | Edge case (bagian 10), preview ke staff sebelum rilis |

Urutan ini sengaja: fase 1–5 sudah menghasilkan fitur utuh yang bisa dirilis; fase 6 bisa dipisah ke 2.1.1 kalau waktu mepet.

---

## 10. Edge Case yang Wajib Dites

- Config v2.0.0 lama tanpa `gui.categories` → GUI tetap jalan dengan default.
- Milestone dengan `category:` yang tidak terdaftar → warning + fallback.
- Kategori kosong (hide-empty true/false).
- Kategori dengan tepat 1 halaman penuh, dan 1 milestone lebih dari itu (jadi 2 halaman).
- Pemain klaim dari halaman 2 kategori X → GUI kembali ke halaman 2 kategori X.
- Semua reward dalam satu kategori diklaim → badge "Semua selesai" tampil.
- Data pemain belum selesai load saat `/pt` dibuka.
- `/pt reload` ketika ada pemain sedang membuka GUI (session harus aman, misal tutup atau rebuild).
- Kategori custom lebih banyak dari jumlah slot di hub (`category-slots`) → log warning, sisanya dipotong atau hub otomatis nambah baris.
- COMMUNITY_PLAYTIME (progres global, bukan per pemain) tetap tampil benar di summary.
- Klik cepat/spam di hub & tree (tidak boleh double-open atau duplikat klaim).
- Nilai `amount` sama antar milestone dalam satu kategori (urutan sorting harus stabil).

---

## 11. Acceptance Criteria

- `/pt` membuka hub kategori; tiap kategori terbuka ke tree-nya dalam 1 klik dan bisa kembali dengan Back.
- Hub menampilkan jumlah diklaim, progress ke milestone berikutnya, dan penanda siap klaim yang akurat untuk tiap kategori.
- Milestone selalu muncul di kategori yang benar sesuai aturan resolusi; tidak ada milestone yang hilang dari GUI.
- Config lama tanpa `gui.categories` berjalan tanpa error dan tanpa perubahan perilaku fungsional.
- Tidak ada perubahan pada skema database dan data pemain (tidak perlu migrasi).
- Tidak ada query DB di main thread dari kode GUI baru.
- Klaim dari kategori/halaman mana pun me-render ulang di kategori/halaman yang sama.
- Tidak ada regresi pada progress bar, choice screen, pagination, dan notifikasi dari 2.0.0.

---

## 12. Rollout

1. Test di server staging dengan config bawaan dan config lama v2.0.0.
2. Preview ke staff: fokus pada keterbacaan hub, warna kategori, dan kejelasan badge siap klaim.
3. Soft rollout ke satu realm terkecil, pantau log (warning kategori/config).
4. Full rollout ke seluruh realm AlwiNation/Minegens.

Karena tidak ada perubahan data dan config lama tetap kompatibel, rollback cukup mengganti jar ke 2.0.0.

---

## 13. Pertanyaan Terbuka

- Opsi 1 (7 kategori) atau Opsi 2 (4 grup) sebagai default bawaan? *(rekomendasi: Opsi 1)*
- Palet warna kategori di atas cukup, atau mau disamakan dengan warna brand ProgressTree (pertanyaan terbuka yang sama di Plan GUI Rework)?
- Kategori Community: ditampilkan dengan gaya berbeda (misal ikon/warna khusus + label "Server-wide") karena progresnya kolektif?
- Perlu fase 6 (placeholder, `/pt <kategori>`, permission per kategori) ikut di 2.1.0, atau dipisah ke 2.1.1?
- Apakah kategori tuntas sebaiknya tetap tampil di hub, atau digeser ke urutan paling belakang?
