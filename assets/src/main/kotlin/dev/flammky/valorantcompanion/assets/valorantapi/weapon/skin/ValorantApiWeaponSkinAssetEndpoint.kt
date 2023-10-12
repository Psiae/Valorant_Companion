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
        return "$SKINS_IMAGE_URL/$id/$typeName.$ext"
    }

    suspend fun active(): Boolean {
        var active = false
        httpClient.get(
            ENDPOINT_STATUS_URL,
            sessionHandler = {if (httpStatusCode == 404) {
                    val bb = ByteBuffer.allocate(1.kiloByteUnit().bytes().toInt())
                    consume(bb)
                    if (bb.position() < 1) return@get
                    runCatching {
                        val json = Json
                            .decodeFromString<JsonElement>(
                                string = String(bb.apply { flip() }.moveToByteArray())
                            )
                        json
                            .expectJsonObject("VALORANT_API_WEAPON_SKIN_ENDPOINT_STATUS_RESPONSE")
                        json
                            .expectJsonProperty("status")
                            .expectJsonPrimitive("")
                            .content
                            .also { status ->
                                if (status != "404") return@get
                            }
                        json.expectJsonProperty("error")
                            .expectJsonPrimitive("")
                            .content
                            .also { str ->
                                if (str != "the requested uuid was not found") return@get
                            }
                        active = true
                    }.onFailure {
                        if (BuildConfig.DEBUG) it.printStackTrace()
                    }
                }
            }
        )
        return active
    }

    companion object {
        val BASE_URL = "https://valorant-api.com"
        val BASE_MEDIA_URL = "https://media.valorant-api.com"

        val SKINS_URL = "$BASE_URL/v1/weapons/skinlevels"
        val SKINS_IMAGE_URL = "$BASE_MEDIA_URL/weaponskinlevels"

        val ENDPOINT_STATUS_URL = "$SKINS_URL/%7BweaponSkinUuid%7D"

        val ID = "valorant-api"
    }
}