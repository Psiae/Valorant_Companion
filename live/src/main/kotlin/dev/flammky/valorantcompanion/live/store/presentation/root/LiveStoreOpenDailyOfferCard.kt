package dev.flammky.valorantcompanion.live.store.presentation.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark
import kotlin.math.ln

@Composable
internal fun OpenDailyOfferCard(
    modifier: Modifier,
    iconModifier: Modifier,
    textModifier: Modifier,
) {
    val shape = remember {
        RoundedCornerShape(5)
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 2.dp,
                shape = shape,
                clip = false,
            )
            .clip(shape)
            .background(
                run {
                    val color = Color(0xFFD84646)
                    if (LocalIsThemeDark.current) {
                        val alpha = ((4.5f * ln(2.dp.value + 1)) + 2f) / 100f
                        val tint = Color.White.copy(alpha = alpha)
                        tint.compositeOver(color)
                    } else {
                        color
                    }
                }
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                modifier = iconModifier.size(36.dp),
                painter = painterResource(id = R_ASSET_DRAWABLE.time_machine_ios_16_glyph_100px),
                contentDescription = "open daily offer",
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(14.dp))
            BasicText(
                modifier = textModifier.fillMaxWidth(),
                text = "DAILY OFFER",
                style = MaterialTheme3.typography.labelLarge.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}