package dev.flammky.valorantcompanion.pvp.store.currency

import dev.flammky.valorantcompanion.pvp.util.mapSealedObjectInstancesToPersistentList
import java.util.*

sealed class StoreCurrency(
    val uuid: String
) {

    override fun equals(other: Any?): Boolean {
        return other === this ||
                other is StoreCurrency &&
                other.uuid == uuid
    }

    override fun hashCode(): Int {
        return Objects.hash(uuid)
    }

    override fun toString(): String {
        return "StoreCurrency(uuid=$uuid)"
    }

    companion object {

        val OBJECTS by lazy {
            StoreCurrency::class.mapSealedObjectInstancesToPersistentList()
        }
    }
}

fun StoreCurrency.Companion.ofID(
    id: String
): StoreCurrency {
    return OBJECTS.find { it.uuid == id }
        ?: OtherStoreCurrency("", id)
}
