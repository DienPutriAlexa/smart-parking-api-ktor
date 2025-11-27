package org.example.domain.models


import kotlinx.serialization.Serializable

@Serializable
data class ParkingSpotCreateRequest(
    val spotNumber: String
)

@Serializable
data class ParkingSpotResponse(
    val id: Long,
    val spotNumber: String,
    val isAvailable: Boolean
)
