package dev.flammky.valorantcompanion.base.util

abstract class ValueContainer<T> internal constructor() {
    abstract val value: T
}

open class ImmutableValueContainer <T> internal constructor(initialValue: T) : ValueContainer<T>() {
    override val value: T = initialValue
}

open class MutableValueContainer <T> internal constructor(initialValue: T): ValueContainer<T>() {
    override var value: T = initialValue
}

public fun <T> immutableValueContainerOf(value: T): ImmutableValueContainer<T> = ImmutableValueContainer(value)
public fun <T> mutableValeContainerOf(value: T): MutableValueContainer<T> = MutableValueContainer(value)