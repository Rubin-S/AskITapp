package com.askit.app.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.askit.app.home.model.FeedPost
import com.askit.app.home.model.PostMedia
import com.askit.designsystem.posts.PostFeedAuthor
import com.askit.designsystem.posts.PostFeedContent
import com.askit.designsystem.posts.PostFeedItem
import com.askit.designsystem.posts.PostFeedMedia
import com.askit.designsystem.posts.PostFeedMediaContent
import com.askit.designsystem.posts.PostFeedPoll

/**
 * Thin adapter composable bridging the Home domain [FeedPost] to the design system [PostFeedItem].
 */
@Composable
fun HomePostItem(
    post: FeedPost,
    modifier: Modifier = Modifier,
    onCommentClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onLikeClick: (() -> Unit)? = null,
    onSaveClick: (() -> Unit)? = null,
) {
    val author = PostFeedAuthor(
        displayName = post.authorName,
        avatarUrl = post.authorAvatarUrl,
    )

    val mediaContent: PostFeedMediaContent? = when (val media = post.media) {
        is PostMedia.SinglePhoto -> {
            PostFeedMediaContent.Photo(
                image = PostFeedMedia(
                    model = media.url,
                    contentDescription = media.contentDescription,
                ),
            )
        }
        is PostMedia.Carousel -> {
            PostFeedMediaContent.Carousel(
                items = media.urls.map { url -> PostFeedMedia(model = url) },
            )
        }
        is PostMedia.BeforeAfter -> {
            PostFeedMediaContent.BeforeAfter(
                before = PostFeedMedia(model = media.beforeUrl),
                after = PostFeedMedia(model = media.afterUrl),
            )
        }
        null -> null
    }

    val pollContent = post.poll?.let { poll ->
        PostFeedPoll(
            question = poll.question,
            options = poll.options,
            closingSummary = poll.closingSummary,
        )
    }

    val content = PostFeedContent(
        media = mediaContent,
        body = post.content.trim().takeIf(String::isNotEmpty),
        poll = pollContent,
    )

    val timeAgo = formatTimeAgo(post.createdAtMillis)

    PostFeedItem(
        author = author,
        locationLabel = post.locationLabel,
        timeAgoLabel = timeAgo,
        likesCount = post.likesCount,
        commentsCount = post.commentsCount,
        content = content,
        onCommentClick = onCommentClick,
        onShareClick = onShareClick,
        onLikeClick = onLikeClick,
        onSaveClick = onSaveClick,
        modifier = modifier,
    )
}

private fun formatTimeAgo(millis: Long): String? {
    if (millis <= 0) return null
    val diff = System.currentTimeMillis() - millis
    val minutes = diff / (60 * 1000)
    val hours = minutes / 60
    val days = hours / 24
    return when {
        days > 0 -> "${days}d"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "now"
    }
}
