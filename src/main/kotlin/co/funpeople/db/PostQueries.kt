package co.funpeople.db

import co.funpeople.models.Location
import co.funpeople.models.Post

fun Db.postsByLocation(locationId: String) = list(
    Post::class, """
        for x in @@collection
            filter x.${Post::locationId.name} == @locationId
            sort x.${Post::createdAt.name} desc
            return merge(x, {
                ${Post::person.name}: document(x.${Post::personId.name})
            })
    """,
    mapOf(
        "locationId" to locationId.asId(Location::class)
    )
)

fun Db.postsByPerson(personId: String) = list(
    Post::class, """
        for x in @@collection
            filter x.${Post::personId.name} == @personId
            return merge(x, {
                ${Post::location.name}: document(x.${Post::locationId.name})
            })
    """,
    mapOf(
        "personId" to personId.asId(Post::class)
    )
)
