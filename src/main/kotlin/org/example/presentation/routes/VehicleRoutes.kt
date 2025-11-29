package org.example.presentation.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.domain.models.VehicleCreateRequest
import org.example.domain.services.VehicleService

fun Route.vehicleRoutes() {
    val service = VehicleService()

    route("/vehicles") {

        // POST /vehicles -> Daftarkan kendaraan baru (Manual/Admin)
        post {
            try {
                val req = call.receive<VehicleCreateRequest>()

                val created = service.create(req)

                // REVISI 1: Gunakan status 201 Created
                call.respond(HttpStatusCode.Created, created)

            } catch (e: IllegalArgumentException) {
                // REVISI 2: Tangkap error validasi (Plat Kembar / Salah Tipe)
                // Pesan error dari Service ("Kendaraan sudah terdaftar!") akan dikirim ke sini
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Invalid request"))
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Gagal mendaftarkan kendaraan")
                )
            }
        }

        // GET /vehicles -> List semua kendaraan
        get {
            val list = service.list()
            call.respond(list)
        }
    }
}