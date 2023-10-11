package dev.flammky.valorantcompanion.assets.weapon.skin

import dev.flammky.valorantcompanion.assets.http.AssetHttpClient
import dev.flammky.valorantcompanion.assets.http.AssetHttpSession
import dev.flammky.valorantcompanion.base.kt.coroutines.initAsParentCompleter
import dev.flammky.valorantcompanion.base.storage.ByteUnit
import dev.flammky.valorantcompanion.base.storage.bitByteUnit
import dev.flammky.valorantcompanion.base.storage.kiloByteUnit
import io.ktor.util.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import java.nio.ByteBuffer

interface ValorantWeaponSkinAssetDownloader {

    fun createIdentityDownloadInstance(
        id: String
    ): WeaponSkinAssetIdentityDownloadInstance
}


class ValorantWeaponSkinAssetDownloaderImpl(
    private val endpointResolver: WeaponSkinAssetEndpointResolver,
    private val httpClientFactory: () -> AssetHttpClient
) : ValorantWeaponSkinAssetDownloader {

    private val coroutineScope = CoroutineScope(SupervisorJob())
    private val httpClient by lazy(httpClientFactory)

    override fun createIdentityDownloadInstance(
        id: String
    ): WeaponSkinAssetIdentityDownloadInstance {
        return WeaponSkinAssetIdentityDownloadInstanceImpl(
            id = id,
            coroutineScope = coroutineScope,
            endpointResolver = endpointResolver,
            httpClientFactory = ::httpClient
        )
    }
}

private class WeaponSkinAssetIdentityDownloadInstanceImpl(
    id: String,
    private val coroutineScope: CoroutineScope,
    private val endpointResolver: WeaponSkinAssetEndpointResolver,
    private val httpClientFactory: () -> AssetHttpClient
) : WeaponSkinAssetIdentityDownloadInstance(id) {

    private val initiated = atomic(false)

    private var sessionHandlerInvoked = atomic(false)

    private val def = CompletableDeferred<Result<ByteArray>>()

    private val httpClient by lazy {
        check(initiated.value)
        httpClientFactory()
    }

    override fun init() {
        if (!initiated.compareAndSet(expect = false, update = true)) {
            return
        }
        coroutineScope.launch(Dispatchers.IO) {

            runCatching {
                val endpoint = resolveEndpoint()
                val url = endpoint.buildUrl(id)

                httpClient.get(
                    url = url,
                    sessionHandler = ::assetSessionHandler
                )

                if (!sessionHandlerInvoked.value) {
                    error("Unexpected Behavior on AssetHttpClient, session handler wasn't invoked")
                }
            }.onFailure { ex ->
                def.complete(Result.failure(ex))
            }

        }.initAsParentCompleter(asDeferred())
    }

    // TODO: raise error flag
    private suspend fun resolveEndpoint(): WeaponSkinAssetEndpoint {
        return endpointResolver.resolve()
            .fold(
                onSuccess = { endpoint -> endpoint },
                onFailure = { throwable -> throw throwable }
            )
    }

    private suspend fun assetSessionHandler(
        session: AssetHttpSession
    ) = with(session) {
        if (!sessionHandlerInvoked.compareAndSet(expect = false, update = true)) {
            return
        }
        if (contentType != "application") {
            session.reject()
            return@with
        }
        if (contentSubType != "json") {
            session.reject()
            return@with
        }
        val sizeLimit = Companion.IDENTITY_SIZE_LIMIT
        val contentLength = contentLength
            ?.let { contentLength ->
                if (contentLength.bitByteUnit() > sizeLimit) {
                    session.reject()
                    return@with
                }
                contentLength
            }
        val bb = ByteBuffer.allocate(
            contentLength?.toInt() ?: sizeLimit.toInt()
        )
        consume(bb)
        def.complete(Result.success(bb.apply { flip() }.moveToByteArray()))
    }

    override suspend fun awaitResult(): Result<ByteArray> {
        return def.await()
    }

    override fun asDeferred(): Deferred<Result<ByteArray>> {
        return def
    }

    companion object {
        val IDENTITY_SIZE_LIMIT: ByteUnit = 5.kiloByteUnit()
    }
}