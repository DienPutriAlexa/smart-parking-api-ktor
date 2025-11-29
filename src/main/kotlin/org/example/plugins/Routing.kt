package org.example.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.* // Impor ini PENTING untuk staticResources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.presentation.routes.parkingLogRoutes
import org.example.presentation.routes.parkingSpotRoutes
import org.example.presentation.routes.vehicleRoutes

fun Application.configureRouting() {
    routing {

        // Halaman Utama: Arahkan ke index.html
        // (Opsional: Kalau mau root URL "/" langsung buka dashboard)
        staticResources("/", "static", index = "index.html")

        // Route API kita
        parkingSpotRoutes()
        vehicleRoutes()
        parkingLogRoutes()

        // Endpoint test (bisa dihapus kalau mau dashboard jadi halaman utama)
        get("/test") {
            call.respondText("Smart Parking API running")
        }
    }
}