package org.example.data.repositories

import org.example.data.tables.ParkingFloorsTable
import org.example.data.tables.ParkingLogsTable
import org.example.data.tables.ParkingSpotsTable
import org.example.data.tables.VehicleTable
import org.example.domain.models.FeeInquiryResponse
import org.example.domain.models.ParkingLogResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.time.LocalDateTime

class ParkingLogRepository {

    fun getAll(): List<ParkingLogResponse> = transaction {
        ParkingLogsTable.selectAll().map { it.toResponse() }
    }

    // REVISI LOGIC CHECK-IN: Lebih Cerdas & Aman
    fun checkIn(vehicleId: Long): ParkingLogResponse = transaction {

        // 1. Cek Tipe Kendaraan dulu (Mobil/Motor?)
        val vehicleRow = VehicleTable.select { VehicleTable.id eq vehicleId }.singleOrNull()
            ?: error("Kendaraan belum terdaftar! Registrasi dulu.")
        val vType = vehicleRow[VehicleTable.type] // "CAR" atau "MOTORCYCLE"

        // 2. Cari Slot Kosong yang SESUAI TIPE
        // Kita join ke ParkingSpotsTable untuk filter spotType == vType
        val spot = ParkingSpotsTable
            .select {
                (ParkingSpotsTable.isAvailable eq true) and
                        (ParkingSpotsTable.spotType eq vType)
            }
            .limit(1)
            //.forUpdate() // Aktifkan jika database support locking
            .singleOrNull() ?: error("Parkiran Penuh untuk jenis kendaraan ini!")

        val selectedSpotId = spot[ParkingSpotsTable.id].value
        val floorId = spot[ParkingSpotsTable.floorId] // Ambil ID lantai untuk cek harga

        // 3. Ambil Tarif SAAT INI dari Tabel Floor (Bukan Hardcode)
        val floorRow = ParkingFloorsTable.select { ParkingFloorsTable.id eq floorId }.single()

        // Pilih tarif berdasarkan tipe kendaraan
        val currentRate = if (vType.equals("CAR", ignoreCase = true)) {
            floorRow[ParkingFloorsTable.carRate]
        } else {
            floorRow[ParkingFloorsTable.motorRate]
        }

        // 4. Update Slot jadi Terisi
        ParkingSpotsTable.update({ ParkingSpotsTable.id eq selectedSpotId }) {
            it[isAvailable] = false
        }

        // 5. Buat Log Tiket (Simpan SNAPSHOT Harga)
        val id = ParkingLogsTable.insertAndGetId {
            it[this.spotId] = selectedSpotId
            it[this.vehicleId] = vehicleId
            it[this.entryTime] = CurrentDateTime
            it[this.hourlyRateSnapshot] = currentRate // <--- INI PENTING BUAT AUDIT
        }.value

        getById(id)!!
    }

    // REVISI LOGIC CHECK-OUT: Gunakan Harga Snapshot
    fun checkOut(logId: Long): ParkingLogResponse = transaction {
        val row = ParkingLogsTable
            .select { ParkingLogsTable.id eq logId }
            .singleOrNull() ?: error("Log tidak ditemukan")

        if (row[ParkingLogsTable.exitTime] != null) {
            error("Sudah check-out sebelumnya!")
        }

        // 1. Ambil Tarif dari SNAPSHOT (Bukan dari Master Floor, apalagi Hardcode)
        // Ini menjamin kalau harga naik pas lagi parkir, user tetap bayar harga lama.
        val snapshotRate = row[ParkingLogsTable.hourlyRateSnapshot]

        // 2. Hitung Durasi
        val entry = row[ParkingLogsTable.entryTime]
        val now = LocalDateTime.now()
        val durationMinutes = Duration.between(entry, now).toMinutes()

        // Logic: 1 menit pun dihitung 1 jam.
        val hoursTotal = if (durationMinutes <= 0) 1 else ((durationMinutes + 59) / 60).toInt()

        val hourlyRate = snapshotRate.toLong()
        val maxFeePerDay = hourlyRate * 10
        val days = hoursTotal / 24
        val remainingHours = hoursTotal % 24
        // 3. Hitung Total (Pakai Long biar aman)
        var dailyFee = remainingHours * hourlyRate
        if (dailyFee > maxFeePerDay) dailyFee = maxFeePerDay

        val totalFee = (days * maxFeePerDay) + dailyFee

        // 4. Update Log
        ParkingLogsTable.update({ ParkingLogsTable.id eq logId }) {
            it[exitTime] = now
            it[fee] = totalFee
        }

        // 5. Lepas Slot Parkir
        val spotId = row[ParkingLogsTable.spotId]
        ParkingSpotsTable.update({ ParkingSpotsTable.id eq spotId }) {
            it[isAvailable] = true
        }

        getById(logId)!!
    }

