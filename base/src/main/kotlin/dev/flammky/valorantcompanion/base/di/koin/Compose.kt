package dev.flammky.valorantcompanion.base.di.koin

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.get

@Composable
inline fun <reified T> getFromKoin(): T = get()