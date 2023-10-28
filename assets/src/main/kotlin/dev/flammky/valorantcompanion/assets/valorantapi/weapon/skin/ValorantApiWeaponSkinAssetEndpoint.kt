package dev.flammky.valorantcompanion.assets.valorantapi.weapon.skin

import dev.flammky.valorantcompanion.assets.BuildConfig
import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonObject
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonPrimitive
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonProperty
import dev.flammky.valorantcompanion.assets.weapon.skin.WeaponSkinEndpoint
import dev.flammky.valorantcompanion.assets.weapon.skin.WeaponSkinImageType
import dev.flammky.valorantcompanion.base.storage.kiloByteUnit
import io.ktor.util.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.io.IOException
import java.nio.ByteBuffer

class ValorantApiWeaponSkinAssetEndpoint(
    private val httpClientFactory: () -> AssetHttpClient
) : WeaponSkinEndpoint {

    private val httpClient by lazy(httpClientFactory)

    override val ID: String
        get() = Companion.ID

    override fun buildIdentityUrl(id: String): String {
        return "$SKINS_URL/$id"
    }

    override fun buildImageUrl(id: String, type: WeaponSkinImageType): String {
        val typeName = when(type) {
            WeaponSkinImageType.DISPLAY_SMALL -> "displayicon"
            WeaponSkinImageType.RENDER_FULL -> "fullrender"
            WeaponSkinImageType.NONE -> RuntimeException("NONE WeaponSkinImageType")
        }
        val ext = "png"
        return "$SKINLEVELS_IMAGE_URL/$id/$typeName.$ext"
    }

    suspend fun active(): Boolean {
        var active = false
        try {
            httpClient.get(
                ENDPOINT_STATUS_URL,
                sessionHandler = {
                    if (httpStatusCode == 200) {
                        if (contentType.equals("application", true) &&
                            contentSubType.equals("json", true)) {
                            active = true
                        }
                    }
                }
            )
        } catch (io: IOException) {

        }
        return active
    }

    override fun buildAllSkinsUrl(): String {
        return SKINS_URL
    }

    companion object {
        val BASE_URL = "https://valorant-api.com"
        val BASE_MEDIA_URL = "https://media.valorant-api.com"

        val SKINS_URL = "$BASE_URL/v1/weapons/skins"

        val SKINLEVELS_URL = "$BASE_URL/v1/weapons/skinlevels"
        val SKINLEVELS_IMAGE_URL = "$BASE_MEDIA_URL/weaponskinlevels"

        val ENDPOINT_STATUS_URL = "https://valorant-api.com/v1/version"

        val ID = "valorant-api"
    }
}