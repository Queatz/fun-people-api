package co.funpeople.models

import kotlinx.serialization.Serializable

@Serializable
class Message : Model() {
    var postId: String? = null
    var locationId: String? = null
    var groupId = ""
    var personId = ""
    var text = ""

    var person: Person? = null
    var post: Post? = null
}
