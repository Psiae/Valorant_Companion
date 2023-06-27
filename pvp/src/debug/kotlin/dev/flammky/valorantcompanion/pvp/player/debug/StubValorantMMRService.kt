package dev.flammky.valorantcompanion.pvp.player.debug

import dev.flammky.valorantcompanion.pvp.ex.SeasonalMMRDataNotFoundException
import dev.flammky.valorantcompanion.pvp.mmr.MMRUserClient
import dev.flammky.valorantcompanion.pvp.mmr.SeasonalMMRData
import dev.flammky.valorantcompanion.pvp.mmr.ValorantMMRService
import kotlinx.coroutines.*

typealias StubSeasonalMMRProvider = (season: String, subject: String) -> SeasonalMMRData?

class StubValorantMMRService(
    private val provider: StubSeasonalMMRProvider
) : ValorantMMRService {

    override fun createUserClient(puuid: String): MMRUserClient {
        return StubValorantMMRUserClient()
    }

    private inner class StubValorantMMRUserClient: MMRUserClient {

        private val coroutineScope = CoroutineScope(SupervisorJob())

        override fun fetchSeasonalMMR(
            season: String,
            subject: String
        ): Deferred<Result<SeasonalMMRData>> {
            val def = CompletableDeferred<Result<SeasonalMMRData>>()

            val task = coroutineScope.launch(Dispatchers.IO) {
                def.complete(
                    provider(season, subject)
                        ?.let { Result.success(it) }
                        ?: Result.failure(SeasonalMMRDataNotFoundException())
                )
            }

            def.invokeOnCompletion { task.cancel() }

            return def
        }
    }
}