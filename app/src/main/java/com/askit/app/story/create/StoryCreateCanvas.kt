package com.askit.app.story.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.story.StoryViewModel

@Composable
fun StoryCreateCanvas(
    viewModel: StoryViewModel,
    backgroundArgb: Long,
    textDraft: String,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isWhiteBackground = backgroundArgb == 0xFFFFFFFF
    val foregroundColor = if (isWhiteBackground) Color.Black else Color.White
    val actionButtonBg = if (isWhiteBackground) Color.Black else Color.White
    val actionButtonTint = if (isWhiteBackground) Color.White else Color.Black

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(backgroundArgb))
            .clickable(enabled = false) { },
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.story_back),
                    tint = foregroundColor,
                )
            }
            IconButton(
                onClick = { viewModel.cycleSolidBackground() },
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = stringResource(R.string.story_background_cycle),
                    tint = foregroundColor,
                )
            }
        }

        // Centered Text Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            BasicTextField(
                value = textDraft,
                onValueChange = viewModel::updateCreateText,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    color = foregroundColor,
                    textAlign = TextAlign.Center,
                ),
                cursorBrush = SolidColor(foregroundColor),
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (textDraft.isEmpty()) {
                            Text(
                                text = stringResource(R.string.story_create_text_hint),
                                style = MaterialTheme.typography.headlineMedium,
                                color = foregroundColor.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        inner()
                    }
                },
            )
        }

        // Bottom Monochrome Action Button
        IconButton(
            onClick = onConfirm,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .size(56.dp)
                .background(actionButtonBg, CircleShape),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.story_create_confirm),
                tint = actionButtonTint,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
