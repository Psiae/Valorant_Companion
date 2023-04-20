package dev.flammky.valorantcompanion.boarding.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.backgroundContentColorAsState

@Composable
fun LoginFormHeader(
    modifier: Modifier,
    inputSlot: LoginFormInputSlot
) = Column(modifier = modifier) {
    Text(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        text = "Sign in",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        color = Material3Theme.backgroundContentColorAsState().value
    )
    Spacer(modifier = Modifier.height(30.dp))
    Text(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .fillMaxWidth(0.8f),
        text = inputSlot.exceptionMessage,
        style = MaterialTheme.typography.labelMedium,
        color = remember { Color(0xFFBE29CC) },
        textAlign = TextAlign.Center
    )
}