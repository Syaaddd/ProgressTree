# PRD: ProgressTree
### Rebrand & Technical Cleanup dari MilestoneMP

**Status:** Draft
**Owner:** ALtss (Syaaddd)
**Versi target:** 2.0.0 (breaking rename dari MilestoneMP v1.0.7)

---

## 1. Latar Belakang

MilestoneMP adalah plugin progression tree di mana pemain mengumpulkan progress (playtime, block break/place, mob kill, PvP kill, login streak, total server playtime) dan memilih sendiri reward yang ingin diklaim dari beberapa pilihan per milestone. Plugin ini sudah berjalan sampai v1.0.7, tapi changelog-nya menunjukkan beberapa patch reaktif (notifikasi tidak muncul, choices gagal ke-load dari config, player data loading dibikin synchronous, notifikasi spam, debug log ketinggalan di production) — indikasi ada celah arsitektur yang lebih baik dibenerin dari akar daripada ditambal lagi.

Rework ini adalah **rebrand total ke nama ProgressTree**, dibarengi **technical cleanup** menyeluruh. Fitur dan mekanik inti **tidak berubah** — fokusnya betulin fondasi, bukan nambah fitur baru.

---

## 2. Tujuan (Goals)

1. Ganti seluruh identitas plugin (nama, command, permission, placeholder, config) dari MilestoneMP → ProgressTree secara konsisten.
2. Perbaiki akar penyebab bug-bug yang selama ini ditambal reaktif di changelog v1.0.5–1.0.7.
3. Pastikan data pemain yang sudah ada di server (progress & histori claim) **tidak hilang** saat migrasi ke nama/skema baru.
4. Codebase lebih gampang dirawat & di-debug ke depannya (logging, struktur event, validasi config).

## 3. Non-Goals (Di Luar Scope)

- Tidak menambah milestone type baru.
- Tidak mengubah mekanik choice-based reward.
- Tidak redesign GUI/progression tree secara visual (layout slot, warna status tetap sama secara fungsional).
- Tidak menambah fitur baru (leaderboard, event musiman, dll) — itu masuk fase rework berikutnya kalau dibutuhkan.

---

## 4. Rebranding — Pemetaan Identitas

| Elemen | MilestoneMP (lama) | ProgressTree (baru) |
|---|---|---|
| Command utama | `/milestone` | `/progresstree` |
| Alias command | `/ms`, `/mp` | `/pt`, `/ptree` |
| Subcommand | `open`, `check`, `claim <id>`, `help`, `reload` | sama, tidak berubah |
| Permission prefix | `milestonemp.*` | `progresstree.*` |
| Permission nodes | `.open`, `.claim`, `.check`, `.admin` | sama, cuma prefix yang ganti |
| Placeholder prefix | `%milestone_mp_*%` | `%progresstree_*%` |
| Prefix pesan chat | `&8[&6Milestone&8] ` | `&8[&b ProgressTree&8] ` *(warna bisa disesuaikan)* |
| Nama database default | `milestoneMP` | `progresstree` |
| Folder data plugin | `plugins/MilestoneMP/` | `plugins/ProgressTree/` |
| Judul GUI | `&8Progression Tree` | tetap, atau `&8ProgressTree` biar konsisten branding |

Catatan: semua 11 placeholder (`current`, `next`, `progress`, `playtime`, `blocks_broken`, `blocks_placed`, `mobs_killed`, `players_killed`, `join_days`, `community_playtime`, `can_claim`) ikut pola prefix baru, contoh `%progresstree_current%`.

---

## 5. Technical Cleanup — Perbaikan Akar Masalah

Ini bagian inti dari "rework besar-besaran"-nya. Tiap item merujuk ke pola bug yang sempat muncul di changelog lama:

