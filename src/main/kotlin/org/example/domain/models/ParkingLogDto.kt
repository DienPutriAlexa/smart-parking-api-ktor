package org.example.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ParkingCheckInRequest(
    val spotId: Long,
    val vehicleId: Long
)

@Serializable
data class ParkingLogResponse(
    val id: Long,
    val spotId: Long,
    val vehicleId: Long,
    val entryTime: String,
    val exitTime: String?,
    val fee: Int?
)
