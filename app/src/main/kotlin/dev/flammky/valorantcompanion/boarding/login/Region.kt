package dev.flammky.valorantcompanion.boarding.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.R
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.backgroundContentColorAsState

@Composable
fun RegionSelectionDropDown(
    current: String,
    select: (String) -> Unit
) {

    val expandedState = remember {
        mutableStateOf(false)
    }

    val textColor = Material3Theme.backgroundContentColorAsState().value

    Box {
        Box(
            modifier = Modifier.clickable { expandedState.value = true }
        ) {
            Row(
                modifier = Modifier.height(30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(5.dp))
                Text(
                    text = current,
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = R.drawable.arrow_drop_down_48px),
                    contentDescription = "drop down",
                    tint = textColor
                )
            }
        }

        Box {
            DropdownMenu(
                modifier = Modifier.background(Color.White),
                expanded = expandedState.value,
                onDismissRequest = { expandedState.value = false }
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "Region",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Divider(color = Color.Black)
                DropdownMenuItem(
                    text = {
                        Text(text = "North America", style = MaterialTheme.typography.labelLarge)

                    },
                    onClick = {
                        select("NA")
                        expandedState.value = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(text = "Latin America", style = MaterialTheme.typography.labelLarge)
                    },
                    onClick = {
                        select("LATAM")
                        expandedState.value = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(text = "Brazil", style = MaterialTheme.typography.labelLarge)
                    },
                    onClick = {
                        select("BR")
                        expandedState.value = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(text = "Europe / EMEA", style = MaterialTheme.typography.labelLarge)
                    },
                    onClick = {
                        select("EU / EMEA")
                        expandedState.value = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(text = "Asia Pacific", style = MaterialTheme.typography.labelLarge)
                    },
                    onClick = {
                        select("APAC")
                        expandedState.value = false
                    }
                )
                DropdownMenuItem(
                    text =  {
                        Text(text = "Korea", style = MaterialTheme.typography.labelLarge)
                    },
                    onClick = {
                        select("KR")
                        expandedState.value = false
                    }
                )
            }
        }
    }


}