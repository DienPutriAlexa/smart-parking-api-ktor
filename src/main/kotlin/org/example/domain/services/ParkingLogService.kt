package org.example.domain.services

// --- BAGIAN IMPORT (WAJIB DI ATAS SINI) ---
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.util.Base64
import kotlinx.serialization.json.*
import org.example.data.repositories.ParkingLogRepository
import org.example.data.repositories.VehicleRepository
import org.example.domain.models.FeeInquiryResponse
import org.example.domain.models.ParkingCheckInRequest
import org.example.domain.models.ParkingLogResponse
import org.example.domain.models.VehicleCreateRequest

class ParkingLogService(
    private val logRepo: ParkingLogRepository = ParkingLogRepository(),
    private val vehicleRepo: VehicleRepository = VehicleRepository()
) {
    // Masukkan Server Key Midtrans di sini
    // Pastikan key ini benar (biasanya diawali "SB-Mid-server-..." untuk Sandbox)
    private val serverKey = "Mid-server-Xz_v517C6u650k6Dknl_jvUs"

    fun list(): List<ParkingLogResponse> = logRepo.getAll()

    // REVISI 2: Logic Pintar (Auto-Register Vehicle)
    fun checkIn(req: ParkingCheckInRequest): ParkingLogResponse {
        // Langkah 1: Cek apakah mobil ini sudah terdaftar di database?
        var vehicle = vehicleRepo.findByPlateNumber(req.platNomor)

        // Langkah 2: Jika belum ada (Mobil Baru), Registrasi Otomatis!
        if (vehicle == null) {
            val newVehicleReq = VehicleCreateRequest(req.platNomor, req.vehicleType)
            vehicle = vehicleRepo.create(newVehicleReq)
        }

        // Validasi double check-in
        val isParked = logRepo.isVehicleCurrentlyParked(vehicle.id)
        if (isParked) {
            throw IllegalArgumentException("Mobil dengan plat ${req.platNomor} sedang parkir! Tidak bisa masuk lagi.")
        }

        // Langkah 3: Panggil Repo Log untuk proses check-in
        return logRepo.checkIn(vehicle.id)
    }

    fun checkOut(logId: Long): ParkingLogResponse {
        return logRepo.checkOut(logId)
    }

    // Fungsi Checkout pakai Plat Nomor
    fun checkOutByPlate(plat: String): ParkingLogResponse {
        val logId = logRepo.findActiveLogByPlate(plat)
            ?: throw IllegalArgumentException("Mobil dengan plat $plat tidak ditemukan atau sudah keluar.")

        return checkOut(logId)
    }

    fun getFeeEstimate(plat: String): FeeInquiryResponse {
        return logRepo.calculateFeeOnly(plat)
    }

    // --- FUNGSI MIDTRANS ---
    suspend fun createMidtransToken(ticketId: Long, grossAmount: Long): String {
        // Menggunakan client CIO
        val client = HttpClient(CIO)

        // Buat JSON Request sesuai standar Midtrans
        val jsonBody = """
            {
                "transaction_details": {
                    "order_id": "TICKET-$ticketId-${System.currentTimeMillis()}", 
                    "gross_amount": $grossAmount
                },
                "credit_card": { "secure": true }
            }
        """.trimIndent()

        // Encode Server Key ke Base64 (Basic Auth)
        // Midtrans mewajibkan format "ServerKey:" (pakai titik dua di belakang)
        val authHeader = Base64.getEncoder().encodeToString("$serverKey:".toByteArray())

        // Tembak API Midtrans
        val response: String = client.post("https://app.sandbox.midtrans.com/snap/v1/transactions") {
            headers {
                append(HttpHeaders.Authorization, "Basic $authHeader")
                append(HttpHeaders.ContentType, "application/json")
                append(HttpHeaders.Accept, "application/json")
            }
            setBody(jsonBody)
        }.bodyAsText()

        // Ambil "token" dari respon JSON secara manual (String parsing)
        val token = response.substringAfter("\"token\":\"").substringBefore("\"")

        return token
    }
}