package co.funpeople.db

import co.funpeople.models.Group
import co.funpeople.models.Member
import co.funpeople.models.Message

fun Db.groupOf(memberIds: List<String>) = one(Group::class, """
        for x in @@collection
            filter count(
                for member in ${Member::class.collection()}
                    filter member.${f(Member::groupId)} == x._id
                        and member.${f(Member::personId)} in @memberIds
                    return member
            ) == ${memberIds.size}
            limit 1
            return x
    """, mapOf(
        "memberIds" to memberIds
    )
)

fun Db.groups(personId: String) = list(Group::class, """
        for x in @@collection
            filter count(
                for member in ${Member::class.collection()}
                    filter member.${f(Member::groupId)} == x._id
                        and member.${f(Member::personId)} == @personId
                    return member
            ) == 1
            limit 20
            let latest = first(
                for message in ${Message::class.collection()}
                    filter message.${f(Member::groupId)} == x._id
                    sort message.createdAt desc
                    limit 1
                    return message
            )
            sort latest.createdAt desc
            limit 20
            return merge(x, {
                members: (
                    for member in ${Member::class.collection()}
                        filter member.${f(Member::groupId)} == x._id
                        return merge(member, {
                            person: unset(document(member.personId), 'email', 'seen')
                        })
                ),
                latest: latest
            })
    """, mapOf(
        "personId" to personId
    )
)
