package dev.flammky.valorantcompanion.assets.http

import java.nio.ByteBuffer

typealias AssetHttpSessionHandler = suspend AssetHttpSession.() -> Unit

interface AssetHttpSession {
    val httpMethod: String
    val httpStatusCode: Int

    val contentType: String?
    val contentSubType: String?
    val contentLength: Long?

    val closed: Boolean
    val consumed: Boolean

    suspend fun consume(byteBuffer: ByteBuffer)

    fun reject()
}