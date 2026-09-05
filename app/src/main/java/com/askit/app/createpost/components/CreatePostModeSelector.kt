package com.askit.app.createpost.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.app.R
import com.askit.app.createpost.PostMediaLayout
import com.askit.designsystem.R as DesignR

enum class PostCreationType {
    BEFORE_AFTER,
    PHOTOS,
    POLL,
}

@Composable
fun CreatePostModeSelector(
    selectedType: PostCreationType,
    onSelectType: (PostCreationType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ModeSegment(
                title = stringResource(R.string.create_post_before_after),
                iconRes = DesignR.drawable.ic_share_work,
                isSelected = selectedType == PostCreationType.BEFORE_AFTER,
                onClick = { onSelectType(PostCreationType.BEFORE_AFTER) },
                testTag = "create_post_before_after_chip",
                modifier = Modifier.weight(1.15f),
            )

            ModeSegment(
                title = "Photos",
                iconRes = DesignR.drawable.ic_photo,
                isSelected = selectedType == PostCreationType.PHOTOS,
                onClick = { onSelectType(PostCreationType.PHOTOS) },
                testTag = "create_post_mode_photos",
                modifier = Modifier.weight(1f),
            )

            ModeSegment(
                title = "Poll",
                iconRes = DesignR.drawable.ic_service,
                isSelected = selectedType == PostCreationType.POLL,
                onClick = { onSelectType(PostCreationType.POLL) },
                testTag = "create_post_mode_poll",
                modifier = Modifier.weight(0.9f),
            )
        }
    }
}

@Composable
private fun ModeSegment(
    title: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.surface
        } else {
            Color.Transparent
        },
        label = "segmentBg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "segmentContent",
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = contentColor,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp,
                ),
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}
