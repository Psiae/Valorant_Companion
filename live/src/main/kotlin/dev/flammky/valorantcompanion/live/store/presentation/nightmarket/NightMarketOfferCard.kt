package dev.flammky.valorantcompanion.live.store.presentation.nightmarket

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.assets.R_ASSET_RAW
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.compose.compose
import dev.flammky.valorantcompanion.base.compose.nearestBlackOrWhite
import dev.flammky.valorantcompanion.base.compose.tintElevatedSurface
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun NightMarketOfferCard(
    modifier: Modifier,
    state: NightMarketOfferCardState,
    canOpenDetail: Boolean,
    openDetail: () -> Unit,
    shape: Shape
) = NightMarketOfferCard(
    modifier = modifier,
    tier = state.tier,
    tierImageKey = state.tierImageKey,
    tierImage = state.tierImage,
    displayImageKey = state.displayImageKey,
    displayImage = state.displayImage,
    displayName = state.displayName,
    discountPercentageText = state.discountPercentageText,
    discountedCostText = state.discountedAmountText,
    costText = state.costText,
    costImageKey = state.costImageKey,
    costImage = state.costImage,
    canOpenDetail = canOpenDetail,
    openDetail = openDetail,
    shape = shape
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NightMarketOfferCard(
    modifier: Modifier,
    tier: WeaponSkinTier,
    tierImageKey: Any,
    tierImage: LocalImage<*>,
    displayImageKey: Any,
    displayImage: LocalImage<*>,
    displayName: String,
    discountPercentageText: String,
    discountedCostText: String,
    costText: String,
    costImageKey: Any,
    costImage: LocalImage<*>?,
    canOpenDetail: Boolean,
    openDetail: () -> Unit,
    shape: Shape
) = BoxWithConstraints(
    modifier
        .composed {
            val isThemeDark = LocalIsThemeDark.current
            localMaterial3Surface(
                color = { color ->
                    if (
                        tier is WeaponSkinTier.UNSET ||
                        tier is WeaponSkinTier.NONE ||
                        tier is WeaponSkinTier.UNKNOWN
                    ) {
                        color.tintElevatedSurface(
                            tint = if (isThemeDark) {
                                Color.White
                            } else {
                                Color.Gray
                            },
                            elevation = 3.dp
                        )
                    } else {
                        Color(tier.highlightColor)
                            .compositeOver(color.nearestBlackOrWhite())
                    }
                },
                tonalElevation = 2.dp,
                tonalTint = Color(tier.highlightColor),
                shadowElevation = 2.dp,
                shape = shape
            )
        }
        .clickable(enabled = canOpenDetail, onClick = openDetail)
) {

    val maxWidth = maxWidth
    val maxHeight = maxHeight
    val ctx = LocalContext.current

    Icon(
        modifier = Modifier
            .requiredSize(maxWidth * 0.65f)
            .offset {
                IntOffset(
                    x = (maxWidth.value * 0.5f * 0.5f)
                        .roundToInt()
                        .inv(),
                    y = (maxHeight.value * 0.25).roundToInt()
                )
            }
            .alpha(0.2f),
        painter = rememberAsyncImagePainter(
            model = remember(ctx, tierImageKey) {
                ImageRequest.Builder(ctx)
                    .data(tierImage.value)
                    .build()
            }
        ),
        contentDescription = null,
        tint = Color.Unspecified
    )

    val discountLabelWidth = remember {
        mutableStateOf(0.dp)
    }

    val contentPadding = Material3Theme.dpPaddingIncrementsOf(2)
    Column(
        modifier = Modifier.padding(contentPadding)
    ) {

        Box {
            Row(modifier = Modifier) {
                Spacer(
                    modifier = Modifier.width(discountLabelWidth.value - contentPadding)
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .interactiveUiElementAlphaEnforcement(true, canOpenDetail)
                    ,
                    painter = painterResource(id = R_ASSET_DRAWABLE.right_arrow_100),
                    contentDescription = null,
                    tint = Material3Theme.surfaceContentColorAsState().value
                )
            }
        }

        AsyncImage(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            model = run {
                val ctx = LocalContext.current
                remember(ctx, displayImageKey) {
                    ImageRequest
                        .Builder(ctx)
                        .data(displayImage.value)
                        .build()
                }
            },
            contentDescription = null,
        )

        Row(horizontalArrangement = Arrangement.SpaceAround) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Bottom)
            ) {
                Icon(
                    modifier = Modifier
                        .size(24.dp),
                    painter = rememberAsyncImagePainter(
                        model = remember(ctx, tierImageKey) {
                            ImageRequest.Builder(ctx)
                                .data(tierImage.value)
                                .build()
                        }
                    ),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                ) {
                    BasicText(
                        modifier = Modifier
                            .basicMarquee(iterations = Int.MAX_VALUE),
                        text = displayName,
                        style = MaterialTheme3.typography.labelLarge.copy(
                            color = Material3Theme.blackOrWhiteContent(),
                        )
                    )
                }
            }


            Spacer(modifier = Modifier.width(8.dp))

            Row(
                modifier = Modifier
                    .align(Alignment.Bottom)
            ) {
                Column {
                    BasicText(
                        modifier = Modifier.align(Alignment.End),
                        text = costText,
                        style = MaterialTheme3.typography.labelSmall.copy(
                            color = Color(0xFFA30C2F),
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                    Spacer(modifier = Modifier.width(Material3Theme.dpPaddingIncrementsOf(1) / 2))
                    BasicText(
                        text = discountedCostText,
                        style = MaterialTheme3.typography.labelLarge.copy(
                            color = Material3Theme.blackOrWhiteContent(),
                        )
                    )
                }
                val imageModel = remember(ctx, costImageKey) {
                    if (costImage != null) {
                        ImageRequest.Builder(ctx)
                            .data(costImage.value)
                            .build()
                    } else {
                        null
                    }
                }
                if (imageModel != null) {
                    Spacer(modifier = Modifier.width(Material3Theme.dpPaddingIncrementsOf(2)))
                    Icon(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Bottom),
                        painter = rememberAsyncImagePainter(model = imageModel),
                        contentDescription = null,
                        tint = Material3Theme.surfaceContentColorAsState().value
                    )
                }
            }
        }
    }
    compose labels@ {
        SubcomposeLayout(
            measurePolicy = { constraint ->
                val textPadding = PaddingValues(
                    all = Material3Theme.dpPaddingIncrementsOf(1)
                )
                val text = subcompose(
                    slotId = "DiscountedText",
                    content = {
                        val labelLarge = MaterialTheme3.typography.labelLarge
                        val textStyle = remember(labelLarge) {
                            labelLarge.copy(color = Color(0xFFA30C2F))
                        }
                        BasicText(
                            modifier = Modifier,
                            text = discountPercentageText.take(3),
                            style = textStyle
                        )
                    }
                )
                val textMeasureResult = text.first().measure(constraint)
                val label = subcompose(
                    slotId = "Label",
                    content = {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.size(
                                maxOf(
                                    textMeasureResult.width.toDp() +
                                            textPadding.calculateLeftPadding(LayoutDirection.Ltr) +
                                            textPadding.calculateRightPadding(LayoutDirection.Ltr),
                                    textMeasureResult.height.toDp() +
                                            textPadding.calculateTopPadding() +
                                            textPadding.calculateBottomPadding()
                                ) * (3f / 2)
                            ),
                            onDraw = {
                                Path()
                                    .apply {
                                        val width = minOf(drawContext.size.width, drawContext.size.height)
                                        lineTo(x = width, y = 0f)
                                        lineTo(x = 0f, y = width)
                                        close()
                                    }
                                    .let { path -> drawPath(path, color = Color.Black, alpha = 0.6f) }
                            }
                        )
                    }
                )
                val labelMeasureResult = label.first().measure(constraint)
                discountLabelWidth.value = labelMeasureResult.width.toDp()
                layout(
                    width = labelMeasureResult.width,
                    height = labelMeasureResult.height,
                    placementBlock = {
                        labelMeasureResult.place(0, 0, 0f)
                        textMeasureResult.place(
                            0 + textPadding.calculateLeftPadding(LayoutDirection.Ltr).roundToPx(),
                            0 + textPadding.calculateTopPadding().roundToPx(),
                            0f
                        )
                    }
                )
            }
        )
    }
}

