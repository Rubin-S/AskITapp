package com.askit.designsystem.posts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.designsystem.R
import com.askit.designsystem.people.AskITAvatar

data class CommentItem(
    val id: String,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val timeAgo: String,
    val text: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val replyingToName: String? = null,
    val replies: List<CommentItem> = emptyList(),
)

val defaultSampleComments: List<CommentItem> = listOf(
    CommentItem(
        id = "c1",
        authorName = "mikakkad",
        authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
        timeAgo = "3h",
        text = "Very important topic to discuss with \uD83D\uDC4F\uD83D\uDC4F. After economy of our country will definitely improve .",
        likesCount = 2,
        isLiked = false,
    ),
    CommentItem(
        id = "c2",
        authorName = "13shaur13",
        authorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
        timeAgo = "6h",
        text = "I am on the side where I just want to know where were their voices when it really mattered\uD83E\uDD14",
        likesCount = 1,
        isLiked = false,
    ),
    CommentItem(
        id = "c3",
        authorName = "laxmi273013_",
        authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
        timeAgo = "7h",
        text = "Kangana always right \uD83D\uDD25\uD83D\uDD25",
        likesCount = 217,
        isLiked = false,
        replies = listOf(
            CommentItem(
                id = "c3-r1",
                authorName = "virat.mishra_17",
                authorAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
                timeAgo = "7h",
                text = "kaise",
                replyingToName = "laxmi273013_",
                likesCount = 2,
                isLiked = false,
            ),
            CommentItem(
                id = "c3-r2",
                authorName = "its_me_ankitchopra",
                authorAvatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150",
                timeAgo = "7h",
                text = "Wow how can u say that she is always right \uD83D\uDE02",
                replyingToName = "laxmi273013_",
                likesCount = 4,
                isLiked = false,
            ),
            CommentItem(
                id = "c3-r3",
                authorName = "_munt4zir_",
                authorAvatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150",
                timeAgo = "7h",
                text = "I agree \uD83D\uDCAF with u , when she said genZ's guttr generation , she was definitely thinking about u ...",
                replyingToName = "laxmi273013_",
                likesCount = 9,
                isLiked = false,
            ),
            CommentItem(
                id = "c3-r4",
                authorName = "madhan_nani321",
                authorAvatarUrl = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=150",
                timeAgo = "6h",
                text = "kyunki wo right wing ko support karti hai.. \uD83D\uDE02",
                replyingToName = "virat.mishra_17",
                likesCount = 1,
                isLiked = false,
            ),
        ),
    ),
    CommentItem(
        id = "c4",
        authorName = "vibekaleshh",
        authorAvatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150",
        timeAgo = "6h",
        text = "People have objections on women's clothes for traditions but no objection to the vulgar dance of people getting up like Gods? Where do these people go then?",
        likesCount = 15,
        isLiked = false,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    onDismiss: () -> Unit,
    initialComments: List<CommentItem> = defaultSampleComments,
    currentUserAvatarUrl: String? = null,
    onCommentAdded: ((parentId: String?, text: String) -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val comments = remember { mutableStateListOf(*initialComments.toTypedArray()) }
    var inputText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<Pair<String, String>?>(null) } // parentCommentId to authorName

    val emojis = listOf("❤️", "👏", "🔥", "😂", "😢", "😍", "😮", "🤣")

    fun submitComment() {
        val trimmed = inputText.trim()
        if (trimmed.isBlank()) return

        val currentReplyingTo = replyingTo
        if (currentReplyingTo != null) {
            val parentId = currentReplyingTo.first
            val targetName = currentReplyingTo.second
            val parentIndex = comments.indexOfFirst { it.id == parentId }
            if (parentIndex != -1) {
                val parent = comments[parentIndex]
                val cleanText = if (trimmed.startsWith("@$targetName ")) {
                    trimmed.removePrefix("@$targetName ")
                } else trimmed

                val newReply = CommentItem(
                    id = "r-${System.currentTimeMillis()}",
                    authorName = "You",
                    authorAvatarUrl = currentUserAvatarUrl,
                    timeAgo = "now",
                    text = cleanText,
                    replyingToName = targetName,
                    likesCount = 0,
                    isLiked = false,
                )
                comments[parentIndex] = parent.copy(replies = parent.replies + newReply)
            }
            onCommentAdded?.invoke(parentId, trimmed)
        } else {
            val newComment = CommentItem(
                id = "c-${System.currentTimeMillis()}",
                authorName = "You",
                authorAvatarUrl = currentUserAvatarUrl,
                timeAgo = "now",
                text = trimmed,
                likesCount = 0,
                isLiked = false,
            )
            comments.add(0, newComment)
            onCommentAdded?.invoke(null, trimmed)
        }

        inputText = ""
        replyingTo = null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("comments_bottom_sheet"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            // Header
            Text(
                text = stringResource(R.string.comments_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("comments_title"),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))

            // Comments List
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                items(comments, key = { it.id }) { comment ->
                    CommentRow(
                        comment = comment,
                        onLikeClick = {
                            val idx = comments.indexOfFirst { it.id == comment.id }
                            if (idx != -1) {
                                val current = comments[idx]
                                val newLiked = !current.isLiked
                                val newCount = if (newLiked) current.likesCount + 1 else maxOf(0, current.likesCount - 1)
                                comments[idx] = current.copy(isLiked = newLiked, likesCount = newCount)
                            }
                        },
                        onReplyClick = { targetAuthor ->
                            replyingTo = comment.id to targetAuthor
                            inputText = "@$targetAuthor "
                        },
                        onLikeReplyClick = { replyId ->
                            val parentIdx = comments.indexOfFirst { it.id == comment.id }
                            if (parentIdx != -1) {
                                val parent = comments[parentIdx]
                                val updatedReplies = parent.replies.map { r ->
                                    if (r.id == replyId) {
                                        val newLiked = !r.isLiked
                                        val newCount = if (newLiked) r.likesCount + 1 else maxOf(0, r.likesCount - 1)
                                        r.copy(isLiked = newLiked, likesCount = newCount)
                                    } else r
                                }
                                comments[parentIdx] = parent.copy(replies = updatedReplies)
                            }
                        },
                    )
                }

                item { Spacer(Modifier.height(8.dp)) }
            }

            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))

            // Replying Banner (if active)
            AnimatedVisibility(
                visible = replyingTo != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                replyingTo?.let { (_, targetUser) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.comments_replying_to, targetUser),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        IconButton(
                            onClick = {
                                replyingTo = null
                                if (inputText.startsWith("@$targetUser ")) {
                                    inputText = ""
                                }
                            },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = stringResource(R.string.comments_cancel_reply),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }

            // Quick emoji reaction bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                emojis.forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable {
                                inputText += emoji
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = emoji, fontSize = 22.sp)
                    }
                }
            }

            // Comment Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AskITAvatar(
                    avatarUrl = currentUserAvatarUrl,
                    avatarSize = 36.dp,
                    fallbackIconSize = 22.dp,
                )

                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.comments_input_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comments_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submitComment() }),
                    trailingIcon = {
                        if (inputText.isNotBlank()) {
                            IconButton(
                                onClick = { submitComment() },
                                modifier = Modifier.testTag("comments_send_button"),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_send_circle),
                                    contentDescription = stringResource(R.string.comments_send),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp),
                                )
                            }
                        }
                    },
                    singleLine = false,
                    maxLines = 4,
                )
            }
        }
    }
}

