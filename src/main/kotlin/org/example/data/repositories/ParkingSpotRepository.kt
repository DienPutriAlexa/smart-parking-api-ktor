package org.example.data.repositories


import org.example.data.tables.ParkingSpotsTable
import org.example.domain.models.ParkingSpotResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ParkingSpotRepository {

    fun getAll(): List<ParkingSpotResponse> = transaction {
        ParkingSpotsTable
            .selectAll()
            .orderBy(ParkingSpotsTable.spotNumber to SortOrder.ASC)
            .map { row ->
                row.toParkingSpotResponse()
            }
    }

    fun getById(id: Long): ParkingSpotResponse? = transaction {
        ParkingSpotsTable
            .select { ParkingSpotsTable.id eq id }
            .singleOrNull()
            ?.toParkingSpotResponse()
    }

    fun getBySpotNumber(spotNumber: String): ParkingSpotResponse? = transaction {
        ParkingSpotsTable
            .select { ParkingSpotsTable.spotNumber eq spotNumber }
            .singleOrNull()
            ?.toParkingSpotResponse()
    }

    fun create(spotNumber: String): ParkingSpotResponse = transaction {
        val existing = ParkingSpotsTable
            .select { ParkingSpotsTable.spotNumber eq spotNumber }
            .singleOrNull()

        require(existing == null) { "Spot number sudah dipakai" }

        val id = ParkingSpotsTable.insertAndGetId {
            it[this.spotNumber] = spotNumber
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

    private fun ResultRow.toParkingSpotResponse() = ParkingSpotResponse(
        id = this[ParkingSpotsTable.id].value,
        spotNumber = this[ParkingSpotsTable.spotNumber],
        isAvailable = this[ParkingSpotsTable.isAvailable]
    )
}
