package com.askit.designsystem.posts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
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

sealed interface PostFeedContent {
    data class Text(
        val body: String,
    ) : PostFeedContent

    data class Photo(
        val image: PostFeedMedia,
        val caption: String? = null,
    ) : PostFeedContent

    data class Carousel(
        val items: List<PostFeedMedia>,
        val caption: String? = null,
    ) : PostFeedContent

    data class BeforeAfter(
        val before: PostFeedMedia,
        val after: PostFeedMedia,
        val caption: String? = null,
        val beforeNote: String? = null,
        val afterNote: String? = null,
    ) : PostFeedContent

    data class Poll(
        val question: String,
        val options: List<String>,
        val closingSummary: String,
        val description: String? = null,
    ) : PostFeedContent
}

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
) {
    val displayName = author.displayName.trim().ifBlank {
        stringResource(R.string.post_feed_author_fallback)
    }
    val location = locationLabel?.trim()?.takeIf(String::isNotEmpty)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("post_feed_item"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PostAuthorHeader(
            author = author.copy(displayName = displayName),
            locationLabel = location,
        )
        when (content) {
            is PostFeedContent.Text -> TextPostBody(content.body)
            is PostFeedContent.Photo -> {
                PostImageMedia(
                    media = content.image,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                )
                PostCaption(
                    authorName = displayName,
                    caption = content.caption,
                )
            }
            is PostFeedContent.Carousel -> {
                CarouselPostBody(
                    items = content.items,
                    caption = content.caption,
                )
                PostCaption(
                    authorName = displayName,
                    caption = content.caption,
                    showOnlyIfNotRendered = true,
                )
            }
            is PostFeedContent.BeforeAfter -> {
                BeforeAfterPostBody(content)
                PostCaption(
                    authorName = displayName,
                    caption = content.caption,
                    showOnlyIfNotRendered = true,
                )
            }
            is PostFeedContent.Poll -> PollPostBody(content)
        }
        Spacer(Modifier.size(4.dp))
    }
}

@Composable
private fun PostAuthorHeader(
    author: PostFeedAuthor,
    locationLabel: String?,
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
            Text(
                text = author.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (locationLabel != null) {
                Text(
                    text = locationLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TextPostBody(body: String) {
    var expanded by rememberSaveable(body) { mutableStateOf(false) }
    var hasOverflow by remember(body) { mutableStateOf(false) }
    val visibleBody = body.trim()
    if (visibleBody.isBlank()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = visibleBody,
            style = MaterialTheme.typography.bodyLarge,
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
            .clip(MaterialTheme.shapes.medium)
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
    caption: String?,
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
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
        if (!caption.isNullOrBlank()) {
            Spacer(Modifier.size(1.dp))
        }
    }
}

@Composable
private fun BeforeAfterPostBody(content: PostFeedContent.BeforeAfter) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val sideBySide = maxWidth >= 480.dp
        if (sideBySide) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ComparisonMedia(
                    label = stringResource(R.string.post_feed_before),
                    media = content.before,
                    modifier = Modifier.weight(1f),
                )
                ComparisonMedia(
                    label = stringResource(R.string.post_feed_after),
                    media = content.after,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ComparisonMedia(
                    label = stringResource(R.string.post_feed_before),
                    media = content.before,
                )
                ComparisonMedia(
                    label = stringResource(R.string.post_feed_after),
                    media = content.after,
                )
            }
        }
    }
    val notes = listOf(
        stringResource(R.string.post_feed_before) to content.beforeNote,
        stringResource(R.string.post_feed_after) to content.afterNote,
    ).filter { (_, note) -> !note.isNullOrBlank() }
    if (notes.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            notes.forEach { (label, note) ->
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("$label: ") }
                        append(note.orEmpty())
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun ComparisonMedia(
    label: String,
    media: PostFeedMedia,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        PostImageMedia(
            media = media.copy(
                contentDescription = media.contentDescription?.let {
                    stringResource(R.string.post_feed_comparison_described, label, it)
                } ?: stringResource(R.string.post_feed_comparison, label),
            ),
        )
    }
}

@Composable
private fun PollPostBody(content: PostFeedContent.Poll) {
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
        content.description?.trim()?.takeIf(String::isNotEmpty)?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = content.closingSummary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PostCaption(
    authorName: String,
    caption: String?,
    showOnlyIfNotRendered: Boolean = false,
) {
    if (showOnlyIfNotRendered && caption.isNullOrBlank()) return
    val visibleCaption = caption?.trim()?.takeIf(String::isNotEmpty) ?: return
    var expanded by rememberSaveable(visibleCaption) { mutableStateOf(false) }
    var hasOverflow by remember(visibleCaption) { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(authorName) }
                append(" ")
                append(visibleCaption)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (expanded) Int.MAX_VALUE else 5,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { hasOverflow = it.hasVisualOverflow },
            modifier = Modifier.testTag("post_feed_caption"),
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
