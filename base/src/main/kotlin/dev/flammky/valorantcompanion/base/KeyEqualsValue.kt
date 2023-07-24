package dev.flammky.valorantcompanion.base

import java.util.Objects

class KeyEqualsValue <K, V>(val key: K, val value: V) {

    override fun equals(other: Any?): Boolean {
        return other is KeyEqualsValue<*, *> && other.key == key
    }

    override fun hashCode(): Int = Objects.hash(key)

    override fun toString(): String = "KeyEqualsValue($key, $value)"
}
