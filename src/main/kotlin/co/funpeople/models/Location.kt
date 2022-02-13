package co.funpeople.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class Location : Model() {
    var locationId: String? = null
    var ownerId: String? = null
    var name = ""
    var description = ""
    var url = ""
    var system: Boolean? = null
    var activity: Instant? = null

    // Db
    var path: List<Location>? = null
}

