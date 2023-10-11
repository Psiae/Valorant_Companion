package dev.flammky.valorantcompanion.assets.weapon.skin

import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonObject
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonPrimitive
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonProperty
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectNonBlankJsonString
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.unexpectedJsonValueError
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier
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
}