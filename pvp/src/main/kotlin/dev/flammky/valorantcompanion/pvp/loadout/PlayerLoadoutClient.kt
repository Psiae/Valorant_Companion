package dev.flammky.valorantcompanion.pvp.loadout

import kotlinx.coroutines.Deferred

interface PlayerLoadoutClient {

    fun getLatestCachedLoadoutAsync(
        puuid: String
    ): Result<PlayerLoadout?>

    fun fetchPlayerLoadoutAsync(
        puuid: String
    ): Deferred<Result<PlayerLoadout>>

    fun dispose()
}