package org.example.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ParkingSpotCreateRequest(
    val spotNumber: String,
    // REVISI: Wajib ada Lantai dan Tipe saat bikin slot baru
    val floorId: Int,
    val spotType: String // "CAR" atau "MOTORCYCLE"
)

@Serializable
data class ParkingSpotResponse(
    val id: Long,
    val spotNumber: String,
    val isAvailable: Boolean,

    // REVISI: Tampilkan info detail ke Frontend/Admin
    val floorId: Int,
    val spotType: String,
    val activePlate: String? = null
)