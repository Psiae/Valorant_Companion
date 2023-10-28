package dev.flammky.valorantcompanion.assets.player_title

import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonBooleanParseJavaBoolean
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonObject
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonPrimitive
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonProperty
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.nio.charset.Charset

interface PlayerTitleAssetSerializer {

    fun deserializeIdentity(
        raw: ByteArray,
        charset: Charset
    ): Result<PlayerTitleIdentity>

    fun deserializeIdentity(
        raw: String
    ): Result<PlayerTitleIdentity>
}

internal class KtxPlayerTitleAssetSerializer : PlayerTitleAssetSerializer {

    override fun deserializeIdentity(
        raw: ByteArray,
        charset: Charset
    ): Result<PlayerTitleIdentity> {
        return runCatching { deserializeIdentity(String(raw, charset)).getOrThrow() }
    }

    override fun deserializeIdentity(raw: String): Result<PlayerTitleIdentity> {
        return runCatching { deserializeIdentity(Json.parseToJsonElement(raw)).getOrThrow() }
    }

    private fun deserializeIdentity(
        jsonElement: JsonElement
    ): Result<PlayerTitleIdentity> {
        return runCatching {
            jsonElement.expectJsonObject("PlayerTitleIdentity")
            PlayerTitleIdentity(
                uuid = jsonElement
                    .expectJsonProperty("uuid")
                    .expectJsonPrimitive("")
                    .content,
                description = jsonElement
                    .expectJsonProperty("description")
                    .expectJsonPrimitive("")
                    .content,
                titleText = jsonElement
                    .expectJsonProperty("titleText")
                    .expectJsonPrimitive("")
                    .content,
                hiddenIfNotOwned = jsonElement
                    .expectJsonProperty("isHiddenIfNotOwned")
                    .expectJsonPrimitive("")
                    .content
                    .let { expectJsonBooleanParseJavaBoolean("", it) },
            )
        }
    }
}