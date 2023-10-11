package dev.flammky.valorantcompanion.assets.spray

import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.*
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.nio.charset.Charset

class KtxValorantSprayAssetSerializer : ValorantSprayAssetSerializer {

    override fun deserializeIdentity(
        uuid: String,
        raw: ByteArray,
        charset: Charset
    ): Result<ValorantSprayAssetIdentity> {
        return runCatching {
            deserializeIdentity(uuid, String(raw, charset)).getOrThrow()
        }
    }

    override fun deserializeIdentity(
        uuid: String,
        raw: String
    ): Result<ValorantSprayAssetIdentity> {
        return runCatching {
            val element = Json.parseToJsonElement(raw)
            deserializeIdentity(uuid, element).getOrThrow()
        }
    }

    fun deserializeIdentity(
        uuid: String,
        element: JsonElement,
    ): Result<ValorantSprayAssetIdentity> {
        return runCatching {

            val obj = element
                .expectJsonObject("ValorantSprayAssetIdentity")

            ValorantSprayAssetIdentity(
                uuid = run {
                    obj
                        .expectJsonProperty(
                            "uuid",
                            "UUID",
                        )
                        .expectJsonPrimitive("uuid")
                        .expectNonBlankJsonString("uuid")
                        .content.also {
                            if (it != uuid) unexpectedJsonValueError("uuid", "uuid mismatch")
                        }
                },
                displayName = run {
                    obj
                        .expectJsonProperty(
                            "displayname",
                            "displayName"
                        )
                        .expectJsonPrimitive("displayname")
                        .expectNonBlankJsonString("displayname")
                        .content
                },
                category = run {
                    obj
                        .expectJsonProperty(
                            "category"
                        )
                        .jsonNullable()
                        .let { element ->
                            val content = element
                                ?.expectJsonPrimitive("category")
                                ?.expectNonBlankJsonString("category")
                                ?.content
                            ValorantSprayAssetIdentity.Category.parse(content)
                        }
                },
                levels = run {
                    obj
                        .expectJsonProperty(
                            "levels"
                        )
                        .jsonNullable()
                        ?.let { element ->
                            val builder = persistentListOf<ValorantSprayAssetIdentity.Level>().builder()
                            element
                                .expectJsonArray("levels")
                                .forEach { arrayElement ->
                                    val levelObj = arrayElement
                                        .expectJsonObjectAsJsonArrayElement("levels")
                                    ValorantSprayAssetIdentity.Level(
                                        uuid = run {
                                            levelObj
                                                .expectJsonProperty("uuid")
                                                .expectJsonPrimitive("levels[]_uuid")
                                                .expectNonBlankJsonString("levels[]_uuid")
                                                .content
                                        },
                                        sprayLevel = run {
                                            levelObj
                                                .expectJsonProperty("sprayLevel")
                                                .expectJsonPrimitive("levels[]_sprayLevel")
                                                .expectJsonNumberParseInt("levels[]_sprayLevel")
                                        },
                                        displayName = run {
                                            levelObj
                                                .expectJsonProperty(
                                                    "displayname",
                                                    "displayName"
                                                )
                                                .expectJsonPrimitive("levels[]_displayName")
                                                .expectNonBlankJsonString("levels[]_displayName")
                                                .content
                                        }
                                    ).also { builder.add(it) }
                                }
                            builder.build()
                        }
                        ?: persistentListOf()
                }
            )
        }
    }
}