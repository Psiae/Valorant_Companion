package dev.flammky.valorantcompanion.live.loadout.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.spray.LoadSprayImageRequest
import dev.flammky.valorantcompanion.assets.spray.ValorantSprayImageType
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.compose.HeightSpacer
import dev.flammky.valorantcompanion.base.compose.clickable
import dev.flammky.valorantcompanion.base.compose.consumeDownGesture
import dev.flammky.valorantcompanion.base.compose.rememberWithCompositionObserver
import dev.flammky.valorantcompanion.base.compose.compose
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.di.requireInject
import dev.flammky.valorantcompanion.base.isUNSET
import dev.flammky.valorantcompanion.base.kt.coroutines.awaitOrCancelOnException
import dev.flammky.valorantcompanion.base.strictResultingLoop
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.live.BuildConfig
import kotlinx.coroutines.delay

@Composable
fun SprayLoadoutPickerDetailScreen(
    modifier: Modifier,
    dismiss: () -> Unit,
    state: SprayLoadoutPickerDetailScreenState
) {
    SprayLoadoutPickerDetailScreenPlacement(
        modifier = modifier,
        surface = {
            Modifier
                .fillMaxSize()
                .localMaterial3Surface(
                    applyInteractiveUxEnforcement = false
                )
        },
        content = {
            if (!state.isUNSET) {
                SprayLoadoutPickerDetailScreenContent(
                    modifier = Modifier,
                    slotTotal = state.slotEquipIds.size,
                    selectedSlotIndex = state.selectedSlotEquipIndex,
                    getSlotSpray = remember(state.slotContentsKey, state.slotEquipIdsKey) {
                        { i ->
                            val slotUUID = state.slotEquipIds[i]
                            state.slotContents[slotUUID]?.sprayId
                        }
                     },
                    selectedSlotSprayDisplayName = state.selectedSlotSprayDisplayName,
                    selectedSprayUUID = state.selectedReplacementSprayUUID,
                    selectedSprayDisplayName = state.selectedReplacementSprayDisplayName,
                    selectSpray = state.selectSpray,
                    canReplaceSpray = state.canReplaceSpray,
                    confirmReplaceSpray = state.confirmReplaceSpray,
                    explicitLoading = state.explicitLoading,
                    explicitLoadingMessage = state.explicitLoadingMessage
                )
            }
        }
    )
}

