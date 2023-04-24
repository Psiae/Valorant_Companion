package dev.flammky.valorantcompanion.auth.riot

import dev.flammky.valorantcompanion.auth.riot.region.GeoShardInfo
import dev.flammky.valorantcompanion.auth.riot.region.RiotRegion
import dev.flammky.valorantcompanion.auth.riot.region.RiotShard

interface RiotGeoRepository {

    fun updateUserGeoShardInfo(
        puuid: String,
        region: RiotRegion,
        shard: RiotShard
    )

    fun registerUserGeoShardInfoChangeListener(
        listener: UserGeoShardInfoListener
    )

    fun unregisterUserGeoShardInfoChangeListener(
        listener: UserGeoShardInfoListener
    )

    fun getGeoShardInfo(puuid: String): GeoShardInfo?
}

abstract class UserGeoShardInfoListener internal constructor(val puuid: String) {

    abstract fun initial(geo: GeoShardInfo?)

    abstract fun onRemoteInfoChanged(geo: GeoShardInfo)

    abstract fun onUserOverriding(geo: GeoShardInfo)
}

fun UserGeoShardInfoListener(
    puuid: String,
    initial: (geo: GeoShardInfo?) -> Unit,
    onRemoteInfoChanged: (geo: GeoShardInfo) -> Unit,
    onUserOverriding: (geo: GeoShardInfo) -> Unit
): UserGeoShardInfoListener {
    return object : UserGeoShardInfoListener(puuid) {

        override fun initial(geo: GeoShardInfo?) {
            initial.invoke(geo)
        }

        override fun onRemoteInfoChanged(geo: GeoShardInfo) {
            onRemoteInfoChanged.invoke(geo)
        }

        override fun onUserOverriding(geo: GeoShardInfo) {
            onUserOverriding.invoke(geo)
        }
    }
}