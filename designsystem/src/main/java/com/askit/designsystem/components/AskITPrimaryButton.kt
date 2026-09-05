package com.askit.designsystem.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Global AskIT Primary CTA Button.
 * Strictly adheres to design system rules:
 * - In Light Mode: Black container (0xFF121418) with White text.
 * - In Dark Mode: AskIT Green container (0xFF7CE605) with Dark text (0xFF0F172A).
 * - Multi-layer silky soft lift shadow and accessible 56dp height.
 */
@Composable
fun AskITPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val containerColor = if (isDark) Color(0xFF7CE605) else Color(0xFF121418)
    val contentColor = if (isDark) Color(0xFF0F172A) else Color.White
    val ambientShadowColor = if (isDark) {
        Color(0xFF7CE605).copy(alpha = if (enabled) 0.28f else 0.05f)
    } else {
        Color.Black.copy(alpha = if (enabled) 0.18f else 0.05f)
    }
    val spotShadowColor = if (isDark) {
        Color(0xFF7CE605).copy(alpha = if (enabled) 0.40f else 0.08f)
    } else {
        Color.Black.copy(alpha = if (enabled) 0.28f else 0.08f)
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = if (isDark) Color(0xFF2C3240) else Color(0xFFE2E8F0),
            disabledContentColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp,
            disabledElevation = 0.dp,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = if (enabled) 14.dp else 0.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = ambientShadowColor,
                spotColor = spotShadowColor,
            ),
        content = content,
    )
}
