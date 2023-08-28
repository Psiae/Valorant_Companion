package dev.flammky.valorantcompanion.assets.http

import dev.flammky.valorantcompanion.assets.ex.UnexpectedHttpResponseException
import dev.flammky.valorantcompanion.assets.ex.UnexpectedHttpResponseIdentityException


internal inline fun unexpectedResponseError(msg: String, cause: Throwable? = null): Nothing {
    throw UnexpectedHttpResponseException(msg, cause)
}

// e.g. the response explicitly said that the info is of other user UUID than the requested
internal inline fun unexpectedResponseIdentityError(msg: String, cause: Throwable? = null): Nothing {
    throw UnexpectedHttpResponseIdentityException(msg, cause)
}