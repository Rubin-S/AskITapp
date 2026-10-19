package com.askit.app.explore

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The selected sort per result tab, saved in [savedStateHandle] so it survives rotation and process
 * death. Options a tab cannot support are ignored. [reset] returns every tab to Best match.
 */
class ExploreSortSelection(private val savedStateHandle: SavedStateHandle) {
    /** The sort options each tab supports; tabs with fewer than two show no sort control. */
    val available: Map<ExploreResultScope, List<ExploreSortOption>> = exploreAvailableSortOptions()

    private val state = MutableStateFlow(
        ExploreResultScope.entries.mapNotNull { scope ->
            savedStateHandle.get<String>(key(scope))
                ?.let { name -> ExploreSortOption.entries.firstOrNull { it.name == name } }
                ?.takeIf { it in available[scope].orEmpty() }
                ?.let { scope to it }
        }.toMap(),
    )
    val selected: StateFlow<Map<ExploreResultScope, ExploreSortOption>> = state.asStateFlow()

    fun select(scope: ExploreResultScope, option: ExploreSortOption) {
        if (option !in available[scope].orEmpty()) return
        state.update { it + (scope to option) }
        savedStateHandle[key(scope)] = option.name
    }

    fun reset() {
        state.value = emptyMap()
        ExploreResultScope.entries.forEach { savedStateHandle.remove<String>(key(it)) }
    }

    private fun key(scope: ExploreResultScope): String = "explore_sort_${scope.name.lowercase()}"
}