@Composable
private fun SprayLoadoutPickerDetailScreenContent(
    modifier: Modifier,
    slotTotal: Int,
    selectedSlotIndex: Int,
    getSlotSpray: (Int) -> String?,
    selectedSlotSprayDisplayName: String,
    selectedSprayUUID: String?,
    selectedSprayDisplayName: String,
    selectSpray: (String) -> Unit,
    canReplaceSpray: Boolean,
    confirmReplaceSpray: () -> Unit,
    explicitLoading: Boolean,
    explicitLoadingMessage: String?
) {
    val openPreview = remember {
        mutableStateOf(false)
    }
    SprayLoadoutPickerDetailScreenContentPlacement(
        modifier = modifier,
        currentSpray = { currentSprayModifier ->
            Column(modifier = currentSprayModifier) {
                BasicText(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = selectedSlotSprayDisplayName,
                    overflow = TextOverflow.Ellipsis,
                    style = compose {
                        val color = Material3Theme.surfaceContentColorAsState().value
                        val style = MaterialTheme3.typography.titleMedium
                        style.copy(color = color, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    },
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                SprayLoadoutPickerDetailImage(
                    modifier = Modifier
                        .size(200.dp),
                    spray = getSlotSpray(selectedSlotIndex)
                )
            }
        },
        selectedSpray = { selectedSprayModifier ->
            // TODO: if nothing is selected then we show that the selected will be shown here
            if (selectedSprayUUID != null) {
                Column(modifier = selectedSprayModifier) {
                    BasicText(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = selectedSprayDisplayName,
                        overflow = TextOverflow.Ellipsis,
                        style = compose {
                            val color = Material3Theme.surfaceContentColorAsState().value
                            val style = MaterialTheme3.typography.titleMedium
                            style.copy(color = color, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        },
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SprayLoadoutPickerDetailImage(
                        modifier = Modifier
                            .size(200.dp),
                        spray = selectedSprayUUID
                    )
                }
            } else {
                Box(modifier = selectedSprayModifier
                    .heightIn(min = 200.dp)
                    .fillMaxWidth()) {
                    BasicText(
                        modifier = Modifier.align(Alignment.Center),
                        text = "SELECTED SPRAY WILL BE DISPLAYED HERE",
                        overflow = TextOverflow.Ellipsis,
                        style = compose {
                            val color = Material3Theme.surfaceContentColorAsState().value.copy(alpha = 0.68f)
                            val style = MaterialTheme3.typography.labelMedium
                            style.copy(color = color, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        },
                        maxLines = 2
                    )
                }
            }

        },
        confirmButton = { confirmButtonModifier ->
            SprayLoadoutPickerDetailConfirmButton(
                modifier = confirmButtonModifier
                    .interactiveUiElementSizeEnforcement(),
                enabled = canReplaceSpray,
                onClick = confirmReplaceSpray
            )
        },
        openPreviewButton = { openPreviewButtonModifier ->
            SprayLoadoutPickerDetailOpenPreviewButton(
                modifier = openPreviewButtonModifier
                    .interactiveUiElementSizeEnforcement(),
                onClick = { openPreview.value = true }
            )
        },
        sprayPool = { sprayPoolModifier ->
            BoxWithConstraints {
                val upSelectSpray = rememberUpdatedState(newValue = selectSpray)
                SprayLoadoutPickerPool(
                    modifier = sprayPoolModifier.height(
                        SprayLoadoutPickerPool.optimalHeight(4).dp.coerceAtMost(maxWidth)
                    ),
                    state = rememberSprayLoadoutPickerPoolPresenter().present(),
                    onSprayClicked = { upSelectSpray.value.invoke(it) }
                )
            }
        },
        explicitLoading = { explicitLoadingModifier ->
            if (explicitLoading) {
                Box(
                    explicitLoadingModifier
                        .fillMaxSize()
                        .background(
                            color = (if (LocalIsThemeDark.current) Color.Black else Color.White).copy(
                                alpha = 0.9f
                            )
                        )
                        .consumeDownGesture()
                ) {
                    Column(Modifier.align(Alignment.Center)) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(60.dp)
                                .align(Alignment.CenterHorizontally),
                            color = Color.Red
                        )
                        if (explicitLoadingMessage != null) {
                            Spacer(Modifier.height(25.dp))
                            Text(
                                text = explicitLoadingMessage,
                                color = if (LocalIsThemeDark.current) Color.White else Color.Black,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

            }
        },
        preview = { previewModifier ->
            // we can also just hide the content
            if (openPreview.value) {
                val canSeeSelected = selectedSprayUUID != null
                val seeInitialState = remember {
                    mutableStateOf(!canSeeSelected)
                }
                Column(
                    previewModifier
                        .fillMaxSize()
                        .localMaterial3Surface(
                            color = { _ -> Color.Black.copy(alpha = 0.96f) }
                        )
                        .clickable(
                            onClick = { openPreview.value = false },
                            indication = null
                        )
                        .padding(48.dp),
                ) {
                    val cellSurface = Material3Theme.surfaceVariantColorAsState().value
                    SprayLoadoutPicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f, false)
                            .consumeDownGesture(),
                        activeSpraySlotCount = slotTotal,
                        cellSurface = { i ->
                            if (i != selectedSlotIndex)
                                Color.Black.copy(alpha = 0.1f).compositeOver(cellSurface)
                            else
                                Color.White.copy(alpha = 0.1f).compositeOver(cellSurface)
                        },
                        cellSurfaceModifier = { i ->
                            if (i != selectedSlotIndex)
                                Modifier
                            else
                                Modifier.clickable(enabled = canSeeSelected) {
                                    seeInitialState.value = !seeInitialState.value
                                }
                        },
                        cellContent = { i ->
                            SprayPickerCellContent(
                                modifier = if (i != selectedSlotIndex) {
                                    Modifier.alpha(0.5f)
                                } else {
                                    Modifier
                                },
                                spray = if (i == selectedSlotIndex) {
                                    if (seeInitialState.value) getSlotSpray(selectedSlotIndex)
                                    else selectedSprayUUID ?: getSlotSpray(selectedSlotIndex)
                                } else {
                                    getSlotSpray(i)
                                },
                            )
                        }
                    )
                    HeightSpacer(36.dp)
                    Box(
                        modifier = modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(30))
                            .clickable(
                                enabled = canSeeSelected,
                                onClick = { seeInitialState.value = !seeInitialState.value })
                            .background(Color(0xFFF4444C))
                            .interactiveUiElementSizeEnforcement()
                            .interactiveUiElementTextAlphaEnforcement(
                                isContent = false,
                                enabled = canSeeSelected
                            )
                            .padding(8.dp)
                    )  {
                        BasicText(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .interactiveUiElementTextAlphaEnforcement(
                                    isContent = true,
                                    enabled = canSeeSelected
                                ),
                            text = "SEE ${ if (seeInitialState.value) "SELECTED" else "INITIAL" }",
                            overflow = TextOverflow.Ellipsis,
                            style = compose {
                                val color = Material3Theme.surfaceContentColorAsState().value.copy(alpha = 1f)
                                val style = MaterialTheme3.typography.labelMedium
                                style.copy(color = color, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            },
                            maxLines = 2
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun SprayLoadoutPickerDetailScreenContentPlacement(
    modifier: Modifier,
    preview: @Composable (Modifier) -> Unit,
    currentSpray: @Composable (Modifier) -> Unit,
    selectedSpray: @Composable (Modifier) -> Unit,
    confirmButton: @Composable (Modifier) -> Unit,
    openPreviewButton: @Composable (Modifier) -> Unit,
    sprayPool: @Composable (Modifier) -> Unit,
    explicitLoading: @Composable (Modifier) -> Unit
) {
    Box(modifier = modifier) {

        Column(modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(12.dp))

            currentSpray(Modifier.align(Alignment.CenterHorizontally))

            Spacer(modifier = Modifier.height(12.dp))

            selectedSpray(Modifier.align(Alignment.CenterHorizontally))

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                confirmButton(
                    Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f, fill = false))
                openPreviewButton(
                    Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f, fill = false))
            }

            Spacer(modifier = Modifier.height(12.dp))

            sprayPool(Modifier.align(Alignment.CenterHorizontally))
        }
        explicitLoading(Modifier)
        preview(Modifier)
    }
}

@Composable
private fun SprayLoadoutPickerDetailImage(
    modifier: Modifier,
    spray: String?
) {
    AsyncImage(
        modifier = modifier.fillMaxSize(),
        model = run {
            val ctx = LocalContext.current
            val assetLoaderService =
                LocalDependencyInjector
                    .current
                    .requireInject<ValorantAssetsService>()
            val assetLoaderClient =
                rememberWithCompositionObserver(
                    key = assetLoaderService,
                    onRemembered = { client -> ; },
                    onForgotten = { client -> client.dispose() },
                    onAbandoned = { client -> client.dispose() },
                    block = { assetLoaderService.createLoaderClient() }
                )
            val dataKeyState = remember(spray) {
                mutableStateOf<Any>(Any())
            }
            val dataState = remember(spray) {
                mutableStateOf<Any?>(null)
            }
            LaunchedEffect(
                key1 = spray,
                key2 = assetLoaderClient,
            ) {
                if (spray.isNullOrEmpty()) return@LaunchedEffect
                dataState.value = strictResultingLoop {
                    val def = assetLoaderClient.loadSprayImageAsync(
                        req = LoadSprayImageRequest(
                            uuid = spray,
                            ValorantSprayImageType.FULL_ICON(transparentBackground = true),
                            ValorantSprayImageType.FULL_ICON(transparentBackground = false),
                            ValorantSprayImageType.DISPLAY_ICON
                        )
                    )
                    def.awaitOrCancelOnException().fold(
                        onSuccess = { localImage ->
                            LOOP_BREAK(localImage.value)
                        },
                        onFailure = {
                            // TODO: ask refresh confirmation
                            delay(1000)
                            LOOP_CONTINUE()
                        }
                    )
                }
                dataKeyState.value = Any()
            }
            remember(dataKeyState.value) {
                ImageRequest.Builder(ctx)
                    .data(dataState.value)
                    .build()
            }
        },
        contentDescription = null
    )
}

@Composable
fun SprayLoadoutPickerDetailConfirmButton(
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val upOnClick = rememberUpdatedState(newValue = onClick)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30))
            .clickable(enabled = enabled, onClick = {
                Log.d(
                    BuildConfig.LIBRARY_PACKAGE_NAME,
                    "loadout.presentation.SprayLoadoutPickerDetailScreen_confirmButton(KFunction<Unit>@${
                        System.identityHashCode(
                            upOnClick.value
                        )
                    })"
                )
                upOnClick.value.invoke()
            })
            .interactiveUiElementAlphaEnforcement(isContent = false, enabled = enabled)
            .background(Color(0xFFF4444C))
            .padding(8.dp)
    )  {
        BasicText(
            modifier = Modifier
                .align(Alignment.Center)
                .interactiveUiElementTextAlphaEnforcement(isContent = true, enabled = enabled),
            text = "REPLACE SPRAY",
            style = compose {
                val color = Material3Theme.surfaceContentColorAsState().value
                val style = MaterialTheme3.typography.labelLarge
                style.copy(color = color, fontWeight = FontWeight.Bold)
            }
        )
    }
}

@Composable
fun SprayLoadoutPickerDetailOpenPreviewButton(
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30))
            .clickable(onClick = onClick)
            .background(Color(0xFFF4444C))
            .padding(8.dp)
    )  {
        BasicText(
            modifier = Modifier.align(Alignment.Center),
            text = "PREVIEW",
            style = compose {
                val color = Material3Theme.surfaceContentColorAsState().value
                val style = MaterialTheme3.typography.labelLarge
                style.copy(color = color, fontWeight = FontWeight.Bold)
            }
        )
    }
}

@Composable
fun SprayLoadoutPickerDetailScreenPlacement(
    modifier: Modifier,
    surface: @Composable () -> Unit,
    content: @Composable () -> Unit
) = Box(modifier) {
    surface()
    content()
}