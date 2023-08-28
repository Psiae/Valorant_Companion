package dev.flammky.valorantcompanion.assets.ktor

import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.assets.http.AssetHttpSession
import dev.flammky.valorantcompanion.assets.http.AssetHttpSessionHandler
import dev.flammky.valorantcompanion.base.loop
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.atomicfu.atomic
import java.nio.ByteBuffer
import io.ktor.client.HttpClient as KtorHttpClient

class KtorWrappedHttpClient(
    private val self: KtorHttpClient
) : AssetHttpClient() {

    // TODO: limit download, simply prepare custom execute block, the call will be closed once it returns
    override suspend fun get(
        url: String,
        sessionHandler: AssetHttpSessionHandler
    ) {
        val request = self.prepareRequest {
            method = HttpMethod.Get
            url(url)
        }
        request.execute { response ->
            val channel = response.bodyAsChannel()
            val consumed = atomic(false)
            val closed = atomic(false)
            object : AssetHttpSession {

                override val httpMethod: String
                    get() = response.request.method.value
                override val httpStatusCode: Int
                    get() = response.status.value

                override val contentType: String?
                    get() = response.contentType()?.contentType
                override val contentSubType: String?
                    get() = response.contentType()?.contentSubtype
                override val contentLength: Long?
                    get() = response.contentLength()

                override val closed: Boolean
                    get() = closed.value

                override val consumed: Boolean
                    get() = consumed.value

                override suspend fun consume(byteBuffer: ByteBuffer) {
                    if (!consumed.compareAndSet(expect = false, update = true)) {
                        error("AssetHttpSession is already consumed")
                    }
                    if (!closed.compareAndSet(false, update = true)) {
                        error("AssetHttpSession is already closed")
                    }
                    try {
                        loop { if (channel.readAvailable(byteBuffer) <= 0) LOOP_BREAK() }
                    } finally {
                        channel.cancel()
                    }
                }

                override fun reject() {
                    channel.cancel()
                }
            }.apply {
                sessionHandler()
                channel.cancel()
            }
        }
    }
}