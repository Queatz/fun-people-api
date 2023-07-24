package co.funpeople.services

import co.funpeople.db.unreadMessages
import co.funpeople.plugins.db
import co.funpeople.plugins.emailing
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

class ScheduledEmails {
    suspend fun start(coroutineContext: CoroutineContext) {
        delayUntil8amTomorrow()

        while (coroutineContext.isActive) {
            try {
                db.unreadMessages().also {
                    Logger.getGlobal().info("Sending unread messages reminders to ${it.size} emails")
                }.filter {
                    it.unreadCount > 0
                }.forEach {
                    emailing.send(
                        it.person!!.email!!,
                        "Visit https://hangoutville.com to read them.",
                        "You have ${it.unreadCount} unread ${if (it.unreadCount == 1) "message" else "messages" }"
                    )
                }

                delayUntil8amTomorrow()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun delayUntil8amTomorrow() =  delayUntil(
        Instant.now()
            .truncatedTo(ChronoUnit.DAYS)
            .plus(1, ChronoUnit.DAYS)
            .plus(8, ChronoUnit.HOURS)
    )
}

suspend fun delayUntil(instant: Instant) =
    delay((instant.toEpochMilli() - Instant.now().toEpochMilli()).coerceAtLeast(0))
