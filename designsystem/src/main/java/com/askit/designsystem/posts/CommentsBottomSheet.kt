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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.designsystem.R
import com.askit.designsystem.inputs.AskITTextField
import com.askit.designsystem.people.AskITAvatar
import kotlinx.coroutines.delay

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    onDismiss: () -> Unit,
    initialComments: List<CommentItem> = emptyList(),
    currentUserAvatarUrl: String? = null,
    onCommentAdded: ((parentId: String?, text: String) -> Unit)? = null,
    canComment: Boolean = true,
    disabledMessage: String? = null,
) {
    val comments = remember { mutableStateListOf(*initialComments.toTypedArray()) }
    var inputText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<Pair<String, String>?>(null) } // parentCommentId to authorName

    fun submitComment() {
        val trimmed = inputText.trim()
        if (!canComment || trimmed.isBlank()) return

        val currentReplyingTo = replyingTo
        if (currentReplyingTo != null) {
            val parentId = currentReplyingTo.first
            val targetName = currentReplyingTo.second
            val parentIndex = comments.indexOfFirst { it.id == parentId }
            if (parentIndex != -1) {
                val parent = comments[parentIndex]
                val cleanText = if (trimmed.startsWith("@$targetName ")) {
                    trimmed.removePrefix("@$targetName ")
                } else {
                    trimmed
                }

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

    var expandedThreadIds by remember { mutableStateOf(emptySet<String>()) }
    ControlledCommentsBottomSheet(
        onDismiss = onDismiss,
        comments = comments.toList(),
        currentUserAvatarUrl = currentUserAvatarUrl,
        inputText = inputText,
        onInputChange = { inputText = it },
        onSend = ::submitComment,
        canComment = canComment,
        disabledMessage = disabledMessage,
        replyingToName = replyingTo?.second,
        onCancelReply = {
            val targetName = replyingTo?.second
            replyingTo = null
            if (targetName != null && inputText.startsWith("@$targetName ")) inputText = ""
        },
        onReply = { parentId, targetId ->
            val parent = comments.first { it.id == parentId }
            val target = if (targetId == parentId) parent else parent.replies.first { it.id == targetId }
            replyingTo = parentId to target.authorName
            inputText = "@${target.authorName} "
        },
        expandedThreadIds = expandedThreadIds,
        onToggleThread = { id -> expandedThreadIds = if (id in expandedThreadIds) expandedThreadIds - id else expandedThreadIds + id },
        showEmojiBar = true,
        onLikeComment = { id ->
            val index = comments.indexOfFirst { it.id == id }
            if (index >= 0) {
                val current = comments[index]
                comments[index] = current.copy(
                    isLiked = !current.isLiked,
                    likesCount = (current.likesCount + if (current.isLiked) -1 else 1).coerceAtLeast(0),
                )
            }
        },
        onLikeReply = { id ->
            val index = comments.indexOfFirst { parent -> parent.replies.any { it.id == id } }
            if (index >= 0) {
                comments[index] = comments[index].copy(
                    replies = comments[index].replies.map {
                        if (it.id == id) {
                            it.copy(
                                isLiked = !it.isLiked,
                                likesCount = (it.likesCount + if (it.isLiked) -1 else 1).coerceAtLeast(0),
                            )
                        } else {
                            it
                        }
                    },
                )
            }
        },
    )
}

/** Presentation only. The caller owns comments, composer and reply state. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlledCommentsBottomSheet(
    onDismiss: () -> Unit,
    comments: List<CommentItem>,
    currentUserAvatarUrl: String? = null,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    replyingToName: String? = null,
    onCancelReply: () -> Unit,
    onReply: ((parentId: String, targetId: String) -> Unit)? = null,
    expandedThreadIds: Set<String>,
    onToggleThread: (String) -> Unit,
    title: String = stringResource(R.string.comments_title),
    subtitle: String? = null,
    inputPlaceholder: String = stringResource(R.string.comments_input_placeholder),
    emptyMessage: String? = null,
    errorMessage: String? = null,
    canComment: Boolean = true,
    disabledMessage: String? = null,
    showEmojiBar: Boolean = false,
    onLikeComment: ((String) -> Unit)? = null,
    onLikeReply: ((String) -> Unit)? = null,
    revealedCommentId: String? = null,
    skipPartiallyExpanded: Boolean = false,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)
    val listState = rememberLazyListState()
    val emojis = listOf("❤️", "👏", "🔥", "😂", "😢", "😍", "😮", "🤣")
    LaunchedEffect(revealedCommentId) {
        if (revealedCommentId != null) {
            val index = comments.indexOfFirst { it.id == revealedCommentId || it.replies.any { reply -> reply.id == revealedCommentId } }
            if (index >= 0) listState.scrollToItem(index + 1)
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
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
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("comments_title"),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            subtitle?.let {
                Text(
                    it,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))

            // Comments List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                if (comments.isEmpty() && emptyMessage != null) {
                    item {
                        Text(
                            emptyMessage,
                            modifier = Modifier.padding(vertical = 20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(comments, key = { it.id }) { comment ->
                    CommentRow(
                        comment = comment,
                        expandedReplies = comment.id in expandedThreadIds,
                        onToggleReplies = { onToggleThread(comment.id) },
                        onLikeClick = onLikeComment?.let { { it(comment.id) } },
                        onReplyClick = onReply?.takeIf { canComment }?.let { callback -> { target -> callback(comment.id, target.id) } },
                        onLikeReplyClick = onLikeReply,
                        revealedCommentId = revealedCommentId,
                    )
                }

                item { Spacer(Modifier.height(8.dp)) }
            }

            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))

            // Replying Banner (if active)
            AnimatedVisibility(
                visible = replyingToName != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                replyingToName?.let { targetUser ->
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
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        IconButton(
                            onClick = onCancelReply,
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

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            }
            if (!canComment && disabledMessage != null) {
                Text(disabledMessage, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (showEmojiBar && canComment) {
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
                                    onInputChange(inputText + emoji)
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }
            }
            if (canComment) {
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

                    AskITTextField(
                        value = inputText,
                        onValueChange = onInputChange,
                        placeholder = {
                            Text(
                                text = inputPlaceholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comments_input_field"),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { onSend() }),
                        trailingIcon = {
                            if (inputText.isNotBlank()) {
                                IconButton(
                                    onClick = { onSend() },
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
}

@Composable
private fun CommentRow(
    comment: CommentItem,
    expandedReplies: Boolean,
    onToggleReplies: () -> Unit,
    onLikeClick: (() -> Unit)?,
    onReplyClick: ((CommentItem) -> Unit)?,
    onLikeReplyClick: ((replyId: String) -> Unit)?,
    revealedCommentId: String?,
) {
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
                        modifier = Modifier.weight(1f, fill = false),
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

                if (onReplyClick != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.comments_reply),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clickable(role = Role.Button) { onReplyClick(comment) }
                                .padding(vertical = 4.dp),
                        )
                    }
                }
            }

            // Comment Like
            if (onLikeClick != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(role = Role.Button, onClick = onLikeClick)
                        .padding(4.dp),
                ) {
                    Icon(
                        painter = painterResource(
                            if (comment.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline,
                        ),
                        contentDescription = stringResource(
                            if (comment.isLiked) R.string.comments_unlike else R.string.comments_like,
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
        }

        // Nested Replies Section
        if (comment.replies.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))

            // Expand / Collapse replies button
            Row(
                modifier = Modifier
                    .padding(start = 48.dp)
                    .heightIn(min = 48.dp)
                    .clickable(role = Role.Button, onClick = onToggleReplies)
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
                        pluralStringResource(R.plurals.comments_reply_count, comment.replies.size, comment.replies.size)
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
                            onLikeClick = onLikeReplyClick?.let { { it(reply.id) } },
                            onReplyClick = onReplyClick?.let { { it(reply) } },
                            revealed = revealedCommentId == reply.id,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplyRow(reply: CommentItem, onLikeClick: (() -> Unit)?, onReplyClick: (() -> Unit)?, revealed: Boolean) {
    val requester = remember { BringIntoViewRequester() }
    LaunchedEffect(revealed) {
        if (revealed) {
            delay(350) // Wait for the reply expansion to finish before revealing the new row.
            requester.bringIntoView()
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().bringIntoViewRequester(requester),
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
                    modifier = Modifier.weight(1f, fill = false),
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

            if (onReplyClick != null) {
                Text(
                    text = stringResource(R.string.comments_reply),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(role = Role.Button, onClick = onReplyClick)
                        .padding(vertical = 2.dp),
                )
            }
        }

        // Reply Like
        if (onLikeClick != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable(role = Role.Button, onClick = onLikeClick)
                    .padding(4.dp),
            ) {
                Icon(
                    painter = painterResource(
                        if (reply.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline,
                    ),
                    contentDescription = stringResource(
                        if (reply.isLiked) R.string.comments_unlike else R.string.comments_like,
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
}
