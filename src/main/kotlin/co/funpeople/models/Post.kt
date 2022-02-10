package co.funpeople.models

import kotlinx.serialization.Serializable

@Serializable
class Post : Model() {
    var locationId = ""
    var personId = ""
    var text = ""

    // Db
    var person: Person? = null
}
