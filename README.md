# 🚗 Smart Parking System API (Ktor + Kotlin)

Project ini adalah implementasi **Smart Parking System** berbasis RESTful API menggunakan **Ktor** dan **Kotlin**. Sistem ini dirancang dengan arsitektur **Clean Architecture** dan memiliki logika bisnis tingkat lanjut (*Enterprise Grade*) untuk menangani parkir otomatis.

Dilengkapi dengan **Web Dashboard (Frontend)** untuk simulasi *Manless Gate* dan pembayaran digital.

---

# 👥 Pembagian Tugas Kelompok
## Anggota 1 – Dien Putri Alexa

- Mendesain arsitektur backend REST API menggunakan Ktor + Kotlin

- Membuat database schema dan integrasi MariaDB dengan Exposed ORM

- Mengimplementasikan endpoint CRUD Parking Spot, Vehicle, dan Parking Log

- Mengelola business logic check-in dan check-out parkir (status spot, durasi, fee)

- Menyusun struktur project, konfigurasi server, dan dokumentasi API (README)

- Melakukan testing endpoint menggunakan Postman

## Anggota 2 – Fikri Zaini

- Membuat dan mempercantik tampilan web (HTML)

- Melakukan testing dan finalisasi project

- Melakukan testing endpoint menggunakan Postman

## Anggota 3 – Ardiansyah Desta

- Membuat Class Diagram

- Membuat Use Case Diagram

## Anggota 4 – Ariyq

- Menyusun laporan akhir project

## 🌟 1. Fitur Unggulan (Smart Features)

Bukan sekadar CRUD biasa, sistem ini memiliki logika cerdas:

1.  **Smart Slot Allocation:** Backend otomatis mencari slot kosong terdekat sesuai jenis kendaraan (Mobil tidak akan masuk slot Motor).
2.  **Concurrency Safety:** Menggunakan Database Locking (`FOR UPDATE`) untuk mencegah *Race Condition* (Dua kendaraan merebut satu slot secara bersamaan).
3.  **Dynamic Pricing & Audit:**
    - Tarif berbeda untuk setiap Lantai dan Jenis Kendaraan.
    - **Snapshot Harga:** Menyimpan harga saat masuk, sehingga kenaikan tarif di masa depan tidak mempengaruhi tiket yang sedang berjalan.
4.  **Auto-Registration:** Kendaraan baru otomatis terdaftar di database saat *check-in* pertama kali.
5.  **Cashless Payment Simulation:** Simulasi pembayaran QRIS/Virtual Account (Midtrans Flow) melalui Dashboard.

---

## 🛠 2. Teknologi yang Digunakan

- **Language:** Kotlin (JDK 17)
- **Framework:** Ktor 2.x (Server Engine: Netty)
- **Database:** MariaDB / MySQL (via XAMPP)
- **ORM:** Exposed (SQL DSL)
- **Connection Pool:** HikariCP
- **Frontend:** HTML5, CSS3, JavaScript (Vanilla), SweetAlert2
- **Tools:** IntelliJ IDEA, Postman (Optional)

---

## 🚀 3. Cara Menjalankan Proyek

### 3.1. Prasyarat
- JDK 17 terinstall.
- XAMPP (MySQL/MariaDB) sudah aktif.

### 3.2. Setup Database (Wajib)
1. Buka **phpMyAdmin** (`http://localhost/phpmyadmin`).
2. Buat database baru dengan nama: **`smart_parking_db`**.
3. **PENTING:** Anda *TIDAK PERLU* membuat tabel manual. Aplikasi akan otomatis membuat tabel saat pertama kali dijalankan (`SchemaUtils.create`).

### 3.3. Konfigurasi
Pastikan file `src/main/resources/application.conf` sesuai dengan XAMPP Anda:
```hocon
ktor {
    deployment {
        port = 8081
        host = "0.0.0.0" # Agar bisa diakses dari device lain (HP)
    }
    application {
        modules = [ org.example.ApplicationKt.module ]
    }
}
db {
    driver = "org.mariadb.jdbc.Driver"
    url = "jdbc:mariadb://localhost:3306/smart_parking_db"
    user = "root"
    password = ""
}
````

### 3.4. Seeding Data (Isi Data Awal)

Agar sistem bisa dipakai, Anda harus membuat Lantai dan Slot Parkir. Jalankan SQL ini di **phpMyAdmin** (Tab SQL):

```sql
-- 1. Buat Lantai Dasar (Mobil 5rb, Motor 2rb)
INSERT INTO parking_floors (name, car_rate, motor_rate) 
VALUES ('Lantai Dasar', 5000, 2000);

