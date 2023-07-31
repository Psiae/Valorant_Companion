package dev.flammky.valorantcompanion.assets.filesystem

import android.app.Application
import dev.flammky.valorantcompanion.assets.util.prefix
import dev.flammky.valorantcompanion.assets.util.suffix
import java.io.File

class AndroidFileSystem(private val application: Application) : PlatformFileSystem {

    override val internalDataFolder: File = File(application.dataDir.absolutePath.suffix("/"))
    override val internalCacheFolder: File = File(application.cacheDir.absolutePath.suffix("/"))

    override fun String.appendFolder(name: String): String = (suffix("/") + name).suffix("/")
    override fun String.appendFile(name: String): String = prefix("/") + name

    // TODO: as extension
    override fun buildStringWithDefaultInternalCacheFolder(path: (cachePath: String) -> String): String {
        return path(internalCacheFolder.absolutePath)
    }

    override fun buildStringWithDefaultInternalDataFolder(path: (dataPath: String) -> String): String {
        return path(internalDataFolder.absolutePath)
    }
}