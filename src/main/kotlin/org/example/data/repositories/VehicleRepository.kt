package org.example.data.repositories

import org.example.data.tables.VehicleTable
import org.example.domain.models.VehicleCreateRequest
import org.example.domain.models.VehicleResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class VehicleRepository {

    fun create(req: VehicleCreateRequest): VehicleResponse = transaction {
        val id = VehicleTable.insertAndGetId {
            it[plateNumber] = req.plateNumber
            it[vehicleType] = req.vehicleType
        }.value

        VehicleResponse(id, req.plateNumber, req.vehicleType)
    }

    fun getAll(): List<VehicleResponse> = transaction {
        VehicleTable.selectAll().map {
            VehicleResponse(
                id = it[VehicleTable.id].value,
                plateNumber = it[VehicleTable.plateNumber],
                vehicleType = it[VehicleTable.vehicleType]
            )
        }
    }
}
