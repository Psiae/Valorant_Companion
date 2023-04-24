package dev.flammky.valorantcompanion.pvp.ext

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

val JsonElement.jsonPrimitiveOrNull
    get() = this as? JsonPrimitive

val JsonElement.jsonObjectOrNull
    get() = this as? JsonObject

val JsonElement.jsonArrayOrNull
    get() = this as? JsonArray