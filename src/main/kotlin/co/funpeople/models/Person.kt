package co.funpeople.models

import kotlinx.serialization.Serializable

@Serializable
class Person : Model() {
    var locationId: String? = null
    var name = ""
    var email = ""
}
