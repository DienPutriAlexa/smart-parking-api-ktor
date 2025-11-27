package org.example.domain.services

import org.example.data.repositories.ParkingLogRepository
import org.example.domain.models.ParkingCheckInRequest
import org.example.domain.models.ParkingLogResponse

class ParkingLogService(
    private val repo: ParkingLogRepository = ParkingLogRepository()
) {
    fun list(): List<ParkingLogResponse> = repo.getAll()

    fun checkIn(req: ParkingCheckInRequest): ParkingLogResponse =
        repo.checkIn(req.spotId, req.vehicleId)

    fun checkOut(logId: Long): ParkingLogResponse =
        repo.checkOut(logId)
}
