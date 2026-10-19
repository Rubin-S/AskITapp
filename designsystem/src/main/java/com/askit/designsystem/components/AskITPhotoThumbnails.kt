package com.askit.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.askit.designsystem.R

/**
 * Wrapping grid of 88dp photo thumbnails for forms, each with a 48dp remove button. Descriptions
 * are per photo (1-based position) so each image and its remove action are announced distinctly.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AskITPhotoThumbnails(
    photos: List<String>,
    photoDescription: @Composable (position: Int) -> String,
    removeDescription: @Composable (position: Int) -> String,
    onRemovePhoto: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (photos.isEmpty()) return
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        photos.forEachIndexed { index, uri ->
            Box(modifier = Modifier.size(88.dp)) {
                AsyncImage(
                    model = uri,
                    contentDescription = photoDescription(index + 1),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                IconButton(
                    onClick = { onRemovePhoto(uri) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = CircleShape,
                        ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = removeDescription(index + 1),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
