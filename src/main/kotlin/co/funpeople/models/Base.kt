package co.funpeople.models

import com.arangodb.entity.DocumentField
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
open class Model {
    @DocumentField(DocumentField.Type.ID)
    @SerialName(value = "id")
    @JsonNames("_id")
    var id: String? = null
    var createdAt: Instant = Clock.System.now()
}

@Serializable
open class Edge(
    @DocumentField(DocumentField.Type.FROM)
    @SerialName(value = "from")
    @JsonNames("_from")
    var from: String? = null,

    @DocumentField(DocumentField.Type.TO)
    @SerialName(value = "to")
    @JsonNames("_to")
    var to: String? = null
) : Model()
