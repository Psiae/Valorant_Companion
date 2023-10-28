package dev.flammky.valorantcompanion.assets.player_title

import android.util.Log
import dev.flammky.valorantcompanion.assets.internal.ValorantAssetRepository
import dev.flammky.valorantcompanion.base.kt.coroutines.awaitOrCancelOnException
import dev.flammky.valorantcompanion.base.kt.coroutines.initAsParentCompleter
import dev.flammky.valorantcompanion.base.kt.sync
import kotlinx.coroutines.*

interface ValorantTitleAssetLoader {

    fun loadTitleIdentityAsync(
        uuid: String,
    ): Deferred<Result<PlayerTitleIdentity>>

    fun dispose()
}

internal class DelegatedValorantTitleAssetLoader(
    private val actual: ValorantTitleAssetLoader
) : ValorantTitleAssetLoader {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    override fun loadTitleIdentityAsync(uuid: String): Deferred<Result<PlayerTitleIdentity>> {
        val def = CompletableDeferred<Result<PlayerTitleIdentity>>()
        coroutineScope.launch(Dispatchers.IO) {
            val result = runCatching {
                actual
                    .loadTitleIdentityAsync(uuid)
                    .awaitOrCancelOnException()
                    .getOrThrow()
            }
            def.complete(result)
        }.initAsParentCompleter(def)
        return def
    }

    override fun dispose() {
        coroutineScope.cancel()
    }
}

internal class ConflatingValorantTitleAssetLoader(
    private val repository: ValorantAssetRepository,
    private val downloader: PlayerTitleAssetDownloader,
): ValorantTitleAssetLoader {
    private val coroutineScope = CoroutineScope(SupervisorJob())
    private val identitySessions = mutableMapOf<String, PlayerTitleIdentityLoadingSession>()

    override fun loadTitleIdentityAsync(uuid: String): Deferred<Result<PlayerTitleIdentity>> {
        val def = CompletableDeferred<Result<PlayerTitleIdentity>>()

        coroutineScope.launch(Dispatchers.IO) {

            val result = runCatching result@ {

                val (session, initiator) = identitySessions.sync {
                    val session = getOrElse(uuid) {
                        return@sync ConflatingPlayerTitleIdentityLoadingSession(uuid) to true
                    }
                    DelegatedPlayerTitleIdentityLoadingSession(session) to false
                }

                if (initiator) {
                    doWork(session as ConflatingPlayerTitleIdentityLoadingSession)
                }

                session.awaitResult().getOrThrow()
            }

            def.complete(result)

        }.initAsParentCompleter(parent = def)

        return def
    }

    private fun doWork(
        session: ConflatingPlayerTitleIdentityLoadingSession
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            runCatching {
                session.startFlag.join()
                withContext(SupervisorJob(session.killFlag)) {
                    repository
                        .loadPlayerTitleIdentity(session.titleUuid)
                        .fold(
                            onSuccess = { cached ->
                                if (cached != null) {
                                    return@withContext cached
                                }
                            },
                            onFailure = { ex ->
                                throw ex
                            }
                        )

                    val download = downloader
                        .downloadIdentity(session.titleUuid)
                        .apply {
                            init()
                        }
                        .awaitResult()
                        .fold(
                            onSuccess = {
                                Log.d("DEBUG", "ConflatingValorantTitleAssetLoader_downloadResult_onSuccess")
                                it
                            },
                            onFailure = {
                                Log.d("DEBUG", "ConflatingValorantTitleAssetLoader_downloadResult_onFailure=$it")
                                throw it
                            }
                        )

                    repository
                        .cachePlayerTitleIdentity(session.titleUuid, download)
                        .getOrThrow()

                    repository
                        .loadPlayerTitleIdentity(session.titleUuid)
                        .getOrThrow()
                        ?: throw RuntimeException("Repository Returned null")
                }
            }.fold(
                onSuccess = { session.complete(it) },
                onFailure = { session.completeExceptionally(it as Exception) }
            )
        }.invokeOnCompletion {
            session.end()
        }
    }

    override fun dispose() {
        coroutineScope.cancel()
    }
}