package co.funpeople.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class Member : Model() {
    var groupId = ""
    var personId = ""
    var readUntil = Instant.fromEpochMilliseconds(0)
}
