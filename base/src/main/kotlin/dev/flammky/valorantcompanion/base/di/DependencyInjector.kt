package dev.flammky.valorantcompanion.base.di

import kotlin.reflect.KClass

interface DependencyInjector {

    fun <T: Any> requireInject(clazz: KClass<T>): T
}

inline fun <reified T: Any> DependencyInjector.requireInject(): T = requireInject(T::class)