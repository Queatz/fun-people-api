package co.funpeople.plugins

import co.funpeople.db.*
import co.funpeople.models.*
import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import java.util.logging.Logger
import kotlin.random.Random

val db = Db()

// todo move to secrets.json
const val mapboxToken = "pk.eyJ1IjoiamFjb2JmZXJyZXJvIiwiYSI6ImNraXdyY211eTBlMmcycW02eDNubWNpZzcifQ.1KtSoMzrPCM0A8UVtI_gdg"

val signInCodes = mutableMapOf<String, String>()
val sessions = mutableMapOf<String, String>()

fun Application.configureRouting() {
    install(Authentication) {
        bearer {
            validate {
                sessions[it.token]?.let {
                    db.personWithEmail(it)
                }?.let {
                    PersonPrincipal(it)
                }
            }
        }
    }

    routing {
        post("/signin") {
            call.receive<Signin>().let {
                call.respond(if (!it.email.isEmailAddress()) {
                    HttpStatusCode.BadRequest
                } else {
                    when (it.code) {
                        null -> {
                            val code = Random.nextInt(100000, 999999).toString()
                            signInCodes[it.email.trim()] = code

                            // todo email code

                            Logger.getGlobal().info("Sign in code sent: $code")

                            HttpStatusCode.OK
                        }
                        else -> {
                            if (signInCodes[it.email.trim()] != it.code?.trim()) {
                                HttpStatusCode.NotFound
                            } else {
                                val token = (1..64).token()
                                sessions[token] = it.email.trim()

                                Token(token, db.personWithEmail(it.email.trim()))
                            }
                        }
                    }
                })
            }
        }


        get("/location-url/{url}") {
            call.respond(db.locationWithUrl(call.parameters["url"]!!.lowercase()) ?: HttpStatusCode.NotFound)
        }

        get("/top-locations") {
            call.respond(db.topLocations())
        }

        get("/location-name/{path...}") {
            val path = call.parameters.getAll("path")!!
            val location = db.locationWithName(path)

            if (location != null) {
                call.respond(location)
            } else {
                call.respond(path.fold<String, Location?>(null) { acc, n ->
                    db.locationWithNameAndParent(n, acc?.id) ?: db.insert(Location().apply {
                        name = n
                        locationId = acc?.id
                        system = true

                        url = name.nextUrl()
                        this.path = acc?.let { listOf(it) }
                    })
                } ?: HttpStatusCode.NotFound)
            }
        }

        get("/location") {
            call.respond(db.locationWithName("") ?: HttpStatusCode.NotFound)
        }

        get("/location/{id}") {
            call.respond(db.document(Location::class, call.parameters["id"]!!) ?: HttpStatusCode.NotFound)
        }

        get("/location/{id}/locations") {
            call.respond(db.locationsOfLocation(call.parameters["id"]!!))
        }

        get("/location/{id}/posts") {
            call.respond(db.postsByLocation(call.parameters["id"]!!))

        }

        get("/search/{query}") {
            val result = MapboxGeocoding.builder()
                .accessToken(mapboxToken)
                .autocomplete(true)
                .query(call.parameters["query"]!!)
                .geocodingTypes(GeocodingCriteria.TYPE_PLACE)
                .build()
                .executeCall()

            call.respond(result.body()!!.features().map {
                db.locationWithName(it.path() + it.text()!!) ?: Location().apply {
                    name = it.text() ?: ""
                    path = it.path().map { Location().apply {
                        name = it
                    } }
                }
            })
        }

        get("/person/{id}") {
            call.respond(db.document(Person::class, call.parameters["id"]!!) ?: HttpStatusCode.NotFound)
        }

        authenticate {
            get("/me") {
                call.respond(call.principal<PersonPrincipal>()!!.person)
            }

            get("/me/posts") {
                call.respond(db.postsByPerson(call.principal<PersonPrincipal>()!!.person.id!!))
            }

            post("/me") {
                call.receive<Person>().let {
                    val person = call.principal<PersonPrincipal>()!!.person

                    it.name.trim().takeIf { it.isNotBlank() }?.let { person.name = it }
                    it.introduction.trim().takeIf { it.isNotBlank() }?.let { person.introduction = it }

                    person.seen = Clock.System.now()

                    call.respond(db.update(person))
                }
            }

            get("/groups") {
                call.respond(db.groups(call.principal<PersonPrincipal>()!!.person.id!!))
            }

            get("/group/{id}/messages") {
                val groupId = call.parameters["id"]!!
                val person = call.principal<PersonPrincipal>()!!.person
                val member = db.members(groupId).firstOrNull { it.personId == person.id }

                if (member == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                member.readUntil = Clock.System.now()
                db.update(member)

                call.respond(db.messagesOfGroup(groupId))
            }

            post("/location/{id}") {
                call.receive<Location>().also {
                    val location = db.document(Location::class, call.parameters["id"]!!)

                    if (location == null) {
                        call.respond(HttpStatusCode.NotFound)
                        return@also
                    }

                    if (it.description.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest.description("Missing 'description'"))
                        return@also
                    }

                    location.description = it.description.trim()

                    call.respond(db.update(location).also {
                        if (it.locationId != null) {
                            it.path = listOfNotNull(db.document(Location::class, it.locationId!!))
                        }
                    })
                }
            }

            post("/locations") {
                call.receive<Location>().also {
                    if (it.name.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest.description("Missing 'text'"))
                        return@also
                    }

                    if (it.locationId.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest.description("Missing 'locationId'"))
                        return@also
                    }

                    it.url = it.name.nextUrl()
                    it.createdAt = Clock.System.now()

//                    if (managed) it.ownerId = me

                    call.respond(db.insert(it))
                }
            }

            post("/posts") {
                call.receive<Post>().also {
                    if (it.text.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest.description("Missing 'text'"))
                        return@also
                    }

                    if (it.locationId.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest.description("Missing 'locationId'"))
                        return@also
                    }

                    it.createdAt = Clock.System.now()
                    it.personId = call.principal<PersonPrincipal>()!!.person.id!!

                    if (db.postsByPersonAndLocation(it.personId, it.locationId).isNotEmpty()) {
                        call.respond(HttpStatusCode.BadRequest.description("Post already exists in this location"))
                        return@also
                    }

                    call.respond(db.insert(it))
                }
            }

            post("/post/{id}") {
                call.receive<Post>().let {
                    val person = call.principal<PersonPrincipal>()!!.person
                    val post = db.document(Post::class, call.parameters["id"]!!)

                    call.respond(
                        if (post == null) {
                            HttpStatusCode.NotFound
                        } else if (post.personId != person.id) {
                            HttpStatusCode.NotFound
                        } else {
                            it.text.trim().takeIf { it.isNotBlank() }?.let { post.text = it }

                            db.update(post)
                        }
                    )
                }
            }

            post("/post/{id}/reply") {
                call.receive<Message>().let {
                    val person = call.principal<PersonPrincipal>()!!.person
                    val post = db.document(Post::class, call.parameters["id"]!!)

                    call.respond(
                        if (it.text.isBlank()) {
                            HttpStatusCode.BadRequest.description("Missing 'text'")
                        } else if (post == null) {
                            HttpStatusCode.NotFound
                        } else if (post.personId == person.id) {
                            HttpStatusCode.BadRequest
                        } else {
                            val group = db.groupOf(listOf(person.id!!, post.personId)) ?: db.insert(Group()).also { group ->
                                db.insert(Member().also {
                                    it.groupId = group.id!!
                                    it.personId = person.id!!
                                    it.readUntil = Clock.System.now()
                                })
                                db.insert(Member().also {
                                    it.groupId = group.id!!
                                    it.personId = post.personId
                                })
                            }

                            Message().apply {
                                postId = post.id
                                locationId = post.locationId
                                groupId = group.id!!
                                personId = person.id!!
                                text = it.text
                            }.let { db.insert(it) }

                            db.member(group.id!!, person.id!!)?.let {
                                it.readUntil = Clock.System.now()
                                db.update(it)
                            }

                            HttpStatusCode.OK
                        }
                    )
                }
            }

            post("/post/{id}/remove") {
                val person = call.principal<PersonPrincipal>()!!.person
                val post = db.document(Post::class, call.parameters["id"]!!)

                call.respond(
                    if (post == null) {
                        HttpStatusCode.NotFound
                    } else if (post.personId != person.id) {
                        HttpStatusCode.NotFound
                    } else {
                        db.delete(post)

                        HttpStatusCode.OK
                    }
                )
            }

            post("/messages") {

            }

            post("/message/{id}") {
                // todo delete
            }

            post("/ideas") {
                call.receive<Idea>().also {
                    if (it.idea.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@also
                    }

                    val idea = Idea().apply {
                        personId = call.principal<PersonPrincipal>()!!.person.id!!
                        idea = it.idea.trim()
                    }

                    db.insert(idea)

                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}

private fun String.nextUrl() = ((generateSequence(1) { it + 1 }
    .takeWhile {
        db.locationWithUrl(if (it == 1) {
            asUrl()
        } else "${asUrl()}$it") != null
    }.lastOrNull() ?: 0) + 1).let {
    if (it == 1) {
        asUrl()
    } else "${asUrl()}-$it"
}

private fun String.asUrl() = lowercase().replace("'", "").replace("[^\\p{L}]+".toRegex(), "-")
    .dropWhile { it == '-' }
    .dropLastWhile { it == '-' }

private fun CarmenFeature.path() = context()
    ?.filter {
        !it.text().isNullOrEmpty() && !it.id()!!.startsWith("district.")
    }
    ?.map { it.text()!! }
    ?.asReversed() ?: listOf()

private val emailAddressPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()

private fun String.isEmailAddress() = this.matches(emailAddressPattern)

private fun IntRange.token() = joinToString("") { Random.nextInt(35).toString(36) }


@Serializable
private class Signin (
    var email: String = "",
    var code: String? = null
)

@Serializable
private class Token (
    var token: String = "",
    var person: Person?
)

class PersonPrincipal(
    val person: Person
) : Principal
