package com.askit.app.createpost.components

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.app.createpost.CreatePostViewModel
import com.askit.app.createpost.PostDisclosure
import com.askit.app.createpost.PostMediaDraft
import com.askit.app.createpost.PostMediaLayout
import com.askit.app.createpost.PostValidationField
import com.askit.designsystem.R as DesignR
import com.askit.designsystem.posts.BeforeAfterSlider
import com.askit.designsystem.posts.PostFeedMedia

@Composable
fun CreatePostMediaCard(
    photos: List<PostMediaDraft>,
    mediaLayout: PostMediaLayout,
    selectedCarouselIndex: Int,
    expandedDisclosures: Set<PostDisclosure>,
    validationErrors: Set<PostValidationField>,
    onToggleMediaLayout: (PostMediaLayout) -> Unit,
    onPickGallery: () -> Unit,
    onPickSlot: (BeforeAfterSlot) -> Unit,
    onSelectCarouselIndex: (Int) -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onClearSlot: (Int) -> Unit,
    onMovePhoto: (Int, Int) -> Unit,
    onDescriptionChange: (Int, String) -> Unit,
    onToggleDisclosure: (PostDisclosure) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isBeforeAfter = mediaLayout == PostMediaLayout.BEFORE_AFTER

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header with unified Segmented Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Visual Media",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // The Segmented Mode Switcher (shows rule without words: you pick Photos OR Before/After!)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.height(34.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MediaSegment(
                            title = "Photos",
                            iconRes = DesignR.drawable.ic_photo,
                            isSelected = !isBeforeAfter,
                            onClick = { onToggleMediaLayout(PostMediaLayout.GALLERY) },
                            testTag = "create_post_segment_photos",
                        )

                        MediaSegment(
                            title = stringResource(R.string.create_post_before_after),
                            iconRes = DesignR.drawable.ic_share_work,
                            isSelected = isBeforeAfter,
                            onClick = { onToggleMediaLayout(PostMediaLayout.BEFORE_AFTER) },
                            testTag = "create_post_before_after_chip",
                        )
                    }
                }
            }

            // Body: Adapt according to selected media layout
            if (isBeforeAfter) {
                BeforeAfterContent(
                    photos = photos,
                    validationErrors = validationErrors,
                    beforeDescriptionExpanded = PostDisclosure.BEFORE_DESCRIPTION in expandedDisclosures,
                    afterDescriptionExpanded = PostDisclosure.AFTER_DESCRIPTION in expandedDisclosures,
                    onPickSlot = onPickSlot,
                    onRemove = onClearSlot,
                    onDescriptionChange = onDescriptionChange,
                    onToggleBeforeDescription = { onToggleDisclosure(PostDisclosure.BEFORE_DESCRIPTION) },
                    onToggleAfterDescription = { onToggleDisclosure(PostDisclosure.AFTER_DESCRIPTION) },
                )
            } else {
                GalleryContent(
                    photos = photos,
                    selectedIndex = selectedCarouselIndex,
                    isDescriptionExpanded = PostDisclosure.CAROUSEL_DESCRIPTION in expandedDisclosures ||
                        PostDisclosure.PHOTO_DESCRIPTION in expandedDisclosures,
                    onPickGallery = onPickGallery,
                    onPickReplace = { onPickSlot(BeforeAfterSlot.REPLACE) },
                    onSelect = onSelectCarouselIndex,
                    onRemove = onRemovePhoto,
                    onMove = onMovePhoto,
                    onDescriptionChange = onDescriptionChange,
                    onToggleDescription = {
                        onToggleDisclosure(
                            if (photos.size > 1) {
                                PostDisclosure.CAROUSEL_DESCRIPTION
                            } else {
                                PostDisclosure.PHOTO_DESCRIPTION
                            },
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun MediaSegment(
    title: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        label = "mediaSegBg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "mediaSegContent",
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = contentColor,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
            ),
            color = contentColor,
        )
    }
}

@Composable
private fun BeforeAfterContent(
    photos: List<PostMediaDraft>,
    validationErrors: Set<PostValidationField>,
    beforeDescriptionExpanded: Boolean,
    afterDescriptionExpanded: Boolean,
    onPickSlot: (BeforeAfterSlot) -> Unit,
    onRemove: (Int) -> Unit,
    onDescriptionChange: (Int, String) -> Unit,
    onToggleBeforeDescription: () -> Unit,
    onToggleAfterDescription: () -> Unit,
) {
    val before = photos.getOrNull(0) ?: PostMediaDraft()
    val after = photos.getOrNull(1) ?: PostMediaDraft()
    val hasBefore = !before.uri.isNullOrBlank()
    val hasAfter = !after.uri.isNullOrBlank()

    if (hasBefore && hasAfter) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BeforeAfterSlider(
                before = PostFeedMedia(
                    model = before.uri,
                    contentDescription = before.imageDescription.trim().takeIf(String::isNotEmpty),
                ),
                after = PostFeedMedia(
                    model = after.uri,
                    contentDescription = after.imageDescription.trim().takeIf(String::isNotEmpty),
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = { onPickSlot(BeforeAfterSlot.BEFORE) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "Change Before",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
                FilledTonalButton(
                    onClick = { onPickSlot(BeforeAfterSlot.AFTER) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "Change After",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SlotCard(
                step = "1",
                label = "Before",
                hint = "Initial problem",
                photo = before,
                isError = PostValidationField.BEFORE_PHOTO in validationErrors,
                errorMessage = R.string.create_post_error_before,
                accentColor = Color(0xFFF59E0B),
                isNext = !hasBefore,
                onPick = { onPickSlot(BeforeAfterSlot.BEFORE) },
                onRemove = { onRemove(0) },
                testTag = "create_post_before_slot",
                modifier = Modifier.weight(1f),
            )

            SlotCard(
                step = "2",
                label = "After",
                hint = "Finished result",
                photo = after,
                isError = PostValidationField.AFTER_PHOTO in validationErrors,
                errorMessage = R.string.create_post_error_after,
                accentColor = MaterialTheme.colorScheme.primary,
                isNext = hasBefore && !hasAfter,
                onPick = { onPickSlot(BeforeAfterSlot.AFTER) },
                onRemove = { onRemove(1) },
                testTag = "create_post_after_slot",
                modifier = Modifier.weight(1f),
            )
        }
    }

    // Hidden or compact descriptions preserving contract tags
    BeforeSlotDisclosures(
        content = before,
        isExpanded = beforeDescriptionExpanded,
        onDescriptionChange = { onDescriptionChange(0, it) },
        onToggleDescription = onToggleBeforeDescription,
        testTag = "create_post_before_slot",
    )
    AfterSlotDisclosures(
        content = after,
        isExpanded = afterDescriptionExpanded,
        onDescriptionChange = { onDescriptionChange(1, it) },
        onToggleDescription = onToggleAfterDescription,
        testTag = "create_post_after_slot",
    )
}

@Composable
private fun SlotCard(
    step: String,
    label: String,
    hint: String,
    photo: PostMediaDraft,
    isError: Boolean,
    @StringRes errorMessage: Int,
    accentColor: Color,
    isNext: Boolean,
    onPick: () -> Unit,
    onRemove: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    val isFilled = !photo.uri.isNullOrBlank()
    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            isFilled -> accentColor.copy(alpha = 0.5f)
            isNext -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        },
        label = "slotBorderColor",
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (isNext || isFilled) 1.5.dp else 1.dp, borderColor),
        modifier = modifier
            .height(170.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onPick)
            .testTag(testTag),
    ) {
        if (isFilled) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = photo.uri,
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                ) {
                    Text(
                        text = "✓ ${label.uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = accentColor,
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f)),
                ) {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_close),
                        contentDescription = "Remove $label photo",
                        modifier = Modifier.size(14.dp),
                        tint = Color.White,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isNext) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(
                            if (label == "Before") DesignR.drawable.ic_camera else DesignR.drawable.ic_sparkle,
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isError) {
                            MaterialTheme.colorScheme.error
                        } else if (isNext) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            accentColor
                        },
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$step. $label",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isError) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(errorMessage),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        modifier = Modifier.semantics { error(label) },
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryContent(
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
) {
    if (photos.isEmpty()) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onPickGallery)
                .testTag("create_post_add_photos"),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_photo),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Add photos of your work",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Select up to 10 photos from gallery",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    val selected = selectedIndex.coerceIn(0, photos.lastIndex)
    val selectedItem = photos[selected]

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (photos.size == 1) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
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
                            .heightIn(min = 200.dp, max = 340.dp)
                            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
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
                        fontSize = 12.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${selected + 1} of ${photos.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

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
                            .size(70.dp)
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

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
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
                            .heightIn(min = 200.dp, max = 340.dp)
                            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
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
                    .heightIn(min = 40.dp)
                    .testTag("create_post_add_more_carousel_photos"),
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_photo),
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
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

