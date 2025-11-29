package org.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureMonitoring() {
    install(CallLogging)

    install(CORS) {
        anyHost() // Boleh diakses dari mana saja (Aman untuk Development)

        // REVISI: Izinkan method selain GET/POST
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch) // <--- PENTING: Karena kita pakai PATCH buat Checkout
        allowMethod(HttpMethod.Delete)

        // REVISI: Izinkan Header Content-Type (biar bisa kirim JSON)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }
}