package org.example.presentation.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.domain.models.VehicleCreateRequest
import org.example.domain.services.VehicleService

fun Route.vehicleRoutes() {
    val service = VehicleService()

    route("/vehicles") {

        post {
            val req = call.receive<VehicleCreateRequest>()
            val created = service.create(req)
            call.respond(created)
        }

        get {
            val list = service.list()
            call.respond(list)
        }
    }
}
