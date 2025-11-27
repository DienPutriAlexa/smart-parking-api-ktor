# Smart Parking System API (Ktor + Kotlin)

Project ini adalah implementasi Smart Parking System berbasis REST API menggunakan Ktor dan Kotlin sebagai tugas besar UAS praktikum.

Fokus: backend API only (diakses via Postman/terminal), tanpa frontend web.

---

## 1. Fitur Utama

- **Manajemen Parking Spot**
  - Tambah spot parkir baru
  - Lihat daftar semua spot
  - Ubah status spot (available / occupied)

- **Manajemen Vehicle**
  - Daftar kendaraan (plat nomor + tipe)
  - Lihat daftar kendaraan yang terdaftar

- **Parking Log**
  - **Check-in**: vehicle parkir di spot tertentu
  - **Check-out**: hitung durasi & fee, ubah spot jadi available lagi
  - Lihat riwayat semua log parkir

---

## 2. Teknologi yang Digunakan

- Kotlin + Gradle
- Ktor 2.x (Netty)
- Exposed ORM
- MariaDB/MySQL (via XAMPP)
- HikariCP (connection pool)
- Postman untuk testing API

---

## 3. Cara Menjalankan Proyek

### 3.1. Prasyarat

- JDK 17 terinstall
- IntelliJ IDEA (Community Edition cukup)
- XAMPP (MySQL/MariaDB aktif)
- Git (opsional, untuk clone)

### 3.2. Setup Database

1. Start **MySQL** di XAMPP.
2. Buka `http://localhost/phpmyadmin`.
3. Buat database baru:

   ```sql
   CREATE DATABASE smart_parking_db;

 Di dalam database smart_parking_db, buat tabel:

    CREATE TABLE parking_spots (
      id BIGINT PRIMARY KEY AUTO_INCREMENT,
      spot_number VARCHAR(10) NOT NULL UNIQUE,
      is_available BOOLEAN NOT NULL DEFAULT TRUE,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    
    CREATE TABLE vehicles (
      id BIGINT PRIMARY KEY AUTO_INCREMENT,
      plate_number VARCHAR(20) NOT NULL,
      vehicle_type VARCHAR(20) NOT NULL,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    
    CREATE TABLE parking_logs (
      id BIGINT PRIMARY KEY AUTO_INCREMENT,
      spot_id BIGINT NOT NULL,
      vehicle_id BIGINT NOT NULL,
      entry_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      exit_time TIMESTAMP NULL,
      fee INT NULL
    );

  ### 3.3. Konfigurasi application.conf
  
  File: src/main/resources/application.conf

    ktor {
      deployment {
        port = 8081
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
      maximumPoolSize = 5
    }

  ### 3.4. Menjalankan Server
  
  1. Buka project di IntelliJ.
  2. Jalankan file Application.kt (run main()).
  3. Cek di browser/Postman:
      ```
      GET http://localhost:8081/ → harus muncul Smart Parking API running.

## 4. Endpoint API
### 4.1. Parking Spots
  
  - GET /spots
  - POST /spots
      - Body (JSON):
    ```
    {
      "spotNumber": "A1"
    }
  - PATCH /spots/{id}/availability?available=true|false
      - Ubah status available/occupied.

### 4.2. Vehicles

  - GET /vehicles
  - POST /vehicles
      - Body:
    ```
    {
      "plateNumber": "B1234XYZ",
      "vehicleType": "Car"
    }
### 4.3. Parking Logs

  - GET /logs
    - Ambil seluruh log parkir.
  - POST /logs/check-in
    - Body:
    ```
    {
      "spotId": 1,
      "vehicleId": 1
    }    
  - PATCH /logs/{id}/check-out
    - Menyelesaikan parkir, mengisi exitTime dan fee.
    
## 5. Struktur Proyek (Folder Utama)

```
src/
└─ main/
   ├─ kotlin/
   │  └─ org/
   │     └─ example/
   │        ├─ Application.kt
   │        ├─ data/
   │        │  ├─ DatabaseFactory.kt
   │        │  ├─ tables/
   │        │  │  ├─ ParkingSpotsTable.kt
   │        │  │  ├─ VehicleTable.kt
   │        │  │  └─ ParkingLogsTable.kt
   │        │  └─ repositories/
   │        │     ├─ ParkingSpotRepository.kt
   │        │     ├─ VehicleRepository.kt
   │        │     └─ ParkingLogRepository.kt
   │        ├─ domain/
   │        │  ├─ models/
   │        │  │  ├─ ParkingSpotDto.kt
   │        │  │  ├─ VehicleDto.kt
   │        │  │  └─ ParkingLogDto.kt
   │        │  └─ services/
   │        │     ├─ ParkingSpotService.kt
   │        │     ├─ VehicleService.kt
   │        │     └─ ParkingLogService.kt
   │        ├─ presentation/
   │        │  └─ routes/
   │        │     ├─ ParkingSpotRoutes.kt
   │        │     ├─ VehicleRoutes.kt
   │        │     └─ ParkingLogRoutes.kt
   │        └─ plugins/
   │           ├─ Routing.kt
   │           ├─ Serialization.kt
   │           └─ Monitoring.kt
   └─ resources/
      └─ application.conf
```




