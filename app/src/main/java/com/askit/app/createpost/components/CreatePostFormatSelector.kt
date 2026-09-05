package com.askit.app.createpost.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.askit.designsystem.R as DesignR

enum class PostCreationFormat {
    PHOTOS,
    BEFORE_AFTER,
    POLL,
}

@Composable
fun CreatePostFormatSelector(
    selectedFormat: PostCreationFormat,
    onSelectFormat: (PostCreationFormat) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FormatPill(
            title = "Photos",
            iconRes = DesignR.drawable.ic_photo,
            isSelected = selectedFormat == PostCreationFormat.PHOTOS,
            onClick = { onSelectFormat(PostCreationFormat.PHOTOS) },
            testTag = "create_post_format_photos",
            modifier = Modifier.weight(1f),
        )

        FormatPill(
            title = stringResource(R.string.create_post_before_after),
            iconRes = DesignR.drawable.ic_share_work,
            isSelected = selectedFormat == PostCreationFormat.BEFORE_AFTER,
            onClick = { onSelectFormat(PostCreationFormat.BEFORE_AFTER) },
            testTag = "create_post_before_after_chip",
            modifier = Modifier.weight(1.25f),
        )

        FormatPill(
            title = "Poll",
            iconRes = DesignR.drawable.ic_service,
            isSelected = selectedFormat == PostCreationFormat.POLL,
            onClick = { onSelectFormat(PostCreationFormat.POLL) },
            testTag = "create_post_add_poll",
            modifier = Modifier.weight(0.9f),
        )
    }
}

@Composable
private fun FormatPill(
    title: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        label = "pillBg",
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "pillContent",
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        },
        label = "pillBorder",
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
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
