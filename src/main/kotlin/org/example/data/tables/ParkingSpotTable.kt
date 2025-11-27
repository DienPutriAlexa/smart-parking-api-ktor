package org.example.data.tables


import org.jetbrains.exposed.dao.id.LongIdTable

object ParkingSpotsTable : LongIdTable("parking_spots") {
    val spotNumber = varchar("spot_number", 10)
    val isAvailable = bool("is_available").default(true)
}
