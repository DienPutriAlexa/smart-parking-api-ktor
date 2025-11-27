package org.example.presentation.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.domain.models.ParkingCheckInRequest
import org.example.domain.services.ParkingLogService

fun Route.parkingLogRoutes() {
    val service = ParkingLogService()

    route("/logs") {

        get {
            val logs = service.list()
            call.respond(logs)
        }

        post("/check-in") {
            try {
                val req = call.receive<ParkingCheckInRequest>()
                val log = service.checkIn(req)
                call.respond(HttpStatusCode.Created, log)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Gagal check-in"))
                )
            }
        }

        patch("/{id}/check-out") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "id wajib ada"))
                return@patch
            }

            try {
                val log = service.checkOut(id)
                call.respond(log)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Gagal check-out"))
                )
            }
        }
    }
}
