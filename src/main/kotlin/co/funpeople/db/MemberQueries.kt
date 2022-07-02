package co.funpeople.db

import co.funpeople.models.Group
import co.funpeople.models.Member
import co.funpeople.models.Person

fun Db.members(groupId: String) = list(
    Member::class, """
        for x in @@collection
            filter x.${f(Member::groupId)} == @groupId
            return x
    """, mapOf(
        "groupId" to groupId.asId(Group::class)
    )
)

fun Db.member(groupId: String, personId: String) = one(
    Member::class, """
        for x in @@collection
            filter x.${f(Member::groupId)} == @groupId
                and x.${f(Member::personId)} == @personId
            return x
    """, mapOf(
        "groupId" to groupId.asId(Group::class),
        "personId" to personId.asId(Person::class)
    )
)
