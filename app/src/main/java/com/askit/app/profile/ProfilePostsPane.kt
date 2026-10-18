package com.askit.app.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.app.home.model.FeedPost
import com.askit.app.home.model.PostMedia

/** Ordered by the shared post store; pinned posts appear first for owners and visitors. */
@Composable
fun ProfilePostsPane(posts: List<FeedPost>, onOpen: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().testTag("profile_posts"), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (posts.isEmpty()) Text(stringResource(R.string.profile_posts_empty), Modifier.padding(24.dp))
        posts.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { post ->
                    val title = post.content.ifBlank { post.poll?.question.orEmpty() }
                        .ifBlank { stringResource(R.string.post_detail_title) }
                    val pinned = stringResource(R.string.post_pinned)
                    Surface(
                        onClick = { onOpen(post.id) },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.weight(1f).aspectRatio(3f / 4f).testTag("profile_post_${post.id}")
                            .semantics { contentDescription = if (post.pinnedAtMillis != null) "$pinned. $title" else title },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                title,
                                Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                            )
                            val image = when (val media = post.media) {
                                is PostMedia.SinglePhoto -> media.url
                                is PostMedia.Carousel -> media.urls.firstOrNull()
                                is PostMedia.BeforeAfter -> media.beforeUrl
                                null -> null
                            }
                            if (image != null) {
                                AsyncImage(
                                    model = image,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            if (post.pinnedAtMillis != null) {
                                Text(
                                    pinned,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
                                        .background(Color.Black.copy(alpha = .8f), RoundedCornerShape(8.dp)).padding(6.dp),
                                )
                            }
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
