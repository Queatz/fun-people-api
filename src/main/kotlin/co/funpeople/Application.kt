package co.funpeople

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import co.funpeople.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureHTTP()
        configureMonitoring()
        configureSerialization()
        configureSockets()
    }.start(wait = true)
}
