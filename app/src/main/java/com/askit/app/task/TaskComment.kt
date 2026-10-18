package com.askit.app.task

data class TaskCommentAuthor(val userId: String, val name: String, val avatarUrl: String? = null)

data class TaskComment(
    val id: String,
    val taskId: String,
    val author: TaskCommentAuthor,
    val text: String,
    val createdAtMillis: Long,
    val parentId: String? = null,
    val replyToId: String? = null,
)

enum class TaskCommentFailure { SIGN_IN_REQUIRED, EMPTY, TASK_UNAVAILABLE, REPLY_UNAVAILABLE }

sealed interface TaskCommentResult {
    data class Added(val comment: TaskComment) : TaskCommentResult
    data class Rejected(val reason: TaskCommentFailure) : TaskCommentResult
}
