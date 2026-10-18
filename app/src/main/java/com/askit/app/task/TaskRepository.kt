package com.askit.app.task

import com.askit.app.posttask.PostTaskDraft
import com.askit.app.posttask.isValid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

interface TaskRepository {
    val tasks: StateFlow<List<Task>>
    val discussions: StateFlow<Map<String, List<TaskComment>>>
    fun getTask(id: String): Task?
    val likes: StateFlow<Map<String, Set<String>>>
    val reports: StateFlow<Map<String, List<TaskReport>>>
    fun createTask(draft: PostTaskDraft, author: TaskCommentAuthor?): TaskMutationResult
    fun editableDraft(taskId: String, accountId: String?): PostTaskDraft?
    fun updateTask(taskId: String, accountId: String?, draft: PostTaskDraft): TaskMutationResult
    fun deleteTask(taskId: String, accountId: String?): TaskMutationResult
    fun toggleLike(taskId: String, accountId: String?): TaskMutationResult
    fun reportTask(taskId: String, accountId: String?, reason: TaskReportReason): TaskMutationResult
    fun addComment(taskId: String, author: TaskCommentAuthor?, text: String, replyToId: String? = null): TaskCommentResult
}

/** Local, process-scoped task store. It starts empty unless a debug/preview build passes fixtures. */
class InMemoryTaskRepository(
    initialTasks: List<Task> = emptyList(),
) : TaskRepository {

    private val _tasks = MutableStateFlow(initialTasks)
    override val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()
    private val _discussions = MutableStateFlow<Map<String, List<TaskComment>>>(emptyMap())
    override val discussions = _discussions.asStateFlow()

    private var nextId = 100

    override fun getTask(id: String): Task? {
        return _tasks.value.firstOrNull { it.id == id }
    }

    private val ownerDrafts = mutableMapOf<String, PostTaskDraft>()
    private val mutableLikes = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    override val likes = mutableLikes.asStateFlow()
    private val mutableReports = MutableStateFlow<Map<String, List<TaskReport>>>(emptyMap())
    override val reports = mutableReports.asStateFlow()

    @Synchronized
    override fun createTask(draft: PostTaskDraft, author: TaskCommentAuthor?): TaskMutationResult {
        if (author == null || author.userId.isBlank() || author.name.isBlank()) return reject(TaskActionFailure.SIGN_IN_REQUIRED)
        if (!draft.isValid) return reject(TaskActionFailure.INVALID_DRAFT)
        val taskId = "task-${nextId++}"
        val snapshot = draft.snapshot()
        val newTask = snapshot.toPublicTask(taskId, author.name).copy(posterId = author.userId, posterAvatarUrl = author.avatarUrl)
        ownerDrafts[taskId] = snapshot
        _tasks.update { listOf(newTask) + it }
        return TaskMutationResult.Success(newTask)
    }

    @Synchronized
    override fun editableDraft(taskId: String, accountId: String?): PostTaskDraft? =
        if (getTask(taskId).actionPolicy(accountId).isOwner) ownerDrafts[taskId]?.snapshot() else null

    @Synchronized
    override fun updateTask(taskId: String, accountId: String?, draft: PostTaskDraft): TaskMutationResult {
        val task = getTask(taskId) ?: return reject(TaskActionFailure.UNAVAILABLE)
        if (!task.actionPolicy(accountId).isOwner) return denied(accountId)
        if (ownerDrafts[taskId] == null) return reject(TaskActionFailure.EDIT_UNAVAILABLE)
        if (!draft.isValid) return reject(TaskActionFailure.INVALID_DRAFT)
        val snapshot = draft.snapshot()
        val updated = snapshot.toPublicTask(taskId, task.posterName).copy(
            posterId = task.posterId, posterAvatarUrl = task.posterAvatarUrl,
            createdAtMillis = task.createdAtMillis, postedTime = task.postedTime, status = task.status,
        )
        ownerDrafts[taskId] = snapshot
        _tasks.update { current -> current.map { if (it.id == taskId) updated else it } }
        return TaskMutationResult.Success(updated)
    }

    @Synchronized
    override fun deleteTask(taskId: String, accountId: String?): TaskMutationResult {
        val task = getTask(taskId) ?: return reject(TaskActionFailure.UNAVAILABLE)
        if (!task.actionPolicy(accountId).isOwner) return denied(accountId)
        ownerDrafts.remove(taskId)
        mutableLikes.update { it - taskId }
        mutableReports.update { it - taskId }
        _discussions.update { it - taskId }
        _tasks.update { current -> current.filterNot { it.id == taskId } }
        return TaskMutationResult.Success()
    }

    @Synchronized
    override fun toggleLike(taskId: String, accountId: String?): TaskMutationResult {
        val task = getTask(taskId) ?: return reject(TaskActionFailure.UNAVAILABLE)
        if (!task.actionPolicy(accountId).canLike) return denied(accountId)
        val userId = requireNotNull(accountId)
        mutableLikes.update { current ->
            val users = current[taskId].orEmpty()
            current + (taskId to if (userId in users) users - userId else users + userId)
        }
        return TaskMutationResult.Success()
    }

    @Synchronized
    override fun reportTask(taskId: String, accountId: String?, reason: TaskReportReason): TaskMutationResult {
        val task = getTask(taskId) ?: return reject(TaskActionFailure.UNAVAILABLE)
        if (!task.actionPolicy(accountId).canReport) return denied(accountId)
        val reports = mutableReports.value[taskId].orEmpty()
        if (reports.any { it.accountId == accountId }) return reject(TaskActionFailure.ALREADY_REPORTED)
        mutableReports.update { it + (taskId to (reports + TaskReport(requireNotNull(accountId), reason, System.currentTimeMillis()))) }
        return TaskMutationResult.Success()
    }

    private fun reject(reason: TaskActionFailure) = TaskMutationResult.Rejected(reason)
    private fun denied(accountId: String?) = reject(if (accountId.isNullOrBlank()) TaskActionFailure.SIGN_IN_REQUIRED else TaskActionFailure.NOT_ALLOWED)

    @Synchronized
    override fun addComment(taskId: String, author: TaskCommentAuthor?, text: String, replyToId: String?): TaskCommentResult {
        fun reject(reason: TaskCommentFailure) = TaskCommentResult.Rejected(reason)
        if (author == null || author.userId.isBlank() || author.name.isBlank()) return reject(TaskCommentFailure.SIGN_IN_REQUIRED)
        val body = text.trim()
        if (body.isEmpty()) return reject(TaskCommentFailure.EMPTY)
        if (getTask(taskId) == null) return reject(TaskCommentFailure.TASK_UNAVAILABLE)
        val entries = _discussions.value[taskId].orEmpty()
        val target = replyToId?.let { id -> entries.firstOrNull { it.id == id } }
        if (replyToId != null && target == null) return reject(TaskCommentFailure.REPLY_UNAVAILABLE)
        val rootId = target?.let { it.parentId ?: it.id }
        if (rootId != null && entries.none { it.id == rootId && it.parentId == null }) return reject(TaskCommentFailure.REPLY_UNAVAILABLE)
        val comment = TaskComment(
            id = UUID.randomUUID().toString(),
            taskId = taskId,
            author = author,
            text = body,
            createdAtMillis = System.currentTimeMillis(),
            parentId = rootId,
            replyToId = replyToId,
        )
        _discussions.update { it + (taskId to (entries + comment)) }
        return TaskCommentResult.Added(comment)
    }
}

// Keep mutable collection references out of the owner-only stored snapshot.
private fun PostTaskDraft.snapshot() = copy(photos = photos.toList(), preferences = preferences.copy(languages = preferences.languages.toSet()))
