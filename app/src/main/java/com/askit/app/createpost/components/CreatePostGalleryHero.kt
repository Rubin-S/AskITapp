package com.askit.app.createpost.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.app.createpost.CreatePostViewModel
import com.askit.app.createpost.PostMediaDraft
import com.askit.designsystem.R as DesignR

@Composable
fun CreatePostGalleryHero(
    photos: List<PostMediaDraft>,
    selectedIndex: Int,
    isDescriptionExpanded: Boolean,
    onPickGallery: () -> Unit,
    onPickReplace: () -> Unit,
    onSelect: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onDescriptionChange: (Int, String) -> Unit,
    onToggleDescription: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (photos.isEmpty()) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            ),
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onPickGallery)
                .testTag("create_post_add_photos"),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_photo),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Add photos of your project",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Tap to choose up to 10 photos from gallery",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    val selected = selectedIndex.coerceIn(0, photos.lastIndex)
    val selectedItem = photos[selected]

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (photos.size == 1) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    AsyncImage(
                        model = selectedItem.uri,
                        contentDescription = selectedItem.imageDescription.ifBlank {
                            stringResource(R.string.create_post_photo_content_description)
                        },
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp, max = 380.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(
                            onClick = onPickReplace,
                            modifier = Modifier.testTag("create_post_replace_photo"),
                        ) {
                            Text(stringResource(R.string.create_post_replace))
                        }
                        TextButton(
                            onClick = { onRemove(0) },
                            modifier = Modifier.testTag("create_post_remove_photo"),
                        ) {
                            Text(
                                text = stringResource(R.string.create_post_remove),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = pluralStringResource(
                        R.plurals.create_post_carousel_count,
                        photos.size,
                        photos.size,
                    ),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Photo ${selected + 1} of ${photos.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Carousel Thumbnails
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("create_post_carousel_thumbnails"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                photos.forEachIndexed { index, item ->
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                BorderStroke(
                                    width = if (index == selected) 2.dp else 1.dp,
                                    color = if (index == selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    },
                                ),
                                RoundedCornerShape(12.dp),
                            )
                            .clickable(
                                role = Role.Button,
                                onClickLabel = stringResource(R.string.create_post_select_image, index + 1),
                                onClick = { onSelect(index) },
                            )
                            .testTag("create_post_carousel_thumbnail_$index"),
                    ) {
                        AsyncImage(
                            model = item.uri,
                            contentDescription = stringResource(
                                R.string.create_post_image_description_number,
                                index + 1,
                            ),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            // Main Preview Image
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    AsyncImage(
                        model = selectedItem.uri,
                        contentDescription = stringResource(
                            R.string.create_post_image_description_number,
                            selected + 1,
                        ),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp, max = 380.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = { onMove(selected, -1) },
                                enabled = selected > 0,
                                modifier = Modifier.testTag("create_post_carousel_move_earlier"),
                            ) {
                                Text(stringResource(R.string.create_post_move_earlier))
                            }
                            TextButton(
                                onClick = { onMove(selected, 1) },
                                enabled = selected < photos.lastIndex,
                                modifier = Modifier.testTag("create_post_carousel_move_later"),
                            ) {
                                Text(stringResource(R.string.create_post_move_later))
                            }
                        }

                        TextButton(
                            onClick = { onRemove(selected) },
                            modifier = Modifier.testTag("create_post_carousel_remove"),
                        ) {
                            Text(
                                text = stringResource(R.string.create_post_remove),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }

        if (photos.size < CreatePostViewModel.MAX_POST_PHOTOS) {
            OutlinedButton(
                onClick = onPickGallery,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 42.dp)
                    .testTag("create_post_add_more_carousel_photos"),
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_photo),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.create_post_add_photos),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }

        TextButton(
            onClick = onToggleDescription,
            modifier = Modifier.testTag(
                if (photos.size > 1) "create_post_carousel_description_toggle" else "create_post_photo_description_toggle",
            ),
        ) {
            Text(
                text = if (isDescriptionExpanded) "Hide photo description" else "+ Add photo description (optional)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (isDescriptionExpanded) {
            OutlinedTextField(
                value = selectedItem.imageDescription,
                onValueChange = { onDescriptionChange(selected, it) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(
                        if (photos.size > 1) "create_post_carousel_description" else "create_post_photo_description",
                    ),
                label = { Text("Photo description") },
                minLines = 2,
            )
        }
    }
}
