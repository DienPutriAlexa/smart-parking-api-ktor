plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "2.3.11"
    kotlin("plugin.serialization") version "1.9.24"
}

group = "org.example"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
}

dependencies {
    val ktor = "2.3.11"
    val exposed = "0.50.1"
    val hikari = "5.1.0"
    val mariadb = "3.3.3"
    val logback = "1.4.14"

    // Ktor server
    implementation("io.ktor:ktor-server-core-jvm:$ktor")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor")
    implementation("io.ktor:ktor-server-cors-jvm:$ktor")

    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:$exposed")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed")

    // Connection pool + MariaDB driver
    implementation("com.zaxxer:HikariCP:$hikari")
    implementation("org.mariadb.jdbc:mariadb-java-client:$mariadb")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logback")

    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor")
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-client-core:2.3.11")
    implementation("io.ktor:ktor-client-cio:2.3.11")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.11")
}

kotlin {
    jvmToolchain(17)
}
