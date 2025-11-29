package org.example.data.repositories

import org.example.data.tables.VehicleTable
import org.example.domain.models.VehicleCreateRequest
import org.example.domain.models.VehicleResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class VehicleRepository {

    // REVISI 1: Sesuaikan nama kolom dengan VehicleTable (platNomor & type)
    fun create(req: VehicleCreateRequest): VehicleResponse = transaction {
        val id = VehicleTable.insertAndGetId {
            it[platNomor] = req.plateNumber
            it[type] = req.vehicleType
        }.value

        VehicleResponse(id, req.plateNumber, req.vehicleType)
    }

    // REVISI 2: Tambah fungsi ini! Penting buat Logic Masuk Parkir (CheckIn)
    fun findByPlateNumber(plateNumber: String): VehicleResponse? = transaction {
        VehicleTable
            .select { VehicleTable.platNomor eq plateNumber }
            .singleOrNull()
            ?.let {
                VehicleResponse(
                    id = it[VehicleTable.id].value,
                    plateNumber = it[VehicleTable.platNomor],
                    vehicleType = it[VehicleTable.type]
                )
            }
    }

    fun getAll(): List<VehicleResponse> = transaction {
        VehicleTable.selectAll().map {
            VehicleResponse(
                id = it[VehicleTable.id].value,
                plateNumber = it[VehicleTable.platNomor],
                vehicleType = it[VehicleTable.type]
            )
        }
    }
}