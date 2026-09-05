package com.askit.app.createpost.components

import androidx.annotation.StringRes
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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.app.createpost.CreatePostFormState
import com.askit.app.createpost.CreatePostViewModel
import com.askit.app.createpost.PostDisclosure
import com.askit.app.createpost.PostMediaDraft
import com.askit.app.createpost.PostMediaLayout
import com.askit.app.createpost.PostValidationField
import com.askit.designsystem.R as DesignR
import com.askit.designsystem.posts.BeforeAfterSlider
import com.askit.designsystem.posts.PostFeedMedia

enum class BeforeAfterSlot {
    BEFORE,
    AFTER,
    REPLACE,
}

@Composable
fun CreatePostMediaSection(
    state: CreatePostFormState,
    validationErrors: Set<PostValidationField>,
    bringIntoViewRequester: BringIntoViewRequester,
    onPickGallery: () -> Unit,
    onPickSlot: (BeforeAfterSlot) -> Unit,
    onToggleLayout: () -> Unit,
    onSelect: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onClearSlot: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onDescriptionChange: (Int, String) -> Unit,
    onToggleDisclosure: (PostDisclosure) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = state.mediaLayout == PostMediaLayout.BEFORE_AFTER,
                onClick = onToggleLayout,
                leadingIcon = {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_share_work),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.create_post_before_after),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier
                    .heightIn(min = 40.dp)
                    .testTag("create_post_before_after_chip"),
            )
        }

        if (state.mediaLayout == PostMediaLayout.BEFORE_AFTER) {
            BeforeAfterEditor(
                photos = state.photos,
                validationErrors = validationErrors,
                beforeDescriptionExpanded = PostDisclosure.BEFORE_DESCRIPTION in state.expandedDisclosures,
                afterDescriptionExpanded = PostDisclosure.AFTER_DESCRIPTION in state.expandedDisclosures,
                onPickSlot = onPickSlot,
                onRemove = onClearSlot,
                onDescriptionChange = onDescriptionChange,
                onToggleBeforeDescription = { onToggleDisclosure(PostDisclosure.BEFORE_DESCRIPTION) },
                onToggleAfterDescription = { onToggleDisclosure(PostDisclosure.AFTER_DESCRIPTION) },
            )
        } else {
            GalleryEditor(
                photos = state.photos,
                selectedIndex = state.selectedCarouselIndex,
                isDescriptionExpanded = PostDisclosure.CAROUSEL_DESCRIPTION in state.expandedDisclosures ||
                    PostDisclosure.PHOTO_DESCRIPTION in state.expandedDisclosures,
                onPickGallery = onPickGallery,
                onPickReplace = { onPickSlot(BeforeAfterSlot.REPLACE) },
                onSelect = onSelect,
                onRemove = onRemove,
                onMove = onMove,
                onDescriptionChange = onDescriptionChange,
                onToggleDescription = {
                    onToggleDisclosure(
                        if (state.photos.size > 1) {
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

@Composable
private fun GalleryEditor(
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
        EmptyMediaPrompt(
            title = R.string.create_post_photos_empty_title,
            buttonLabel = R.string.create_post_add_photos,
            onClick = onPickGallery,
            testTag = "create_post_add_photos",
        )
        return
    }

    val selected = selectedIndex.coerceIn(0, photos.lastIndex)
    val selectedItem = photos[selected]

    if (photos.size == 1) {
        ComposerImage(
            uri = selectedItem.uri,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp, max = 440.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentDescription = selectedItem.imageDescription.ifBlank {
                stringResource(R.string.create_post_photo_content_description)
            },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = onPickReplace,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("create_post_replace_photo"),
            ) {
                Text(stringResource(R.string.create_post_replace))
            }
            TextButton(
                onClick = { onRemove(0) },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("create_post_remove_photo"),
            ) {
                Text(
                    text = stringResource(R.string.create_post_remove),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    } else {
        Text(
            text = pluralStringResource(
                R.plurals.create_post_carousel_count,
                photos.size,
                photos.size,
            ),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )

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
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            BorderStroke(
                                width = if (index == selected) 2.dp else 1.dp,
                                color = if (index == selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
                    ComposerImage(
                        uri = item.uri,
                        contentDescription = stringResource(
                            R.string.create_post_image_description_number,
                            index + 1,
                        ),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        ComposerImage(
            uri = selectedItem.uri,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp, max = 440.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentDescription = stringResource(
                R.string.create_post_image_description_number,
                selected + 1,
            ),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(
                onClick = { onMove(selected, -1) },
                enabled = selected > 0,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("create_post_carousel_move_earlier"),
            ) {
                Text(stringResource(R.string.create_post_move_earlier))
            }
            TextButton(
                onClick = { onMove(selected, 1) },
                enabled = selected < photos.lastIndex,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("create_post_carousel_move_later"),
            ) {
                Text(stringResource(R.string.create_post_move_later))
            }
            TextButton(
                onClick = { onRemove(selected) },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("create_post_carousel_remove"),
            ) {
                Text(
                    text = stringResource(R.string.create_post_remove),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    if (photos.size < CreatePostViewModel.MAX_POST_PHOTOS) {
        OutlinedButton(
            onClick = onPickGallery,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .testTag("create_post_add_more_carousel_photos"),
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_photo),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.create_post_add_photos))
        }
    }

    DisclosureButton(
        expanded = isDescriptionExpanded,
        collapsedLabel = R.string.create_post_add_image_description,
        expandedLabel = R.string.create_post_hide_image_description,
        onClick = onToggleDescription,
        testTag = if (photos.size > 1) {
            "create_post_carousel_description_toggle"
        } else {
            "create_post_photo_description_toggle"
        },
    )

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
            label = { Text(stringResource(R.string.create_post_image_description_optional)) },
            supportingText = { Text(stringResource(R.string.create_post_image_description_helper)) },
            minLines = 2,
        )
    }
}

@Composable
private fun BeforeAfterEditor(
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

    if (!before.uri.isNullOrBlank() && !after.uri.isNullOrBlank()) {
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
        BeforeAfterSlotEditor(
            label = R.string.create_post_before,
            content = before,
            isError = false,
            isDescriptionExpanded = beforeDescriptionExpanded,
            onPick = { onPickSlot(BeforeAfterSlot.BEFORE) },
            onRemove = { onRemove(0) },
            onDescriptionChange = { onDescriptionChange(0, it) },
            onToggleDescription = onToggleBeforeDescription,
            showImage = false,
            testTag = "create_post_before_slot",
        )
        BeforeAfterSlotEditor(
            label = R.string.create_post_after,
            content = after,
            isError = false,
            isDescriptionExpanded = afterDescriptionExpanded,
            onPick = { onPickSlot(BeforeAfterSlot.AFTER) },
            onRemove = { onRemove(1) },
            onDescriptionChange = { onDescriptionChange(1, it) },
            onToggleDescription = onToggleAfterDescription,
            showImage = false,
            testTag = "create_post_after_slot",
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BeforeAfterSlotEditor(
            label = R.string.create_post_before,
            content = before,
            isError = PostValidationField.BEFORE_PHOTO in validationErrors,
            isDescriptionExpanded = beforeDescriptionExpanded,
            onPick = { onPickSlot(BeforeAfterSlot.BEFORE) },
            onRemove = { onRemove(0) },
            onDescriptionChange = { onDescriptionChange(0, it) },
            onToggleDescription = onToggleBeforeDescription,
            testTag = "create_post_before_slot",
        )
        BeforeAfterSlotEditor(
            label = R.string.create_post_after,
            content = after,
            isError = PostValidationField.AFTER_PHOTO in validationErrors,
            isDescriptionExpanded = afterDescriptionExpanded,
            onPick = { onPickSlot(BeforeAfterSlot.AFTER) },
            onRemove = { onRemove(1) },
            onDescriptionChange = { onDescriptionChange(1, it) },
            onToggleDescription = onToggleAfterDescription,
            testTag = "create_post_after_slot",
        )
    }
}

@Composable
private fun BeforeAfterSlotEditor(
    @StringRes label: Int,
    content: PostMediaDraft,
    isError: Boolean,
    isDescriptionExpanded: Boolean,
    onPick: () -> Unit,
    onRemove: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onToggleDescription: () -> Unit,
    modifier: Modifier = Modifier,
    showImage: Boolean = true,
    testTag: String,
) {
    Column(
        modifier = modifier.testTag(testTag),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.semantics { heading() },
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (content.uri.isNullOrBlank()) {
            OutlinedButton(
                onClick = onPick,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_photo),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.create_post_add_photo))
            }
            if (isError) {
                PostError(
                    if (label == R.string.create_post_before) {
                        R.string.create_post_error_before
                    } else {
                        R.string.create_post_error_after
                    },
                )
            }
        } else {
            if (showImage) {
                ComposerImage(
                    uri = content.uri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp, max = 440.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentDescription = content.imageDescription.ifBlank {
                        stringResource(
                            if (label == R.string.create_post_before) {
                                R.string.create_post_before_content_description
                            } else {
                                R.string.create_post_after_content_description
                            },
                        )
                    },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onPick, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.create_post_replace))
                }
                TextButton(onClick = onRemove, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(
                        text = stringResource(R.string.create_post_remove),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        DisclosureButton(
            expanded = isDescriptionExpanded,
            collapsedLabel = R.string.create_post_add_image_description,
            expandedLabel = R.string.create_post_hide_image_description,
            onClick = onToggleDescription,
            testTag = "${testTag}_description_toggle",
        )

        if (isDescriptionExpanded) {
            OutlinedTextField(
                value = content.imageDescription,
                onValueChange = onDescriptionChange,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.create_post_image_description_optional)) },
                minLines = 2,
            )
        }
    }
}

@Composable
private fun EmptyMediaPrompt(
    @StringRes title: Int,
    @StringRes buttonLabel: Int,
    onClick: () -> Unit,
    testTag: String,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_photo),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(buttonLabel),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

@Composable
private fun ComposerImage(
    uri: String?,
    modifier: Modifier = Modifier,
    contentDescription: String,
) {
    AsyncImage(
        model = uri,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    )
}

@Composable
private fun DisclosureButton(
    expanded: Boolean,
    @StringRes collapsedLabel: Int,
    @StringRes expandedLabel: Int,
    onClick: () -> Unit,
    testTag: String,
) {
    val labelRes = if (expanded) expandedLabel else collapsedLabel
    val label = stringResource(labelRes)
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .heightIn(min = 40.dp)
            .testTag(testTag)
            .semantics { stateDescription = label },
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PostError(@StringRes message: Int) {
    val text = stringResource(message)
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.semantics { error(text) },
    )
}
