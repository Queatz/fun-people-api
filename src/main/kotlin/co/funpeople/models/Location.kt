package co.funpeople.models

import kotlinx.serialization.Serializable

@Serializable
open class Location : Model() {
    var locationId: String? = null
    var name = ""
    var description = ""
    var url = ""

    // Db
    var path: List<Location>? = null
}

