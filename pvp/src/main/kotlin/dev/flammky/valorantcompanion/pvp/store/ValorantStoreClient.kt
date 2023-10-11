package dev.flammky.valorantcompanion.pvp.store

import kotlinx.coroutines.Deferred

interface ValorantStoreClient {

    fun fetchStoreFrontAsync(): Deferred<Result<StoreFrontData>>

    fun fetchBundleDataAsync(uuid: String): Deferred<Result<FeaturedBundleDisplayData>>

    fun dispose()
}