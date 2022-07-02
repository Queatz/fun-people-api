package co.funpeople.db

import co.funpeople.models.Person

fun Db.personWithEmail(email: String) = one(
    Person::class, """
        upsert { ${f(Person::email)}: @email }
            insert { ${f(Person::email)}: @email, ${f(Person::seen)}: DATE_ISO8601(DATE_NOW()), ${f(Person::createdAt)}: DATE_ISO8601(DATE_NOW()) }
            update { ${f(Person::seen)}: DATE_ISO8601(DATE_NOW()) }
            in @@collection
            return NEW || OLD
    """,
    mapOf(
        "email" to email
    )
)
