package com.askit.app.story.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(backgroundArgb))
            .clickable(enabled = false) { },
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.story_back),
                tint = if (backgroundArgb == 0xFFFFFFFF) Color.Black else Color.White,
            )
        }

        BasicTextField(
            value = textDraft,
            onValueChange = viewModel::updateCreateText,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                color = if (backgroundArgb == 0xFFFFFFFF) Color.Black else Color.White,
                textAlign = TextAlign.Center,
            ),
            cursorBrush = SolidColor(
                if (backgroundArgb == 0xFFFFFFFF) Color.Black else Color.White,
            ),
            decorationBox = { inner ->
                if (textDraft.isEmpty()) {
                    Text(
                        text = stringResource(R.string.story_create_text_hint),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (backgroundArgb == 0xFFFFFFFF) {
                            Color.Black.copy(alpha = 0.5f)
                        } else {
                            Color.White.copy(alpha = 0.5f)
                        },
                        textAlign = TextAlign.Center,
                    )
                }
                inner()
            },
        )

        IconButton(
            onClick = {
                viewModel.cycleSolidBackground()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.story_background_cycle),
                color = if (backgroundArgb == 0xFFFFFFFF) Color.Black else Color.White,
            )
        }

        IconButton(
            onClick = onConfirm,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.story_create_confirm),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }
}