@Composable
private fun BeforeSlotDisclosures(
    content: PostMediaDraft,
    isExpanded: Boolean,
    onDescriptionChange: (String) -> Unit,
    onToggleDescription: () -> Unit,
    testTag: String,
) {
    Column(
        modifier = Modifier.testTag("${testTag}_container"),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        TextButton(
            onClick = onToggleDescription,
            modifier = Modifier.testTag("${testTag}_description_toggle"),
        ) {
            Text(
                text = if (isExpanded) "Hide before description" else "+ Add before description (optional)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (isExpanded) {
            OutlinedTextField(
                value = content.imageDescription,
                onValueChange = onDescriptionChange,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Before work description") },
                minLines = 2,
            )
        }
    }
}

@Composable
private fun AfterSlotDisclosures(
    content: PostMediaDraft,
    isExpanded: Boolean,
    onDescriptionChange: (String) -> Unit,
    onToggleDescription: () -> Unit,
    testTag: String,
) {
    Column(
        modifier = Modifier.testTag("${testTag}_container"),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        TextButton(
            onClick = onToggleDescription,
            modifier = Modifier.testTag("${testTag}_description_toggle"),
        ) {
            Text(
                text = if (isExpanded) "Hide after description" else "+ Add after description (optional)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (isExpanded) {
            OutlinedTextField(
                value = content.imageDescription,
                onValueChange = onDescriptionChange,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                label = { Text("After work description") },
                minLines = 2,
            )
        }
    }
}
