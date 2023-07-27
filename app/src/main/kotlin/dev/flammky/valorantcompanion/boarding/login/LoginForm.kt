@file:OptIn(ExperimentalMaterial3Api::class)

package dev.flammky.valorantcompanion.boarding.login

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.R
import dev.flammky.valorantcompanion.base.rememberThis
import dev.flammky.valorantcompanion.base.theme.material3.*

@Composable
fun LoginForm(
    modifier: Modifier,
    state: LoginFormState
) {
    val slot = state.inputSlot
    LoginFormPlacements(
        modifier = modifier,
        header = { headerModifier ->
            LoginFormHeader(
                modifier = headerModifier,
                inputSlot = slot
            )
        },
        textFields = { textFieldsModifier ->
            LoginFormTextFields(
                textFieldsModifier,
                inputSlot = slot
            )
        },
        rememberMe = { rememberMeModifier ->
            val enabled = /* TODO */ false
            val textColor = Material3Theme
                .backgroundContentColorAsState()
                .value.copy(alpha = if (enabled) 1f else 0.38f)
            Row(modifier = rememberMeModifier.height(30.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    modifier = Modifier.size(24.dp),
                    checked = false,
                    onCheckedChange = {},
                    enabled = enabled,
                    colors = run {
                        val background = if (LocalIsThemeDark.current) {
                            Color(0xFF121212)
                        } else {
                            Color(0xFFE7E7E7)
                        }
                        CheckboxDefaults.colors(
                            uncheckedColor = background,
                            disabledUncheckedColor = background.copy(alpha = 0.38f)
                        )
                    }
                )
                Spacer(Modifier.width(5.dp))
                Text("remember me", color = textColor, style = MaterialTheme.typography.labelLarge)
            }
        },
        regionSelection = { },
        loginButton = { modifier ->
            LoginFormButton(
                modifier,
                state.canLogin,
                onClick = state::login
            )
        }
    )
}

