package dev.flammky.valorantcompanion.base

fun <T, R> referentialEqualityFun(): (T, R) -> Boolean = { old, new -> old === new }