| Masalah lama | Root cause dugaan | Perbaikan yang diusulkan |
|---|---|---|
| Notifikasi milestone tidak muncul di beberapa jenis aktivitas (v1.0.7) | Logika notifikasi kemungkinan tidak seragam antar trigger (playtime vs block break vs mob kill, dst) | Satukan semua trigger lewat satu internal event (`ProgressUpdateEvent`) supaya jalur completion & notifikasi sama persis buat semua tipe milestone |
| Player data loading dibikin synchronous (v1.0.6) | Kemungkinan race condition saat data belum siap dipakai | Balik ke async load, tapi dengan state "loading" per player + queue aksi yang nunggu sampai data siap, bukan blocking main thread |
| Choices gagal ke-load dari config (v1.0.6) | Parsing YAML list yang rapuh | Tambah validasi skema config saat startup — kalau ada milestone/choice yang gagal parse, log jelas nama milestone-nya, jangan silent fail |
| Notifikasi spam, ditambal jadi "cuma notify sekali pas join" (v1.0.6) | Tidak ada tracking idempoten per (player, milestone) | Simpan flag "sudah dinotif" per player-milestone di data layer, bukan tempel di logic join |
| Debug log dihapus total dari production (v1.0.6) | Tidak ada leveled logging | Tambah `debug: false` di config, log tetap ada tapi cuma nongol kalau debug diaktifkan |

Tambahan cleanup umum:
- Rename seluruh class, package, dan config key internal supaya konsisten sama nama baru (hindari sisa referensi "Milestone"/"MP" di kode kalau memang mau bersih total).
- Bump versi ke **2.0.0** karena ini breaking change (rename command, permission, placeholder).

---

## 6. Migrasi Data

Karena plugin ini sudah jalan di server AlwiNation/Minegens dengan data pemain live, migrasi **wajib lossless**:

1. Deteksi otomatis saat startup: kalau ada data lama dengan skema MilestoneMP tapi belum ada data ProgressTree, tawarkan migrasi.
2. Backup otomatis (copy file SQLite / dump tabel MySQL) sebelum migrasi jalan.
3. Command manual sebagai fallback: `/progresstree migrate` (admin only) — buat kasus migrasi otomatis gagal atau mau dijalankan terkontrol.
4. Setelah migrasi sukses, tampilkan ringkasan di console (jumlah player, jumlah progress record, jumlah claim yang berhasil dipindah).

---

## 7. Functional Requirements (Fitur yang Dipertahankan)

Semua ini harus tetap identik secara fungsional ke pemain, cuma beda "kulit":

- Progression Tree GUI dengan status locked/available/claimed
- Choice-based rewards (pemain pilih dari beberapa opsi reward)
- 7 tipe milestone: PLAYTIME, BLOCK_BREAK, BLOCK_PLACE, MOB_KILL, PLAYER_KILL, JOIN, COMMUNITY_PLAYTIME
- Reward dieksekusi sebagai console command dengan placeholder `{player}`
- Dukungan SQLite & MySQL
- Integrasi PlaceholderAPI
- Broadcast community reward (opsional, sesuai config)

---

## 8. Rollout Plan

| Fase | Isi |
|---|---|
| 1. Rebrand core | Rename identifier (command, permission, placeholder, package, config keys) |
| 2. Technical cleanup | Implementasi perbaikan di bagian 5 |
| 3. Migrasi & testing | Jalankan migrasi di server staging/testing dulu, bukan langsung ke live realm |
| 4. Soft rollout | Deploy ke satu realm dulu (misal yang paling kecil trafficnya) buat pantau error log |
| 5. Full rollout | Deploy ke seluruh realm AlwiNation/Minegens, retire MilestoneMP lama |

---

## 9. Acceptance Criteria

- Plugin start tanpa error dengan nama & identifier baru di semua realm.
- Data progress & claim history pemain lama 100% termigrasi, tidak ada yang hilang.
- Kelima bug pattern di bagian 5 tidak muncul lagi setelah cleanup (perlu diverifikasi manual per kasus).
- Semua command, permission, dan placeholder baru berfungsi sesuai pemetaan di bagian 4.
- Tidak ada regresi fungsional dibanding MilestoneMP v1.0.7.

---

## 10. Open Questions

- Command lama (`/milestone`, `/ms`, `/mp`) mau langsung dimatikan, atau dipertahankan sementara sebagai alias deprecated dengan warning ke player/admin?
- Nama package Java final mau apa (perlu disesuaikan sama namespace repo yang dipakai)?
- Timeline rollout — mau dikejar cepat, atau nunggu testing menyeluruh dulu di server staging?