@Composable
private fun CommentRow(
    comment: CommentItem,
    onLikeClick: () -> Unit,
    onReplyClick: (authorName: String) -> Unit,
    onLikeReplyClick: (replyId: String) -> Unit,
) {
    var expandedReplies by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            AskITAvatar(
                avatarUrl = comment.authorAvatarUrl,
                avatarSize = 36.dp,
                fallbackIconSize = 22.dp,
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = comment.authorName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = comment.timeAgo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = comment.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.comments_reply),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable { onReplyClick(comment.authorName) }
                            .padding(vertical = 4.dp),
                    )
                }
            }

            // Comment Like
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = onLikeClick)
                    .padding(4.dp),
            ) {
                Icon(
                    painter = painterResource(
                        if (comment.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                    ),
                    contentDescription = stringResource(
                        if (comment.isLiked) R.string.comments_unlike else R.string.comments_like
                    ),
                    tint = if (comment.isLiked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                if (comment.likesCount > 0) {
                    Text(
                        text = comment.likesCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Nested Replies Section
        if (comment.replies.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))

            // Expand / Collapse replies button
            Row(
                modifier = Modifier
                    .padding(start = 48.dp)
                    .clickable { expandedReplies = !expandedReplies }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)),
                )
                Text(
                    text = if (expandedReplies) {
                        stringResource(R.string.comments_hide_replies)
                    } else {
                        stringResource(R.string.comments_view_replies, comment.replies.size)
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                visible = expandedReplies,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 48.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    comment.replies.forEach { reply ->
                        ReplyRow(
                            reply = reply,
                            onLikeClick = { onLikeReplyClick(reply.id) },
                            onReplyClick = { onReplyClick(reply.authorName) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplyRow(
    reply: CommentItem,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AskITAvatar(
            avatarUrl = reply.authorAvatarUrl,
            avatarSize = 28.dp,
            fallbackIconSize = 18.dp,
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = reply.authorName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = reply.timeAgo,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(2.dp))

            val replyText = buildAnnotatedString {
                if (!reply.replyingToName.isNullOrBlank()) {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)) {
                        append("@${reply.replyingToName} ")
                    }
                }
                append(reply.text)
            }

            Text(
                text = replyText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.comments_reply),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clickable(onClick = onReplyClick)
                    .padding(vertical = 2.dp),
            )
        }

        // Reply Like
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(onClick = onLikeClick)
                .padding(4.dp),
        ) {
            Icon(
                painter = painterResource(
                    if (reply.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                ),
                contentDescription = stringResource(
                    if (reply.isLiked) R.string.comments_unlike else R.string.comments_like
                ),
                tint = if (reply.isLiked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
            if (reply.likesCount > 0) {
                Text(
                    text = reply.likesCount.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
