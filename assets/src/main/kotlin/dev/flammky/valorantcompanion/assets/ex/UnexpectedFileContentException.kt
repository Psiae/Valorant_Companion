package dev.flammky.valorantcompanion.assets.ex

import java.io.IOException

open class UnexpectedFileContentException : IOException, AssetModuleException {

    internal constructor() : super()

    internal constructor(message: String) : super(message)

    internal constructor(message: String, cause: Throwable?) : super(message, cause)
}