package co.funpeople.db

import co.funpeople.models.Group
import co.funpeople.models.Message

fun Db.messagesOfGroup(groupId: String) = list(
    Message::class, """
        for x in @@collection
            filter x.${Message::groupId.name} == @groupId
            sort x.createdAt desc
            limit 20
            return merge(x, {
                person: unset(document(x.personId), 'email', 'seen'),
                location: document(x.locationId)
            })
    """,
    mapOf("groupId" to groupId.asId(Group::class))
)
