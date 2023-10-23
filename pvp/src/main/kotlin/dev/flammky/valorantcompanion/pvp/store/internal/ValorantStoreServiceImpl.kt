package dev.flammky.valorantcompanion.pvp.store.internal

import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.internal.AuthProvider
import dev.flammky.valorantcompanion.pvp.internal.GeoProvider
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreEndpointResolver
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreEndpointResolverImpl
import dev.flammky.valorantcompanion.pvp.store.ValorantUserStoreClient
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreService

internal class ValorantStoreServiceImpl(
    private val auth: AuthProvider,
    private val geo: GeoProvider,
    private val endpoint: ValorantStoreEndpointService,
    private val responseHandler: ValorantStoreResponseHandler,
    private val httpClientFactory: () -> HttpClient,
    private val endpointResolver: ValorantStoreEndpointResolver
) : ValorantStoreService{

    private val httpClient by lazy { httpClientFactory() }

    override fun createClient(user: String): ValorantUserStoreClient {
        return DisposableValorantUserStoreClient(
            user = user,
            httpClientFactory = ::httpClient::get,
            auth = auth,
            geo = geo,
            endpoint = endpoint,
            responseHandler = responseHandler,
            endpointResolver = endpointResolver
        )
    }
}