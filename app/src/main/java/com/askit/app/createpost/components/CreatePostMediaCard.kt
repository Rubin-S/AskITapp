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

enum class BeforeAfterSlot {
    BEFORE,
    AFTER,
    REPLACE,
}

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
                    text = stringResource(R.string.create_post_media_title),
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
                            title = stringResource(R.string.create_post_media_photos),
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

            Text(
                stringResource(R.string.post_crop_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
private fun MediaSegment(title: String, iconRes: Int, isSelected: Boolean, onClick: () -> Unit, testTag: String) {
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
