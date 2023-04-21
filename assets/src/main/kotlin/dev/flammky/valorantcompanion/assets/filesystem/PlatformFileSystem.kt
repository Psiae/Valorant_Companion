package dev.flammky.valorantcompanion.assets.filesystem

import java.io.File

interface PlatformFileSystem {

    val internalDataFolder: File
    val internalCacheFolder: File

    fun String.appendFolder(name: String): String
    fun String.appendFile(name: String): String
    fun defaultInternalCacheFolder(
        path: (cachePath: String) -> String
    ): String
}