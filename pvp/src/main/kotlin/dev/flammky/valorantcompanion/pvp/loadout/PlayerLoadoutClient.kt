package dev.flammky.valorantcompanion.pvp.loadout

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Deferred

interface PlayerLoadoutClient {

    fun getLatestCachedLoadout(
        puuid: String
    ): Result<PlayerLoadout?>

    fun fetchPlayerLoadoutAsync(
        puuid: String
    ): Deferred<Result<PlayerLoadout>>

    fun fetchPlayerAvailableSprayLoadoutAsync(
        puuid: String
    ): Deferred<Result<PlayerAvailableSprayLoadout>>

    fun modifyPlayerLoadoutAsync(
        puuid: String,
        data: PlayerLoadoutChangeData
    ): Deferred<Result<PlayerLoadout>>

    fun dispose()
}