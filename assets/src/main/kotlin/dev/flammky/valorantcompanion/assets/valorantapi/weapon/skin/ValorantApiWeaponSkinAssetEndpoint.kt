package dev.flammky.valorantcompanion.assets.valorantapi.weapon.skin

import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonObject
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonPrimitive
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonProperty
import dev.flammky.valorantcompanion.assets.weapon.skin.WeaponSkinAssetEndpoint
import dev.flammky.valorantcompanion.base.storage.kiloByteUnit
import io.ktor.util.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.nio.ByteBuffer

class ValorantApiWeaponSkinAssetEndpoint(
    private val httpClientFactory: () -> AssetHttpClient
) : WeaponSkinAssetEndpoint {

    private val httpClient by lazy(httpClientFactory)

    override fun buildUrl(id: String): String {
        return "$SKINS_URL/$id"
    }

    suspend fun active(): Boolean {
        var active = false
        httpClient.get(
            ENDPOINT_STATUS_URL,
            sessionHandler = {
                if (httpStatusCode == 404) {
                    val bb = ByteBuffer.allocate(1.kiloByteUnit().toInt())
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
                    }
                }
            }
        )
        return active
    }

    companion object {
        val BASE_URL = "https://valorant-api.com"

        val SKINS_URL = "$BASE_URL/v1/weapons/skins"

        val ENDPOINT_STATUS_URL = "$SKINS_URL/%7BweaponSkinUuid%7D"
    }
}