    fun getById(id: Long): ParkingLogResponse? = transaction {
        ParkingLogsTable
            .select { ParkingLogsTable.id eq id }
            .singleOrNull()
            ?.toResponse()
    }
    // Cek apakah kendaraan masih ada di dalam (exitTime is NULL)
    fun isVehicleCurrentlyParked(vehicleId: Long): Boolean = transaction {
        ParkingLogsTable.select {
            (ParkingLogsTable.vehicleId eq vehicleId) and (ParkingLogsTable.exitTime.isNull())
        }.count() > 0
    }
    // Cari Log ID yang aktif berdasarkan Plat Nomor
    fun findActiveLogByPlate(plat: String): Long? = transaction {
        val vehicle = VehicleTable.select { VehicleTable.platNomor eq plat }.singleOrNull()
        val vehicleId = vehicle?.get(VehicleTable.id)?.value ?: return@transaction null

        ParkingLogsTable
            .select { (ParkingLogsTable.vehicleId eq vehicleId) and (ParkingLogsTable.exitTime.isNull()) }
            .singleOrNull()
            ?.get(ParkingLogsTable.id)?.value
    }
    // HANYA HITUNG BIAYA (Tanpa Update DB)
    fun calculateFeeOnly(plat: String): FeeInquiryResponse = transaction {
        // 1. Cari Vehicle
        val vehicle = VehicleTable.select { VehicleTable.platNomor eq plat }.singleOrNull()
            ?: error("Plat nomor tidak ditemukan")
        val vehicleId = vehicle[VehicleTable.id].value
        val type = vehicle[VehicleTable.type] // CAR/MOTORCYCLE

        // 2. Cari Log Aktif
        val log = ParkingLogsTable.select {
            (ParkingLogsTable.vehicleId eq vehicleId) and (ParkingLogsTable.exitTime.isNull())
        }.singleOrNull() ?: error("Mobil sudah keluar atau tidak ada tiket aktif")

        // 3. Hitung Biaya
        val entry = log[ParkingLogsTable.entryTime]
        val now = LocalDateTime.now()
        val durationMinutes = Duration.between(entry, now).toMinutes()
        val hours = if (durationMinutes <= 0) 1 else ((durationMinutes + 59) / 60).toInt()

        val snapshotRate = log[ParkingLogsTable.hourlyRateSnapshot]
        val totalFee = hours.toLong() * snapshotRate.toLong() // Logic simpel dulu

        // Return data mentah untuk ditampilkan
        FeeInquiryResponse(
            fee = totalFee,
            entryTime = entry.toString(),
            logId = log[ParkingLogsTable.id].value
        )
    }

    private fun ResultRow.toResponse() = ParkingLogResponse(
        id = this[ParkingLogsTable.id].value,

        // REVISI: Tambahkan .value karena sekarang ini adalah Foreign Key (EntityID)
        spotId = this[ParkingLogsTable.spotId].value,
        vehicleId = this[ParkingLogsTable.vehicleId].value,

        entryTime = this[ParkingLogsTable.entryTime].toString(),
        exitTime = this[ParkingLogsTable.exitTime]?.toString(),

        // REVISI: Pastikan DTO menerima tipe Long, bukan Int
        fee = this[ParkingLogsTable.fee]
    )
}