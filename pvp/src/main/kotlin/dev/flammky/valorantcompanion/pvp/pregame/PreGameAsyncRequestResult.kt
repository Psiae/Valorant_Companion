package dev.flammky.valorantcompanion.pvp.pregame

import dev.flammky.valorantcompanion.pvp.error.PVPModuleErrorCodes

class PreGameAsyncRequestResult <T> private constructor(
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
        ): PreGameAsyncRequestResult<T> {
            return PreGameAsyncRequestResult.failure(exception, errorCode)
        }

        fun success(
            data: T
        ): PreGameAsyncRequestResult<T> {
            return PreGameAsyncRequestResult.success(data)
        }
    }

    companion object {

        internal fun <T> success(data: T) = PreGameAsyncRequestResult<T>(
            data = data,
            ex = null,
            errorCode = null
        )

        internal fun <T> failure(
            ex: Exception,
            errorCode: Int
        ) = PreGameAsyncRequestResult<T>(
            data = null,
            ex = ex,
            errorCode = errorCode
        )

        internal inline fun <T> build(
            block: Builder<T>.() -> PreGameAsyncRequestResult<T>
        ): PreGameAsyncRequestResult<T> {
            return Builder<T>().block()
        }

        internal inline fun <T> buildCatching(
            block: Builder<T>.() -> PreGameAsyncRequestResult<T>
        ): PreGameAsyncRequestResult<T> {
            return runCatching { build(block) }
                .getOrElse { ex -> failure(ex as Exception, PVPModuleErrorCodes.UNHANDLED_EXCEPTION) }
        }
    }
}

inline fun <T> PreGameAsyncRequestResult<T>.onSuccess(
    block: (data: T) -> Unit
): PreGameAsyncRequestResult<T> {
    if (isSuccess) {
        block(getOrNull()!!)
    }
    return this
}

inline fun <T> PreGameAsyncRequestResult<T>.onFailure(
    block: (exception: Exception, errorCode: Int) -> Unit
): PreGameAsyncRequestResult<T> {
    if (!isSuccess) {
        block(getExceptionOrNull()!!, getErrorCodeOrNull()!!)
    }
    return this
}

inline fun <T> PreGameAsyncRequestResult<T>.getOrElse(
    block: (exception: Exception, errorCode: Int) -> T
): T {
    return if (!isSuccess) {
        block(getExceptionOrNull()!!, getErrorCodeOrNull()!!)
    } else {
        getOrNull()!!
    }
}

inline fun <T> PreGameAsyncRequestResult<T>.getOrThrow(): T {
    return if (!isSuccess) {
        throw getExceptionOrNull()!!
    } else {
        getOrNull()!!
    }
}

