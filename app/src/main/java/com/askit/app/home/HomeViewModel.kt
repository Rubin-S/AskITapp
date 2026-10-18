package com.askit.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.askit.app.home.data.HomeRepository
import com.askit.app.home.model.FeedItem
import com.askit.app.home.model.Story
import com.askit.app.session.SessionProfile
import com.askit.app.story.StoryDraft
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {
    val posts = repository.posts

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val loadedPages = sortedMapOf<Int, List<FeedItem>>()
    private var homeJob: Job? = null
    private var loadMoreJob: Job? = null

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        observeHome(keepContent = false)
    }

    fun refresh() {
        if ((_uiState.value as? HomeUiState.Success)?.isRefreshing == true) return
        observeHome(keepContent = true)
    }

    private fun observeHome(keepContent: Boolean) {
        homeJob?.cancel()
        loadMoreJob?.cancel()
        val current = _uiState.value as? HomeUiState.Success
        _uiState.value = if (keepContent && current != null) {
            current.copy(isRefreshing = true, isLoadingMore = false)
        } else {
            HomeUiState.Loading
        }
        homeJob = viewModelScope.launch {
            var firstEmission = true
            combine(
                repository.getStories(),
                repository.getFeed(page = 1),
            ) { stories, feed -> stories to feed }
                .catch { throwable ->
                    val latest = _uiState.value as? HomeUiState.Success
                    _uiState.value = latest?.copy(isRefreshing = false, isLoadingMore = false)
                        ?: HomeUiState.Error(throwable.message)
                }
                .collect { (stories, feed) ->
                    if (firstEmission) loadedPages.clear()
                    loadedPages[1] = feed
                    val latest = _uiState.value as? HomeUiState.Success
                    _uiState.value = if (firstEmission || latest == null) {
                        HomeUiState.Success(
                            stories = stories,
                            feedItems = feed,
                            currentPage = 1,
                            hasMore = feed.isNotEmpty(),
                        )
                    } else {
                        latest.copy(
                            stories = stories,
                            feedItems = posts.project(loadedPages.values.flatten()),
                            hasMore = if (loadedPages.containsKey(2)) latest.hasMore else feed.isNotEmpty(),
                        )
                    }
                    firstEmission = false
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

        loadMoreJob = viewModelScope.launch {
            try {
                val nextItems = repository.getFeed(page = nextPage).first()
                val latest = _uiState.value as? HomeUiState.Success ?: return@launch
                loadedPages[nextPage] = nextItems
                _uiState.value = latest.copy(
                    feedItems = posts.project(loadedPages.values.flatten()),
                    currentPage = if (nextItems.isEmpty()) latest.currentPage else nextPage,
                    isLoadingMore = false,
                    hasMore = nextItems.isNotEmpty(),
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                val latest = _uiState.value as? HomeUiState.Success
                if (latest != null) _uiState.value = latest.copy(isLoadingMore = false)
            }
        }
    }

    fun addStoryFromDraft(draft: StoryDraft, author: SessionProfile, accountId: String?): Boolean {
        val newStory = Story(
            id = UUID.randomUUID().toString(),
            authorName = author.username,
            authorId = accountId,
            authorAvatarUrl = author.avatarUrl,
            content = draft,
            createdAtMillis = System.currentTimeMillis(),
            isOwn = true,
        )
        return repository.addStory(newStory, accountId) is com.askit.app.home.stories.StoryActionResult.Success
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
