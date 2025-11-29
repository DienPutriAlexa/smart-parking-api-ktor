package org.example.data.repositories

import org.example.data.tables.ParkingLogsTable  // <--- PENTING
import org.example.data.tables.ParkingSpotsTable
import org.example.data.tables.VehicleTable      // <--- PENTING
import org.example.domain.models.ParkingSpotResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ParkingSpotRepository {

    // Query Join untuk mengambil Plat Nomor yang sedang parkir
    fun getAll(): List<ParkingSpotResponse> = transaction {
        // Join: Spot -> Log (yg aktif) -> Vehicle
        val query = ParkingSpotsTable
            .join(
                ParkingLogsTable,
                JoinType.LEFT,
                onColumn = ParkingSpotsTable.id,
                otherColumn = ParkingLogsTable.spotId,
                additionalConstraint = { ParkingLogsTable.exitTime.isNull() } // Hanya log aktif
            )
            .join(
                VehicleTable,
                JoinType.LEFT,
                onColumn = ParkingLogsTable.vehicleId,
                otherColumn = VehicleTable.id
            )
            .selectAll()
            .orderBy(ParkingSpotsTable.spotNumber to SortOrder.ASC)

        query.map { row ->
            ParkingSpotResponse(
                id = row[ParkingSpotsTable.id].value,
                spotNumber = row[ParkingSpotsTable.spotNumber],
                isAvailable = row[ParkingSpotsTable.isAvailable],

                // REVISI: Tambahkan .value karena floorId adalah Foreign Key
                floorId = row[ParkingSpotsTable.floorId].value,

                spotType = row[ParkingSpotsTable.spotType],

                // Ambil Plat Nomor (Kalau ada mobilnya, kalau gak ada null)
                activePlate = if (row.getOrNull(ParkingLogsTable.vehicleId) != null)
                    row[VehicleTable.platNomor] else null
            )
        }
    }

    // --- FITUR UTAMA SMART PARKING (Locking & Auto-Allocation) ---
    fun findAndBookAvailableSpot(vehicleType: String): ParkingSpotResponse? = transaction {
        // 1. Cari slot kosong yang SESUAI TIPE dan KUNCI row-nya (Locking)
        val spot = ParkingSpotsTable
            .select {
                (ParkingSpotsTable.isAvailable eq true) and
                        (ParkingSpotsTable.spotType eq vehicleType)
            }
            .limit(1)
            // .forUpdate() // Aktifkan jika database support
            .map { row -> row.toParkingSpotResponse() }
            .singleOrNull()

        // 2. Jika ketemu, langsung tandai sebagai terisi (Occupied)
        if (spot != null) {
            ParkingSpotsTable.update({ ParkingSpotsTable.id eq spot.id }) {
                it[isAvailable] = false
            }
            // Kembalikan object slot dengan status terbaru
            return@transaction spot.copy(isAvailable = false)
        }

        return@transaction null
    }
    // -------------------------------------------------------------

    fun getById(id: Long): ParkingSpotResponse? = transaction {
        ParkingSpotsTable
            .select { ParkingSpotsTable.id eq id }
            .singleOrNull()
            ?.toParkingSpotResponse()
    }

    fun create(spotNumber: String, spotType: String, floorId: Int): ParkingSpotResponse = transaction {
        val existing = ParkingSpotsTable
            .select { ParkingSpotsTable.spotNumber eq spotNumber }
            .singleOrNull()

        require(existing == null) { "Spot number sudah dipakai" }

        val id = ParkingSpotsTable.insertAndGetId {
            it[this.spotNumber] = spotNumber
            it[this.spotType] = spotType
            it[this.floorId] = floorId
            it[this.isAvailable] = true
        }.value

        getById(id)!!
    }

    fun setAvailability(id: Long, available: Boolean): Boolean = transaction {
        val updated = ParkingSpotsTable.update({ ParkingSpotsTable.id eq id }) {
            it[isAvailable] = available
        }
        updated > 0
    }

    // --- HELPER FUNCTION YANG HILANG (INI PENTING) ---
    private fun ResultRow.toParkingSpotResponse() = ParkingSpotResponse(
        id = this[ParkingSpotsTable.id].value,
        spotNumber = this[ParkingSpotsTable.spotNumber],
        isAvailable = this[ParkingSpotsTable.isAvailable],
        floorId = this[ParkingSpotsTable.floorId].value, // Pakai .value
        spotType = this[ParkingSpotsTable.spotType],
        activePlate = null // Default null untuk query biasa
    )
}