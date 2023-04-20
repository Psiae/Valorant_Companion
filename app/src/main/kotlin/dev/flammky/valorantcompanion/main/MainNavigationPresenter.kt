package dev.flammky.valorantcompanion.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController

@Composable
fun rememberMainNavigationPresenter(): MainNavigationPresenter {
    return remember { MainNavigationPresenter() }
}

class MainNavigationPresenter() {

    @Composable
    fun present(): MainNavigationState {
        val navController = rememberNavController()
        val state = remember { MainNavigationState(navController) }
        return state
    }
}