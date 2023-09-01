package dev.flammky.valorantcompanion.pvp.store.internal

import dev.flammky.valorantcompanion.pvp.http.HttpClient
import dev.flammky.valorantcompanion.pvp.internal.AuthProvider
import dev.flammky.valorantcompanion.pvp.internal.GeoProvider
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreClient
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreService
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import kotlinx.collections.immutable.ImmutableSet

internal class ValorantStoreServiceImpl(
    private val auth: AuthProvider,
    private val geo: GeoProvider,
    private val endpoint: ValorantStoreEndpoint,
    private val responseHandler: ValorantStoreResponseHandler,
    private val httpClientFactory: () -> HttpClient
) : ValorantStoreService{

    override fun createClient(user: String): ValorantStoreClient {
        return DisposableValorantStoreClient(
            user = user,
            httpClient = httpClientFactory(),
            auth = auth,
            geo = geo,
            endpoint = endpoint,
            responseHandler = responseHandler
        )
    }

}