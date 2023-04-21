package dev.flammky.valorantcompanion.auth.riot

import kotlinx.serialization.json.JsonElement

data class CookieHttpRequestResponse(
    val status: Int,
    val body: JsonElement
)
