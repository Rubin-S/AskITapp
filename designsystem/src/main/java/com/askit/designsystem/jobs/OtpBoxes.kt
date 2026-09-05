package com.askit.designsystem.jobs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun OtpBoxes(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    digitCount: Int = 4,
    boxSize: Int = if (digitCount > 4) 44 else 56,
    isError: Boolean = false,
    autoFocus: Boolean = true,
    onComplete: ((String) -> Unit)? = null,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val shakeOffset = remember { Animatable(0f) }

    // Auto-focus keyboard on launch
    LaunchedEffect(autoFocus) {
        if (autoFocus && enabled) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // Shake animation on error
    LaunchedEffect(isError) {
        if (isError) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 350
                    -10f at 50 using LinearEasing
                    10f at 100 using LinearEasing
                    -7f at 150 using LinearEasing
                    7f at 200 using LinearEasing
                    -3f at 250 using LinearEasing
                    3f at 300 using LinearEasing
                    0f at 350 using LinearEasing
                },
            )
        }
    }

    // Active cursor blinking animation
    val infiniteTransition = rememberInfiniteTransition(label = "otp_cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cursor_alpha",
    )

    val isDark = isSystemInDarkTheme()
    val accentColor = if (isDark) Color(0xFF7CE605) else Color(0xFF121418)
    val errorColor = MaterialTheme.colorScheme.error

    val slotWidth: Dp = if (digitCount > 4) 44.dp else 56.dp
    val slotHeight: Dp = if (digitCount > 4) 54.dp else 60.dp

    BasicTextField(
        value = value,
        onValueChange = { incoming ->
            val clean = incoming.filter(Char::isDigit).take(digitCount)
            onValueChange(clean)
            if (clean.length == digitCount) {
                onComplete?.invoke(clean)
            }
        },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (value.length == digitCount) {
                    onComplete?.invoke(value)
                }
            },
        ),
        cursorBrush = SolidColor(Color.Transparent),
        modifier = modifier
            .focusRequester(focusRequester)
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .semantics { contentDescription = "OTP verification code input, $digitCount digits" },
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(if (digitCount > 4) 8.dp else 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                },
            ) {
                for (index in 0 until digitCount) {
                    // Optional 3-3 chunking divider for 6 digits
                    if (digitCount == 6 && index == 3) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    val char = value.getOrNull(index)?.toString() ?: ""
                    val isCurrentSlot = index == value.length && enabled
                    val isFilled = char.isNotEmpty()

                    val borderColor = when {
                        isError -> errorColor
                        isCurrentSlot -> accentColor
                        isFilled -> accentColor.copy(alpha = if (isDark) 0.6f else 0.4f)
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    }

                    val borderWidth = if (isCurrentSlot || isError) 2.dp else 1.dp
                    val containerColor = when {
                        isError -> errorColor.copy(alpha = 0.08f)
                        isCurrentSlot -> accentColor.copy(alpha = if (isDark) 0.10f else 0.04f)
                        isFilled -> if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow
                        else -> if (isDark) MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerLowest
                    }

                    Box(
                        modifier = Modifier
                            .size(width = slotWidth, height = slotHeight)
                            .clip(RoundedCornerShape(14.dp))
                            .background(containerColor)
                            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
                            .testTag("otp_box_$index"),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isFilled) {
                            Text(
                                text = char,
                                style = TextStyle(
                                    fontSize = if (digitCount > 4) 22.sp else 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    fontFeatureSettings = "tnum",
                                ),
                            )
                        } else if (isCurrentSlot) {
                            // High-contrast pulsing cursor
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(accentColor.copy(alpha = cursorAlpha)),
                            )
                        }
                    }
                }
            }
        },
    )
}
