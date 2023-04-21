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

    override fun buildPathInDefaultInternalCacheFolder(path: (cachePath: String) -> String): String {
        return path(internalCacheFolder.absolutePath)
    }
}