@Composable
@Preview
private fun NightMarketOfferCardPreview(

) {
    DefaultMaterial3Theme(dark = true) {
        NightMarketOfferCard(
            modifier = Modifier
                .padding(16.dp)
                .height(180.dp),
            state = NightMarketOfferCardState(
                tier = WeaponSkinTier.EXCLUSIVE,
                tierImageKey = Any(),
                tierImage = LocalImage.Resource(R_ASSET_RAW.contenttier_exclusive_displayicon),
                displayImageKey = Any(),
                displayImage = LocalImage.Resource(R_ASSET_RAW.debug_wpn_skn_exclusive_neofrontier_melee_displayicon),
                discountPercentageText = "35%",
                discountedAmountText = ceil(((100 - 35) / 100f * 4350f)).toInt().let { cost ->
                    val amountStr = cost.toString()
                    if (amountStr.isEmpty() || amountStr.length == 1) return@let amountStr
                    amountStr
                        .first()
                        .plus(amountStr.drop(1).chunked(3).joinToString(prefix = ",", separator = ","))
                },
                displayName = "Neo Frontier Axe",
                costText = 4350.let { cost ->
                    val amountStr = cost.toString()
                    if (amountStr.isEmpty() || amountStr.length == 1) return@let amountStr
                    amountStr
                        .first()
                        .plus(amountStr.drop(1).chunked(3).joinToString(prefix = ",", separator = ","))
                },
                costImageKey = Any(),
                costImage = LocalImage.Resource(R_ASSET_RAW.currency_vp_displayicon),
                showLoading = false,
                requireRefresh = false,
                refresh = {},
            ),
            canOpenDetail = false,
            openDetail = { },
            shape = RoundedCornerShape(12.dp)
        )
    }
}