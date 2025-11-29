package org.example.domain.services

import org.example.data.repositories.VehicleRepository
import org.example.domain.models.VehicleCreateRequest
import org.example.domain.models.VehicleResponse

class VehicleService(
    private val repo: VehicleRepository = VehicleRepository()
) {
    fun create(req: VehicleCreateRequest): VehicleResponse {
        // Validasi 1: Plat tidak boleh kosong
        require(req.plateNumber.isNotBlank()) { "Plat nomor wajib diisi" }

        // Validasi 2: Tipe Kendaraan
        val type = req.vehicleType.uppercase()
        require(type == "CAR" || type == "MOTORCYCLE") { "Tipe harus CAR atau MOTORCYCLE" }

        // Validasi 3: Cek Duplikasi (Biar gak crash SQL Error)
        val existing = repo.findByPlateNumber(req.plateNumber)
        if (existing != null) {
            throw IllegalArgumentException("Kendaraan dengan plat ${req.plateNumber} sudah terdaftar!")
        }

        return repo.create(req)
    }

    fun list() = repo.getAll()

    // Tambahan: Berguna kalau Admin mau cari data mobil
    fun getByPlate(plat: String) = repo.findByPlateNumber(plat)
}