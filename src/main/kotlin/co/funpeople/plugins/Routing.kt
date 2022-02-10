package co.funpeople.plugins

import co.funpeople.db.Db
import co.funpeople.db.locationWithName
import co.funpeople.db.locationWithNameAndParent
import co.funpeople.db.locationWithUrl
import co.funpeople.models.Location
import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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

                        val u = name.asUrl()

                        // Find the next available sequence number
                        url = ((generateSequence(1) { it + 1 }
                            .takeWhile {
                                db.locationWithUrl(if (it == 1) u else "$u$it") != null
                            }.lastOrNull() ?: 0) + 1).let {
                                if (it == 1) u else "$u-$it"
                        }
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
        post("/location") {

        }
        post("/location/{id}") {

        }
        post("/post") {

        }
        post("/post/{id}") {

        }
        post("/messages") {

        }
        post("/message/{id}") {

        }
    }
}

private fun String.asUrl() = lowercase().replace("[^\\p{L}]+".toRegex(), "-")

private fun CarmenFeature.path() = context()
    ?.filter {
        !it.text().isNullOrEmpty() && !it.id()!!.startsWith("district.")
    }
    ?.map { it.text()!! }
    ?.asReversed() ?: listOf()
