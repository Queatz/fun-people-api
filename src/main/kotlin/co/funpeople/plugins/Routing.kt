package co.funpeople.plugins

import co.funpeople.db.*
import co.funpeople.models.Location
import co.funpeople.models.Post
import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock

val db = Db()

// todo move to secrets.json
const val mapboxToken = "pk.eyJ1IjoiamFjb2JmZXJyZXJvIiwiYSI6ImNraXdyY211eTBlMmcycW02eDNubWNpZzcifQ.1KtSoMzrPCM0A8UVtI_gdg"

fun Application.configureRouting() {

    routing {
        get("/location-url/{url}") {
            call.respond(db.locationWithUrl(call.parameters["url"]!!) ?: HttpStatusCode.NotFound)
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

//                it.personId = me // creator

                call.respond(db.insert(it))
            }
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
        get("/groups") {

        }
        get("/group/{id}") {

        }

        // todo authentication

        get("/me") {

        }

        post("/me") {

        }
        post("/location/{id}") {

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

//                it.personId = me

                call.respond(db.insert(it))
            }
        }
        post("/post/{id}") {

        }
        post("/messages") {

        }
        post("/message/{id}") {

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

private fun String.asUrl() = lowercase().replace("[^\\p{L}]+".toRegex(), "-")

private fun CarmenFeature.path() = context()
    ?.filter {
        !it.text().isNullOrEmpty() && !it.id()!!.startsWith("district.")
    }
    ?.map { it.text()!! }
    ?.asReversed() ?: listOf()
