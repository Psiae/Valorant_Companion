package dev.flammky.valorantcompanion.pvp.riot.store

import dev.flammky.valorantcompanion.base.kt.mapToIndexed
import dev.flammky.valorantcompanion.pvp.http.json.*
import dev.flammky.valorantcompanion.pvp.http.json.expectJsonObject
import dev.flammky.valorantcompanion.pvp.http.json.expectJsonPrimitive
import dev.flammky.valorantcompanion.pvp.http.json.expectJsonProperty
import dev.flammky.valorantcompanion.pvp.http.json.expectNonBlankJsonString
import dev.flammky.valorantcompanion.pvp.store.ItemType
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreEntitledItems
import dev.flammky.valorantcompanion.pvp.store.internal.ValorantStoreEndpointResponseParser
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.nio.charset.Charset

internal class RiotValorantStoreResponseParser: ValorantStoreEndpointResponseParser {

    override fun parseGetEntitledItemResponse(
        raw: ByteArray,
        charset: Charset?
    ): Result<ValorantStoreEntitledItems> {
        return runCatching {
            val str = String(raw, charset ?: Charsets.UTF_8)
            val element = Json.decodeFromString<JsonElement>(str)
            kotlinxParseGetEntitledItemResponse(jsonElement = element)
        }
    }

    fun kotlinxParseGetEntitledItemResponse(
        jsonElement: JsonElement
    ): ValorantStoreEntitledItems {
        val rootTreePropertyName = "GetEntitledItemResponse"
        var treePropertyName = rootTreePropertyName
        val obj = jsonElement.expectJsonObject("GetEntitledItemResponse")
        return ValorantStoreEntitledItems(
            type = run {
                val snapTreePropertyName = treePropertyName
                val typeUUID = run {
                    val propertyName = "ItemTypeID"
                    treePropertyName += ";$propertyName"
                    obj
                        .expectJsonProperty(propertyName)
                        .expectJsonPrimitive(treePropertyName)
                        .expectNonBlankJsonString(treePropertyName)
                        .content
                }
                run {
                    ItemType
                        .ofUUID(typeUUID)
                        ?: ItemType.Other(
                            name = "UNKNOWN",
                            id = typeUUID
                        )
                }.also {
                    treePropertyName = snapTreePropertyName
                }
            },
            itemUUIDs = run {
                val snapTreePropertyName = treePropertyName
                val propertyName = "Entitlements"
                treePropertyName += ";$propertyName"
                obj
                    .expectJsonProperty(propertyName)
                    .expectJsonArray(treePropertyName)
                    .mapToIndexed(
                        persistentSetOf<String>().builder(),
                        transform = { index, element ->
                            val snapTreePropertyName = treePropertyName
                            treePropertyName += "[$index]"
                            run {
                                val propertyName = "ItemID"
                                element
                                    .expectJsonObjectAsJsonArrayElement(treePropertyName)
                                    .expectJsonProperty(propertyName)
                                    .expectJsonPrimitive(treePropertyName)
                                    .expectNonBlankJsonString(treePropertyName)
                                    .content
                            }.also {
                                treePropertyName = snapTreePropertyName
                            }
                        }
                    )
                    .build()
                    .also {
                        treePropertyName = snapTreePropertyName
                    }
            },
        )
    }
}