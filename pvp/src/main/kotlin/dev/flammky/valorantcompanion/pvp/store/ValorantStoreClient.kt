package dev.flammky.valorantcompanion.pvp.store

import kotlinx.coroutines.Deferred

interface ValorantStoreClient {

    fun fetchDataAsync(): Deferred<Result<StoreFrontData>>

    fun dispose()
}