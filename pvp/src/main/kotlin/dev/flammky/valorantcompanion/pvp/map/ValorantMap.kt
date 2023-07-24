package dev.flammky.valorantcompanion.pvp.map

import dev.flammky.valorantcompanion.pvp.util.mapSealedObjectInstancesToPersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlin.reflect.KClass

sealed class ValorantMap(
    val identity: ValorantMapIdentity
) {

    object ASCENT : ValorantMap(ValorantMapIdentity.ASCENT)

    object BIND : ValorantMap(ValorantMapIdentity.BIND)

    object BREEZE : ValorantMap(ValorantMapIdentity.BREEZE)

    object FRACTURE : ValorantMap(ValorantMapIdentity.FRACTURE)

    object HAVEN : ValorantMap(ValorantMapIdentity.HAVEN)

    object ICEBOX : ValorantMap(ValorantMapIdentity.ICEBOX)

    object LOTUS : ValorantMap(ValorantMapIdentity.LOTUS)

    object THE_RANGE : ValorantMap(ValorantMapIdentity.THE_RANGE)

    object PEARL : ValorantMap(ValorantMapIdentity.PEARL)

    object SPLIT : ValorantMap(ValorantMapIdentity.SPLIT)

    // TDM
    object DISTRICT : ValorantMap(ValorantMapIdentity.DISTRICT)
    object KASBAH : ValorantMap(ValorantMapIdentity.KASBAH)
    object PIAZZA : ValorantMap(ValorantMapIdentity.PIAZZA)

    companion object {
        private val SUBCLASSES by lazy {
            ValorantMap::class.mapSealedObjectInstancesToPersistentList()
        }

        fun iter(): Iterator<ValorantMap> = SUBCLASSES.iterator()
        fun asList(): List<ValorantMap> = SUBCLASSES
    }
}
