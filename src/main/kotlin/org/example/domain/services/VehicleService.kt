package org.example.domain.services

import org.example.data.repositories.VehicleRepository
import org.example.domain.models.VehicleCreateRequest

class VehicleService(
    private val repo: VehicleRepository = VehicleRepository()
) {
    fun create(req: VehicleCreateRequest) = repo.create(req)
    fun list() = repo.getAll()
}
