package co.funpeople.db

import co.funpeople.models.*
import com.arangodb.model.PersistentIndexOptions

fun collections() = listOf(
    Group::class.db {},
    Location::class.db {
        ensurePersistentIndex(listOf("name"), PersistentIndexOptions())
        ensurePersistentIndex(listOf("name", "locationId"), PersistentIndexOptions().unique(true))
        ensurePersistentIndex(listOf("locationId"), PersistentIndexOptions())
        ensurePersistentIndex(listOf("url"), PersistentIndexOptions().unique(true))
    },
    Member::class.db {
        ensurePersistentIndex(listOf("groupId"), PersistentIndexOptions())
        ensurePersistentIndex(listOf("personId"), PersistentIndexOptions())
    },
    Message::class.db {
        ensurePersistentIndex(listOf("groupId"), PersistentIndexOptions())
        ensurePersistentIndex(listOf("personId"), PersistentIndexOptions())
    },
    Person::class.db {
        ensurePersistentIndex(listOf("email"), PersistentIndexOptions())
    },
    Post::class.db {
        ensurePersistentIndex(listOf("locationId"), PersistentIndexOptions())
        ensurePersistentIndex(listOf("personId"), PersistentIndexOptions())
    },
    Idea::class.db {}
)
