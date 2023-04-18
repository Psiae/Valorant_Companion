package dev.flammky.valorantcompanion.auth.ext

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

fun JsonElement.getJsonObject(key: String) = jsonObject[key]!!.jsonObject
fun JsonElement.getJsonObjectOrNull(key: String) = jsonObject[key]?.jsonObject

val JsonElement.jsonPrimitiveOrNull
    get() = this as? JsonPrimitive