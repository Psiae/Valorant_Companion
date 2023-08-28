package dev.flammky.valorantcompanion.assets.spray

import java.nio.charset.Charset

interface ValorantSprayAssetResponseParser {

    fun parseIdentity(
        uuid: String,
        byteArray: ByteArray,
        charset: Charset
    ): Result<ValorantSprayAssetIdentity>
}