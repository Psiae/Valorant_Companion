package dev.flammky.valorantcompanion.pvp.store.currency

import java.util.Objects

// a currency that we don't yet have defined
class OtherStoreCurrency(
    uuid: String,
    singularName: String,
    pluralName: String,
) : StoreCurrency(
    uuid,
    singularName,
    pluralName
) {

    override fun equals(other: Any?): Boolean {
        return other === this ||
                other is OtherStoreCurrency &&
                super.equals(other)
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode())
    }

    override fun toString(): String {
        return "OtherCurrency(uuid=$uuid)"
    }
}

val StoreCurrency.isOtherCurrency: Boolean
    get() = this is OtherStoreCurrency

val StoreCurrency.isKnownCurrency: Boolean
    get() = this !is OtherStoreCurrency