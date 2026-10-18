package com.askit.app.task

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TaskDiscussionDraft(
    val text: String = "",
    val replyToId: String? = null,
    val isOpen: Boolean = false,
    val expandedThreadIds: Set<String> = emptySet(),
    val error: TaskCommentFailure? = null,
    val revealedCommentId: String? = null,
)

class TaskDiscussionViewModel(private val repository: TaskRepository, private val savedStateHandle: SavedStateHandle) : ViewModel() {
    val discussions = repository.discussions
    private var accountId: String? = savedStateHandle["discussion_account"]
    private val mutableDrafts = MutableStateFlow(restoreDrafts())
    val drafts = mutableDrafts.asStateFlow()

    fun isActiveAccount(userId: String?): Boolean = accountId == userId?.takeIf(String::isNotBlank)

    fun activateAccount(userId: String?) {
        val nextId = userId?.takeIf(String::isNotBlank)
        if (accountId == nextId) return
        accountId = nextId
        mutableDrafts.value = emptyMap()
        persist()
    }

    fun clearTask(taskId: String) {
        mutableDrafts.value = mutableDrafts.value - taskId
        persist()
    }

    fun open(taskId: String) = update(taskId) { copy(isOpen = true) }
    fun dismiss(taskId: String) = update(taskId) { copy(isOpen = false) }
    fun updateText(taskId: String, text: String) {
        if (accountId != null) update(taskId) { copy(text = text, error = null) }
    }

    fun reply(taskId: String, commentId: String) {
        if (accountId == null) return
        val target = repository.discussions.value[taskId].orEmpty().firstOrNull { it.id == commentId } ?: return
        update(taskId) {
            copy(replyToId = commentId, expandedThreadIds = expandedThreadIds + (target.parentId ?: target.id), error = null)
        }
    }

    fun cancelReply(taskId: String) = update(taskId) { copy(replyToId = null, error = null) }
    fun toggleThread(taskId: String, threadId: String) = update(taskId) {
        copy(expandedThreadIds = if (threadId in expandedThreadIds) expandedThreadIds - threadId else expandedThreadIds + threadId)
    }

    fun submit(taskId: String, author: TaskCommentAuthor?) {
        val draft = mutableDrafts.value[taskId] ?: return
        if (accountId == null || author?.userId != accountId) {
            update(taskId) { copy(error = TaskCommentFailure.SIGN_IN_REQUIRED) }
            return
        }
        // Both send controls reach this synchronous operation; a second tap sees the cleared draft.
        if (draft.text.isBlank()) return
        when (val result = repository.addComment(taskId, author, draft.text, draft.replyToId)) {
            is TaskCommentResult.Rejected -> update(taskId) { copy(error = result.reason) }

            is TaskCommentResult.Added -> update(taskId) {
                copy(
                    text = "",
                    replyToId = null,
                    error = null,
                    expandedThreadIds = result.comment.parentId?.let { expandedThreadIds + it } ?: expandedThreadIds,
                    revealedCommentId = result.comment.id,
                )
            }
        }
    }

    private fun update(taskId: String, transform: TaskDiscussionDraft.() -> TaskDiscussionDraft) {
        mutableDrafts.value = mutableDrafts.value + (taskId to (mutableDrafts.value[taskId] ?: TaskDiscussionDraft()).transform())
        persist()
    }

    private fun persist() {
        savedStateHandle["discussion_account"] = accountId
        savedStateHandle["discussion_drafts"] = Bundle().apply {
            mutableDrafts.value.forEach { (taskId, draft) ->
                putBundle(
                    taskId,
                    Bundle().apply {
                        putString("text", draft.text)
                        putString("reply", draft.replyToId)
                        putBoolean("open", draft.isOpen)
                        putStringArrayList("expanded", ArrayList(draft.expandedThreadIds))
                    },
                )
            }
        }
    }

    private fun restoreDrafts(): Map<String, TaskDiscussionDraft> {
        val bundle = savedStateHandle.get<Bundle>("discussion_drafts") ?: return emptyMap()
        return bundle.keySet().mapNotNull { taskId ->
            bundle.getBundle(taskId)?.let {
                taskId to TaskDiscussionDraft(
                    text = it.getString("text").orEmpty(),
                    replyToId = it.getString("reply"),
                    isOpen = it.getBoolean("open"),
                    expandedThreadIds = it.getStringArrayList("expanded").orEmpty().toSet(),
                )
            }
        }.toMap()
    }
}
