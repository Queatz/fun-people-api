package co.funpeople.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class Person : Model() {
    var name = ""
    var email = ""
    var seen: Instant? = null
}
