package org.example.data.tables


import org.jetbrains.exposed.dao.id.LongIdTable

object VehicleTable : LongIdTable("vehicles") {
    val plateNumber = varchar("plate_number", 20)
    val vehicleType = varchar("vehicle_type", 20)
}
