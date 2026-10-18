package com.askit.app.story.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.story.StoryAudience
import com.askit.app.story.StoryFormState
import com.askit.app.story.StoryViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StoryEditorBottomChrome(viewModel: StoryViewModel, state: StoryFormState, onNext: () -> Unit, modifier: Modifier = Modifier) {
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

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
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
        }
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("story_editor_next"),
        ) {
            Text(stringResource(R.string.story_next))
        }
    }
}
