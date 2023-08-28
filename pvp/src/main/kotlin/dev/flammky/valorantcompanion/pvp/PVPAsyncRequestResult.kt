package dev.flammky.valorantcompanion.pvp

import dev.flammky.valorantcompanion.pvp.error.PVPModuleErrorCodes
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class PVPAsyncRequestResult <T> private constructor(
    private val data: T?,
    private val ex: Exception?,
    private val errorCode: Int?
) {

    val isSuccess: Boolean
        get() = ex == null

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

        internal inline fun <T> fromKtResult(
            ktResult: Result<T>,
            errorCode: Int?
        ): PVPAsyncRequestResult<T> {
            return ktResult
                .getOrElse { ex ->
                    return failure(
                        ex as Exception,
                        errorCode ?: PVPModuleErrorCodes.UNHANDLED_EXCEPTION)
                }.let { data -> success(data) }
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

@OptIn(ExperimentalContracts::class)
inline fun <T, R> PVPAsyncRequestResult<T>.fold(
    onSuccess: (data: T) -> R,
    onFailure: (exception: Exception, errorCode: Int) -> R
): R {
    contract {
        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
    }
    return if (isSuccess) {
        onSuccess(getOrNull()!!)
    } else {
        onFailure(getExceptionOrNull()!!, getErrorCodeOrNull()!!)
    }
}

