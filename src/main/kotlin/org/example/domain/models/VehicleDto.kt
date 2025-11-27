package org.example.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class VehicleCreateRequest(
    val plateNumber: String,
    val vehicleType: String
)

@Serializable
data class VehicleResponse(
    val id: Long,
    val plateNumber: String,
    val vehicleType: String
)
