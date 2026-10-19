package com.askit.designsystem.empty

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Inline empty / error state for a section inside a scrolling list (the full-screen version is
 * [AskITEmptyState]). Announced politely when it appears; the title is a heading so screen-reader
 * users can jump to it. The action (Retry, Change area, Edit filters) only shows when [onAction]
 * is set, so a state never offers a button that does nothing.
 */
@Composable
fun AskITStateCard(
    @DrawableRes iconRes: Int,
    title: String,
    supporting: String,
    modifier: Modifier = Modifier,
    tone: AskITStateTone = AskITStateTone.Neutral,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    actionModifier: Modifier = Modifier,
) {
    val colors = stateCardColors(tone)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(16.dp),
        color = colors.container,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = colors.icon,
                modifier = Modifier.size(64.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    color = colors.content,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = supporting,
                    color = colors.supporting,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (actionLabel != null && onAction != null) {
                    TextButton(
                        onClick = onAction,
                        modifier = actionModifier,
                        colors = ButtonDefaults.textButtonColors(contentColor = colors.content),
                    ) {
                        Text(actionLabel)
                    }
                }
            }
        }
    }
}

private data class StateCardColors(val container: Color, val content: Color, val supporting: Color, val icon: Color)

@Composable
private fun stateCardColors(tone: AskITStateTone): StateCardColors {
    val scheme = MaterialTheme.colorScheme
    return when (tone) {
        AskITStateTone.Neutral -> StateCardColors(
            container = scheme.surfaceContainerHigh,
            content = scheme.onSurface,
            supporting = scheme.onSurfaceVariant,
            icon = scheme.onSurfaceVariant,
        )

        AskITStateTone.Offline -> StateCardColors(
            container = scheme.tertiaryContainer,
            content = scheme.onTertiaryContainer,
            supporting = scheme.onTertiaryContainer,
            icon = scheme.tertiary,
        )

        AskITStateTone.Error -> StateCardColors(
            container = scheme.errorContainer,
            content = scheme.onErrorContainer,
            supporting = scheme.onErrorContainer,
            icon = scheme.error,
        )
    }
}
