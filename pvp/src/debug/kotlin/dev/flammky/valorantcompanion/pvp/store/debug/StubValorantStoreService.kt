package dev.flammky.valorantcompanion.pvp.store.debug

import dev.flammky.valorantcompanion.pvp.store.FeaturedBundleDisplayData
import dev.flammky.valorantcompanion.pvp.store.StoreFrontData
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreClient
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreService
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

typealias StubStoreFrontProvider = (puuid: String) -> StoreFrontData?
typealias StubFeaturedBundleDataProvider = (uuid: String) -> FeaturedBundleDisplayData?

class StubValorantStoreService(
    private val storeFrontProvider: StubStoreFrontProvider,
    private val bundleDataProvider: StubFeaturedBundleDataProvider
): ValorantStoreService {

    override fun createClient(user: String): ValorantStoreClient {
       return StubValorantStoreClient(user)
    }

    private inner class StubValorantStoreClient(
        private val user: String
    ) : ValorantStoreClient {

        private val _disposed = atomic(false)

        override fun fetchStoreFrontAsync(): Deferred<Result<StoreFrontData>> {
            val def = CompletableDeferred<Result<StoreFrontData>>()

            if (_disposed.value) {
                def.complete(Result.failure(IllegalStateException("Disposed")))
            } else {
                def.complete(runCatching { storeFrontProvider.invoke(user) ?: error("StubStoreService StoreFront data returns null") })
            }

            return def
        }

        override fun fetchBundleDataAsync(uuid: String): Deferred<Result<FeaturedBundleDisplayData>> {
            val def = CompletableDeferred<Result<FeaturedBundleDisplayData>>()

            if (_disposed.value) {
                def.complete(Result.failure(IllegalStateException("Disposed")))
            } else {
                def.complete(runCatching { bundleDataProvider.invoke(uuid) ?: error("StubStoreService bundle data returns null") })
            }

            return def
        }

        override fun dispose() {
            _disposed.value = true
        }
    }
}