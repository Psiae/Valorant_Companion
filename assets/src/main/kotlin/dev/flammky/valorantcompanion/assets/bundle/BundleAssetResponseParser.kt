package dev.flammky.valorantcompanion.assets.bundle

import java.nio.charset.Charset as jCharset

interface BundleAssetResponseParser {

    fun parseImageResponse(
        byteArray: ByteArray,
        charset: jCharset
    ): Result<ByteArray>
}