package dev.flammky.valorantcompanion.pvp.store

import kotlinx.coroutines.Deferred

interface FetchEntitledItemSession {

    val type: ItemType

    fun init(): Boolean

    fun asDeferred(): Deferred<Result<Set<String>>>
}