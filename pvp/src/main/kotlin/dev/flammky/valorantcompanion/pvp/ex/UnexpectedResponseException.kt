package dev.flammky.valorantcompanion.pvp.ex

class UnexpectedResponseException internal constructor(
    override val message: String?
) : Exception() {
}