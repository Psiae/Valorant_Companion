package dev.flammky.valorantcompanion.pvp

import dev.flammky.valorantcompanion.pvp.error.PVPModuleErrorCodes
import dev.flammky.valorantcompanion.pvp.party.ex.PVPModuleException

class PVPAsyncRequestResult <T> private constructor(
    private val data: T?,
    private val ex: Exception?,
    private val errorCode: Int?
) {

    val isSuccess: Boolean
        get() = data != null

    fun getOrNull(): T? = data

    fun getExceptionOrNull(): Exception? = ex

    fun getErrorCodeOrNull(): Int? = errorCode

    internal class Builder <T> internal constructor() {

        fun failure(
            exception: Exception,
            errorCode: Int
        ): PVPAsyncRequestResult<T> {
            return PVPAsyncRequestResult.failure(exception, errorCode)
        }

        fun success(
            data: T
        ): PVPAsyncRequestResult<T> {
            return PVPAsyncRequestResult.success(data)
        }
    }

    companion object {

        internal fun <T> success(data: T) = PVPAsyncRequestResult<T>(
            data = data,
            ex = null,
            errorCode = null
        )

        internal fun <T> failure(
            ex: Exception,
            errorCode: Int
        ) = PVPAsyncRequestResult<T>(
            data = null,
            ex = ex,
            errorCode = errorCode
        )

        internal inline fun <T> build(
            block: Builder<T>.() -> PVPAsyncRequestResult<T>
        ): PVPAsyncRequestResult<T> {
            return Builder<T>().block()
        }

        internal inline fun <T> buildCatching(
            block: Builder<T>.() -> PVPAsyncRequestResult<T>
        ): PVPAsyncRequestResult<T> {
            return runCatching { build(block) }
                .getOrElse { ex -> failure(ex as Exception, PVPModuleErrorCodes.UNHANDLED_EXCEPTION) }
        }
    }
}

inline fun <T> PVPAsyncRequestResult<T>.onSuccess(
    block: (data: T) -> Unit
): PVPAsyncRequestResult<T> {
    if (isSuccess) {
        block(getOrNull() as T)
    }
    return this
}

inline fun <T> PVPAsyncRequestResult<T>.onFailure(
    block: (exception: Exception, errorCode: Int) -> Unit
): PVPAsyncRequestResult<T> {
    if (!isSuccess) {
        block(getExceptionOrNull()!!, getErrorCodeOrNull()!!)
    }
    return this
}

inline fun <T> PVPAsyncRequestResult<T>.getOrElse(
    block: (exception: Exception, errorCode: Int) -> T
): T {
    return if (!isSuccess) {
        block(getExceptionOrNull()!!, getErrorCodeOrNull()!!)
    } else {
        getOrNull() as T
    }
}

inline fun <T> PVPAsyncRequestResult<T>.getOrThrow(): T {
    return if (!isSuccess) {
        throw getExceptionOrNull()!!
    } else {
        getOrNull() as T
    }
}

inline fun <T> PVPAsyncRequestResult<T>.asKtResult(): Result<T> {
    return if (isSuccess) {
        Result.success(getOrNull() as T)
    } else {
        Result.failure(getExceptionOrNull()!!)
    }
}

