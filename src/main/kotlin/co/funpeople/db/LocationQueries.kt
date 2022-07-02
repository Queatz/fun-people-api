package co.funpeople.db

import co.funpeople.models.Location

fun Db.locationWithName(name: String) = one(
    Location::class, """
        for x in @@collection
            filter x.${f(Location::name)} == @name
            return merge(x, {
                ${f(Location::path)}: x.locationId == null ? [] : [ document(x.locationId) ]
            })
    """, mapOf(
        "name" to name
    )
)

fun Db.updateLocationActivity(locationId: String) = one(
    Location::class, """
        for x in @@collection
            filter x._id == @locationId
            update { _key: x._key, activity: DATE_ISO8601(DATE_NOW()) } in @@collection
    """, mapOf(
        "locationId" to locationId.asId(Location::class)
    )
)

fun Db.locationWithNameAndParent(name: String, locationId: String?) = one(
    Location::class, """
        for x in @@collection
            filter x.${f(Location::name)} == @name
            filter x.${f(Location::locationId)} == @locationId
            return merge(x, {
                ${f(Location::path)}: x.${f(Location::locationId)} == null ? [] : [ document(x.${f(Location::locationId)}) ]
            })
    """, mapOf(
        "locationId" to locationId,
        "name" to name,
    )
)

fun Db.locationWithName(path: List<String>) = one(
    Location::class, """
        let z = (0..length(@path)-1)[* return {name: @path[CURRENT], locationName: CURRENT == 0 ? null : @path[CURRENT-1]}]

        let path = (
          for p in z
            return first(
              for l in @@collectionf(
                filter lf() == p) && document(l.${f(Location::locationId)}).${f(Location::name)} == p.locationName
                return l
            )
        )
        filter path all != null
        let x = last(path) 
        return merge(x, {
                ${f(Location::path)}: x.${f(Location::locationId)} == null ? [] : [ document(x.${f(Location::locationId)}) ]
            })
    """, mapOf(
        "path" to path
    )
)

fun Db.locationWithUrl(url: String) = one(
    Location::class, """
        for x in @@collection
            filter x.${f(Location::url)} == @url
            return merge(x, {
                ${f(Location::path)}: x.${f(Location::locationId)} == null ? [] : [ document(x.${f(Location::locationId)}) ]
            })
    """, mapOf(
        "url" to url
    )
)

fun Db.locationsOfLocation(locationId: String) = list(
    Location::class, """
        for x in @@collection
            filter x.${f(Location::locationId)} == @locationId
            sort x.${f(Location::activity)} desc
            limit 20
            return merge(x, {
                ${f(Location::path)}: x.${f(Location::locationId)} == null ? [] : [ document(x.${f(Location::locationId)}) ]
            })
    """, mapOf(
        "locationId" to locationId.asId(Location::class)
    )
)

fun Db.topLocations() = list(
    Location::class, """
        for x in @@collection
            sort rand()
            limit 5
            return merge(x, {
                ${f(Location::path)}: x.${f(Location::locationId)} == null ? [] : [ document(x.${f(Location::locationId)}) ]
            })
    """
)
