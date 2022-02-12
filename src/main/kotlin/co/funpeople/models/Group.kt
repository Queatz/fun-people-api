package co.funpeople.models

import kotlinx.serialization.Serializable

@Serializable
class Group : Model() {
    // Db
    var members: List<Member>? = null
    var latest: Message? = null
}
