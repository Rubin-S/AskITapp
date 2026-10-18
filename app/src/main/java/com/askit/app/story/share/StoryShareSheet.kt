package com.askit.app.story.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.story.StoryAudience
import com.askit.app.story.StoryFormState
import com.askit.app.story.StoryViewModel
import com.askit.app.story.editor.StoryCanvas
import com.askit.app.story.editor.StoryFrame
import com.askit.app.story.stickers.PostReshareSticker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryShareSheet(
    viewModel: StoryViewModel,
    state: StoryFormState,
    solidBackgroundArgb: Long,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("story_share_sheet")
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.story_share_title),
                style = MaterialTheme.typography.titleLarge,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                StoryFrame(state.viewport) {
                    StoryCanvas(
                        mediaUri = state.mediaUri,
                        mediaType = state.mediaType,
                        solidBackgroundArgb = solidBackgroundArgb,
                        durationMs = state.durationMs,
                        trimStartMs = state.trimStartMs,
                        trimEndMs = state.trimEndMs,
                        transform = state.transform,
                        layers = state.layers,
                        readOnly = true,
                    )
                    state.sharedPost?.let { post ->
                        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                            PostReshareSticker(post = post, cardStyle = state.reshareCardStyle)
                        }
                    }
                }
            }

            val destinationLabel = when (state.audience) {
                StoryAudience.CloseCircle -> stringResource(R.string.story_close_circle)
                StoryAudience.Message -> stringResource(R.string.story_audience_message)
                StoryAudience.Everyone -> stringResource(R.string.story_your_story)
            }
            Text(
                text = stringResource(R.string.story_share_destination, destinationLabel),
                style = MaterialTheme.typography.bodyLarge,
            )

            FilterChip(
                selected = state.audience == StoryAudience.Message,
                onClick = {
                    if (state.audience == StoryAudience.Message) {
                        viewModel.setAudience(StoryAudience.Everyone)
                    } else {
                        viewModel.setAudience(StoryAudience.Message)
                    }
                },
                label = { Text(stringResource(R.string.story_audience_message)) },
            )

            if (state.audience == StoryAudience.Message) {
                Text(
                    text = stringResource(R.string.story_message_empty_state),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(
                onClick = onShare,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("story_share_confirm"),
            ) {
                Text(stringResource(R.string.story_share_action))
            }
        }
    }
}
