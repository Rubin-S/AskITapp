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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
import com.askit.app.createpost.photoRatio
import com.askit.designsystem.inputs.AskITTextField
import com.askit.designsystem.posts.BeforeAfterSlider
import com.askit.designsystem.posts.PostFeedMedia
import com.askit.designsystem.theme.StoryScrimColor
import com.askit.designsystem.R as DesignR

@Composable
internal fun BeforeAfterContent(
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
                    aspectRatio = before.photoRatio?.value ?: 4f / 3f,
                    contentDescription = before.imageDescription.trim().takeIf(String::isNotEmpty),
                ),
                after = PostFeedMedia(
                    model = after.uri,
                    aspectRatio = after.photoRatio?.value ?: 4f / 3f,
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
                        text = stringResource(R.string.create_post_change_before),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
                FilledTonalButton(
                    onClick = { onPickSlot(BeforeAfterSlot.AFTER) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(R.string.create_post_change_after),
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
                step = 1,
                label = stringResource(DesignR.string.post_feed_before),
                hint = stringResource(R.string.create_post_initial_problem),
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
                step = 2,
                label = stringResource(DesignR.string.post_feed_after),
                hint = stringResource(R.string.create_post_finished_result),
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
    step: Int,
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
                // 48dp touch target; the 28dp dark circle is only the visual.
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .drawBehind { drawCircle(color = StoryScrimColor, radius = 14.dp.toPx()) },
                ) {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_close),
                        contentDescription = stringResource(R.string.create_post_remove_slot_photo, label),
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
                            if (step == 1) DesignR.drawable.ic_camera else DesignR.drawable.ic_sparkle,
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
                text = if (isExpanded) {
                    stringResource(
                        R.string.create_post_hide_before_description,
                    )
                } else {
                    stringResource(R.string.create_post_add_before_description)
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (isExpanded) {
            AskITTextField(
                value = content.imageDescription,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.create_post_before_description_label)) },
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
                text = if (isExpanded) {
                    stringResource(
                        R.string.create_post_hide_after_description,
                    )
                } else {
                    stringResource(R.string.create_post_add_after_description)
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (isExpanded) {
            AskITTextField(
                value = content.imageDescription,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.create_post_after_description_label)) },
                minLines = 2,
            )
        }
    }
}
