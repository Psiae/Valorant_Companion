package dev.flammky.valorantcompanion.pvp.loadout

interface PlayerLoadoutRepository {
    suspend fun getCached(puuid: String): Result<PlayerLoadout?>
    suspend fun update(puuid: String, loadout: PlayerLoadout): Result<Unit>
    suspend fun updateAvail(puuid: String, availableLoadout: PlayerAvailableSprayLoadout): Result<Unit>

    suspend fun getAvailCached(puuid: String): Result<PlayerAvailableSprayLoadout?>
}