package dev.flammky.valorantcompanion.base

fun <T, R> referentialEquality(): (T, R) -> Boolean = { old, new -> old === new }