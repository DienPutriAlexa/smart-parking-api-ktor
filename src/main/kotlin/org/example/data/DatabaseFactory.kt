package org.example.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.example.data.tables.* // Import semua tabel kita

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val driver = config.property("db.driver").getString()
        val url = config.property("db.url").getString()
        val user = config.property("db.user").getString()
        val password = config.property("db.password").getString()
        val maxPool = config.property("db.maximumPoolSize").getString().toInt()

        val hikariConfig = HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = url
            username = user
            this.password = password
            maximumPoolSize = maxPool
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        Database.connect(HikariDataSource(hikariConfig))

        // --- TAMBAHAN PENTING ---
        // Ini akan otomatis membuat tabel di database jika belum ada.
        transaction {
            SchemaUtils.create(
                ParkingFloorsTable,
                ParkingSpotsTable,
                VehicleTable,
                ParkingLogsTable
            )
        }
        // ------------------------
    }
}