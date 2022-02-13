package co.funpeople.services

import co.funpeople.plugins.secrets
import io.ktor.http.*
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class Emailing {
    fun send(emailAddress: String, text: String, subject: String = "Message from Hangoutville") {
        val props = Properties().also { props ->
            props["mail.smtp.host"] = "smtp.gmail.com"
            props["mail.smtp.socketFactory.port"] = "465"
            props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.port"] = "465"
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication() = PasswordAuthentication(
                secrets.config.fromEmailAddress,
                secrets.config.fromEmailPassword
            )
        })

        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(secrets.config.fromEmailAddress, "Hangoutville.com"))
            message.addRecipient(Message.RecipientType.TO, InternetAddress(emailAddress))
            message.setSubject(subject, Charsets.UTF_8.name())
            message.setContent(text, ContentType.Text.Plain.toString())
            Transport.send(message)
        } catch (e: MessagingException) {
            e.printStackTrace()
        }
    }
}
