package dev.flammky.valorantcompanion.live.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.flammky.valorantcompanion.base.compose.lazy.LazyContent
import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead
import dev.flammky.valorantcompanion.base.theme.material3.localMaterial3Background
import dev.flammky.valorantcompanion.base.theme.material3.localMaterial3Surface
import dev.flammky.valorantcompanion.live.loadout.presentation.root.LiveLoadout
import dev.flammky.valorantcompanion.live.pvp.presentation.LivePVP
import dev.flammky.valorantcompanion.live.store.presentation.root.LiveStore

@Composable
internal fun LiveMainContent() {
    val selectedTabIndex = remember {
        mutableStateOf(0)
    }
    val screenHost = remember {
        object {
            private val visibleScreenState = mutableStateListOf<Pair<String, @Composable () -> Unit>>()

            val hasVisibleScreen
                @SnapshotRead get() = visibleScreenState.isNotEmpty()

            fun dismiss(key: String) {
                visibleScreenState.removeAll { it.first == key }
            }

            fun dismissAll() {
                if (!visibleScreenState.isEmpty()) {
                    visibleScreenState.clear()
                }
            }

            fun pop() {
                if (!visibleScreenState.isEmpty()) {
                    visibleScreenState.removeAt(visibleScreenState.lastIndex)
                }
            }

            fun show(key: String, content: @Composable LiveMainScreenScope.() -> Unit) {
                dismiss(key)
                visibleScreenState.add(
                    key to @Composable {
                        remember {
                            object : LiveMainScreenScope {
                                override val hasFocus: Boolean
                                    get() = visibleScreenState.lastOrNull()?.first == key
                                override fun dismiss() {
                                    dismiss(key)
                                }
                            }
                        }.content()
                    }
                )
            }

            @Composable
            fun Content() {
                if (hasVisibleScreen) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {}
                            .localMaterial3Background()
                    ) {
                        visibleScreenState.forEach { (key, screen) -> screen.invoke() }
                    }
                }
            }
        }
    }
    BackHandler(
        enabled = screenHost.hasVisibleScreen
    ) {
        screenHost.pop()
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
                val visible = selectedTabIndex.value == 0
                LivePVP(
                    modifier = Modifier
                        .zIndex(if (visible) 1f else 0f)
                        .fillMaxSize()
                        .localMaterial3Surface(),
                    isVisibleToUser = visible && !screenHost.hasVisibleScreen,
                    openScreen = { content ->
                        val key = 0.toString()
                        screenHost.show(
                            key = key,
                            content = content
                        )
                    }
                )
            }

            LazyContent(trigger = selectedTabIndex.value == 1) {
                LiveLoadout(
                    modifier = Modifier
                        .zIndex(if (selectedTabIndex.value == 1) 1f else 0f)
                        .fillMaxSize()
                        .localMaterial3Surface(),
                    openScreen = { content ->
                        val key = 1.toString()
                        screenHost.show(
                            key = key,
                            content = content
                        )
                    }
                )
            }
            LazyContent(trigger = selectedTabIndex.value == 2) {
                val visible = selectedTabIndex.value == 2
                LiveStore(
                    modifier = Modifier
                        .zIndex(if (selectedTabIndex.value == 2) 1f else 0f)
                        .fillMaxSize()
                        .localMaterial3Surface(),
                    isVisibleToUser = visible,
                    openScreen = { content ->
                        val key = 2.toString()
                        screenHost.show(
                            key = key,
                            content = content
                        )
                    }
                )
            }
        },
        screenHost = {
            screenHost.Content()
        }
    )
}

@Composable
internal inline fun LiveMainContentPlacement(
    topTab: @Composable () -> Unit,
    topTabContent: @Composable () -> Unit,
    screenHost: @Composable () -> Unit,
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
    screenHost()
}