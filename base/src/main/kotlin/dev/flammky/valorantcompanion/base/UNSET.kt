package dev.flammky.valorantcompanion.base

interface UNSET <T> where T: Any {

    @Suppress("PropertyName")
    val UNSET: T
}

val <T: Any> UNSET<T>.isUNSET
    get() = this === this.UNSET