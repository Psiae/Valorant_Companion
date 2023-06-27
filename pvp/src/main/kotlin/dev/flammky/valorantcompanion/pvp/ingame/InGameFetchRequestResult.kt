package dev.flammky.valorantcompanion.pvp.ingame
import dev.flammky.valorantcompanion.pvp.error.PVPModuleErrorCodes

class InGameFetchRequestResult <T> private constructor(
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
        ): InGameFetchRequestResult<T> {
            return InGameFetchRequestResult.failure(exception, errorCode)
        }

        fun success(
            data: T
        ): InGameFetchRequestResult<T> {
            return InGameFetchRequestResult.success(data)
        }
    }

    companion object {

        internal fun <T> success(data: T) = InGameFetchRequestResult<T>(
            data = data,
            ex = null,
            errorCode = null
        )

        internal fun <T> failure(
            ex: Exception,
            errorCode: Int
        ) = InGameFetchRequestResult<T>(
            data = null,
            ex = ex,
            errorCode = errorCode
        )

        internal inline fun <T> build(
            block: Builder<T>.() -> InGameFetchRequestResult<T>
        ): InGameFetchRequestResult<T> {
            return Builder<T>().block()
        }

        internal inline fun <T> buildCatching(
            block: Builder<T>.() -> InGameFetchRequestResult<T>
        ): InGameFetchRequestResult<T> {
            return runCatching { build(block) }
                .getOrElse { ex -> failure(ex as Exception, PVPModuleErrorCodes.UNHANDLED_EXCEPTION) }
        }
    }
}

inline fun <T> InGameFetchRequestResult<T>.onSuccess(
    block: (data: T) -> Unit
): InGameFetchRequestResult<T> {
    if (isSuccess) {
        block(getOrNull()!!)
    }
    return this
}

inline fun <T> InGameFetchRequestResult<T>.onFailure(
    block: (exception: Exception, errorCode: Int) -> Unit
): InGameFetchRequestResult<T> {
    if (!isSuccess) {
        block(getExceptionOrNull()!!, getErrorCodeOrNull()!!)
    }
    return this
}

inline fun <T, R> InGameFetchRequestResult<T>.getOrElse(
    block: (exception: Exception, errorCode: Int) -> R
): R where T : R {
    return if (!isSuccess) {
        block(getExceptionOrNull()!!, getErrorCodeOrNull()!!)
    } else {
        getOrNull()!!
    }
}

inline fun <T> InGameFetchRequestResult<T>.getOrThrow(): T {
    return if (!isSuccess) {
        throw getExceptionOrNull()!!
    } else {
        getOrNull()!!
    }
}

