package co.funpeople.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class BearerAuthenticationProvider internal constructor(
    configuration: Configuration
) : AuthenticationProvider(configuration) {
    internal val authenticationFunction = configuration.authenticationFunction

    class Configuration internal constructor(name: String?) : AuthenticationProvider.Configuration(name) {
        internal var authenticationFunction: AuthenticationFunction<BearerAuth> = {
            throw NotImplementedError("Bearer auth validate function is not specified. Use bearer { validate { ... } } to fix.")
        }

        fun validate(body: suspend ApplicationCall.(BearerAuth) -> Principal?) {
            authenticationFunction = body
        }
    }
}

fun Authentication.Configuration.bearer(
    name: String? = null,
    configure: BearerAuthenticationProvider.Configuration.() -> Unit
) {
    val provider = BearerAuthenticationProvider(BearerAuthenticationProvider.Configuration(name).apply(configure))
    val authenticate = provider.authenticationFunction

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val credentials = call.request.header(HttpHeaders.Authorization)?.split("\\s+".toRegex())?.takeIf {
            it.size == 2 && it.first() == "Bearer"
        }?.last()?.takeIf { it.isNotEmpty() }

        val principal = credentials?.let { authenticate(call, BearerAuth(it)) }

        val cause = when {
            credentials == null -> AuthenticationFailedCause.NoCredentials
            principal == null -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (cause != null) {
            context.challenge(BearerAuth::class.simpleName!!, cause) {
                call.respond(UnauthorizedResponse())
                it.complete()
            }
        }
        if (principal != null) {
            context.principal(principal)
        }
    }

    register(provider)
}


class BearerAuth(
    val token: String
)
