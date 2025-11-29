package org.example.data.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object ParkingFloorsTable : IntIdTable("parking_floors") {
    val name = varchar("name", 50) // Misal: "Lantai 1", "Basement"

    // Tarif Parkir disimpan di sini
    val carRate = integer("car_rate").default(5000)
    val motorRate = integer("motor_rate").default(2000)
}