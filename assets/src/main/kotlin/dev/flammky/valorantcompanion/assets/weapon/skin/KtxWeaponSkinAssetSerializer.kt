package dev.flammky.valorantcompanion.assets.weapon.skin

import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.*
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonObject
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonPrimitive
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonProperty
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectNonBlankJsonString
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.nio.charset.Charset

class KtxWeaponSkinAssetSerializer : WeaponSkinAssetSerializer {

    override fun deserializeIdentity(
        raw: ByteArray,
        charset: Charset
    ): Result<WeaponSkinIdentity> {
        return runCatching {
            deserializeIdentity(String(raw, charset)).getOrThrow()
        }
    }

    override fun deserializeIdentity(
        raw: String
    ): Result<WeaponSkinIdentity> {
        return runCatching {
            val element = Json.parseToJsonElement(raw)
            deserializeIdentity(element).getOrThrow()
        }
    }

    fun deserializeIdentity(
        element: JsonElement,
    ): Result<WeaponSkinIdentity> {
        return runCatching {

            val obj = element
                .expectJsonObject("WeaponSkinAssetIdentity")

            WeaponSkinIdentity(
                uuid = run {
                    obj
                        .expectJsonProperty(
                            "uuid",
                            "UUID",
                        )
                        .expectJsonPrimitive("uuid")
                        .expectNonBlankJsonString("uuid")
                        .content
                },
                displayName = run {
                    obj
                        .expectJsonProperty(
                            "displayname",
                            "displayName"
                        )
                        .expectJsonPrimitive("displayName")
                        .expectNonBlankJsonString("displayName")
                        .content
                },
                tier = run {
                    val tierUUID = obj
                        .expectJsonProperty(
                            "contentTierUuid",
                            "contentTierUUID",
                            "contenttieruuid"
                        )
                        .ifJsonNull { return@run WeaponSkinTier.NONE }
                        .expectJsonPrimitive("contentTierUUID")
                        .expectNonBlankJsonString("contentTierUUID")
                        .content
                    when (tierUUID) {
                        WeaponSkinTier.SELECT.uuid -> WeaponSkinTier.SELECT
                        WeaponSkinTier.DELUXE.uuid -> WeaponSkinTier.DELUXE
                        WeaponSkinTier.PREMIUM.uuid -> WeaponSkinTier.PREMIUM
                        WeaponSkinTier.EXCLUSIVE.uuid -> WeaponSkinTier.EXCLUSIVE
                        WeaponSkinTier.ULTRA.uuid -> WeaponSkinTier.ULTRA
                        else -> WeaponSkinTier.UNKNOWN
                    }
                }
            )
        }
    }

    override fun deserializeSkinsAssets(raw: String): Result<WeaponSkinsAssets> {
        return runCatching {
            val element = Json.parseToJsonElement(raw)
            deserializeSkinsAssets(element).getOrThrow()
        }
    }

    private fun deserializeSkinsAssets(
        element: JsonElement
    ): Result<WeaponSkinsAssets> {
        return runCatching {

            WeaponSkinsAssets(
                version = "",
                items = element
                    .expectJsonArray("WeaponSkinsAssets;data")
                    .associateTo(persistentMapOf<String, WeaponSkinsAssets.Item>().builder()) { element ->
                        val id = element
                            .expectJsonObjectAsJsonArrayElement("WeaponSkinsAssets;data")
                            .expectJsonProperty("uuid")
                            .expectJsonPrimitive("WeaponSkinsAssets;data;uuid")
                            .expectNonBlankJsonString("WeaponSkinsAssets;data;uuid")
                            .content
                        id to WeaponSkinsAssets.Item(
                            uuid = id,
                            displayName = run {
                                element
                                    .expectJsonProperty("displayName")
                                    .expectJsonPrimitive("WeaponSkinsAssets;data;displayName")
                                    .expectNonBlankJsonString("WeaponSkinsAssets;data;displayName")
                                    .content
                            },
                            contentTierUUID = run {
                                element
                                    .expectJsonProperty("contentTierUuid")
                                    .ifJsonNull { return@run null }
                                    .expectJsonPrimitive("WeaponSkinsAssets;data;contentTierUuid")
                                    .expectNonBlankJsonString("WeaponSkinsAssets;data;contentTierUuid")
                                    .content
                            },
                            gameAssetPath = run {
                                element
                                    .expectJsonProperty("assetPath")
                                    .expectJsonPrimitive("WeaponSkinsAssets;data;assetPath")
                                    .expectNonBlankJsonString("WeaponSkinsAssets;data;assetPath")
                                    .content
                            },
                            chromas = run {
                                element
                                    .expectJsonProperty("chromas")
                                    .expectJsonArray("chromas")
                                    .associateTo(persistentMapOf<String, WeaponSkinsAssets.Item.Chroma>().builder()) { element ->
                                        val id = element
                                            .expectJsonObjectAsJsonArrayElement("WeaponSkinsAssets;data;chromas")
                                            .expectJsonProperty("uuid")
                                            .expectJsonPrimitive("WeaponSkinsAssets;data;chromas;uuid")
                                            .expectNonBlankJsonString("WeaponSkinsAssets;data;chromas;uuid")
                                            .content
                                        id to WeaponSkinsAssets.Item.Chroma(
                                            uuid = id
                                        )
                                    }
                                    .build()
                            },
                            levels = run {
                                element
                                    .expectJsonProperty("levels")
                                    .expectJsonArray("levels")
                                    .associateTo(persistentMapOf<String, WeaponSkinsAssets.Item.Level>().builder()) { element ->
                                        val id = element
                                            .expectJsonObjectAsJsonArrayElement("WeaponSkinsAssets;data;levels")
                                            .expectJsonProperty("uuid")
                                            .expectJsonPrimitive("WeaponSkinsAssets;data;levels;uuid")
                                            .expectNonBlankJsonString("WeaponSkinsAssets;data;levels;uuid")
                                            .content
                                        id to WeaponSkinsAssets.Item.Level(
                                            uuid = id
                                        )
                                    }
                                    .build()
                            }
                        )
                    }
                    .build()
            )
        }
    }
}