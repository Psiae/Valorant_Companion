package dev.flammky.valorantcompanion.pvp.http

import kotlinx.serialization.json.JsonElement

data class JsonHttpResponse(
    val statusCode: Int,
    val body: JsonElement
) {
}