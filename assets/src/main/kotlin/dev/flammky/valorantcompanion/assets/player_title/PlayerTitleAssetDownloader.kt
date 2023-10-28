package dev.flammky.valorantcompanion.assets.player_title

import android.util.Log
import dev.flammky.valorantcompanion.base.kt.coroutines.awaitOrCancelOnException
import dev.flammky.valorantcompanion.base.kt.coroutines.initAsParentCompleter
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*

interface PlayerTitleAssetDownloader {

    fun downloadIdentity(
        uuid: String
    ): PlayerTitleAssetDownloadInstance
}

class PlayerTitleAssetDownloaderImpl(
    private val endpointResolver: PlayerTitleAssetEndpointResolver,
    private val endpointDownloader: (PlayerTitleAssetEndpoint) -> PlayerTitleAssetDownloader?
) : PlayerTitleAssetDownloader {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    override fun downloadIdentity(
        uuid: String
    ): PlayerTitleAssetDownloadInstance {
        return PlayerTitleIdentityDownloadInstance(uuid)
    }


    private inner class PlayerTitleIdentityDownloadInstance(
        uuid: String
    ): PlayerTitleAssetDownloadInstance(
        uuid = uuid
    ) {

        private val initiated = atomic(false)

        private val _result = CompletableDeferred<Result<ByteArray>>()

        override fun init() {
            if (!initiated.compareAndSet(expect = false, update = true)) {
                return
            }
            doWork()
        }

        override suspend fun awaitResult(): Result<ByteArray> {
            return _result.await()
        }

        override fun asDeferred(): Deferred<Result<ByteArray>> {
            val def = CompletableDeferred<Result<ByteArray>>()
            _result.invokeOnCompletion { ex ->
                if (ex != null)
                    def.cancel(ex.message ?: "", ex.cause)
                else
                    def.complete(_result.getCompleted())
            }
            return def
        }

        private fun doWork() {
            coroutineScope.launch(Dispatchers.IO) {
                runCatching {
                    withContext(SupervisorJob()) {
                        val endpoint = endpointResolver.resolveEndpoint(
                            capabilities = setOf(
                                PlayerTitleAssetEndpoint.CAPABILITY_TITLE_IDENTITY,
                            )
                        )
                        if (endpoint == null) {
                            // no available endpoint
                            throw RuntimeException("No Endpoint Were Available")
                        }
                        val downloader = endpointDownloader(endpoint)
                        if (downloader == null) {
                            throw RuntimeException("No Endpoint Downloader Were Available")
                        }
                        downloader.downloadIdentity(uuid)
                            .apply {
                                init()
                            }
                            .asDeferred()
                            .awaitOrCancelOnException()
                    }
                }.fold(
                    onSuccess = { result ->
                        Log.d("DEBUG", "PlayerTitleAssetDownloader_onSuccess($result)")
                        _result.complete(result)
                    },
                    onFailure = { ex ->
                        Log.d("DEBUG", "PlayerTitleAssetDownloader_onFailure")
                        _result.completeExceptionally(ex)
                    }
                )
            }
        }

        private suspend fun ensureSessionActive() {
            _result.ensureActive()
        }
    }
}