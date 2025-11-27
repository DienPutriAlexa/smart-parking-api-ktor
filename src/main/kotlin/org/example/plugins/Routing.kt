package org.example.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.presentation.routes.parkingLogRoutes
import org.example.presentation.routes.parkingSpotRoutes
import org.example.presentation.routes.vehicleRoutes

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Smart Parking API running")
        }

        parkingSpotRoutes()
        vehicleRoutes()
        parkingLogRoutes()

    }
}
