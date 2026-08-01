package com.askit.app.explore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ExploreUiState(
    val query: String = "",
)

class ExploreViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val queryState = savedStateHandle.getStateFlow(QUERY_KEY, "")

    val uiState: StateFlow<ExploreUiState> = queryState
        .map(::ExploreUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ExploreUiState(queryState.value),
        )

    fun onQueryChanged(query: String) {
        savedStateHandle[QUERY_KEY] = query
    }

    fun onQueryCleared() {
        savedStateHandle[QUERY_KEY] = ""
    }

    private companion object {
        const val QUERY_KEY = "explore_query"
    }
}
