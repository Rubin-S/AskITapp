package com.askit.app.task

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.askit.app.R
import com.askit.designsystem.posts.CommentItem
import com.askit.designsystem.posts.ControlledCommentsBottomSheet
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date

@Composable
fun TaskDiscussionSheet(
    task: Task,
    viewModel: TaskDiscussionViewModel,
    author: TaskCommentAuthor?,
    currentAuthor: () -> TaskCommentAuthor? = { author },
) {
    val drafts by viewModel.drafts.collectAsStateWithLifecycle()
    if (!viewModel.isActiveAccount(author?.userId)) return
    val draft = drafts[task.id] ?: return
    if (!draft.isOpen) return
    val discussions by viewModel.discussions.collectAsStateWithLifecycle()
    val entries = discussions[task.id].orEmpty()
    val context = LocalContext.current
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(60_000)
        }
    }
    fun item(comment: TaskComment, replies: List<CommentItem> = emptyList()) = CommentItem(
        id = comment.id,
        authorName = comment.author.name,
        authorAvatarUrl = comment.author.avatarUrl,
        text = comment.text,
        timeAgo = commentTime(context, comment.createdAtMillis, now),
        replyingToName = comment.replyToId?.let { id -> entries.firstOrNull { it.id == id }?.author?.name },
        replies = replies,
    )
    val comments = entries.filter { it.parentId == null }.map { root ->
        item(root, entries.filter { it.parentId == root.id }.map { item(it) })
    }
    val target = entries.firstOrNull { it.id == draft.replyToId }
    val error = draft.error ?: TaskCommentFailure.REPLY_UNAVAILABLE.takeIf { draft.replyToId != null && target == null }
    val onReply: ((String, String) -> Unit)? = if (author != null) {
        { _, targetId -> viewModel.reply(task.id, targetId) }
    } else null

    ControlledCommentsBottomSheet(
        onDismiss = { viewModel.dismiss(task.id) },
        comments = comments,
        currentUserAvatarUrl = author?.avatarUrl,
        inputText = draft.text,
        onInputChange = { viewModel.updateText(task.id, it) },
        onSend = { viewModel.submit(task.id, currentAuthor()) },
        replyingToName = draft.replyToId?.let { target?.author?.name ?: stringResource(R.string.task_discussion_missing_target) },
        onCancelReply = { viewModel.cancelReply(task.id) },
        onReply = onReply,
        expandedThreadIds = draft.expandedThreadIds,
        onToggleThread = { viewModel.toggleThread(task.id, it) },
        title = stringResource(R.string.task_discussion_title),
        subtitle = task.title,
        inputPlaceholder = stringResource(R.string.task_discussion_placeholder),
        emptyMessage = stringResource(R.string.task_discussion_empty),
        errorMessage = error?.let { stringResource(it.messageRes) },
        canComment = author != null,
        disabledMessage = stringResource(R.string.task_discussion_sign_in),
        revealedCommentId = draft.revealedCommentId,
        skipPartiallyExpanded = true,
    )
}

private val TaskCommentFailure.messageRes: Int
    get() = when (this) {
        TaskCommentFailure.SIGN_IN_REQUIRED -> R.string.task_discussion_sign_in
        TaskCommentFailure.EMPTY -> R.string.task_discussion_empty_message
        TaskCommentFailure.TASK_UNAVAILABLE -> R.string.task_discussion_task_unavailable
        TaskCommentFailure.REPLY_UNAVAILABLE -> R.string.task_discussion_reply_unavailable
    }

private fun commentTime(context: Context, createdAt: Long, now: Long): String {
    val minutes = ((now - createdAt).coerceAtLeast(0) / 60_000).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    return when {
        minutes == 0 -> context.getString(R.string.task_just_posted)
        minutes < 60 -> context.resources.getQuantityString(R.plurals.task_discussion_minutes, minutes, minutes)
        minutes < 1_440 -> (minutes / 60).let { context.resources.getQuantityString(R.plurals.task_discussion_hours, it, it) }
        minutes < 10_080 -> (minutes / 1_440).let { context.resources.getQuantityString(R.plurals.task_discussion_days, it, it) }
        else -> DateFormat.getDateInstance(DateFormat.MEDIUM, context.resources.configuration.locales[0]).format(Date(createdAt))
    }
}
