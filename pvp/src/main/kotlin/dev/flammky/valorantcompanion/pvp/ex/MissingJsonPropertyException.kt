package dev.flammky.valorantcompanion.pvp.ex

class MissingJsonPropertyException internal constructor(
    override val message: String?
) : UnexpectedResponseException(message) {
}