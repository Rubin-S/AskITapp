package com.askit.app.story.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.story.StoryAudience
import com.askit.app.story.StoryFormState
import com.askit.app.story.StoryViewModel

@Composable
fun StoryEditorBottomChrome(
    viewModel: StoryViewModel,
    state: StoryFormState,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (viewModel.needsTrimmer()) {
            val duration = (state.durationMs ?: 0L).toFloat().coerceAtLeast(1f)
            val end = (state.trimEndMs ?: state.durationMs ?: 0L).toFloat()
            Text(
                text = stringResource(R.string.story_trim_title),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
            RangeSlider(
                value = state.trimStartMs.toFloat()..end,
                onValueChange = { range ->
                    viewModel.updateTrimRange(range.start.toLong(), range.endInclusive.toLong())
                },
                valueRange = 0f..duration,
            )
        }

        BasicTextField(
            value = state.caption,
            onValueChange = viewModel::updateCaption,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            cursorBrush = SolidColor(Color.White),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Box {
                    if (state.caption.isEmpty()) {
                        Text(
                            text = stringResource(R.string.story_caption_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }
                    inner()
                }
            },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val chipColors = FilterChipDefaults.filterChipColors(
                containerColor = Color.White.copy(alpha = 0.12f),
                labelColor = Color.White,
                selectedContainerColor = Color.White,
                selectedLabelColor = Color.Black,
            )
            FilterChip(
                selected = state.audience == StoryAudience.Everyone,
                onClick = { viewModel.setAudience(StoryAudience.Everyone) },
                label = { Text(stringResource(R.string.story_your_story)) },
                colors = chipColors,
            )
            FilterChip(
                selected = state.audience == StoryAudience.CloseCircle,
                onClick = { viewModel.setAudience(StoryAudience.CloseCircle) },
                label = { Text(stringResource(R.string.story_close_circle)) },
                colors = chipColors,
            )
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.story_next))
            }
        }
    }
}
