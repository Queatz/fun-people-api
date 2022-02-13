package co.funpeople.db

import co.funpeople.models.Auth

fun Db.authWithToken(token: String) = one(
    Auth::class, """
        for x in @@collection
            filter x.${Auth::token.name} == @token
            update { _key: x._key, accessed: DATE_ISO8601(DATE_NOW()) }
            in @@collection
            return NEW || OLD
    """, mapOf(
        "token" to token
    )
)
