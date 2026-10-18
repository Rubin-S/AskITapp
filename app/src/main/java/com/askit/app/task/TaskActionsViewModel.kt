package com.askit.app.task

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.askit.app.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TaskActionDraft(
    val reportOpen: Boolean = false,
    val reason: TaskReportReason? = null,
    val deleteOpen: Boolean = false,
    val feedbackRes: Int? = null,
)

class TaskActionsViewModel(
    private val repository: TaskRepository,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {
    private var accountId: String? = savedStateHandle["task_actions_account"]
    private val mutableDrafts = MutableStateFlow(restore())
    val drafts = mutableDrafts.asStateFlow()

    fun isActiveAccount(id: String?) = accountId == id?.takeIf(String::isNotBlank)

    fun activateAccount(id: String?) {
        val next = id?.takeIf(String::isNotBlank)
        if (next == accountId) return
        accountId = next
        mutableDrafts.value = emptyMap()
        persist()
    }

    fun feedback(taskId: String, messageRes: Int) = update(taskId) { copy(feedbackRes = messageRes) }
    fun consumeFeedback(taskId: String) = update(taskId) { copy(feedbackRes = null) }
    fun clearTask(taskId: String) {
        mutableDrafts.value = mutableDrafts.value - taskId
        persist()
    }

    fun like(taskId: String, currentAccountId: String?) {
        if (!checkAccount(taskId, currentAccountId)) return
        showFailure(taskId, repository.toggleLike(taskId, currentAccountId))
    }

    fun openReport(taskId: String, currentAccountId: String?) {
        if (!checkAccount(taskId, currentAccountId)) return
        if (!repository.getTask(taskId).actionPolicy(currentAccountId).canReport) return
        if (repository.reports.value[taskId].orEmpty().any { it.accountId == currentAccountId }) {
            feedback(taskId, R.string.task_report_already_saved)
        } else update(taskId) { copy(reportOpen = true, reason = null) }
    }

    fun selectReason(taskId: String, reason: TaskReportReason) = update(taskId) { copy(reason = reason) }
    fun cancelReport(taskId: String) = update(taskId) { copy(reportOpen = false, reason = null) }

    fun report(taskId: String, currentAccountId: String?) {
        if (!checkAccount(taskId, currentAccountId)) return
        val draft = mutableDrafts.value[taskId] ?: return
        if (!draft.reportOpen) return
        val reason = draft.reason ?: return
        when (val result = repository.reportTask(taskId, currentAccountId, reason)) {
            is TaskMutationResult.Rejected -> feedback(taskId, result.reason.messageRes)
            is TaskMutationResult.Success -> update(taskId) {
                copy(reportOpen = false, reason = null, feedbackRes = R.string.task_report_saved_locally)
            }
        }
    }

    fun openDelete(taskId: String, currentAccountId: String?) {
        if (!checkAccount(taskId, currentAccountId)) return
        if (repository.getTask(taskId).actionPolicy(currentAccountId).isOwner) update(taskId) { copy(deleteOpen = true) }
    }

    fun cancelDelete(taskId: String) = update(taskId) { copy(deleteOpen = false) }

    fun delete(taskId: String, currentAccountId: String?): Boolean {
        if (!checkAccount(taskId, currentAccountId) || mutableDrafts.value[taskId]?.deleteOpen != true) return false
        val result = repository.deleteTask(taskId, currentAccountId)
        if (result is TaskMutationResult.Success) {
            clearTask(taskId)
            return true
        }
        showFailure(taskId, result)
        return false
    }

    private fun checkAccount(taskId: String, id: String?): Boolean {
        if (id.isNullOrBlank() || !isActiveAccount(id)) {
            feedback(taskId, R.string.task_action_sign_in)
            return false
        }
        return true
    }

    private fun showFailure(taskId: String, result: TaskMutationResult) {
        if (result is TaskMutationResult.Rejected) feedback(taskId, result.reason.messageRes)
    }

    private fun update(taskId: String, transform: TaskActionDraft.() -> TaskActionDraft) {
        mutableDrafts.value = mutableDrafts.value + (taskId to (mutableDrafts.value[taskId] ?: TaskActionDraft()).transform())
        persist()
    }

    private fun persist() {
        savedStateHandle["task_actions_account"] = accountId
        savedStateHandle["task_action_drafts"] = Bundle().apply {
            mutableDrafts.value.forEach { (id, draft) -> putBundle(id, Bundle().apply {
                putBoolean("report", draft.reportOpen)
                putString("reason", draft.reason?.name)
                putBoolean("delete", draft.deleteOpen)
            }) }
        }
    }

    private fun restore(): Map<String, TaskActionDraft> {
        val bundle = savedStateHandle.get<Bundle>("task_action_drafts") ?: return emptyMap()
        return bundle.keySet().mapNotNull { id -> bundle.getBundle(id)?.let {
            id to TaskActionDraft(
                reportOpen = it.getBoolean("report"),
                reason = TaskReportReason.entries.firstOrNull { reason -> reason.name == it.getString("reason") },
                deleteOpen = it.getBoolean("delete"),
            )
        } }.toMap()
    }
}
