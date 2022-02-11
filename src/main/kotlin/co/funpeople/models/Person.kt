package co.funpeople.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class Person : Model() {
    var name = ""
    var email: String? = null
    var introduction = ""
    var seen: Instant? = null
}
