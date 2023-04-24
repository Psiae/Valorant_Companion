package dev.flammky.valorantcompanion.pvp.internal.loadout

import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadout
import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadoutRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PlayerLoadoutRepositoryImpl : PlayerLoadoutRepository {

    private val _map = mutableMapOf<String, PlayerLoadout>()
    private val _mutex = Mutex()

    override suspend fun getCached(puuid: String): Result<PlayerLoadout?> {
        return runCatching { _mutex.withLock { _map[puuid] } }
    }

    override suspend fun update(puuid: String, loadout: PlayerLoadout): Result<Unit> {
        return runCatching { _mutex.withLock { _map[puuid] = loadout } }
    }
}