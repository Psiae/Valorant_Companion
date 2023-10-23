package dev.flammky.valorantcompanion.pvp.store.debug

import dev.flammky.valorantcompanion.pvp.store.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job

typealias StubStoreFrontProvider = (puuid: String) -> StoreFrontData?
typealias StubFeaturedBundleDataProvider = (uuid: String) -> FeaturedBundleDisplayData?
typealias StubEntitledItemProvider = (uuid: String, itemType: ItemType) -> Set<String>?

class StubValorantStoreService(
    private val storeFrontProvider: StubStoreFrontProvider = { null },
    private val bundleDataProvider: StubFeaturedBundleDataProvider = { null },
    private val entitledItemProvider: StubEntitledItemProvider = { _, _ -> null }
): ValorantStoreService {

    override fun createClient(user: String): ValorantUserStoreClient {
       return StubValorantUserStoreClient(user)
    }

    private inner class StubValorantUserStoreClient(
        private val user: String
    ) : ValorantUserStoreClient {

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

        override fun fetchEntitledAgent(): FetchEntitledItemSession {
            val completion = CompletableDeferred<Result<Set<String>>>()
            return object : FetchEntitledItemSession {

                override val type: ItemType
                    get() = ItemType.Agent

                override fun asDeferred(): Deferred<Result<Set<String>>> {
                    return completion
                }

                override fun init(): Boolean {
                    return if (_disposed.value) {
                        completion.complete(Result.failure(IllegalStateException("Disposed")))
                    } else {
                        completion.complete(runCatching { entitledItemProvider.invoke(user, ItemType.Agent) ?: error("StubStoreService entitledAgents data returns null") })
                    }
                }
            }
        }

        override fun dispose() {
            _disposed.value = true
        }
    }
}