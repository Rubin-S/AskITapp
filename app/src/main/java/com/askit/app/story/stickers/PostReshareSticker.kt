package com.askit.app.story.stickers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.app.home.model.FeedPost
import com.askit.app.home.model.PostMedia
import com.askit.app.story.StoryReshareCardStyle
import com.askit.designsystem.people.AskITAvatar

@Composable
fun PostReshareSticker(
    post: FeedPost,
    cardStyle: StoryReshareCardStyle,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showViewPostTooltip: Boolean = false,
    onViewPostClick: (() -> Unit)? = null,
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "sticker_scale",
    )

    val mediaUrl: String? = when (val media = post.media) {
        is PostMedia.SinglePhoto -> media.url
        is PostMedia.Carousel -> media.urls.firstOrNull()
        is PostMedia.BeforeAfter -> media.afterUrl
        null -> null
    }

    Box(
        modifier = modifier
            .width(300.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center,
    ) {
        if (cardStyle == StoryReshareCardStyle.FullCard) {
            FullCardView(
                post = post,
                mediaUrl = mediaUrl,
                onClick = onClick,
            )
        } else {
            MinimalCardView(
                post = post,
                mediaUrl = mediaUrl,
                onClick = onClick,
            )
        }

        // Instagram-style "View Post ›" Frosted Pill Tooltip
        AnimatedVisibility(
            visible = showViewPostTooltip,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            ViewPostTooltipPill(
                onViewPostClick = onViewPostClick,
            )
        }
    }
}

@Composable
private fun FullCardView(
    post: FeedPost,
    mediaUrl: String?,
    onClick: (() -> Unit)?,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .testTag("story_post_sticker_full"),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Avatar + username
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AskITAvatar(
                    avatarUrl = post.authorAvatarUrl,
                    avatarSize = 28.dp,
                    fallbackIconSize = 16.dp,
                )
                Text(
                    text = post.authorName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Media
            if (!mediaUrl.isNullOrBlank()) {
                AsyncImage(
                    model = mediaUrl,
                    contentDescription = post.content,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            }

            // Caption snippet below media
            if (post.content.isNotBlank()) {
                val captionText = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("${post.authorName} ")
                    }
                    append(post.content)
                }
                Text(
                    text = captionText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun MinimalCardView(
    post: FeedPost,
    mediaUrl: String?,
    onClick: (() -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .testTag("story_post_sticker_minimal"),
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (!mediaUrl.isNullOrBlank()) {
                AsyncImage(
                    model = mediaUrl,
                    contentDescription = post.content,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Handle below card: @authorName
        Text(
            text = "@${post.authorName}",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
            ),
            color = Color.White,
            modifier = Modifier
                .padding(start = 4.dp)
                .shadow(elevation = 4.dp),
        )
    }
}

@Composable
fun ViewPostTooltipPill(
    onViewPostClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xDD1E1E1E),
        shadowElevation = 12.dp,
        modifier = modifier
            .then(
                if (onViewPostClick != null) {
                    Modifier.clickable(onClick = onViewPostClick)
                } else Modifier
            )
            .testTag("story_view_post_pill"),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.story_view_post),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                ),
                color = Color.White,
            )
            Icon(
                painter = painterResource(com.askit.designsystem.R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
