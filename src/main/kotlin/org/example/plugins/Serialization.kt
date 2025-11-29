package org.example.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            // Biar output JSON di Postman rapi (ada enter & spasi)
            prettyPrint = true

            // Biar gak error kalau JSON kurang tanda kutip dikit
            isLenient = true

            // PENTING: Biar gak error kalau Client kirim field tambahan yang gak ada di DTO
            ignoreUnknownKeys = true
        })
    }
}