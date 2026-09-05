package com.askit.app.home

import com.askit.app.home.model.FeedItem
import com.askit.app.home.model.Story

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val stories: List<Story> = emptyList(),
        val feedItems: List<FeedItem> = emptyList(),
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val currentPage: Int = 1,
        val hasMore: Boolean = true,
    ) : HomeUiState

    data class Error(
        val message: String? = null,
    ) : HomeUiState
}
