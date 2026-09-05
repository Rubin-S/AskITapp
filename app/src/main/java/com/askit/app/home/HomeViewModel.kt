package com.askit.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.askit.app.home.data.FakeHomeRepository
import com.askit.app.home.data.HomeRepository
import com.askit.app.home.model.PostMedia
import com.askit.app.home.model.Story
import com.askit.app.story.StoryDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HomeRepository = FakeHomeRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            combine(
                repository.getStories(),
                repository.getFeed(page = 1),
            ) { stories, feed ->
                HomeUiState.Success(
                    stories = stories,
                    feedItems = feed,
                    currentPage = 1,
                    hasMore = feed.isNotEmpty(),
                    isRefreshing = false,
                    isLoadingMore = false,
                )
            }
                .catch { throwable ->
                    _uiState.value = HomeUiState.Error(throwable.message)
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun refresh() {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(isRefreshing = true)
        }
        viewModelScope.launch {
            try {
                val stories = repository.getStories().first()
                val feed = repository.getFeed(page = 1).first()
                _uiState.value = HomeUiState.Success(
                    stories = stories,
                    feedItems = feed,
                    currentPage = 1,
                    hasMore = feed.isNotEmpty(),
                    isRefreshing = false,
                    isLoadingMore = false,
                )
            } catch (e: Exception) {
                if (currentState !is HomeUiState.Success) {
                    _uiState.value = HomeUiState.Error(e.message)
                } else {
                    _uiState.value = currentState.copy(isRefreshing = false)
                }
            }
        }
    }

    fun loadMore() {
        val currentState = _uiState.value as? HomeUiState.Success ?: return
        if (!currentState.hasMore || currentState.isLoadingMore || currentState.isRefreshing) {
            return
        }

        _uiState.value = currentState.copy(isLoadingMore = true)
        val nextPage = currentState.currentPage + 1

        viewModelScope.launch {
            try {
                val nextItems = repository.getFeed(page = nextPage).first()
                if (nextItems.isEmpty()) {
                    _uiState.value = currentState.copy(
                        isLoadingMore = false,
                        hasMore = false,
                    )
                } else {
                    _uiState.value = currentState.copy(
                        feedItems = currentState.feedItems + nextItems,
                        currentPage = nextPage,
                        isLoadingMore = false,
                        hasMore = nextItems.isNotEmpty(),
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(isLoadingMore = false)
            }
        }
    }

    fun addStoryFromDraft(draft: StoryDraft) {
        val currentState = _uiState.value as? HomeUiState.Success ?: return
        val newStory = Story(
            id = "story-${System.currentTimeMillis()}",
            authorName = "You",
            authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
            mediaUrl = draft.mediaUri ?: draft.sharedPost?.media?.let { media ->
                when (media) {
                    is PostMedia.SinglePhoto -> media.url
                    is PostMedia.Carousel -> media.urls.firstOrNull() ?: ""
                    is PostMedia.BeforeAfter -> media.afterUrl
                }
            } ?: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800",
            caption = draft.caption.ifBlank { draft.sharedPost?.content },
            createdAtMillis = System.currentTimeMillis(),
            isSeen = false,
            sharedPost = draft.sharedPost,
        )

        val updatedStories = listOf(newStory) + currentState.stories.filter { it.authorName != "You" }
        _uiState.value = currentState.copy(stories = updatedStories)
        repository.addStory(newStory)
    }

    fun markStorySeen(storyId: String) {
        val currentState = _uiState.value as? HomeUiState.Success ?: return
        val updatedStories = currentState.stories.map { story ->
            if (story.id == storyId) story.copy(isSeen = true) else story
        }
        _uiState.value = currentState.copy(stories = updatedStories)
        repository.markStorySeen(storyId)
    }
}
