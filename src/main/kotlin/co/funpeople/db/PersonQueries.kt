package co.funpeople.db

import co.funpeople.models.Person

fun Db.personWithEmail(email: String) = one(
    Person::class, """
        upsert { ${Person::email.name}: @email }
            insert { ${Person::email.name}: @email, ${Person::seen.name}: DATE_ISO8601(DATE_NOW()), ${Person::createdAt.name}: DATE_ISO8601(DATE_NOW()) }
            update { ${Person::seen.name}: DATE_ISO8601(DATE_NOW()) }
            in @@collection OPTION { ignoreErrors: true }
            return NEW
    """,
    mapOf(
        "email" to email
    )
)
