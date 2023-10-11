package dev.flammky.valorantcompanion.pvp.store.currency

import dev.flammky.valorantcompanion.pvp.util.mapSealedObjectInstancesToPersistentList
import java.util.*

sealed class StoreCurrency(
    val uuid: String,
    val pluralDisplayName: String,
    val singularDisplayName: String
) {

    override fun equals(other: Any?): Boolean {
        return other === this ||
                other is StoreCurrency &&
                other.uuid == uuid &&
                other.pluralDisplayName == pluralDisplayName &&
                other.singularDisplayName == singularDisplayName
    }

    override fun hashCode(): Int {
        return Objects.hash(uuid, pluralDisplayName, singularDisplayName)
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
    return OBJECTS.find { it.uuid == id || it.uuid.lowercase() == id || it.uuid.uppercase() == id }
        ?: OtherStoreCurrency(id, "UNKNOWN Points", "UNKNOWN Point")
}

fun StoreCurrency.Companion.ofIDOrNull(
    id: String
): StoreCurrency? {
    return OBJECTS.find { it.uuid == id }
}

val StoreCurrency.Companion.ValorantPoint: ValorantPoint
    get() = dev.flammky.valorantcompanion.pvp.store.currency.ValorantPoint

val StoreCurrency.Companion.KingdomCredit: KingdomCredit
    get() = dev.flammky.valorantcompanion.pvp.store.currency.KingdomCredit

val StoreCurrency.Companion.RadianitePoint: RadianitePoint
    get() = dev.flammky.valorantcompanion.pvp.store.currency.RadianitePoint
