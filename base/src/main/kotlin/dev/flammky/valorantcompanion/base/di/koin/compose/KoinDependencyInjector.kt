package dev.flammky.valorantcompanion.base.di.koin.compose

import dev.flammky.valorantcompanion.base.di.DependencyInjector
import org.koin.core.context.KoinContext
import kotlin.reflect.KClass

class KoinDependencyInjector(
    private val context: KoinContext
): DependencyInjector {

    override fun <T : Any> requireInject(clazz: KClass<T>): T {
        val koin = context.get()
        return koin.get<T>(clazz, null, null)
    }
}