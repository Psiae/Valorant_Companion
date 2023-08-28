package dev.flammky.valorantcompanion.assets.spray

import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonObject
import dev.flammky.valorantcompanion.assets.kotlinx.serialization.json.expectJsonProperty
import kotlinx.serialization.json.Json
import java.nio.charset.Charset

class ValorantApiSprayResponseParser(
    private val serializer: KotlinxSerializationValorantSprayAssetSerializer
) : ValorantSprayAssetResponseParser {

    override fun parseIdentity(
        uuid: String,
        byteArray: ByteArray,
        charset: Charset
    ): Result<ValorantSprayAssetIdentity> {
        return runCatching {
            val element = Json.parseToJsonElement(String(byteArray, charset))
            val data = element
                .expectJsonObject("ValorantApiSprayIdentityResponse")
                .expectJsonProperty("data")
                .expectJsonObject("data")
            serializer
                .deserializeIdentity(uuid, data)
                .getOrThrow()
        }
    }
}