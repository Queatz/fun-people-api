package co.funpeople.models

import kotlinx.serialization.Serializable

@Serializable
class Message : Model() {
    var groupId = ""
    var personId = ""
    var text = ""
}
