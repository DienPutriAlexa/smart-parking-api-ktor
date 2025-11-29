package org.example.data.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object ParkingSpotsTable : LongIdTable("parking_spots") {
    // REVISI 1: Hubungkan ke Tabel Lantai (Foreign Key)
    // Ini memenuhi syarat "Minimal 3 Relasi Antar Table"
    val floorId = reference("floor_id", ParkingFloorsTable)

    val spotNumber = varchar("spot_number", 10)

    // REVISI 2: Tambah Tipe Slot (CAR / MOTORCYCLE)
    // Agar sistem bisa memvalidasi "Mobil dilarang masuk slot Motor"
    val spotType = varchar("spot_type", 20)

    val isAvailable = bool("is_available").default(true)
}