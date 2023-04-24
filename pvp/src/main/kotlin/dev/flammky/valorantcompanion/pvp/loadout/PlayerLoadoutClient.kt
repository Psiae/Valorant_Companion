package dev.flammky.valorantcompanion.pvp.loadout

import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadout
import kotlinx.coroutines.Deferred

interface PlayerLoadoutClient {

    fun getCachedOrFetchPlayerLoadoutAsync(
        puuid: String
    ): Deferred<Result<PlayerLoadout>>

    fun dispose()
}