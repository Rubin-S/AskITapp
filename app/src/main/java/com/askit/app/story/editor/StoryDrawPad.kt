package com.askit.app.story.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.story.StoryViewModel

private val DRAW_COLORS = listOf(0xFFFFFFFF, 0xFF000000, 0xFF7CE605, 0xFFFF5252)

@Composable
fun StoryDrawPad(
    viewModel: StoryViewModel,
    selectedColorArgb: Long,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.story_draw_colors),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(end = 8.dp),
        )
        DRAW_COLORS.forEach { color ->
            FilterChip(
                selected = selectedColorArgb == color,
                onClick = { viewModel.setDrawColor(color) },
                label = { Text("") },
                modifier = Modifier.background(Color(color), CircleShape),
            )
        }
        IconButton(onClick = onDone) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.story_text_done),
            )
        }
    }
}
