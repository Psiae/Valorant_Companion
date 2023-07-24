package dev.flammky.valorantcompanion.live.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.flammky.valorantcompanion.base.compose.ComposableFun
import dev.flammky.valorantcompanion.base.compose.lazy.LazyContent
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.backgroundColorAsState
import dev.flammky.valorantcompanion.live.loadout.presentation.root.LiveLoadout
import dev.flammky.valorantcompanion.live.pvp.presentation.LivePVP
import dev.flammky.valorantcompanion.live.store.presentation.root.LiveStore

@Composable
internal fun LiveMainContent() {
    val selectedTabIndex = remember {
        mutableStateOf(0)
    }
    val container = remember {
        object : LiveMainScreenContainer {
            private val visibleScreenState = mutableStateListOf<Pair<String, @Composable () -> Unit>>()

            override fun dismiss() = error("dismiss with key instead")

            fun dismiss(key: String) {
                visibleScreenState.removeAll { it.first == key }
            }

            fun show(key: String, content: @Composable LiveMainScreenContainer.() -> Unit) {
                dismiss(key)
                visibleScreenState.add(
                    key to @Composable {
                        object : LiveMainScreenContainer {
                            override fun dismiss() {
                                dismiss(key)
                            }
                        }.content()
                    }
                )
            }

            fun lastContentOrNull(): ComposableFun? {
                return visibleScreenState.lastOrNull()?.second
            }
        }
    }
    LiveMainContentPlacement(
        topTab = {
            LiveMainContentTopTab(
                modifier = Modifier.padding(top = 12.dp),
                selectedTabIndex = selectedTabIndex.value,
                selectTab = { selectedTabIndex.value = it },
            )
        },
        topTabContent = {
            LazyContent(trigger = selectedTabIndex.value == 0) {
                LivePVP(
                    modifier = Modifier
                        .zIndex(if (selectedTabIndex.value == 0) 1f else 0f),
                    openScreen = { content ->
                        val key = 0.toString()
                        container.show(
                            key = key,
                            content = content
                        )
                    }
                )
            }

            LazyContent(trigger = selectedTabIndex.value == 1) {
                LiveLoadout(
                    modifier = Modifier
                        .zIndex(if (selectedTabIndex.value == 1) 1f else 0f),
                    openScreen = { content ->
                        val key = 1.toString()
                        container.show(
                            key = key,
                            content = content
                        )
                    }
                )
            }

            LazyContent(trigger = selectedTabIndex.value == 2) {
                LiveStore(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .zIndex(if (selectedTabIndex.value == 1) 1f else 0f),
                    openScreen = { content ->
                        val key = 2.toString()
                        container.show(
                            key = key,
                            content = content
                        )
                    }
                )
            }
        },
        screenContainer = {
            container.lastContentOrNull()?.let { content ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {}
                        .background(Material3Theme.backgroundColorAsState().value)
                ) {
                    content()
                }
            }
        }
    )
}

@Composable
internal inline fun LiveMainContentPlacement(
    topTab: @Composable () -> Unit,
    topTabContent: @Composable () -> Unit,
    screenContainer: @Composable () -> Unit,
    // TODO: define navigation bars inset
) = Box(
    modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(bottom = 80.dp)
) {
    Column() {
        Box { topTab() }
        Box { topTabContent() }
    }
    screenContainer()
}