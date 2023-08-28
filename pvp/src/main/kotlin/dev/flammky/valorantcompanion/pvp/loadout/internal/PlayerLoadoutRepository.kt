package dev.flammky.valorantcompanion.pvp.loadout.internal

import dev.flammky.valorantcompanion.pvp.loadout.PlayerAvailableSprayLoadout
import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadout
import dev.flammky.valorantcompanion.pvp.loadout.PlayerLoadoutRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PlayerLoadoutRepositoryImpl : PlayerLoadoutRepository {

    private val _map = mutableMapOf<String, PlayerLoadout>()
    private val _mutex = Mutex()

    private val _availMap = mutableMapOf<String, PlayerAvailableSprayLoadout>()
    private val _availMutex = Mutex()

    override suspend fun getCached(puuid: String): Result<PlayerLoadout?> {
        return runCatching { _mutex.withLock { _map[puuid] } }
    }

    override suspend fun update(puuid: String, loadout: PlayerLoadout): Result<Unit> {
        return runCatching { _mutex.withLock { _map[puuid] = loadout } }
    }

    override suspend fun updateAvail(
        puuid: String,
        availableLoadout: PlayerAvailableSprayLoadout
    ): Result<Unit> {
        return runCatching { _availMutex.withLock { _availMap[puuid] = availableLoadout } }
    }

    override suspend fun getAvailCached(puuid: String): Result<PlayerAvailableSprayLoadout?> {
        return runCatching { _availMutex.withLock { _availMap[puuid] } }
    }
}