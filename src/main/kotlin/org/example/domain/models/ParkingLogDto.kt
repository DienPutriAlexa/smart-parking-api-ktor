package org.example.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ParkingCheckInRequest(
    // REVISI 1: Input bukan ID, tapi Plat Nomor & Tipe
    // Biar sistem yang mikir slot mana yang kosong.
    val platNomor: String,
    val vehicleType: String // "CAR" atau "MOTORCYCLE"
)

@Serializable
data class ParkingLogResponse(
    val id: Long,
    val spotId: Long,
    val vehicleId: Long,
    val entryTime: String,
    val exitTime: String?,

    // REVISI 2: Ubah ke Long? agar cocok dengan Database & Repository
    val fee: Long?
)
// ... class yang lain ...

@Serializable
data class FeeInquiryResponse(
    val fee: Long,
    val entryTime: String,
    val logId: Long
)