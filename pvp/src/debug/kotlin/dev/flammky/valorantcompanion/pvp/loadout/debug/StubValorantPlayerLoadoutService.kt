package dev.flammky.valorantcompanion.pvp.loadout.debug

import dev.flammky.valorantcompanion.pvp.loadout.*
import kotlinx.coroutines.*

typealias StubValorantLoadoutProvider = suspend (String) -> PlayerLoadout
typealias StubValorantAvailableSprayLoadoutProvider = suspend (String) -> PlayerAvailableSprayLoadout
typealias StubValorantLoadoutModifyHandler = suspend (String, PlayerLoadoutChangeData) -> PlayerLoadout

class StubValorantPlayerLoadoutService(
    private val loadoutProvider: StubValorantLoadoutProvider,
    private val availableLoadoutProvider: StubValorantAvailableSprayLoadoutProvider,
    private val loadoutModifyHandler: StubValorantLoadoutModifyHandler
) : PlayerLoadoutService {

    private val coroutineScope = CoroutineScope(SupervisorJob())

    override fun createClient(): PlayerLoadoutClient {
        return StubValorantPlayerLoadoutClient()
    }

    private inner class StubValorantPlayerLoadoutClient(): PlayerLoadoutClient {

        override fun getLatestCachedLoadout(puuid: String): Result<PlayerLoadout?> {
            TODO("Not yet implemented")
        }

        override fun fetchPlayerLoadoutAsync(puuid: String): Deferred<Result<PlayerLoadout>> {
            return coroutineScope.async(Dispatchers.IO) { runCatching { loadoutProvider.invoke(puuid) } }
        }

        override fun fetchPlayerAvailableSprayLoadoutAsync(puuid: String): Deferred<Result<PlayerAvailableSprayLoadout>> {
            return coroutineScope.async(Dispatchers.IO) { runCatching { availableLoadoutProvider.invoke(puuid) } }
        }

        override fun modifyPlayerLoadoutAsync(
            puuid: String,
            data: PlayerLoadoutChangeData
        ): Deferred<Result<PlayerLoadout>> {
            return coroutineScope.async(Dispatchers.IO) { runCatching { loadoutModifyHandler.invoke(puuid, data) } }
        }

        override fun dispose() {

        }
    }
}