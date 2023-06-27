package dev.flammky.valorantcompanion.pvp.player

import kotlinx.coroutines.Deferred

interface ValorantNameService {

    fun getPlayerNameAsync(
        request: GetPlayerNameRequest
    ): Deferred<GetPlayerNameRequestResult>
}