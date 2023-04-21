package dev.flammky.valorantcompanion.di

import dev.flammky.valorantcompanion.ValorantCompanion
import dev.flammky.valorantcompanion.assets.di.KoinAssetsModule
import dev.flammky.valorantcompanion.auth.di.KoinAuthModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

object DI {

    fun ValorantCompanion.onPostCreate() {
        val self = this
        startKoin {
            androidContext(self)
            modules(KoinAuthModule)
            modules(KoinAssetsModule)
        }
    }
}