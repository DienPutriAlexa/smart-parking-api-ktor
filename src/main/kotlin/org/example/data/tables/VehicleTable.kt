package org.example.data.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object VehicleTable : LongIdTable("vehicles") {
    // REVISI 1: Ubah nama variabel jadi 'platNomor' (biar cocok sama Repository)
    // REVISI 2: Tambahkan .uniqueIndex() agar database menolak jika ada plat kembar
    val platNomor = varchar("plate_number", 20).uniqueIndex()

    // REVISI 3: Ubah nama variabel jadi 'type' (biar cocok sama Repository)
    val type = varchar("vehicle_type", 20)
}