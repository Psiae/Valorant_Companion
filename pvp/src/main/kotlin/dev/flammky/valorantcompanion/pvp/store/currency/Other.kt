package dev.flammky.valorantcompanion.pvp.store.currency

import java.util.Objects

// a currency that we don't yet have defined
class OtherStoreCurrency(
    val name: String,
    uuid: String
) : StoreCurrency(uuid) {

    override fun equals(other: Any?): Boolean {
        return other === this ||
                other is OtherStoreCurrency &&
                other.name == this.name &&
                super.equals(other)
    }

    override fun hashCode(): Int {
        return Objects.hash(name, super.hashCode())
    }

    override fun toString(): String {
        return "OtherCurrency(name=$name, uuid=$uuid)"
    }
}