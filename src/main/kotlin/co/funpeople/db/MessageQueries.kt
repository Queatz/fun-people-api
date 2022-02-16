package co.funpeople.db

import co.funpeople.models.Group
import co.funpeople.models.Member
import co.funpeople.models.Message
import co.funpeople.models.Person

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

fun Db.unreadMessages() = query(
    PersonUnreadCount::class, """
        for person in ${Person::class.collection()}
            let unread = sum(
                for member in ${Member::class.collection()}
                        filter member.${Member::personId.name} == person._id
                    for group in ${Group::class.collection()}
                        filter member.${Member::groupId.name} == group._id
                        return count(
                            for message in ${Message::class.collection()}
                                filter message.${Message::groupId.name} == group._id
                                    and message.${Message::createdAt.name} > date_add(member.${Member::readUntil.name}, 1, 'day')
                                return true
                        )
        )
        filter unread > 0
        return {
            unreadCount: unread,
            person: person
        }
    """
)

@kotlinx.serialization.Serializable
class PersonUnreadCount {
    var person: Person? = null
    var unreadCount = 0
}
