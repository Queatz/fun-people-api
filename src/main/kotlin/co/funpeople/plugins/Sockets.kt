package co.funpeople.plugins

import co.funpeople.db.members
import co.funpeople.db.personWithEmail
import co.funpeople.models.Message
import co.funpeople.models.Person
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.collections.LinkedHashSet
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val json = Json {
    encodeDefaults = true
    isLenient = true
    allowSpecialFloatingPointValues = true
    allowStructuredMapKeys = true
    explicitNulls = false
}

class Connection(val session: DefaultWebSocketSession) {
    var person: Person? = null
}

@Serializable
data class TokenMessage (
    val token: String
)

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = 15.seconds.toJavaDuration()
        timeout = 15.seconds.toJavaDuration()
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())

        webSocket("/ws") {
            val thisConnection = Connection(this)
            connections += thisConnection

            fun receiveMessage(messageText: Message) {
                if (thisConnection.person == null) {
                    return
                }

                val members = db.members(messageText.groupId)

                if (members.none { it.personId == thisConnection.person!!.id}) {
                    return
                }

                val message = Message().apply {
                    person = Person().also {
                        it.id = thisConnection.person!!.id
                        it.name = thisConnection.person!!.name
                        it.introduction = thisConnection.person!!.introduction
                    }
                    personId = thisConnection.person!!.id!!
                    groupId = messageText.groupId
                    text = messageText.text
                }.also {
                    db.insert(it)
                }

                launch {
                    connections.filter {
                        members.any { member -> member.personId == it.person?.id }
                    }.forEach {
                        it.session.outgoing.send(Frame.Text(json.encodeToString(message)))
                    }
                }
            }

            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()

                        if (text.contains("\"token\":")) {
                            thisConnection.person = sessions[
                                    json.decodeFromString<TokenMessage>(text).token
                            ]?.let {
                                db.personWithEmail(it)
                            }
                        } else {
                            receiveMessage(json.decodeFromString(text))
                        }

                        if (text.equals("bye", ignoreCase = true)) {
                            close(CloseReason(CloseReason.Codes.NORMAL, "buy"))
                        }
                    }
                    else -> {

                    }
                }
            }

            connections -= thisConnection
        }
    }
}
