package org.example.presentation.routes


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.domain.models.ParkingSpotCreateRequest
import org.example.domain.services.ParkingSpotService

fun Route.parkingSpotRoutes() {
    val service = ParkingSpotService()

    route("/spots") {

        // GET /spots
        get {
            val spots = service.list()
            call.respond(spots)
        }

        // POST /spots
        post {
            try {
                val req = call.receive<ParkingSpotCreateRequest>()
                val created = service.create(req)
                call.respond(HttpStatusCode.Created, created)
            } catch (e: IllegalArgumentException) {
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

        // PATCH /spots/{id}/availability?available=true|false
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
