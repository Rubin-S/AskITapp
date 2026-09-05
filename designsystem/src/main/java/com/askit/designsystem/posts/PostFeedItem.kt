package com.askit.designsystem.posts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.askit.designsystem.R
import com.askit.designsystem.people.AskITAvatar

data class PostFeedAuthor(
    val displayName: String,
    val avatarUrl: String? = null,
)

data class PostFeedMedia(
    val model: Any,
    val contentDescription: String? = null,
)

sealed interface PostFeedMediaContent {
    data class Photo(
        val image: PostFeedMedia,
    ) : PostFeedMediaContent

    data class Carousel(
        val items: List<PostFeedMedia>,
    ) : PostFeedMediaContent

    data class BeforeAfter(
        val before: PostFeedMedia,
        val after: PostFeedMedia,
    ) : PostFeedMediaContent
}

data class PostFeedPoll(
    val question: String,
    val options: List<String>,
    val closingSummary: String,
)

data class PostFeedContent(
    val media: PostFeedMediaContent? = null,
    val body: String? = null,
    val poll: PostFeedPoll? = null,
)

/**
 * The canonical flat social Post renderer used by creation Preview and future Home content.
 * It intentionally accepts presentation data rather than app-owned form state.
 */
@Composable
fun PostFeedItem(
    author: PostFeedAuthor,
    content: PostFeedContent,
    modifier: Modifier = Modifier,
    locationLabel: String? = null,
    timeAgoLabel: String? = null,
    likesCount: Int = 0,
    commentsCount: Int = 0,
    isLiked: Boolean = false,
    isSaved: Boolean = false,
    onLikeClick: (() -> Unit)? = null,
    onCommentClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onSaveClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
) {
    val displayName = author.displayName.trim().ifBlank {
        stringResource(R.string.post_feed_author_fallback)
    }
    val location = locationLabel?.trim()?.takeIf(String::isNotEmpty)

    var localLiked by rememberSaveable(isLiked) { mutableStateOf(isLiked) }
    var localLikesCount by rememberSaveable(likesCount) { mutableIntStateOf(likesCount) }
    var localSaved by rememberSaveable(isSaved) { mutableStateOf(isSaved) }
    var doubleTapTrigger by remember { mutableIntStateOf(0) }

    fun handleLikeToggle() {
        if (onLikeClick != null) {
            onLikeClick()
        } else {
            if (localLiked) {
                localLiked = false
                localLikesCount = maxOf(0, localLikesCount - 1)
            } else {
                localLiked = true
                localLikesCount += 1
            }
        }
    }

    fun handleDoubleTap() {
        doubleTapTrigger++
        if (!localLiked) {
            localLiked = true
            localLikesCount += 1
            onLikeClick?.invoke()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("post_feed_item"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PostAuthorHeader(
            author = author.copy(displayName = displayName),
            locationLabel = location,
            timeAgoLabel = timeAgoLabel,
            onMoreClick = onMoreClick,
        )
        if (content.media != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { handleDoubleTap() },
                        )
                    },
            ) {
                when (val media = content.media) {
                    is PostFeedMediaContent.Photo -> PostImageMedia(
                        media = media.image,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    is PostFeedMediaContent.Carousel -> CarouselPostBody(items = media.items)
                    is PostFeedMediaContent.BeforeAfter -> BeforeAfterSlider(
                        before = media.before,
                        after = media.after,
                    )
                }
                DoubleTapHeartBurst(
                    trigger = doubleTapTrigger,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
        PostActionBar(
            likesCount = localLikesCount,
            commentsCount = commentsCount,
            isLiked = localLiked,
            isSaved = localSaved,
            onLikeClick = { handleLikeToggle() },
            onCommentClick = onCommentClick,
            onShareClick = onShareClick,
            onSaveClick = {
                if (onSaveClick != null) onSaveClick() else localSaved = !localSaved
            },
        )
        content.body?.let { TextPostBody(body = it, authorName = displayName) }
        content.poll?.let { PollPostBody(it) }
        Spacer(Modifier.size(4.dp))
    }
}

@Composable
private fun DoubleTapHeartBurst(
    trigger: Int,
    modifier: Modifier = Modifier,
) {
    if (trigger == 0) return
    val scale = remember(trigger) { Animatable(0f) }
    val alpha = remember(trigger) { Animatable(1f) }

    LaunchedEffect(trigger) {
        scale.snapTo(0f)
        alpha.snapTo(1f)
        launch {
            scale.animateTo(
                targetValue = 1.3f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(120),
            )
            delay(200)
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(250),
            )
        }
    }

    if (alpha.value > 0f) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_heart_filled),
                contentDescription = null,
                tint = Color.White.copy(alpha = alpha.value * 0.95f),
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    }
                    .shadow(
                        elevation = 16.dp,
                        shape = CircleShape,
                        spotColor = Color.Black.copy(alpha = 0.6f),
                    ),
            )
        }
    }
}

