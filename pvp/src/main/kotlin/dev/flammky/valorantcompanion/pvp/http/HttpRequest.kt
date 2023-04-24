package dev.flammky.valorantcompanion.pvp.http

import kotlinx.serialization.json.JsonElement

data class JsonHttpRequest(
    val url: String,
    val headers: List<Pair<String, String>>,
    val body: JsonElement?
)