# AGENTS.md — Cafe Order & Kitchen Queue System (Backend)

> File ini dibaca otomatis oleh Antigravity di setiap awal sesi/tugas. Kalau ada instruksi di sini yang bertentangan dengan permintaan chat sesaat, **file ini yang menang** kecuali user eksplisit bilang "abaikan AGENTS.md untuk ini".

---

## 0. ATURAN WAJIB SEBELUM MENYUSUN PLAN APAPUN

Sebelum menampilkan "Proposed Changes" untuk Phase manapun (baru atau revisi), WAJIB:

1. Baca ulang seluruh checklist di bagian **§2 Konvensi Wajib** di bawah — jangan mengandalkan ingatan dari percakapan sebelumnya, karena detail sering hilang seiring context makin panjang.
2. Cocokkan rencana terhadap checklist itu **satu per satu**.
3. Di akhir setiap "Proposed Changes", tambahkan bagian **"Self-Check"** berisi:
   - Poin konvensi yang sudah dipastikan terpenuhi
   - Poin yang masih meragukan/belum yakin, dan butuh konfirmasi user
4. JANGAN lanjut mengeksekusi (jangan tunggu "Proceed" saja) kalau ada poin Self-Check yang statusnya "belum yakin" — tanyakan dulu secara eksplisit ke user.

---

## 1. TECH STACK (terkunci, jangan diganti diam-diam)

- Java 21, Spring Boot 3.3.x, Maven
- PostgreSQL 15 via **Spring Data R2DBC** (reactive, bukan JPA blocking)
- MongoDB 6 via **Spring Data MongoDB Reactive**
- Redis 7 via **Spring Data Redis Reactive**
- Kafka/Redpanda untuk event-driven
- Project Reactor (`Mono`/`Flux`) di semua layer — controller, service, repository
- Flyway untuk migrasi schema Postgres (bukan `ddl-auto`)
- Kalau ada dependency/versi yang deprecated atau bermasalah saat dikerjakan: **beri tahu user dan usulkan alternatif, jangan diam-diam mengganti**.

---

## 2. KONVENSI WAJIB (checklist — cek satu-satu tiap Phase)

### 2.1 Tabel PostgreSQL — WAJIB ada di SETIAP tabel baru
```sql
id                UUID PRIMARY KEY DEFAULT gen_random_uuid()
created_by        VARCHAR(255) NOT NULL DEFAULT 'SYSTEM'
created_date      TIMESTAMP NOT NULL DEFAULT now()
updated_by        VARCHAR(255) NOT NULL DEFAULT 'SYSTEM'
updated_date      TIMESTAMP NOT NULL DEFAULT now()
mark_for_delete   BOOLEAN NOT NULL DEFAULT false
optlock           BIGINT NOT NULL DEFAULT 0
```
- Delete = `UPDATE mark_for_delete = true`, BUKAN `DELETE FROM`.
- Semua SELECT default harus `WHERE mark_for_delete = false`.
- Kolom yang isinya bisa "sampah/tidak valid" dari sumber eksternal (misal CSV upload) HARUS bertipe `VARCHAR`, bukan `UUID`/tipe ketat lain — biar baris invalid tetap bisa disimpan sebagai catatan anomali, bukan gagal insert. (Pelajaran dari kasus `order_id_raw` di reconciliation.)

### 2.2 Dokumen MongoDB — WAJIB ada di SETIAP collection baru
```json
{
  "createdBy": "SYSTEM",
  "createdDate": "...",
  "lastModifiedBy": "SYSTEM",
  "lastModifiedDate": "...",
  "markForDelete": false
}
```
Gunakan `@EnableMongoAuditing` + `@CreatedBy`/`@CreatedDate`/`@LastModifiedBy`/`@LastModifiedDate` otomatis, jangan diisi manual di tiap service.

### 2.3 Naming
- JSON API response/request: **camelCase**
- Kolom database (Postgres & Mongo field yang bukan bagian audit standar): **snake_case** untuk Postgres, **camelCase** untuk Mongo (ikuti konvensi native masing-masing driver)
- Endpoint path: **singular** untuk resource utama sesuai definisi awal (`/menu`, bukan `/menus`) kecuali didefinisikan lain secara eksplisit di spek Phase terkait
- Nama Kafka topic, nama field event, dan status enum harus PERSIS seperti yang didefinisikan di spek tiap Phase — jangan disingkat atau diganti nama sendiri (contoh kesalahan masa lalu: `order-events` generik dipakai padahal seharusnya 2 topic terpisah `cafe.order.placed` dan `cafe.order.status.changed`).

### 2.4 Reactive — non-negotiable
- **JANGAN PERNAH** pakai `Thread.sleep()` di reactive pipeline. Semua delay/jeda waktu WAJIB pakai `Mono.delay()` atau scheduler non-blocking sejenis. `Thread.sleep()` di WebFlux bisa memblokir thread event-loop Netty dan macetin seluruh aplikasi.
- Sinks/broadcast (SSE dkk) WAJIB dikonfigurasi dengan strategi yang tidak silently drop event (misal `Sinks.many().multicast().onBackpressureBuffer()`), bukan varian yang bisa kehilangan pesan kalau salah satu subscriber lambat.

