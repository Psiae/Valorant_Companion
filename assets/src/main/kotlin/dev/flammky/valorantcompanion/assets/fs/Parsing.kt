package dev.flammky.valorantcompanion.assets.fs

import dev.flammky.valorantcompanion.assets.ex.MissingFileContentException
import dev.flammky.valorantcompanion.assets.ex.UnexpectedFileContentIdentityException

internal inline fun missingFileContentException(msg: String): Nothing {
    throw MissingFileContentException(msg)
}

// e.g. the response explicitly said that the info is of other user UUID than the requested
internal inline fun unexpectedFileIdentityException(msg: String): Nothing {
    throw UnexpectedFileContentIdentityException()
}