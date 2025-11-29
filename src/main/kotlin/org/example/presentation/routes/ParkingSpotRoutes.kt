package org.example.presentation.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.domain.models.ParkingSpotCreateRequest
import org.example.domain.services.ParkingSpotService

fun Route.parkingSpotRoutes() {
    val service = ParkingSpotService()

    route("/spots") {

        // GET /spots -> List semua slot
        get {
            val spots = service.list()
            call.respond(spots)
        }

        // POST /spots -> Tambah slot baru (Admin)
        // JSON Input: { "spotNumber": "A1", "floorId": 1, "spotType": "CAR" }
        post {
            try {
                // KEMBALI KE STANDARD (Generics)
                // Karena import request sudah benar, ini aman.
                val req = call.receive<ParkingSpotCreateRequest>()

                val created = service.create(req)
                call.respond(HttpStatusCode.Created, created)

            } catch (e: IllegalArgumentException) {
                // Menangkap error validasi dari Service (misal: Salah tipe, nomor kosong)
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Invalid request"))
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Internal server error")
                )
            }
        }

        // PATCH /spots/{id}/availability -> Manual Override (Admin Maintenance)
        patch("/{id}/availability") {
            val id = call.parameters["id"]?.toLongOrNull()
            val availableParam = call.request.queryParameters["available"]

            if (id == null || availableParam == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "id dan available wajib diisi")
                )
                return@patch
            }

            val available = availableParam.toBooleanStrictOrNull()
            if (available == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "available harus true/false")
                )
                return@patch
            }

            val ok = service.setAvailable(id, available)
            if (!ok) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Spot tidak ditemukan")
                )
            } else {
                call.respond(
                    mapOf("message" to "Status updated")
                )
            }
        }
    }
}