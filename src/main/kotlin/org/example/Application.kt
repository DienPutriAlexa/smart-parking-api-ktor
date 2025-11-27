package org.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.example.data.DatabaseFactory
import org.example.plugins.configureMonitoring
import org.example.plugins.configureRouting
import org.example.plugins.configureSerialization

fun main() {
    EngineMain.main(emptyArray())
}

@Suppress("unused")
fun Application.module() {
    DatabaseFactory.init(environment.config)

    configureMonitoring()
    configureSerialization()
    configureRouting()
}
