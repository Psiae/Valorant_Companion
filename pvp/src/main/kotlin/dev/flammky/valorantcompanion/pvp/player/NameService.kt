package dev.flammky.valorantcompanion.pvp.player

import kotlinx.coroutines.Deferred

interface NameService {

    fun getPlayerNameAsync(
        request: GetPlayerNameRequest
    ): Deferred<GetPlayerNameRequestResult>
}