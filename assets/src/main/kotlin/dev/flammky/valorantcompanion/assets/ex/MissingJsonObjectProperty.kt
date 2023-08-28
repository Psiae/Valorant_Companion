package dev.flammky.valorantcompanion.assets.ex

class MissingJsonObjectProperty : JsonParsingException {

    internal constructor() : super()

    internal constructor(message: String) : super(message)

    internal constructor(message: String, cause: Throwable?) : super(message, cause)
}