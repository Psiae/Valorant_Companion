package dev.flammky.valorantcompanion.live.loadout.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead
import dev.flammky.valorantcompanion.base.theme.material3.localMaterial3Background
import dev.flammky.valorantcompanion.base.theme.material3.localMaterial3Surface
import kotlinx.collections.immutable.persistentListOf

@Composable
fun SprayLoadoutScreenContent(
    modifier: Modifier,
    state: SprayLoadoutScreenState,
) {
    val sprayPickerState = rememberSprayLoadoutPickerPresenter().present()
    val sprayPickerPoolState = rememberSprayLoadoutPickerPoolPresenter().present()
    val screenHost = remember {
        object {
            val visibleScreenState = mutableStateListOf<Pair<String, @Composable () -> Unit>>()

            val hasVisibleScreen
                @SnapshotRead get() = visibleScreenState.isNotEmpty()

            @SnapshotRead
            fun hosted(key: String): Boolean {
                return visibleScreenState.any { (vKey, vContent) -> key == vKey }
            }

            fun show(key: String, content: @Composable SprayLoadoutScreenHostContentScope.() -> Unit) {
                dismiss(key)
                visibleScreenState.add(
                    key to @Composable {
                        object : SprayLoadoutScreenHostContentScope {
                            override val hasFocus: Boolean
                                get() = visibleScreenState.lastOrNull()?.first == key

                            override fun dismiss() = dismiss(key)
                        }.content()
                    }
                )
            }

            fun dismiss(key: String) {
                visibleScreenState.removeAll { (vKey, vContent) -> key == vKey }
            }

            @Composable
            fun Content(modifier: Modifier) {
                if (hasVisibleScreen) {
                    BackHandler {
                        visibleScreenState.lastOrNull()?.first?.let { dismiss(it) }
                    }
                    visibleScreenState.forEach { (key, screen) ->
                        Box(
                            modifier = modifier
                                .fillMaxSize()
                                .localMaterial3Surface()
                        ) {
                            screen.invoke()
                        }
                    }
                }
            }
        }
    }
    SprayLoadoutScreenContentPlacement(
        modifier = modifier,
        sprayPicker = { sprayPickerModifier ->
            SprayLoadoutPicker(
                modifier = sprayPickerModifier,
                state = sprayPickerState,
                onSlotClicked = { slot ->
                    screenHost.show("picker") @Composable {
                        val equipsIds = remember(sprayPickerState.activeSpraysKey) {
                            sprayPickerState.activeSprays.mapTo(
                                persistentListOf<String>().builder(),
                                { item -> item.equipSlotId }
                            ).build()
                        }
                        SprayLoadoutPickerDetailScreen(
                            modifier = Modifier,
                            dismiss = ::dismiss ,
                            state = rememberSprayLoadoutPickerDetailPresenter().present(
                                equipIdsKey = sprayPickerState.activeSpraysKey,
                                equipIds = equipsIds,
                                selectedSlotIndex = equipsIds.indexOf(slot)
                            )
                        )
                    }
                }
            )
        },
        sprayPickerPool = { sprayPoolModifier ->
            SprayLoadoutPickerPool(
                modifier = sprayPoolModifier,
                state = sprayPickerPoolState,
                onSprayClicked = { spray ->

                    screenHost.show("pickerpool") @Composable {

                    }
                }
            )
        },
        screenHost =  { screenHostModifier ->
            screenHost.Content(screenHostModifier)
        }
    )
}

@Composable
private fun SprayLoadoutScreenContentPlacement(
    modifier: Modifier,
    sprayPicker: @Composable (Modifier) -> Unit,
    sprayPickerPool: @Composable (Modifier) -> Unit,
    screenHost: @Composable (Modifier) -> Unit
) {
    Box {
        Column(modifier.fillMaxSize()) {
            sprayPicker(Modifier.padding(12.dp))
            Spacer(Modifier.height(12.dp))
            sprayPickerPool(Modifier.padding(12.dp))
        }
        screenHost(Modifier)
    }
}