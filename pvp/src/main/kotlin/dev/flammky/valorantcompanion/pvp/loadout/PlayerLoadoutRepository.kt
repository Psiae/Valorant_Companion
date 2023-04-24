package dev.flammky.valorantcompanion.pvp.loadout

interface PlayerLoadoutRepository {
    suspend fun getCached(puuid: String): Result<PlayerLoadout?>
    suspend fun update(puuid: String, loadout: PlayerLoadout): Result<Unit>
}