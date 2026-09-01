package com.askit.designsystem.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

data class AskITVerticalToolItem(
    val icon: ImageVector,
    @StringRes val labelRes: Int,
    val onClick: () -> Unit,
    val testTag: String? = null,
    val selected: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskITVerticalToolColumn(
    items: List<AskITVerticalToolItem>,
    modifier: Modifier = Modifier,
    overlay: Boolean = false,
) {
    val idleTint = MaterialTheme.colorScheme.onSurface
    val selectedTint = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items.forEach { item ->
            key(item.testTag ?: item.labelRes) {
            val label = stringResource(item.labelRes)
            val tooltipState = rememberTooltipState()
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Left,
                ),
                tooltip = { PlainTooltip { Text(label) } },
                state = tooltipState,
            ) {
                IconButton(
                    onClick = item.onClick,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .then(
                            if (item.testTag != null) Modifier.testTag(item.testTag) else Modifier,
                        )
                        .semantics { contentDescription = label },
                ) {
                    if (overlay) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (item.selected) Color.White else Color.White.copy(alpha = 0.55f),
                                    shape = CircleShape,
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.95f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = Color.Black,
                            )
                        }
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (item.selected) selectedTint else idleTint,
                        )
                    }
                }
            }
            }
        }
    }
}