@Composable
private fun LoginFormPlacements(
    modifier: Modifier,
    header: @Composable (Modifier) -> Unit,
    textFields: @Composable (Modifier) -> Unit,
    regionSelection: @Composable (Modifier) -> Unit,
    rememberMe: @Composable (Modifier) -> Unit,
    loginButton: @Composable (Modifier) -> Unit,
) = Column(modifier = modifier.fillMaxSize()) {
    Spacer(modifier = Modifier.height(100.dp))
    header(Modifier.align(Alignment.CenterHorizontally))
    Spacer(modifier = Modifier.height(10.dp))
    textFields(Modifier.align(Alignment.CenterHorizontally))
    Spacer(modifier = Modifier.height(5.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .align(Alignment.CenterHorizontally),
    ) {
        rememberMe(Modifier)
        Box(Modifier.weight(1f, true))
        /*regionSelection(Modifier)*/
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        loginButton(Modifier.align(Alignment.CenterHorizontally))
        Spacer(
            modifier = Modifier.height(
                maxOf(
                    100.dp,
                    with(LocalDensity.current) {
                        WindowInsets.ime.getBottom(this).toDp().also { Log.d("LoginScreen", "ime height=$it") }
                    } + 20.dp
                )
            )
        )
    }
}

@Composable
private fun LoginFormTextFields(
    modifier: Modifier,
    inputSlot: LoginFormInputSlot
) {
    Column(modifier = modifier) {
        UsernameTextField(
            value = inputSlot.username,
            onValueChange = inputSlot::usernameInput
        )
        Spacer(modifier = Modifier.height(15.dp))
        PasswordTextField(
            value = inputSlot.password,
            onValueChange = inputSlot::passwordInput,
            !inputSlot.visiblePassword,
            inputSlot::toggleVisiblePassword
        )
    }
}

@Composable
private fun UsernameTextField(
    value: String,
    onValueChange: (String) -> Unit
) {
    val hasFocusState = remember {
        mutableStateOf(false)
    }
    val textColor = Material3Theme.backgroundContentColorAsState().value
    val outlineColor = Material3Theme.backgroundContentColorAsState().value
    TextField(
        modifier = remember {
            Modifier
                .fillMaxWidth(0.8f)
                .onFocusChanged { state ->
                    hasFocusState.value = state.isFocused
                }
        }.rememberThis(hasFocusState.value, outlineColor) {
            if (hasFocusState.value) border(width = 2.dp, color = outlineColor, shape = RoundedCornerShape(10)) else this
        },
        textStyle = MaterialTheme.typography.labelLarge.rememberThis {
            copy(color = textColor, fontWeight = FontWeight.SemiBold)
        },
        value = value,
        label = {
            Text(
                "USERNAME",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Material3Theme.backgroundContentColorAsState()
                    .value
                    .copy(alpha = 0.8f)
                    .compositeOver(Material3Theme.backgroundColorAsState().value)
            )
        },
        onValueChange = onValueChange,
        maxLines = 1,
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = if (hasFocusState.value) {
                Color.Transparent
            } else {
                if (LocalIsThemeDark.current) {
                    remember { Color(0xFF181818) }
                } else {
                    remember { Color(0xFFE7E7E7) }
                }
            },
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    mask: Boolean,
    toggleMask: () -> Unit
) {
    val maskInteractionSource = remember {
        MutableInteractionSource()
    }
    val hasFocusState = remember {
        mutableStateOf(false)
    }
    val textColor = Material3Theme.backgroundContentColorAsState().value
    val outlineColor = Material3Theme.backgroundContentColorAsState().value
    TextField(
        modifier = remember {
            Modifier
                .fillMaxWidth(0.8f)
                .onFocusChanged { state ->
                    hasFocusState.value = state.isFocused
                }
        }.rememberThis(hasFocusState.value, outlineColor) {
            if (hasFocusState.value) border(width = 2.dp, color = outlineColor, shape = RoundedCornerShape(10)) else this
        },
        textStyle = MaterialTheme.typography.labelLarge.rememberThis {
            copy(color = textColor, fontWeight = FontWeight.SemiBold)
        },
        value = value,
        label = {
            Text(
                "PASSWORD",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Material3Theme.backgroundContentColorAsState()
                    .value
                    .copy(alpha = 0.8f)
                    .compositeOver(Material3Theme.backgroundColorAsState().value)
            )
        },
        trailingIcon = {
            if (!hasFocusState.value) {
                return@TextField
            }
            Icon(
                modifier = remember {
                    Modifier
                        .size(24.dp)
                }.rememberThis(toggleMask) {
                    clickable(
                        interactionSource = maskInteractionSource,
                        indication = null,
                        onClick = toggleMask
                    )
                },
                painter = rememberVectorPainter(
                    image = ImageVector.vectorResource(
                        id = if (mask) { R.drawable.visibility_off_48px } else R.drawable.visibility_on_48px
                    )
                ),
                contentDescription = "mask",
                tint = Material3Theme.backgroundContentColorAsState().value
            )
        },
        onValueChange = onValueChange,
        maxLines = 1,
        singleLine = true,
        visualTransformation = if (mask) {
            remember { PasswordVisualTransformation() }
        } else {
            VisualTransformation.None
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = if (hasFocusState.value) {
                Color.Transparent
            } else {
                if (LocalIsThemeDark.current) {
                    remember { Color(0xFF181818) }
                } else {
                    remember { Color(0xFFE7E7E7) }
                }
            },
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        )
    )
}

@Composable
private fun LoginFormButton(
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (enabled) {
        Color.Red
    } else {
        Material3Theme.backgroundContentColorAsState().value.copy(alpha = 0.2f)
    }
    val iconTint = if (enabled) {
        Color.White
    } else {
        Material3Theme.backgroundContentColorAsState().value.copy(alpha = 0.2f)
    }
    val backgroundColor = if (enabled) {
        Color.Red
    } else {
        Color.Transparent
    }
    val interactionSource = remember {
        MutableInteractionSource()
    }
    Box(
        modifier = remember {
            modifier
                .size(65.dp)
                .clip(shape = RoundedCornerShape(25))
        }.rememberThis(backgroundColor) {
            background(color = backgroundColor)
        }.rememberThis(borderColor) {
            border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(25))
        }.rememberThis(enabled, onClick) {
            clickable(
                enabled = enabled,
                onClick = onClick,
                indication = null,
                interactionSource = interactionSource
            )
        }
    ) {
        Icon(
            modifier = Modifier
                .size(35.dp)
                .align(Alignment.Center),
            painter = rememberVectorPainter(image = ImageVector.vectorResource(id = R.drawable.right_arrow_100dp)),
            contentDescription = "Log In",
            tint = iconTint
        )
    }
}