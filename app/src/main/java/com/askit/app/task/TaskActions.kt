package com.askit.app.task

import com.askit.app.R

data class TaskActionPolicy(
    val signedIn: Boolean,
    val isOwner: Boolean,
    val canLike: Boolean,
    val canReport: Boolean,
    val showApply: Boolean,
    val canApply: Boolean,
)

fun Task?.actionPolicy(accountId: String?, hasServiceListing: Boolean = false, applied: Boolean = false): TaskActionPolicy {
    val signedIn = !accountId.isNullOrBlank()
    val owner = this != null && signedIn && !posterId.isNullOrBlank() && posterId == accountId
    val showApply = this != null && signedIn && !owner && hasServiceListing
    return TaskActionPolicy(
        signedIn = signedIn,
        isOwner = owner,
        canLike = this != null && signedIn,
        canReport = this != null && signedIn && !owner,
        showApply = showApply,
        canApply = showApply && this.status.equals("Open", ignoreCase = true) && !applied,
    )
}

enum class TaskReportReason(val labelRes: Int) {
    SPAM_SCAM(R.string.task_report_spam),
    INAPPROPRIATE(R.string.task_report_inappropriate),
    MISLEADING(R.string.task_report_misleading),
    OTHER(R.string.task_report_other),
}

data class TaskReport(val accountId: String, val reason: TaskReportReason, val createdAtMillis: Long)

enum class TaskActionFailure(val messageRes: Int) {
    SIGN_IN_REQUIRED(R.string.task_action_sign_in),
    UNAVAILABLE(R.string.task_action_unavailable),
    NOT_ALLOWED(R.string.task_action_not_allowed),
    INVALID_DRAFT(R.string.task_action_invalid_draft),
    EDIT_UNAVAILABLE(R.string.task_edit_unavailable),
    ALREADY_REPORTED(R.string.task_report_already_saved),
}

sealed interface TaskMutationResult {
    data class Success(val task: Task? = null) : TaskMutationResult
    data class Rejected(val reason: TaskActionFailure) : TaskMutationResult
}
