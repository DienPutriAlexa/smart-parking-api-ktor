package org.example.data.repositories

import org.example.data.tables.ParkingLogsTable
import org.example.data.tables.ParkingSpotsTable
import org.example.domain.models.ParkingLogResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration

class ParkingLogRepository {

    fun getAll(): List<ParkingLogResponse> = transaction {
        ParkingLogsTable.selectAll().map { it.toResponse() }
    }

    fun checkIn(spotId: Long, vehicleId: Long): ParkingLogResponse = transaction {
        // cek spot masih available
        val spot = ParkingSpotsTable
            .select { ParkingSpotsTable.id eq spotId }
            .singleOrNull() ?: error("Spot tidak ditemukan")

        if (!spot[ParkingSpotsTable.isAvailable]) {
            error("Spot sedang terpakai")
        }

        // buat log
        val id = ParkingLogsTable.insertAndGetId {
            it[this.spotId] = spotId
            it[this.vehicleId] = vehicleId
            it[this.entryTime] = CurrentDateTime
        }.value

        // ubah spot jadi tidak available
        ParkingSpotsTable.update({ ParkingSpotsTable.id eq spotId }) {
            it[isAvailable] = false
        }

        getById(id)!!
    }

    fun checkOut(logId: Long, tarifPerJam: Int = 5000): ParkingLogResponse = transaction {
        val row = ParkingLogsTable
            .select { ParkingLogsTable.id eq logId }
            .singleOrNull() ?: error("Log tidak ditemukan")

        if (row[ParkingLogsTable.exitTime] != null) {
            error("Sudah check-out")
        }

        val entry = row[ParkingLogsTable.entryTime]
        val nowExpr = CurrentDateTime

        // update exit_time dulu
        ParkingLogsTable.update({ ParkingLogsTable.id eq logId }) {
            it[exitTime] = nowExpr
        }

        val updated = ParkingLogsTable
            .select { ParkingLogsTable.id eq logId }
            .single()

        val exitTime = updated[ParkingLogsTable.exitTime]!!
        val durationMinutes = Duration.between(entry, exitTime).toMinutes()
        val hours = if (durationMinutes <= 0) 1 else ((durationMinutes + 59) / 60).toInt()
        val fee = hours * tarifPerJam

        ParkingLogsTable.update({ ParkingLogsTable.id eq logId }) {
            it[ParkingLogsTable.fee] = fee
        }

        // set spot available lagi
        val spotId = updated[ParkingLogsTable.spotId]
        ParkingSpotsTable.update({ ParkingSpotsTable.id eq spotId }) {
            it[ParkingSpotsTable.isAvailable] = true
        }

        getById(logId)!!
    }

    fun getById(id: Long): ParkingLogResponse? = transaction {
        ParkingLogsTable
            .select { ParkingLogsTable.id eq id }
            .singleOrNull()
            ?.toResponse()
    }

    private fun ResultRow.toResponse() = ParkingLogResponse(
        id = this[ParkingLogsTable.id].value,
        spotId = this[ParkingLogsTable.spotId],
        vehicleId = this[ParkingLogsTable.vehicleId],
        entryTime = this[ParkingLogsTable.entryTime].toString(),
        exitTime = this[ParkingLogsTable.exitTime]?.toString(),
        fee = this[ParkingLogsTable.fee]
    )
}
