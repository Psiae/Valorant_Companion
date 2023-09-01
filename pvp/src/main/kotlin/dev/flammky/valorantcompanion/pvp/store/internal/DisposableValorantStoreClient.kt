package dev.flammky.valorantcompanion.pvp.store.internal

import dev.flammky.valorantcompanion.base.kt.coroutines.initAsParentCompleter
import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.internal.AuthProvider
import dev.flammky.valorantcompanion.pvp.internal.GeoProvider
import dev.flammky.valorantcompanion.pvp.store.StoreFrontData
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreClient
import kotlinx.coroutines.*

internal class DisposableValorantStoreClient(
    val user: String,
    private val httpClient: HttpClient,
    private val auth: AuthProvider,
    private val geo: GeoProvider,
    private val endpoint: ValorantStoreEndpoint,
    private val responseHandler: ValorantStoreResponseHandler
) : ValorantStoreClient {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    override fun fetchDataAsync(): Deferred<Result<StoreFrontData>> {
        val def = CompletableDeferred<Result<StoreFrontData>>()

        coroutineScope.launch(Dispatchers.IO) {
            def.complete(
                fetchData().onFailure {
                    // logger
                    it.printStackTrace()
                }
            )
        }.apply {
            initAsParentCompleter(parent = def)
        }


        return def
    }

    private suspend fun fetchData(): Result<StoreFrontData> {
        return runCatching {
            val authToken = auth.get_authorization_token(user).getOrElse {
                return Result.failure(IllegalStateException("Unable to get auth token", it))
            }
            val entitlement = auth.get_entitlement_token(user).getOrElse {
                return Result.failure(IllegalStateException("Unable to get entitlement", it))
            }

            val shard = geo.get_shard(user).getOrElse {
                return Result.failure(IllegalStateException("Unable to get entitlement", it))
            }

            val response = httpClient
                .jsonRequest(endpoint.buildGetStoreFrontDataRequest(user, authToken, entitlement, shard))

            responseHandler.storeFront(response).getOrElse { ex ->
                throw IllegalStateException("Cannot Parse StoreFrontData response", ex)
            }
        }
    }

    override fun dispose() {
        coroutineScope.cancel()
        return
    }
}