package dev.flammky.valorantcompanion.pvp.store.internal

import dev.flammky.valorantcompanion.base.kt.coroutines.initAsParentCompleter
import dev.flammky.valorantcompanion.pvp.BuildConfig
import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.internal.AuthProvider
import dev.flammky.valorantcompanion.pvp.internal.GeoProvider
import dev.flammky.valorantcompanion.pvp.store.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*

internal class DisposableValorantUserStoreClient(
    val user: String,
    private val httpClientFactory: () -> HttpClient,
    private val auth: AuthProvider,
    private val geo: GeoProvider,
    private val endpoint: ValorantStoreEndpointService,
    private val responseHandler: ValorantStoreResponseHandler,
    private val endpointResolver: ValorantStoreEndpointResolver
) : ValorantUserStoreClient {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    private val httpClient by lazy { httpClientFactory() }

    override fun fetchStoreFrontAsync(): Deferred<Result<StoreFrontData>> {
        val def = CompletableDeferred<Result<StoreFrontData>>()

        coroutineScope.launch(Dispatchers.IO) {
            def.complete(
                fetchData().onFailure {
                    // TODO: Logger
                    if (BuildConfig.DEBUG) it.printStackTrace()
                }
            )
        }.apply {
            initAsParentCompleter(parent = def)
        }


        return def
    }

    override fun fetchBundleDataAsync(uuid: String): Deferred<Result<FeaturedBundleDisplayData>> {
        val def = CompletableDeferred<Result<FeaturedBundleDisplayData>>()

        coroutineScope.launch(Dispatchers.IO) {
            def.complete(
                fetchFeaturedBundleData(uuid).onFailure {
                    // TODO: Logger
                    if (BuildConfig.DEBUG) it.printStackTrace()
                }
            )
        }.apply {
            initAsParentCompleter(parent = def)
        }

        return def
    }

    override fun fetchEntitledAgent(): FetchEntitledItemSession {
        val session = FetchEntitledItemSessionImpl(
            puuid = user,
            type = ItemType.Agent,
            coroutineScope = coroutineScope,
            endpointResolver = endpointResolver,
            httpClientFactory = httpClientFactory,
            authProvider = auth,
            geoProvider = geo
        )
        return session
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

    private suspend fun fetchFeaturedBundleData(uuid: String): Result<FeaturedBundleDisplayData> {
        return runCatching {

            val response = httpClient
                .jsonRequest(endpoint.buildGetFeaturedBundleDataRequest(uuid))

            responseHandler.featuredBundleData(response).getOrElse { ex ->
                throw IllegalStateException("Cannot Parse StoreFrontData response", ex)
            }
        }
    }

    override fun dispose() {
        coroutineScope.cancel()
        return
    }
}

private class FetchEntitledItemSessionImpl(
    val puuid: String,
    override val type: ItemType,
    private val coroutineScope: CoroutineScope,
    private val endpointResolver: ValorantStoreEndpointResolver,
    private val httpClientFactory: () -> HttpClient,
    private val authProvider: AuthProvider,
    private val geoProvider: GeoProvider
): FetchEntitledItemSession {

    private val def = CompletableDeferred<Result<Set<String>>>()
    private val _httpClient by lazy(httpClientFactory)

    val initiated = atomic(false)

    override fun init(): Boolean {
        if (!initiated.compareAndSet(expect = false, update = true)) {
            return false
        }
        start()
        return true
    }

    private fun start() {
        coroutineScope.launch(Dispatchers.IO) {
            runCatching {
                val httpClient = _httpClient
                val endpoint = endpointResolver
                    .resolveActiveEndpoint()
                    .fold(
                        onFailure = {
                            // TODO
                            throw it
                        },
                        onSuccess = { it }
                    )
                endpoint
                    .getUserEntitledItems(
                        puuid = puuid,
                        httpClient = httpClient,
                        authToken = authProvider
                            .get_authorization_token(puuid = puuid)
                            .onFailure { ex ->
                                // TODO
                            }
                            .getOrThrow(),
                        authEntitlement = authProvider
                            .get_entitlement_token(puuid = puuid)
                            .onFailure { ex ->
                                // TODO
                            }
                            .getOrThrow(),
                        shard = geoProvider
                            .get_shard(puuid = puuid)
                            .onFailure { ex ->
                                // TODO
                            }
                            .getOrThrow(),
                        itemType = type
                    )
                    .fold(
                        onSuccess = { set ->
                            def.complete(Result.success(set))
                        },
                        onFailure = { ex ->
                            // TODO: raise flag, prepare retry, etc.
                            def.complete(Result.failure(ex))
                        }
                    )
            }.onFailure { ex ->
                def.complete(Result.failure(ex))
            }
        }.initAsParentCompleter(def)
    }

    override fun asDeferred(): Deferred<Result<Set<String>>> {
        return def
    }
}