package dev.flammky.valorantcompanion.base.di.koin.compose

import dev.flammky.valorantcompanion.base.di.RuntimeDependencyInjector
import dev.flammky.valorantcompanion.base.di.koin.getFromKoin
import org.koin.core.context.GlobalContext
import org.koin.core.context.KoinContext
import org.koin.core.scope.get
import kotlin.reflect.KClass

class KoinRuntimeDependencyInjector(
    private val context: KoinContext
): RuntimeDependencyInjector {

    override fun <T : Any> requireInject(clazz: KClass<T>): T {
        val koin = context.get()
        return koin.get<T>(clazz, null, null)
    }
}