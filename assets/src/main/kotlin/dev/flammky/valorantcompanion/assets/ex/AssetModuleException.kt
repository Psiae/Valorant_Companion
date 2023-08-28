package dev.flammky.valorantcompanion.assets.ex

interface AssetModuleException {

    fun asJavaException(): Exception = this as java.lang.Exception
}