package dev.flammky.valorantcompanion.pvp.map

import dev.flammky.valorantcompanion.pvp.util.mapSealedObjectInstancesToPersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlin.reflect.KClass

sealed class ValorantMap {

    object ASCENT : ValorantMap()

    object BIND : ValorantMap()

    object BREEZE : ValorantMap()

    object FRACTURE : ValorantMap()

    object HAVEN : ValorantMap()

    object ICEBOX : ValorantMap()

    object LOTUS : ValorantMap()

    object THE_RANGE : ValorantMap()

    object PEARL : ValorantMap()

    object SPLIT : ValorantMap()

    companion object {
        private val SUBCLASSES by lazy {
            ValorantMap::class.mapSealedObjectInstancesToPersistentList()
        }

        fun iter(): Iterator<ValorantMap> = SUBCLASSES.iterator()
        fun asList(): List<ValorantMap> = SUBCLASSES
    }
}
