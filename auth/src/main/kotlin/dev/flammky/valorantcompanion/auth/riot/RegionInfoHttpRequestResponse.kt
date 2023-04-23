package dev.flammky.valorantcompanion.auth.riot

import kotlinx.serialization.json.JsonElement

data class RegionInfoHttpRequestResponse(
    val statusCode: Int,
    val body: JsonElement
) {
}