### 2.5 State machine / status
- Urutan transisi status harus PERSIS sesuai spek per-Phase — jangan menambah state transisi implisit sendiri (contoh kesalahan masa lalu: order yang gagal sempat "mampir" ke `COOKING` dulu sebelum `CANCELLED`, padahal seharusnya langsung `QUEUED → CANCELLED` tanpa transit).
- Kalau menambah field/status baru ke entity yang sudah dipakai state machine lain (misal `orders.status`), WAJIB cek ulang semua tempat lain yang mengasumsikan jumlah kemungkinan status yang lama (contoh: validasi cancel di Phase 1 cuma cek status `QUEUED`).
- Semua nilai enum status (termasuk kasus gagal seperti `FAILED`) harus lengkap sesuai spek, jangan cuma yang jalur normal. Contoh: `cashier_report_upload.upload_status` harus punya `PROCESSING`, `COMPLETED`, **dan** `FAILED` — bukan cuma 2 dari 3.

### 2.6 Data yang "menunggu" sebelum jadi transaksi resmi
- Kalau spek menyebut sesuatu perlu "ditunda"/"dijadwalkan" sebelum benar-benar diproses (delayed job pattern), data itu WAJIB disimpan di collection/tabel staging terpisah (biasanya MongoDB) — **BUKAN** ditambahkan sebagai kolom/status baru di tabel transaksional utama. (Pelajaran dari kasus pre-order yang awalnya salah ditaruh sebagai kolom `scheduled_time` di tabel `orders`.)
- Job/scheduler yang polling data staging ini WAJIB menandai record sebagai "sudah diproses" (`TRIGGERED`/sejenisnya) SEGERA setelah trigger, supaya run scheduler berikutnya tidak memproses ulang (idempotency). Ini WAJIB dibuktikan di DoD dengan skenario 2x siklus scheduler berturut-turut.

### 2.7 Testing & fase
- Test ditulis PER PHASE, bukan ditumpuk di akhir project.
- JANGAN menambah fitur di luar yang diminta di Phase tersebut (contoh: jangan tiba-tiba menambahkan autentikasi/login kalau belum diminta).
- Setelah menyelesaikan satu Phase: laporkan ringkas (file yang dibuat/diubah, cara verifikasi, hasil pass/fail), lalu **berhenti dan tunggu konfirmasi user** sebelum lanjut ke Phase berikutnya. Jangan menggabungkan beberapa Phase sekaligus tanpa diminta.

---

## 3. STRUKTUR REPOSITORY

```
cafe-queue/                    <- folder lokal biasa, TIDAK di-git init di level ini
├── backend/                    <- git repo terpisah, push ke cafe-queue-backend
└── frontend/                   <- git repo terpisah (baru dikerjakan setelah backend Phase 1-6 selesai), push ke cafe-queue-frontend
```
JANGAN `git init` di level `cafe-queue/`. JANGAN campur backend & frontend jadi satu repo.

---

## 4. RIWAYAT KOREKSI PENTING (biar tidak terulang)

Daftar ini diperbarui tiap kali ada kesalahan berulang yang ditemukan saat review — anggap sebagai kasus uji yang harus tetap benar di masa depan:

| Phase | Kesalahan yang pernah terjadi | Yang seharusnya |
|---|---|---|
| 1 | Nama file repository sama persis dengan entity (`QueueTicketHistory.java` dipakai dua kali) | Beri suffix `Repository` konsisten di semua repository class |
| 1 | Sequence global (`ticket_number_seq`) dibuat tapi nomor tiket seharusnya reset harian | Jangan pakai sequence untuk nomor yang perlu reset per tanggal; pakai query `MAX+1 WHERE DATE(created_date) = CURRENT_DATE` |
| 2 | Kitchen simulator: probabilitas gagal dicek SETELAH transisi ke `COOKING` | Probabilitas gagal dicek SEBELUM `COOKING`; kalau gagal, langsung `QUEUED → CANCELLED` |
| 2 | Field event pakai nama generik (`timestamp`, `qty`) beda dari spek (`eventTimestamp`, `quantity`) | Nama field event harus persis sesuai spek |
| 3 | Payload SSE broadcast pakai event mentah (`OrderStatusChangedEvent`) tanpa mapping ke DTO publik | Selalu mapping ke DTO khusus display sebelum broadcast, jangan expose struktur internal |
| 4 | Plan sempat salah fase — mengerjakan menu (harusnya Phase 5) padahal diminta Redis cache queue board (Phase 4) | Selalu cek ulang scope Phase yang sedang diminta sebelum menyusun plan |
| 4 | DoD tidak menguji skenario Redis benar-benar down (cuma cache miss biasa) | DoD harus eksplisit uji: matikan container Redis, buktikan fallback ke Postgres tetap jalan, bukan error 500 |
| 5 | Pre-order awalnya didesain sebagai kolom baru di tabel `orders` (Postgres) | Harus di collection staging MongoDB terpisah (`pre_order_schedule`), baru masuk Postgres saat scheduler trigger |
| 6 | Status upload cuma `PROCESSING`/`COMPLETED`, tidak ada `FAILED` | Lengkapi enum sesuai spek termasuk jalur kegagalan |

---

## 5. FORMAT SELF-CHECK YANG WAJIB DISERTAKAN DI SETIAP PLAN

Di akhir setiap "Proposed Changes", sertakan blok ini:

```
## Self-Check
✅ Terpenuhi:
- [daftar poin dari §2 yang sudah dicek dan sesuai]

⚠️ Perlu konfirmasi user:
- [daftar poin yang ambigu / berpotensi menyimpang dari konvensi, WAJIB ditanyakan dulu sebelum eksekusi]
```