@Composable
private fun PostAuthorHeader(
    author: PostFeedAuthor,
    locationLabel: String?,
    timeAgoLabel: String? = null,
    onMoreClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            AskITAvatar(
                avatarUrl = author.avatarUrl,
                avatarSize = 40.dp,
                fallbackIconSize = 26.dp,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = author.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (timeAgoLabel != null) {
                    Text(
                        text = "• $timeAgoLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (locationLabel != null) {
                Text(
                    text = locationLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(
            onClick = { onMoreClick?.invoke() },
            modifier = Modifier
                .size(36.dp)
                .testTag("post_feed_more_options"),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_more_horiz),
                contentDescription = stringResource(R.string.post_feed_more_options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun PostActionBar(
    likesCount: Int,
    commentsCount: Int,
    isLiked: Boolean,
    isSaved: Boolean,
    onLikeClick: (() -> Unit)? = null,
    onCommentClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onSaveClick: (() -> Unit)? = null,
) {
    var localLiked by rememberSaveable(isLiked) { mutableStateOf(isLiked) }
    var localLikesCount by rememberSaveable(likesCount) { mutableIntStateOf(likesCount) }
    var localSaved by rememberSaveable(isSaved) { mutableStateOf(isSaved) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .testTag("post_feed_action_bar"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Like button
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        if (onLikeClick != null) {
                            onLikeClick()
                        } else {
                            if (localLiked) {
                                localLiked = false
                                localLikesCount = maxOf(0, localLikesCount - 1)
                            } else {
                                localLiked = true
                                localLikesCount += 1
                            }
                        }
                    }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
                    .testTag("post_feed_like_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    painter = painterResource(
                        if (localLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                    ),
                    contentDescription = stringResource(
                        if (localLiked) R.string.post_feed_unlike else R.string.post_feed_like
                    ),
                    tint = if (localLiked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
                if (localLikesCount > 0) {
                    Text(
                        text = localLikesCount.toString(),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Comment button
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onCommentClick?.invoke() }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
                    .testTag("post_feed_comment_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_comment_bubble),
                    contentDescription = stringResource(R.string.post_feed_comment),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
                if (commentsCount > 0) {
                    Text(
                        text = commentsCount.toString(),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Share button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onShareClick?.invoke() }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
                    .testTag("post_feed_share_button"),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_paper_plane),
                    contentDescription = stringResource(R.string.post_feed_share),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        // Save button
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    if (onSaveClick != null) {
                        onSaveClick()
                    } else {
                        localSaved = !localSaved
                    }
                }
                .padding(vertical = 4.dp, horizontal = 2.dp)
                .testTag("post_feed_save_button"),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(
                    if (localSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline
                ),
                contentDescription = stringResource(
                    if (localSaved) R.string.post_feed_unsave else R.string.post_feed_save
                ),
                tint = if (localSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun TextPostBody(
    body: String,
    authorName: String? = null,
) {
    var expanded by rememberSaveable(body) { mutableStateOf(false) }
    var hasOverflow by remember(body) { mutableStateOf(false) }
    val visibleBody = body.trim()
    if (visibleBody.isBlank()) return

    val annotatedText = buildAnnotatedString {
        if (!authorName.isNullOrBlank()) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(authorName)
                append(" ")
            }
        }
        append(visibleBody)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = annotatedText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (expanded) Int.MAX_VALUE else 12,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { hasOverflow = it.hasVisualOverflow },
            modifier = Modifier.testTag("post_feed_text_body"),
        )
        if (hasOverflow && !expanded) {
            TextButton(
                onClick = { expanded = true },
                modifier = Modifier.heightIn(min = 48.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            ) {
                Text(stringResource(R.string.post_feed_more))
            }
        }
    }
}

@Composable
private fun PostImageMedia(
    media: PostFeedMedia,
    modifier: Modifier = Modifier,
    fallbackDescription: String = stringResource(R.string.post_feed_photo_content_description),
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(media.model)
            .crossfade(false)
            .build(),
    )
    val painterState by painter.state.collectAsState()
    val ratio = when (val state = painterState) {
        is AsyncImagePainter.State.Success -> {
            val width = state.result.image.width
            val height = state.result.image.height
            if (width > 0 && height > 0) width.toFloat() / height.toFloat() else 1f
        }
        else -> 1f
    }.coerceIn(0.65f, 1.85f)
    val imageDescription = media.contentDescription?.trim()?.takeIf(String::isNotEmpty)
        ?: fallbackDescription
    val unavailable = painterState is AsyncImagePainter.State.Error

    Box(
        modifier = modifier
            .aspectRatio(ratio)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .semantics { contentDescription = imageDescription },
        contentAlignment = Alignment.Center,
    ) {
        if (unavailable) {
            Text(
                text = stringResource(R.string.post_feed_photo_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun CarouselPostBody(
    items: List<PostFeedMedia>,
) {
    if (items.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { items.size })
    val page = pagerState.currentPage.coerceIn(items.indices)
    val counterLabel = stringResource(R.string.post_feed_carousel_counter, page + 1, items.size)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_feed_carousel_pager"),
            ) { index ->
                val item = items[index]
                val pageDescription = item.contentDescription?.trim()?.takeIf(String::isNotEmpty)
                    ?.let {
                        stringResource(
                            R.string.post_feed_carousel_page_described,
                            it,
                            index + 1,
                            items.size,
                        )
                    }
                    ?: stringResource(R.string.post_feed_carousel_page, index + 1, items.size)
                PostImageMedia(
                    media = item.copy(contentDescription = pageDescription),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.92f),
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clearAndSetSemantics { contentDescription = counterLabel },
            ) {
                Text(
                    text = counterLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics {},
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (index == page) 7.dp else 5.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == page) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                        ),
                )
            }
        }
    }
}

@Composable
private fun PollPostBody(content: PostFeedPoll) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = content.question.trim(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("post_feed_poll_question"),
        )
        content.options.map(String::trim).filter(String::isNotEmpty).forEach { option ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
        Text(
            text = content.closingSummary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
