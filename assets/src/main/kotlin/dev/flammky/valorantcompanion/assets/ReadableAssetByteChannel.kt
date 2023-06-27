package dev.flammky.valorantcompanion.assets

import java.nio.ByteBuffer

fun interface ReadableAssetByteChannel {

    suspend fun consume(dst: ByteBuffer)
}