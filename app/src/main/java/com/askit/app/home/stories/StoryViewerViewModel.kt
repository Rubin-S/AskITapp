package com.askit.app.home.stories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.askit.app.home.data.HomeRepository
import com.askit.app.home.model.isAvailable
import com.askit.app.home.model.Story
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoryViewerViewModel(
    private val repository: HomeRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val social = repository.storySocial
    private var accountId: String? = savedStateHandle["story_account"]
    private var entryId: String? = savedStateHandle["story_entry"]
    private var savedStoryId: String? = savedStateHandle["story_current"]
    private val mutableState = MutableStateFlow(StoryViewerUiState(
        entryStoryId = entryId,
        menuStoryId = savedStateHandle["story_menu"],
        reporting = savedStateHandle["story_reporting"] ?: false,
        reportReason = StoryReportReason.entries.firstOrNull { it.name == savedStateHandle.get<String>("story_reason") },
    ))
    val uiState = mutableState.asStateFlow()

    init {
        viewModelScope.launch { repository.getStories().collect(::reconcile) }
    }

    fun activateAccount(id: String?) {
        val next = id?.takeIf(String::isNotBlank)
        if (next != accountId) {
            accountId = next
            entryId = null
            savedStoryId = null
            setState(mutableState.value.copy(menuStoryId = null, reporting = false, reportReason = null,
                actionError = null, paused = false, entryStoryId = null, shouldClose = false))
        }
        social.activateAccount(next)
        savedStateHandle["story_account"] = next
    }

    fun startAt(storyId: String) {
        if (entryId == storyId && !mutableState.value.shouldClose) return
        entryId = storyId
        savedStoryId = storyId
        val state = mutableState.value
        if (!state.isLoading && state.groups.none { group -> group.stories.any { it.id == storyId } }) {
            social.notify(com.askit.app.R.string.story_shared_unavailable)
            setState(state.copy(entryStoryId = storyId, shouldClose = true))
            return
        }
        val selected = StoryViewerUiState.forStart(state.groups, storyId)
        setState(selected.copy(isLoading = state.isLoading, entryStoryId = storyId))
    }

    fun closeViewer() {
        entryId = null
        savedStoryId = null
        setState(mutableState.value.copy(menuStoryId = null, reporting = false, reportReason = null,
            actionError = null, shouldClose = false, paused = false, entryStoryId = null))
    }

    fun advanceToNext(): Boolean {
        val state = mutableState.value
        if (state.menuStoryId != null) return true
        val group = state.currentGroup ?: return false
        when {
            state.itemIndex < group.stories.lastIndex -> setState(state.copy(itemIndex = state.itemIndex + 1, paused = false))
            state.groupIndex < state.groups.lastIndex -> setState(state.copy(groupIndex = state.groupIndex + 1, itemIndex = 0, paused = false))
            else -> return false
        }
        return true
    }

    fun backToPrevious() {
        val state = mutableState.value
        if (state.menuStoryId != null) return
        when {
            state.itemIndex > 0 -> setState(state.copy(itemIndex = state.itemIndex - 1, paused = false))
            state.groupIndex > 0 -> setState(state.copy(groupIndex = state.groupIndex - 1,
                itemIndex = state.groups[state.groupIndex - 1].stories.lastIndex, paused = false))
        }
    }

    fun selectGroup(groupIndex: Int) {
        val state = mutableState.value
        if (state.menuStoryId != null || state.groups.isEmpty()) return
        val index = groupIndex.coerceIn(0, state.groups.lastIndex)
        if (index != state.groupIndex) setState(state.copy(groupIndex = index, itemIndex = 0, paused = false))
    }

    fun setPaused(paused: Boolean) { setState(mutableState.value.copy(paused = paused)) }
    fun toggleLike(currentAccountId: String?) {
        val story = mutableState.value.currentStory?.id?.let(repository::getStory)
        val result = if (story == null) StoryActionResult.Rejected(StoryActionFailure.UNAVAILABLE)
            else social.toggleLike(currentAccountId, story)
        if (result is StoryActionResult.Rejected) social.notify(result.reason.messageRes)
    }

    fun refreshAvailability() = reconcile(mutableState.value.groups.flatMap { it.stories })

    fun availableStory(id: String): Story? = repository.getStory(id)?.takeIf {
        mutableState.value.groups.any { group -> group.stories.any { story -> story.id == id } }
    }


    fun openActions() {
        val story = mutableState.value.currentStory ?: return
        if (story.authorId != null && story.authorId == accountId) return
        setState(mutableState.value.copy(menuStoryId = story.id, reporting = false, reportReason = null, actionError = null, paused = false))
    }
    fun closeActions() = setState(mutableState.value.copy(menuStoryId = null, reporting = false, reportReason = null, actionError = null))
    fun openReport() = setState(mutableState.value.copy(reporting = true, actionError = null))
    fun selectReportReason(reason: StoryReportReason) = setState(mutableState.value.copy(reportReason = reason, actionError = null))
    fun menuStory(): Story? = mutableState.value.menuStoryId?.let(repository::getStory)

    fun follow(currentAccountId: String?) {
        val story = mutableState.value.currentStory?.id?.let(repository::getStory) ?: return
        val result = social.setFollowing(currentAccountId, story.authorId, true)
        if (result is StoryActionResult.Rejected) social.notify(result.reason.messageRes)
    }

    fun mute(currentAccountId: String?) = submitAction(currentAccountId) { story -> social.mute(currentAccountId, story) }
    fun hide(currentAccountId: String?) = submitAction(currentAccountId) { story -> social.hideStory(currentAccountId, story) }
    fun report(currentAccountId: String?) {
        val state = mutableState.value
        if (!state.reporting) return
        val reason = state.reportReason ?: return
        submitAction(currentAccountId) { story -> social.report(currentAccountId, story, reason) }
    }

    private fun submitAction(currentAccountId: String?, action: (Story) -> StoryActionResult) {
        if (currentAccountId != accountId || !social.isCurrentAccount(currentAccountId)) {
            setState(mutableState.value.copy(actionError = StoryActionFailure.SIGN_IN))
            return
        }
        val story = menuStory()
        if (story == null) {
            setState(mutableState.value.copy(actionError = StoryActionFailure.UNAVAILABLE))
            return
        }
        when (val result = action(story)) {
            is StoryActionResult.Success -> closeActions()
            is StoryActionResult.Rejected -> setState(mutableState.value.copy(actionError = result.reason))
        }
    }

    private fun reconcile(stories: List<Story>) {
        val state = mutableState.value
        val available = stories.filter { it.isAvailable() }
        val groups = available.groupedByAuthor()
        val currentId = savedStoryId ?: state.currentStory?.id
        val visibleIds = available.map { it.id }.toSet()
        val oldOrder = state.groups.flatMap { it.stories }.map { it.id }
        val selectedId = when {
            currentId in visibleIds -> currentId
            currentId != null && currentId in oldOrder -> oldOrder.drop(oldOrder.indexOf(currentId) + 1).firstOrNull { it in visibleIds }
            currentId != null && entryId == currentId -> null
            else -> available.firstOrNull()?.id
        }
        val close = entryId != null && (stories.isEmpty() || selectedId == null)
        val selected = StoryViewerUiState.forStart(groups, selectedId)
        val menuStillVisible = state.menuStoryId?.let { it in visibleIds } == true
        setState(state.copy(isLoading = false, groups = groups, groupIndex = selected.groupIndex,
            itemIndex = selected.itemIndex, shouldClose = close,
            menuStoryId = state.menuStoryId.takeIf { menuStillVisible },
            reporting = state.reporting && menuStillVisible,
            reportReason = state.reportReason.takeIf { menuStillVisible }))
    }

    private fun setState(next: StoryViewerUiState) {
        mutableState.value = next
        // Do not replace a restored position with an empty initial/loading emission.
        if (!next.isLoading && next.currentStory != null && entryId != null) savedStoryId = next.currentStory?.id
        savedStateHandle["story_entry"] = entryId
        savedStateHandle["story_current"] = savedStoryId
        savedStateHandle["story_menu"] = next.menuStoryId
        savedStateHandle["story_reporting"] = next.reporting
        savedStateHandle["story_reason"] = next.reportReason?.name
    }
}
