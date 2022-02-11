package co.funpeople.plugins

import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureHTTP() {
    install(CORS) {
        allowNonSimpleContentTypes = true
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        host("localhost:4200")
        host("hangoutville.com", listOf("https"))
    }

    install(Compression)
    install(DefaultHeaders)
}
