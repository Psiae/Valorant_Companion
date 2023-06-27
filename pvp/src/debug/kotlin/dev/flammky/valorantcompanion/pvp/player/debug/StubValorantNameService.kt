package dev.flammky.valorantcompanion.pvp.player.debug

import dev.flammky.valorantcompanion.pvp.ex.PlayerNotFoundException
import dev.flammky.valorantcompanion.pvp.player.GetPlayerNameRequest
import dev.flammky.valorantcompanion.pvp.player.GetPlayerNameRequestResult
import dev.flammky.valorantcompanion.pvp.player.ValorantNameService
import dev.flammky.valorantcompanion.pvp.player.PlayerPVPName
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

class StubValorantNameService(
    private val map: Map<String, PlayerPVPName>
) : ValorantNameService {

    override fun getPlayerNameAsync(
        request: GetPlayerNameRequest
    ): Deferred<GetPlayerNameRequestResult> {
        return CompletableDeferred(
            value = GetPlayerNameRequestResult(
                persistentMapOf<String, Result<PlayerPVPName>>().builder()
                    .apply {
                        request.lookupPUUIDs.forEach { uuid ->
                            put(
                                key = uuid,
                                value = map[uuid]
                                    ?.let { Result.success(it) }
                                    ?: Result.failure(PlayerNotFoundException())
                            )
                        }
                    }
                    .build(),
                ex = null
            )
        )
    }
}