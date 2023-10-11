package dev.flammky.valorantcompanion.assets.valorantapi.bundle

import dev.flammky.valorantcompanion.assets.bundle.BundleAssetResponseParser
import java.nio.charset.Charset

class ValorantApiBundleAssetParser : BundleAssetResponseParser {

    private val serializer = ValorantApiBundleAssetSerializer()

    override fun parseImageResponse(byteArray: ByteArray, charset: Charset): Result<ByteArray> {
        // TODO: validate
        return Result.success(byteArray)
    }
}