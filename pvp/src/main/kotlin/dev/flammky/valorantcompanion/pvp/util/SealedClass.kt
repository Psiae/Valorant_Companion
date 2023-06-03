package dev.flammky.valorantcompanion.pvp.util

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlin.reflect.KClass

inline fun <T: Any, K: KClass<out T>> K.mapSealedObjectInstancesToPersistentList(): PersistentList<T> {
     return sealedSubclasses.mapSealedObjectInstancesToPersistentList()
}

inline fun <T: Any, K: KClass<out T>> List<K>.mapSealedObjectInstancesToPersistentList(): PersistentList<T> {
    return mapNotNullTo(
        destination = persistentListOf<T>().builder(),
        transform = KClass<out T>::objectInstance
    ).build()
}