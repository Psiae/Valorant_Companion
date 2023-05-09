package dev.flammky.valorantcompanion.live.shared.presentation

sealed class LocalImageData<T>(val value: T) {
    class File(file: java.io.File) : LocalImageData<java.io.File>(file)
    class Resource(id: Int) : LocalImageData<Int>(id)
}
