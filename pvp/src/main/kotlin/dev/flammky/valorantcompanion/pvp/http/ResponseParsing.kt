package dev.flammky.valorantcompanion.pvp.http

import dev.flammky.valorantcompanion.pvp.ex.MissingJsonPropertyException
import dev.flammky.valorantcompanion.pvp.ex.UnexpectedResponseException
import dev.flammky.valorantcompanion.pvp.ex.UnexpectedResponseIdentityException


internal fun unexpectedResponseError(msg: String, cause: Throwable? = null): Nothing {
    throw UnexpectedResponseException(msg, cause)
}

internal fun missingJsonObjectPropertyError(msg: String): Nothing {
    throw MissingJsonPropertyException(msg)
}

// e.g. the response explicitly said that the info is of other user UUID than the requested
internal fun unexpectedResponseIdentityError(msg: String): Nothing {
    throw UnexpectedResponseIdentityException(msg)
}