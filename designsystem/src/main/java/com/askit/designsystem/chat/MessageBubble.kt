package com.askit.designsystem.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.askit.designsystem.people.AskITAvatar

@Composable
fun MessageBubble(
    body: String,
    photoModel: Any?,
    fromLocalUser: Boolean,
    showAvatar: Boolean,
    avatarUrl: String?,
    modifier: Modifier = Modifier,
) {
    val bubbleColor = if (fromLocalUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (fromLocalUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val alignment = if (fromLocalUser) Alignment.End else Alignment.Start
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (fromLocalUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!fromLocalUser) {
            if (showAvatar) {
                AskITAvatar(
                    avatarUrl = avatarUrl,
                    avatarSize = 24.dp,
                    fallbackIconSize = 16.dp,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(24.dp)
                        .clip(CircleShape),
                )
            } else {
                Box(modifier = Modifier.size(32.dp))
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalAlignment = alignment,
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bubbleColor)
                    .padding(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (photoModel != null) {
                        AsyncImage(
                            model = photoModel,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                        )
                    }
                    if (body.isNotBlank()) {
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodyLarge,
                            color = contentColor,
                        )
                    }
                }
            }
        }
    }
}
