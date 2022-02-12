package co.funpeople.models

import kotlinx.serialization.Serializable

@Serializable
class Idea : Model() {
    var personId = ""
    var idea = ""
}
