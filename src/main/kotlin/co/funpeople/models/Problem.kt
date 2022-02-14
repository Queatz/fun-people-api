package co.funpeople.models

import kotlinx.serialization.Serializable

@Serializable
class Problem : Model() {
    var personId = ""
    var groupId: String? = null
    var problem = ""
}
