package co.funpeople.db

import co.funpeople.models.Group
import co.funpeople.models.Member

fun Db.members(groupId: String) = list(
    Member::class, """
        for x in @@collection
            filter x.${Member::groupId.name} == @groupId
            return x
    """, mapOf(
        "groupId" to groupId.asId(Group::class)
    )
)
