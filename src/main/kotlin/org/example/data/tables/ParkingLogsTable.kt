package org.example.data.tables


import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object ParkingLogsTable : LongIdTable("parking_logs") {
    val spotId = long("spot_id")
    val vehicleId = long("vehicle_id")
    val entryTime = datetime("entry_time")
    val exitTime = datetime("exit_time").nullable()
    val fee = integer("fee").nullable()
}
