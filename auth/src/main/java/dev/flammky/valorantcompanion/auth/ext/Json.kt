package dev.flammky.valorantcompanion.auth.ext

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

fun JsonElement.asJsonObjectGetElementOrNull(key: String) = jsonObject[key]?.jsonObject

val JsonElement.jsonPrimitiveOrNull
    get() = this as? JsonPrimitive

val JsonElement.jsonObjectOrNull
    get() = this as? JsonObject