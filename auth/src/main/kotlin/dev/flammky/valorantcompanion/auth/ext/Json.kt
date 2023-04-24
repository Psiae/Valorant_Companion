package dev.flammky.valorantcompanion.auth.ext

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

internal val JsonElement.jsonPrimitiveOrNull
    get() = this as? JsonPrimitive

internal val JsonElement.jsonObjectOrNull
    get() = this as? JsonObject