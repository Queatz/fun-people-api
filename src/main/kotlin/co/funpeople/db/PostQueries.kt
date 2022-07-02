package co.funpeople.db

import co.funpeople.models.Location
import co.funpeople.models.Person
import co.funpeople.models.Post
import kotlin.reflect.KMutableProperty1

fun Db.postsByLocation(locationId: String) = list(
    Post::class, """
        for x in @@collection
            filter x.${f(Post::locationId)} == @locationId
            sort x.${f(Post::createdAt)} desc
            limit 20
            return merge(x, {
                ${f(Post::person)}: unset(document(x.${f(Post::personId)}), 'email', 'seen')
            })
    """,
    mapOf(
        "locationId" to locationId.asId(Location::class)
    )
)

fun Db.postsByPerson(personId: String) = list(
    Post::class, """
        for x in @@collection
            filter x.${f(Post::personId)} == @personId
            return merge(x, {
                ${f(Post::location)}: document(x.${f(Post::locationId)})
            })
    """,
    mapOf(
        "personId" to personId.asId(Post::class)
    )
)

fun Db.postsByPersonAndLocation(personId: String, locationId: String) = list(
    Post::class, """
        for x in @@collection
            filter x.${f(Post::personId)} == @personId
                and x.${f(Post::locationId)} == @locationId
            return merge(x, {
                ${f(Post::location)}: document(x.${f(Post::locationId)})
            })
    """,
    mapOf(
        "personId" to personId.asId(Person::class),
        "locationId" to locationId.asId(Location::class),
    )
)
