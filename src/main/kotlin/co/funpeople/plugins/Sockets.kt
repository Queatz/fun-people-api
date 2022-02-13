package co.funpeople.plugins

import co.funpeople.db.authWithToken
import co.funpeople.db.members
import co.funpeople.db.personWithEmail
import co.funpeople.models.Member
import co.funpeople.models.Message
import co.funpeople.models.Person
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
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

@Serializable
data class TypingMessage (
    val groupId: String,
    val typing: Boolean,
    var name: String? = null
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

            fun relay(members: List<Member>, frame: Frame) {
                launch {
                    connections.filter {
                        members.any { member -> member.personId == it.person?.id }
                    }.forEach {
                        it.session.outgoing.send(frame)
                    }
                }
            }

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
                    text = messageText.text.trim()
                }.also {
                    db.insert(it)
                }

                members.firstOrNull { it.personId == thisConnection.person!!.id!! }?.let {
                    it.readUntil = Clock.System.now()
                    db.update(it)
                }

                relay(members, Frame.Text(json.encodeToString(message)))
            }

            fun receiveTyping(typing: TypingMessage) {
                if (thisConnection.person == null) {
                    return
                }

                val members = db.members(typing.groupId)

                if (members.none { it.personId == thisConnection.person!!.id }) {
                    return
                }

                relay(members.filter {
                    it.personId != thisConnection.person!!.id
                }, Frame.Text(json.encodeToString(typing.also {
                    it.name = thisConnection.person!!.name
                })))
            }

            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()

                        if (text.contains("\"token\":")) {
                            thisConnection.person = db.authWithToken(json.decodeFromString<TokenMessage>(text).token)?.email?.let {
                                db.personWithEmail(it)
                            }
                        } else if (text.contains("\"typing\":")) {
                            receiveTyping(json.decodeFromString(text))
                        } else {
                            receiveMessage(json.decodeFromString(text))
                        }

                        if (text.equals("bye", ignoreCase = true)) {
                            close(CloseReason(CloseReason.Codes.NORMAL, "bye"))
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
