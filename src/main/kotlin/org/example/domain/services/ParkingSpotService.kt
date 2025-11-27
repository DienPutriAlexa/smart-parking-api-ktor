package org.example.domain.services


import org.example.data.repositories.ParkingSpotRepository
import org.example.domain.models.ParkingSpotCreateRequest
import org.example.domain.models.ParkingSpotResponse

class ParkingSpotService(
    private val repo: ParkingSpotRepository = ParkingSpotRepository()
) {

    fun list(): List<ParkingSpotResponse> = repo.getAll()

    fun create(req: ParkingSpotCreateRequest): ParkingSpotResponse {
        require(req.spotNumber.isNotBlank()) { "spotNumber tidak boleh kosong" }
        return repo.create(req.spotNumber.trim())
    }

    fun setAvailable(id: Long, available: Boolean): Boolean =
        repo.setAvailability(id, available)
}
