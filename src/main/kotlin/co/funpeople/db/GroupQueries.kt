package co.funpeople.db

import co.funpeople.models.Group
import co.funpeople.models.Member

fun Db.groupOf(memberIds: List<String>) = one(Group::class, """
        for x in @@collection
            filter count(
                for member in ${Member::class.collection()}
                    filter member.${Member::groupId.name} == x._id
                        and member.${Member::personId.name} in @memberIds
                    return member
            ) == ${memberIds.size}
            limit 1
            return x
    """, mapOf(
        "memberIds" to memberIds
    )
)
