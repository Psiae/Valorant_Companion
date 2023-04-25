package dev.flammky.valorantcompanion.pvp.util

import kotlin.reflect.KProperty

/**
 * Lazy delegate, but with construct function instead of constructor
 */

internal class LazyConstructor<T> @JvmOverloads constructor(lock: Any = Any()) {

    /**
     * Placeholder Object
     */
    private object UNSET

    /**
     * The Lock
     */
    private val _lock: Any = lock

    /**
     * The value holder. [UNSET] if not set
     *
     * @throws IllegalStateException if trying to set value without lock
     * @throws IllegalStateException if value was already set
     */
    private var _value: Any? = UNSET
        set(value) {
            check(Thread.holdsLock(_lock)) {
                "Trying to set field without lock"
            }
            check(field === UNSET) {
                "localValue was $field when trying to set $value"
            }
            field = value
        }

    @Suppress("UNCHECKED_CAST")
    private val castValue: T
        get() = try {
            _value as T
        } catch (cce: ClassCastException) {
            error("localValue=$_value was UNSET")
        }

    /**
     * The value.
     *
     * @throws IllegalStateException if [_value] is [UNSET]
     */
    val value: T
        get() {
            if (!isConstructed()) {
                // The value is not yet initialized, check if its still being initialized.
                // If not then IllegalStateException will be thrown
                sync()
            }
            return castValue
        }

    /**
     *  Whether [_value] is already initialized
     *  @see isConstructedAtomic
     */
    fun isConstructed() = _value !== UNSET

    /**
     * Whether [_value] is already initialized, atomically
     * @see isConstructed
     */
    fun isConstructedAtomic() = sync { isConstructed() }

    /** Construct the delegated value, if not already constructed */
    fun construct(lazyValue: () -> T): T {
        if (isConstructed()) {
            return castValue
        }
        return sync {
            if (!isConstructed()) {
                _value = lazyValue()
            }
            castValue
        }
    }

    fun constructOrThrow(
        lazyValue: () -> T,
        lazyThrow: () -> Nothing
    ): T {
        if (isConstructed()) {
            lazyThrow()
        }
        return sync {
            if (!isConstructed()) {
                _value = lazyValue()
            } else {
                lazyThrow()
            }
            castValue
        }
    }

    private fun sync(): Unit = sync { }
    private fun <T> sync(block: () -> T): T = synchronized(_lock) { block() }

    companion object {

        val AlreadyConstructedException
            get() = IllegalStateException("Value was already been initialized")

        val <T> LazyConstructor<T>.asLazy: Lazy<T>
            get() = object : Lazy<T> {
                override val value: T get() = this@asLazy.value
                override fun isInitialized(): Boolean = isConstructed()
            }

        fun <T> LazyConstructor<T>.valueOrNull(): T? {
            return try { value } catch (ise: IllegalStateException) { null }
        }

        fun <T> LazyConstructor<T>.constructOrThrow(value: T): T {
            return constructOrThrow(
                lazyValue = { value },
                lazyThrow = { throw AlreadyConstructedException }
            )
        }

        fun <T> LazyConstructor<T>.constructOrThrow(
            lazyValue: () -> T
        ): T {
            return constructOrThrow(
                lazyValue = lazyValue,
                lazyThrow = { throw AlreadyConstructedException }
            )
        }

        operator fun <T> LazyConstructor<T>.getValue(receiver: Any?, property: KProperty<*>): T {
            return value
        }

        operator fun <T> LazyConstructor<T>.setValue(receiver: Any?, property: KProperty<*>, value: T) {
            construct { value }
        }
    }
}