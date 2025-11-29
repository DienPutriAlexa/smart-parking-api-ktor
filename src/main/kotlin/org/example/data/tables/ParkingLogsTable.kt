package org.example.data.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object ParkingLogsTable : LongIdTable("parking_logs") {

    // REVISI 1: Gunakan .references() agar terbentuk Foreign Key (Relasi) di Database
    // Ini menjamin integritas data (sesuai syarat 3 relasi minimal)
    val spotId = reference("spot_id", ParkingSpotsTable)
    val vehicleId = reference("vehicle_id", VehicleTable)

    val entryTime = datetime("entry_time")
    val exitTime = datetime("exit_time").nullable()

    // REVISI 2: Kolom Audit (Snapshot Harga)
    // Menyimpan harga per jam SAAT mobil masuk.
    // Jadi kalau Admin ubah harga besok, tiket hari ini tetap pakai harga lama.
    val hourlyRateSnapshot = integer("hourly_rate_snapshot")

    // Ganti ke Long (BigInt) biar aman kalau harganya jutaan (misal denda)
    val fee = long("fee").nullable()
}