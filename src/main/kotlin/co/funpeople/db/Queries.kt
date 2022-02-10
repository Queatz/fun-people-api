package co.funpeople.db

import co.funpeople.models.Location

fun Db.locationWithName(name: String) = one(
    Location::class, """
        for x in @@collection
            filter x.${Location::name.name} == @name
            return MERGE(x, {
                path: x.locationId == null ? [] : [ document(x.locationId) ]
            })
    """, mapOf(
        "name" to name
    )
)

fun Db.locationWithNameAndParent(name: String, locationId: String?) = one(
    Location::class, """
        for x in @@collection
            filter x.${Location::name.name} == @name
            filter x.${Location::locationId.name} == @locationId
            return MERGE(x, {
                path: x.locationId == null ? [] : [ document(x.locationId) ]
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
              for l in @@collection
                filter l.name == p.name && document(l.${Location::locationId.name}).${Location::name.name} == p.locationName
                return l
            )
        )
        filter path all != null
        let x = last(path) 
        return MERGE(x, {
                path: x.locationId == null ? [] : [ document(x.locationId) ]
            })
    """, mapOf(
        "path" to path
    )
)

fun Db.locationWithUrl(url: String) = one(
    Location::class, """
        for x in @@collection
            filter x.${Location::url.name} == @url
            return MERGE(x, {
                path: x.locationId == null ? [] : [ document(x.locationId) ]
            })
    """, mapOf(
        "url" to url
    )
)
