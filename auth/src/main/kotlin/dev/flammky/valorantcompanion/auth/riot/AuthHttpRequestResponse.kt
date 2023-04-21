package dev.flammky.valorantcompanion.auth.riot

import kotlinx.serialization.json.JsonElement

data class AuthHttpRequestResponse(
    val status: Int,
    val body: JsonElement
)
