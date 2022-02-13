package co.funpeople.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class Auth : Model() {
    var token = ""
    var email = ""
    var accessed: Instant = Clock.System.now()
}
