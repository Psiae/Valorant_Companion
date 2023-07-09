package dev.flammky.valorantcompanion.pvp.pregame

import dev.flammky.valorantcompanion.pvp.error.PVPModuleErrorCodes

class PreGameFetchRequestResult <T> private constructor(
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
        ): PreGameFetchRequestResult<T> {
            return PreGameFetchRequestResult.failure(exception, errorCode)
        }

        fun success(
            data: T
        ): PreGameFetchRequestResult<T> {
            return PreGameFetchRequestResult.success(data)
        }
    }

    companion object {

        internal fun <T> success(data: T) = PreGameFetchRequestResult<T>(
            data = data,
            ex = null,
            errorCode = null
        )

        internal fun <T> failure(
            ex: Exception,
            errorCode: Int
        ) = PreGameFetchRequestResult<T>(
            data = null,
            ex = ex,
            errorCode = errorCode
        )

        internal inline fun <T> build(
            block: Builder<T>.() -> PreGameFetchRequestResult<T>
        ): PreGameFetchRequestResult<T> {
            return Builder<T>().block()
        }

        internal inline fun <T> buildCatching(
            block: Builder<T>.() -> PreGameFetchRequestResult<T>
        ): PreGameFetchRequestResult<T> {
            return runCatching { build(block) }
                .getOrElse { ex -> failure(ex as Exception, PVPModuleErrorCodes.UNHANDLED_EXCEPTION) }
        }
    }
}

inline fun <T> PreGameFetchRequestResult<T>.onSuccess(
    block: (data: T) -> Unit
): PreGameFetchRequestResult<T> {
    if (isSuccess) {
        block(getOrNull()!!)
    }
    return this
}

inline fun <T> PreGameFetchRequestResult<T>.onFailure(
    block: (exception: Exception, errorCode: Int) -> Unit
): PreGameFetchRequestResult<T> {
    if (!isSuccess) {
        block(getExceptionOrNull()!!, getErrorCodeOrNull()!!)
    }
    return this
}

inline fun <T> PreGameFetchRequestResult<T>.getOrElse(
    block: (exception: Exception, errorCode: Int) -> T
): T {
    return if (!isSuccess) {
        block(getExceptionOrNull()!!, getErrorCodeOrNull()!!)
    } else {
        getOrNull()!!
    }
}

inline fun <T> PreGameFetchRequestResult<T>.getOrThrow(): T {
    return if (!isSuccess) {
        throw getExceptionOrNull()!!
    } else {
        getOrNull()!!
    }
}