-- 2. Buat 5 Slot Mobil (A-01 sd A-05)
INSERT INTO parking_spots (floor_id, spot_number, spot_type, is_available) VALUES 
(1, 'A-01', 'CAR', 1), (1, 'A-02', 'CAR', 1), (1, 'A-03', 'CAR', 1), (1, 'A-04', 'CAR', 1), (1, 'A-05', 'CAR', 1);

-- 3. Buat 5 Slot Motor (M-201 sd M-205)
INSERT INTO parking_spots (floor_id, spot_number, spot_type, is_available) VALUES 
(1, 'M-201', 'MOTORCYCLE', 1), (1, 'M-202', 'MOTORCYCLE', 1), (1, 'M-203', 'MOTORCYCLE', 1), (1, 'M-204', 'MOTORCYCLE', 1), (1, 'M-205', 'MOTORCYCLE', 1);
```

### 3.5. Run & Demo

1.  Klik tombol **Run (▶)** di IntelliJ IDEA.
2.  Buka Browser: **`http://localhost:8081`**
3.  Anda akan melihat **Web Dashboard**. Silakan simulasi Masuk/Keluar dari sana.

-----

## 📚 4. Dokumentasi API (Endpoints)

Jika ingin testing manual tanpa Web Dashboard, gunakan Postman ke endpoint berikut:

### 🅿️ Parking Logic (Transaksi)

| Method | URL | Deskripsi | Body JSON (Contoh) |
| :--- | :--- | :--- | :--- |
| **POST** | `/logs/check-in` | Masuk Parkir (Auto Slot) | `{"platNomor": "B 1234 XY", "vehicleType": "CAR"}` |
| **POST** | `/logs/check-out-by-plate` | Keluar & Bayar | `{"platNomor": "B 1234 XY"}` |
| **GET** | `/logs/fee/{plat}` | Cek Tagihan (Inquiry) | - |
| **GET** | `/logs` | Lihat Riwayat Transaksi | - |

### 🛠 Manajemen Data (Admin)

| Method | URL | Deskripsi | Body JSON (Contoh) |
| :--- | :--- | :--- | :--- |
| **GET** | `/spots` | Lihat Status Slot (Denah) | - |
| **POST** | `/spots` | Tambah Slot Baru | `{"spotNumber": "B-01", "floorId": 1, "spotType": "CAR"}` |
| **GET** | `/vehicles` | Lihat Kendaraan Terdaftar | - |

-----

## 📂 5. Struktur Proyek (Clean Architecture)

```
src/main/kotlin/org/example/
├── data/                  # LAYER DATA (Akses Database)
│   ├── tables/            # Definisi Tabel (Exposed DAO)
│   │   ├── ParkingFloorsTable.kt
│   │   ├── ParkingSpotsTable.kt
│   │   ├── VehicleTable.kt
│   │   └── ParkingLogsTable.kt
│   └── repositories/      # Query SQL & Locking Logic
│       ├── ParkingSpotRepository.kt  (Logika Locking Slot)
│       └── ParkingLogRepository.kt   (Logika Hitung Tarif)
│
├── domain/                # LAYER BISNIS (Aturan Aplikasi)
│   ├── models/            # Data Transfer Objects (DTO)
│   └── services/          # Logika Bisnis Utama
│       ├── ParkingLogService.kt      (Orchestrator Masuk/Keluar)
│       └── ...
│
├── presentation/          # LAYER PRESENTASI (Interface Luar)
│   └── routes/            # HTTP Endpoints (API)
│       ├── ParkingLogRoutes.kt
│       └── ...
│
└── plugins/               # Konfigurasi Framework
    ├── Routing.kt         # Setup Route & Static Web
    ├── Serialization.kt   # Setup JSON
    └── Monitoring.kt      # Setup CORS & Logging
```

-----

**Catatan untuk Dosen/Penguji:**
Aplikasi ini menggunakan pendekatan **"Manless Gate Simulation"**.
Frontend (HTML) di folder `resources/static` berfungsi sebagai simulasi layar sentuh pada gerbang parkir, yang berkomunikasi dengan Backend Ktor secara *real-time* menggunakan Fetch API.

```
```
