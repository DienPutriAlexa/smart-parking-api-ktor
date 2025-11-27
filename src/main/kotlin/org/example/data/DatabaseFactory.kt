package org.example.data


import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database

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
    }
}
