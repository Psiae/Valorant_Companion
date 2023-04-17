package dev.flammky.valorantcompanion

import android.app.Application
import dev.flammky.valorantcompanion.di.DI

class ValorantCompanion : Application() {

    override fun onCreate() {
        super.onCreate()
        with(DI) {
            onPostCreate()
        }
    }
}