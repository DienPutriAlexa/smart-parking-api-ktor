package org.example.presentation.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.domain.models.ParkingCheckInRequest
import org.example.domain.services.ParkingLogService

fun Route.parkingLogRoutes() {
    val service = ParkingLogService()

    route("/logs") {

        // Endpoint 1: Lihat History Parkir
        get {
            val logs = service.list()
            call.respond(logs)
        }

        // Endpoint 2: MASUK PARKIR (Check-In)
        // Client kirim JSON: { "platNomor": "B 1234 XYZ", "vehicleType": "CAR" }
        post("/check-in") {
            try {
                // 1. Terima Data DTO Baru (Plat & Tipe)
                val req = call.receive<ParkingCheckInRequest>()

                // 2. Panggil Service (Service yang akan cari slot & registrasi mobil otomatis)
                val log = service.checkIn(req)

                // 3. Sukses
                call.respond(HttpStatusCode.Created, log)

            } catch (e: Exception) {
                // Handle Error (Misal: Parkiran Penuh)
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Gagal check-in"))
                )
            }
        }

        // Endpoint 3: KELUAR PARKIR (Check-Out)
        // Client kirim request ke: /logs/1/check-out
        patch("/{id}/check-out") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID Tiket wajib ada"))
                return@patch
            }

            try {
                // Service akan menghitung durasi & biaya otomatis
                val log = service.checkOut(id)
                call.respond(HttpStatusCode.OK, log)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Gagal check-out"))
                )
            }
        }
        // --- TAMBAHAN PENTING (CHECK-OUT VIA PLAT) ---
        post("/check-out-by-plate") {
            // 1. Terima JSON: { "platNomor": "B 1234 XY" }
            val params = call.receive<Map<String, String>>()
            val plat = params["platNomor"]

            if (plat == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Plat nomor wajib diisi"))
                return@post
            }

            try {
                // 2. Panggil Service baru (pastikan service sudah punya fungsi ini)
                val log = service.checkOutByPlate(plat)
                call.respond(HttpStatusCode.OK, log)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Gagal check-out"))
                )
            }
        }
        // GET /logs/fee/{plat} -> Cuma cek harga
        get("/fee/{plat}") {
            val plat = call.parameters["plat"]
            if (plat == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Plat wajib diisi"))
                return@get
            }
            try {
                val data = service.getFeeEstimate(plat)
                call.respond(data)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Error")))
            }
        }
        // POST /logs/pay-midtrans
        post("/pay-midtrans") {
            val params = call.receive<Map<String, String>>()
            val plat = params["platNomor"] ?: return@post call.respond(HttpStatusCode.BadRequest)

            // 1. Hitung harga dulu (Logic lama)
            val feeData = service.getFeeEstimate(plat)
            val fee = feeData.fee
            val ticketId = feeData.logId

            // 2. Minta Token ke Midtrans
            try {
                val snapToken = service.createMidtransToken(ticketId, fee)

                // 3. Kirim Token ke Frontend
                call.respond(mapOf("token" to snapToken))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}