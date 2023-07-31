package dev.flammky.valorantcompanion.assets.ex

open class AssetNotFoundException : Exception {
    internal constructor()
    internal constructor(msg: String) : super(msg)
}