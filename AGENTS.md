# Agents Architecture

## 1. Overview
The architecture is based on microservices mapping (simulated as a single JVM but with distributed components).

## 2. Rules and Patterns

### 2.1 Persistence
Postgres for strongly consistent domains (orders, finance).
MongoDB for document-oriented flexible schemas (menus, delayed preorders).

### 2.2 Async Event Driven
Kafka is used for cross-domain communication (Order -> Kitchen, Order -> LiveBoard).

### 2.3 UI/UX Design System
Brutalist design. Function over form. No rounded corners.

### 2.4 Reactive
**Semua publish Kafka WAJIB lewat Outbox pattern (tulis ke `outbox_event` dalam transaksi yang sama dengan data utama). TIDAK BOLEH publish langsung dari service layer lagi mulai dari sini.**
