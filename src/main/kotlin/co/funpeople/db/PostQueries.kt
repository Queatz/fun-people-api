package co.funpeople.db

import co.funpeople.models.Location
import co.funpeople.models.Post

fun Db.postsByLocation(locationId: String) = list(
    Post::class, """
        for x in @@collection
            filter x.${Post::locationId.name} == @locationId
            sort x.${Post::createdAt.name} desc
            return merge(x, {
                person: document(x.personId)
            })
    """,
    mapOf(
        "locationId" to locationId.asId(Location::class)
    )
)
