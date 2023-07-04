package dev.flammky.valorantcompanion.pvp.mmr.debug

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

    companion object {
        val DEFAULT_FAKE_NAMES = persistentMapOf(
            "dokka" to PlayerPVPName(
                "dokka",
                "Dokka",
                "Dokka",
                "zap"
            ),
            "dex" to PlayerPVPName(
                "dex",
                "Dex",
                "Dex",
                "301"
            ),
            "moon" to PlayerPVPName(
                "moon",
                "Moon",
                "Moon",
                "301"
            ),
            "hive" to PlayerPVPName(
                "hive",
                "Hive",
                "Hive",
                "301"
            ),
            "lock" to PlayerPVPName(
                "lock",
                "Lock",
                "Lock",
                "301"
            ),
            "player1" to PlayerPVPName(
                "player1",
                "Player1",
                "Player1",
                "fake"
            ),
            "player2" to PlayerPVPName(
                "player1",
                "Player1",
                "Player1",
                "fake"
            ),
            "player3" to PlayerPVPName(
                "player1",
                "Player1",
                "Player1",
                "fake"
            ),
            "player4" to PlayerPVPName(
                "player1",
                "Player1",
                "Player1",
                "fake"
            ),
            "player5" to PlayerPVPName(
                "player1",
                "Player1",
                "Player1",
                "fake"
            )
        )
    }
}