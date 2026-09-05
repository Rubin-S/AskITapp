package com.askit.app.createpost.components

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.app.createpost.PostMediaDraft
import com.askit.app.createpost.PostValidationField
import com.askit.designsystem.R as DesignR
import com.askit.designsystem.posts.BeforeAfterSlider
import com.askit.designsystem.posts.PostFeedMedia

@Composable
fun CreatePostBeforeAfterHero(
    photos: List<PostMediaDraft>,
    validationErrors: Set<PostValidationField>,
    beforeDescriptionExpanded: Boolean,
    afterDescriptionExpanded: Boolean,
    onPickSlot: (BeforeAfterSlot) -> Unit,
    onRemove: (Int) -> Unit,
    onDescriptionChange: (Int, String) -> Unit,
    onToggleBeforeDescription: () -> Unit,
    onToggleAfterDescription: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val before = photos.getOrNull(0) ?: PostMediaDraft()
    val after = photos.getOrNull(1) ?: PostMediaDraft()
    val hasBefore = !before.uri.isNullOrBlank()
    val hasAfter = !after.uri.isNullOrBlank()

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
            // Header Row: Visual context
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_share_work),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Transformation Showcase",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Completion status pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (hasBefore && hasAfter) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                ) {
                    Text(
                        text = when {
                            hasBefore && hasAfter -> "✓ Ready to compare"
                            hasBefore -> "1/2 steps done"
                            else -> "0/2 steps done"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                        ),
                        color = if (hasBefore && hasAfter) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            // Interactive Live Slider when BOTH are filled
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
                // Dual Slot Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // BEFORE SLOT
                    GuidedSlotCard(
                        step = "1",
                        label = "Before",
                        hint = "Initial state",
                        photo = before,
                        isError = PostValidationField.BEFORE_PHOTO in validationErrors,
                        errorMessage = R.string.create_post_error_before,
                        accentColor = Color(0xFFF59E0B),
                        isNextStep = !hasBefore,
                        onPick = { onPickSlot(BeforeAfterSlot.BEFORE) },
                        onRemove = { onRemove(0) },
                        testTag = "create_post_before_slot",
                        modifier = Modifier.weight(1f),
                    )

                    // AFTER SLOT
                    GuidedSlotCard(
                        step = "2",
                        label = "After",
                        hint = "Finished result",
                        photo = after,
                        isError = PostValidationField.AFTER_PHOTO in validationErrors,
                        errorMessage = R.string.create_post_error_after,
                        accentColor = MaterialTheme.colorScheme.primary,
                        isNextStep = hasBefore && !hasAfter,
                        onPick = { onPickSlot(BeforeAfterSlot.AFTER) },
                        onRemove = { onRemove(1) },
                        testTag = "create_post_after_slot",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Description Disclosures preserving test tags
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
    }
}

@Composable
private fun GuidedSlotCard(
    step: String,
    label: String,
    hint: String,
    photo: PostMediaDraft,
    isError: Boolean,
    @StringRes errorMessage: Int,
    accentColor: Color,
    isNextStep: Boolean,
    onPick: () -> Unit,
    onRemove: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    val isFilled = !photo.uri.isNullOrBlank()

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            isFilled -> accentColor.copy(alpha = 0.6f)
            isNextStep -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        },
        label = "slotBorder",
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (isNextStep || isFilled) 1.5.dp else 1.dp, borderColor),
        modifier = modifier
            .height(180.dp)
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

                // Top Label Pill
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

                // Remove Button
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
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (isNextStep) {
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
                        modifier = Modifier.size(22.dp),
                        tint = if (isError) {
                            MaterialTheme.colorScheme.error
                        } else if (isNextStep) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            accentColor
                        },
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "$step. $label",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (isError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(errorMessage),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
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
        verticalArrangement = Arrangement.spacedBy(4.dp),
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
        verticalArrangement = Arrangement.spacedBy(4.dp),
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
