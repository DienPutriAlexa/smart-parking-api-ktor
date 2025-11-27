package org.example.plugins


import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureMonitoring() {
    install(CallLogging)
    install(CORS) {
        anyHost()
    }
}
