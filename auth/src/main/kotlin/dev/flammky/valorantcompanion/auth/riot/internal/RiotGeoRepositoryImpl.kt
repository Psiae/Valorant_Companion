package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.riot.region.GeoShardInfo
import dev.flammky.valorantcompanion.auth.riot.region.RiotRegion
import dev.flammky.valorantcompanion.auth.riot.region.RiotShard
import dev.flammky.valorantcompanion.auth.riot.RiotGeoRepository
import dev.flammky.valorantcompanion.auth.riot.UserGeoShardInfoListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RiotGeoRepositoryImpl : RiotGeoRepository {

    private val _lock = Any()
    private val _registry = RiotGeoRegistry()

    override fun updateUserGeoShardInfo(puuid: String, region: RiotRegion, shard: RiotShard) {
        synchronized(_lock) {
            _registry.updateUserGeoShardInfo(puuid, region, shard)
        }
    }

    override fun registerUserGeoShardInfoChangeListener(listener: UserGeoShardInfoListener) {
        synchronized(_lock) {
            _registry.registerUserGeoShardInfoChangeListener(listener)
        }
    }

    override fun unregisterUserGeoShardInfoChangeListener(listener: UserGeoShardInfoListener) {
        synchronized(_lock) {
            _registry.unregisterUserGeoShardInfoChangeListener(listener)
        }
    }

    override fun getGeoShardInfo(puuid: String): GeoShardInfo? {
        return synchronized(_lock) {
            _registry.getGeoShardInfo(puuid)
        }
    }
}

private class RiotGeoRegistry() {
    private val _map = mutableMapOf<String, GeoShardInfo>()
    private val _listeners = mutableMapOf<String, MutableList<UserGeoShardInfoListener>>()
    private val _coroutineScope = CoroutineScope(SupervisorJob())

    fun updateUserGeoShardInfo(puuid: String, region: RiotRegion, shard: RiotShard) {
        val new = GeoShardInfo(region, shard)
        val old = _map.put(puuid, new)
        if (old == new) return
        // TODO: we should not care about this, should use flow / channel instead
        _coroutineScope.launch(Dispatchers.Main) {
            _listeners[puuid]?.forEach { it.onRemoteInfoChanged(new) }
        }
    }

    fun registerUserGeoShardInfoChangeListener(listener: UserGeoShardInfoListener) {
        _listeners.getOrPut(listener.puuid) { mutableListOf() }.add(listener)
        listener.apply { initial(_map[listener.puuid]) }
    }

    fun unregisterUserGeoShardInfoChangeListener(listener: UserGeoShardInfoListener) {
        _listeners[listener.puuid]?.remove(listener)
    }

    fun getGeoShardInfo(puuid: String): GeoShardInfo? {
        return _map[puuid]
    }
}
