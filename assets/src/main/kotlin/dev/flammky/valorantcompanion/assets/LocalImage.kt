package dev.flammky.valorantcompanion.assets

import java.util.*

sealed class LocalImage<T>(val value: T) {
    class File(file: java.io.File) : LocalImage<java.io.File>(file) {

        override fun equals(other: Any?): Boolean {
            return other is File && other.value == value
        }

        override fun hashCode(): Int {
            return Objects.hash(value)
        }
    }
    class Resource(id: Int) : LocalImage<Int>(id) {

        override fun equals(other: Any?): Boolean {
            return other is Resource && other.value == value
        }

        override fun hashCode(): Int {
            return Objects.hash(value)
        }
    }
}