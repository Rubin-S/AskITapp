package com.askit.app.home.stories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.askit.app.home.data.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StoryViewerViewModel(
    private val repository: HomeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryViewerUiState())
    val uiState: StateFlow<StoryViewerUiState> = _uiState.asStateFlow()

    private var pendingStartStoryId: String? = null

    init {
        viewModelScope.launch {
            val groups = repository.getStories().first().groupedByAuthor()
            _uiState.value =
                StoryViewerUiState.forStart(groups = groups, startStoryId = pendingStartStoryId)
        }
    }

    /** Requests that the viewer opens on the story with [storyId] once loaded. */
    fun startAt(storyId: String) {
        pendingStartStoryId = storyId
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val groups = repository.getStories().first().groupedByAuthor()
            _uiState.value = StoryViewerUiState.forStart(groups = groups, startStoryId = storyId)
        }
    }

    /** Selects the story to open; safe to call before stories finish loading. */
    private fun jumpTo(storyId: String) {
        val state = _uiState.value
        state.groups.forEachIndexed { g, group ->
            val i = group.stories.indexOfFirst { it.id == storyId }
            if (i >= 0) {
                _uiState.value = state.copy(groupIndex = g, itemIndex = i)
                return
            }
        }
    }

    /**
     * Moves to the next item, crossing into the next author's group when the current one ends.
     * @return false when there is nothing left to show and the viewer should close.
     */
    fun advanceToNext(): Boolean {
        val state = _uiState.value
        val group = state.currentGroup ?: return false
        return when {
            state.itemIndex < group.stories.lastIndex -> {
                _uiState.value = state.copy(itemIndex = state.itemIndex + 1)
                true
            }
            state.groupIndex < state.groups.lastIndex -> {
                _uiState.value = state.copy(groupIndex = state.groupIndex + 1, itemIndex = 0)
                true
            }
            else -> false
        }
    }

    /** Moves back one item, crossing into the previous author's last item at group edges. */
    fun backToPrevious() {
        val state = _uiState.value
        when {
            state.itemIndex > 0 ->
                _uiState.value = state.copy(itemIndex = state.itemIndex - 1)
            state.groupIndex > 0 -> {
                val previousGroup = state.groups[state.groupIndex - 1]
                _uiState.value = state.copy(
                    groupIndex = state.groupIndex - 1,
                    itemIndex = previousGroup.stories.lastIndex,
                )
            }
        }
    }

    /** Called when the user swipes the pager to a different author. */
    fun selectGroup(groupIndex: Int) {
        val clamped = groupIndex.coerceIn(0, (_uiState.value.groups.lastIndex).coerceAtLeast(0))
        if (clamped != _uiState.value.groupIndex) {
            _uiState.value = _uiState.value.copy(groupIndex = clamped, itemIndex = 0)
        }
    }

    fun setPaused(paused: Boolean) {
        if (_uiState.value.paused != paused) {
            _uiState.value = _uiState.value.copy(paused = paused)
        }
    }

    fun toggleLike() {
        val state = _uiState.value
        val id = state.currentStory?.id ?: return
        val liked = state.likedStoryIds
        _uiState.value = state.copy(
            likedStoryIds = if (id in liked) liked - id else liked + id,
        )
    }
}
