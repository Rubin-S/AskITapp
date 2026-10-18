package com.askit.app.home.stories

import com.askit.app.R
import com.askit.app.home.model.Story
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class StoryAuthor(val id: String, val name: String, val avatarUrl: String?)
enum class StoryReportReason(val labelRes: Int) {
    SPAM(R.string.story_report_spam),
    INAPPROPRIATE(R.string.story_report_inappropriate),
    MISLEADING(R.string.story_report_misleading),
    OTHER(R.string.story_report_other),
}
data class StoryReport(val reason: StoryReportReason, val createdAtMillis: Long)
data class StorySocialState(
    val accountId: String? = null,
    val following: Set<String> = emptySet(),
    val muted: Map<String, StoryAuthor> = emptyMap(),
    val hiddenStoryIds: Set<String> = emptySet(),
    val reports: Map<String, StoryReport> = emptyMap(),
    val likedStoryIds: Set<String> = emptySet(),
)
enum class StoryActionFailure(val messageRes: Int) {
    SIGN_IN(R.string.story_action_sign_in),
    UNAVAILABLE(R.string.story_action_unavailable),
    NOT_ALLOWED(R.string.story_action_not_allowed),
    ALREADY_REPORTED(R.string.story_report_duplicate),
}
sealed interface StoryActionResult {
    data object Success : StoryActionResult
    data class Rejected(val reason: StoryActionFailure) : StoryActionResult
}
data class StoryUndo(val accountId: String, val targetId: String, val isMute: Boolean)
data class StoryFeedback(val messageRes: Int, val undo: StoryUndo? = null, val id: String = UUID.randomUUID().toString())

/** Local frontend state. Account scopes survive navigation, but not process death. */
class StorySocialStore(
    initialRelationships: Map<String, Set<String>> = emptyMap(),
    private val persistRelationships: (Map<String, Set<String>>) -> Unit = {},
) {
    private val accounts = initialRelationships.mapValues { (id, following) ->
        StorySocialState(accountId = id, following = following)
    }.toMutableMap()
    private val mutableRelationships = MutableStateFlow(initialRelationships)
    val relationships = mutableRelationships.asStateFlow()
    fun followingIds(id: String): Set<String> = accounts[id]?.following.orEmpty()
    fun followerIds(id: String): Set<String> = accounts.values.filter { id in it.following }.mapNotNull { it.accountId }.toSet()
    private val mutableState = MutableStateFlow(StorySocialState())
    val state = mutableState.asStateFlow()
    private val mutableFeedback = MutableStateFlow<StoryFeedback?>(null)
    val feedback = mutableFeedback.asStateFlow()

    fun activateAccount(accountId: String?) {
        val next = accountId?.takeIf(String::isNotBlank)
        if (next == mutableState.value.accountId) return
        mutableState.value = next?.let { accounts[it] ?: StorySocialState(accountId = it) } ?: StorySocialState()
        mutableFeedback.value = null
    }

    fun canAct(accountId: String?, authorId: String?): Boolean = !accountId.isNullOrBlank() && accountId == state.value.accountId &&
        !authorId.isNullOrBlank() && authorId != accountId

    fun setFollowing(accountId: String?, authorId: String?, following: Boolean): StoryActionResult {
        guard(accountId, authorId)?.let { return it }
        val id = requireNotNull(authorId)
        return runCatching {
            commit(state.value.copy(following = if (following) state.value.following + id else state.value.following - id))
            StoryActionResult.Success
        }.getOrElse { StoryActionResult.Rejected(StoryActionFailure.UNAVAILABLE) }
    }

    fun toggleLike(accountId: String?, story: Story): StoryActionResult {
        guard(accountId, story.authorId)?.let { return it }
        val likes = state.value.likedStoryIds
        commit(state.value.copy(likedStoryIds = if (story.id in likes) likes - story.id else likes + story.id))
        return StoryActionResult.Success
    }

    fun mute(accountId: String?, story: Story): StoryActionResult {
        guard(accountId, story.authorId)?.let { return it }
        val author = StoryAuthor(requireNotNull(story.authorId), story.authorName, story.authorAvatarUrl)
        commit(state.value.copy(muted = state.value.muted + (author.id to author)))
        notify(R.string.story_muted, StoryUndo(requireNotNull(accountId), author.id, isMute = true))
        return StoryActionResult.Success
    }

    fun unmute(accountId: String?, authorId: String): StoryActionResult {
        guard(accountId, authorId)?.let { return it }
        commit(state.value.copy(muted = state.value.muted - authorId))
        return StoryActionResult.Success
    }

    fun hideStory(accountId: String?, story: Story): StoryActionResult {
        guard(accountId, story.authorId)?.let { return it }
        commit(state.value.copy(hiddenStoryIds = state.value.hiddenStoryIds + story.id))
        notify(R.string.story_hidden, StoryUndo(requireNotNull(accountId), story.id, isMute = false))
        return StoryActionResult.Success
    }

    fun report(accountId: String?, story: Story, reason: StoryReportReason): StoryActionResult {
        guard(accountId, story.authorId)?.let { return it }
        if (story.id in state.value.reports) return StoryActionResult.Rejected(StoryActionFailure.ALREADY_REPORTED)
        commit(state.value.copy(reports = state.value.reports + (story.id to StoryReport(reason, System.currentTimeMillis()))))
        notify(R.string.story_report_saved)
        return StoryActionResult.Success
    }

    fun undo(accountId: String?, undo: StoryUndo): StoryActionResult {
        if (accountId.isNullOrBlank() || accountId != state.value.accountId || accountId != undo.accountId) {
            return StoryActionResult.Rejected(StoryActionFailure.SIGN_IN)
        }
        commit(
            if (undo.isMute) {
                state.value.copy(muted = state.value.muted - undo.targetId)
            } else {
                state.value.copy(hiddenStoryIds = state.value.hiddenStoryIds - undo.targetId)
            },
        )
        return StoryActionResult.Success
    }

    fun notify(messageRes: Int, undo: StoryUndo? = null) {
        mutableFeedback.value = StoryFeedback(messageRes, undo)
    }
    fun consumeFeedback(id: String) {
        if (mutableFeedback.value?.id == id) mutableFeedback.value = null
    }

    fun isCurrentAccount(accountId: String?) = !accountId.isNullOrBlank() && accountId == state.value.accountId

    private fun guard(accountId: String?, authorId: String?): StoryActionResult.Rejected? = when {
        !isCurrentAccount(accountId) -> StoryActionResult.Rejected(StoryActionFailure.SIGN_IN)
        authorId.isNullOrBlank() -> StoryActionResult.Rejected(StoryActionFailure.UNAVAILABLE)
        authorId == accountId -> StoryActionResult.Rejected(StoryActionFailure.NOT_ALLOWED)
        else -> null
    }

    private fun commit(next: StorySocialState) {
        val nextRelationships = (accounts + (requireNotNull(next.accountId) to next)).mapValues { it.value.following }
        if (nextRelationships != mutableRelationships.value) persistRelationships(nextRelationships)
        accounts[requireNotNull(next.accountId)] = next
        mutableRelationships.value = nextRelationships
        mutableState.value = next
    }
}
