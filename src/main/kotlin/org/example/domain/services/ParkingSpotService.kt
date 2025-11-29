package org.example.domain.services

import org.example.data.repositories.ParkingSpotRepository
import org.example.domain.models.ParkingSpotCreateRequest
import org.example.domain.models.ParkingSpotResponse

class ParkingSpotService(
    private val repo: ParkingSpotRepository = ParkingSpotRepository()
) {

    fun list(): List<ParkingSpotResponse> = repo.getAll()

    fun create(req: ParkingSpotCreateRequest): ParkingSpotResponse {
        require(req.spotNumber.isNotBlank()) { "Nomor slot tidak boleh kosong" }

        // REVISI 1: Validasi Tipe Slot
        val type = req.spotType.uppercase()
        require(type == "CAR" || type == "MOTORCYCLE") { "Tipe slot harus CAR atau MOTORCYCLE" }

        // REVISI 2: Kirim data lengkap (Lantai & Tipe) ke Repository
        return repo.create(req.spotNumber.trim(), type, req.floorId)
    }

    fun setAvailable(id: Long, available: Boolean): Boolean =
        repo.setAvailability(id, available)
}