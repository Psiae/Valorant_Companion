package dev.flammky.valorantcompanion.assets.http

import dev.flammky.valorantcompanion.assets.ReadableAssetByteChannel

class AssetHttpResponse(
    val statusCode: Int,
    val content: ReadableAssetByteChannel
) {
}