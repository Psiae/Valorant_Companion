package dev.flammky.valorantcompanion.assets

import java.nio.ByteBuffer

interface AssetHttpContentChannel {
    val contentLengthInfo: Long?
    val contentTypeInfo: String?

    suspend fun consume(dst: ByteBuffer)
}