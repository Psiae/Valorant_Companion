package dev.flammky.valorantcompanion.pvp.http.ktor

import android.os.SystemClock
import dev.flammky.valorantcompanion.pvp.http.HTTP_REQUEST_SENDING_TIMESTAMP_SYSTEM_ELAPSED_CLOCK_MILLIS
import dev.flammky.valorantcompanion.pvp.http.HTTP_REQUEST_RECEIVED_TIMESTAMP_SYSTEM_ELAPSED_CLOCK_MILLIS
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.CoroutineContext

class PVPOkHttpEngine(
    config: PVPOkHttpEngineConfig
) : HttpClientEngineBase("pvp-ktor-okhttp") {

    private val okhttp = OkHttpEngine(config.asKtorOkHttpConfig())

    override val config: HttpClientEngineConfig = config

    override val dispatcher: CoroutineDispatcher =
        okhttp.dispatcher

    override val supportedCapabilities: Set<HttpClientEngineCapability<*>> =
        okhttp.supportedCapabilities

    @InternalAPI
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        data.attributes.put(
            AttributeKey(HTTP_REQUEST_SENDING_TIMESTAMP_SYSTEM_ELAPSED_CLOCK_MILLIS),
            SystemClock.elapsedRealtime()
        )
        val response = okhttp.execute(data)
        data.attributes.put(
            AttributeKey(HTTP_REQUEST_RECEIVED_TIMESTAMP_SYSTEM_ELAPSED_CLOCK_MILLIS),
            SystemClock.elapsedRealtime()
        )
        return response
    }

    @InternalAPI
    override fun install(client: HttpClient) {
        super.install(client)
    }

    override val coroutineContext: CoroutineContext
        get() = super.coroutineContext

    override fun close() {
        super.close()
    }
}

private fun PVPOkHttpEngineConfig.asKtorOkHttpConfig(): OkHttpConfig {
    return OkHttpConfig()
}

object PVPOkHttpEngineFactory : HttpClientEngineFactory<PVPOkHttpEngineConfig> {

    override fun create(block: PVPOkHttpEngineConfig.() -> Unit): HttpClientEngine {
        return PVPOkHttpEngine(PVPOkHttpEngineConfig().apply(block))
    }
}