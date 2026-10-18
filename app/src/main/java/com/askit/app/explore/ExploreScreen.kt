package com.askit.app.explore

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.askit.app.R
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun ExploreRoute(
    viewModel: ExploreViewModel,
    onSearchFiltersClick: () -> Unit,
    resultState: ExploreResultState = ExploreResultState.Loading,
    browseState: ExploreBrowseState = ExploreBrowseState(),
    onRetryResults: (() -> Unit)? = null,
    onRetryBrowseSection: ((ExploreBrowseSection) -> Unit)? = null,
    availableSortOptions: Map<ExploreResultScope, List<ExploreSortOption>> = emptyMap(),
    selectedSortOptions: Map<ExploreResultScope, ExploreSortOption> = emptyMap(),
    onSortChanged: ((ExploreResultScope, ExploreSortOption) -> Unit)? = null,
    onPersonClick: ((String) -> Unit)? = null,
    onTaskClick: ((String) -> Unit)? = null,
    availableFilterOptions: Map<ExploreResultScope, List<ExploreFilterOption>> = emptyMap(),
    appliedFilterOptions: Map<ExploreResultScope, Set<ExploreFilterOption>> = emptyMap(),
    onFiltersChanged: ((ExploreResultScope, Set<ExploreFilterOption>) -> Unit)? = null,
    onFilterScopeChanged: (ExploreResultScope) -> Unit = {},
    onSearchAreaClick: () -> Unit = onSearchFiltersClick,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val priceFilters by viewModel.priceFilters.collectAsStateWithLifecycle()

    ExploreScreen(
        priceFilters = priceFilters,
        onPriceFilterCleared = { viewModel.onPriceFilterChanged(it, ExplorePriceFilter()) },
        query = uiState.query,
        searchArea = uiState.searchArea,
        recentSearches = uiState.recentSearches,
        onQueryChanged = viewModel::onQueryChanged,
        onQueryCleared = viewModel::onQueryCleared,
        onQuerySubmitted = viewModel::submitQuery,
        onRecentSearchRemoved = viewModel::removeRecentSearch,
        onRecentSearchesCleared = viewModel::clearRecentSearches,
        onSearchFiltersClick = onSearchFiltersClick,
        onSearchAreaClick = onSearchAreaClick,
        people = (resultState as? ExploreResultState.Results)?.people.orEmpty(),
        tasks = (resultState as? ExploreResultState.Results)?.tasks.orEmpty(),
        resultState = resultState,
        browseState = browseState,
        onRetryResults = onRetryResults,
        onRetryBrowseSection = onRetryBrowseSection,
        availableSortOptions = availableSortOptions,
        selectedSortOptions = selectedSortOptions,
        onSortChanged = onSortChanged,
        availableFilterOptions = availableFilterOptions,
        appliedFilterOptions = appliedFilterOptions,
        onFiltersChanged = onFiltersChanged,
        onFilterScopeChanged = onFilterScopeChanged,
        onPersonClick = onPersonClick,
        onTaskClick = onTaskClick,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ExploreScreen(
    query: String,
    searchArea: ExploreSearchArea,
    recentSearches: List<String>,
    onQueryChanged: (String) -> Unit,
    onQueryCleared: () -> Unit,
    onQuerySubmitted: (String) -> Unit,
    onRecentSearchRemoved: (String) -> Unit,
    onRecentSearchesCleared: () -> Unit,
    onSearchFiltersClick: () -> Unit,
    people: List<ExplorePersonResult> = emptyList(),
    tasks: List<ExploreTaskResult> = emptyList(),
    resultState: ExploreResultState = ExploreResultState.Loading,
    browseState: ExploreBrowseState = ExploreBrowseState(),
    onRetryResults: (() -> Unit)? = null,
    onRetryBrowseSection: ((ExploreBrowseSection) -> Unit)? = null,
    availableSortOptions: Map<ExploreResultScope, List<ExploreSortOption>> = emptyMap(),
    selectedSortOptions: Map<ExploreResultScope, ExploreSortOption> = emptyMap(),
    onSortChanged: ((ExploreResultScope, ExploreSortOption) -> Unit)? = null,
    onPersonClick: ((String) -> Unit)? = null,
    onTaskClick: ((String) -> Unit)? = null,
    availableFilterOptions: Map<ExploreResultScope, List<ExploreFilterOption>> = emptyMap(),
    appliedFilterOptions: Map<ExploreResultScope, Set<ExploreFilterOption>> = emptyMap(),
    priceFilters: Map<ExploreResultScope, ExplorePriceFilter> = emptyMap(),
    onPriceFilterCleared: (ExploreResultScope) -> Unit = {},
    onFiltersChanged: ((ExploreResultScope, Set<ExploreFilterOption>) -> Unit)? = null,
    onFilterScopeChanged: (ExploreResultScope) -> Unit = {},
    onSearchAreaClick: () -> Unit = onSearchFiltersClick,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val scrollState = rememberExploreScrollState()
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()
    val coroutineScope = rememberCoroutineScope()
    val latestOnQueryChanged = rememberUpdatedState(onQueryChanged)
    val inputTransformation = remember {
        object : InputTransformation {
            override fun TextFieldBuffer.transformInput() {
                latestOnQueryChanged.value(toString())
            }
        }
    }
    val isSearchActive = searchBarState.currentValue == SearchBarValue.Expanded
    val normalizedQuery = normalizeExploreQuery(query)

    LaunchedEffect(query) {
        if (textFieldState.text.toString() != query) {
            textFieldState.setTextAndPlaceCursorAtEnd(query)
        }
    }
    var selectedScopeOrdinal by rememberSaveable(normalizedQuery) {
        mutableIntStateOf(ExploreResultScope.All.ordinal)
    }
    val selectedScope = ExploreResultScope.entries[selectedScopeOrdinal]
    val currentFilterKeys = exploreFilterKeys(availableFilterOptions, appliedFilterOptions, priceFilters)
    val currentSortKeys = exploreSortKeys(selectedSortOptions)
    val filterKey = currentFilterKeys.joinedScopeKey()
    val sortKey = currentSortKeys.joinedScopeKey()
    val requestKey = exploreRequestKey(normalizedQuery, searchArea)

    LaunchedEffect(requestKey, filterKey, sortKey, selectedScope, resultState) {
        scrollState.synchronize(
            currentRequestKey = requestKey,
            currentFilterKeys = currentFilterKeys,
            currentSortKeys = currentSortKeys,
        )
        val savedPosition = scrollState.position(selectedScope)
        val itemCount = listState.layoutInfo.totalItemsCount
        val safeIndex = if (itemCount == 0) {
            0
        } else {
            savedPosition.index.coerceAtMost(itemCount - 1)
        }
        listState.scrollToItem(
            index = safeIndex,
            scrollOffset = if (safeIndex == savedPosition.index) savedPosition.offset else 0,
        )
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            scrollState.record(selectedScope, index, offset)
        }
    }

    fun closeSearch() {
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
        coroutineScope.launch {
            searchBarState.animateToCollapsed()
            // Collapsing can transfer focus back to the compact field; dismiss after that transfer.
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }
    }

    fun openFilters() {
        closeSearch()
        onFilterScopeChanged(selectedScope)
        onSearchFiltersClick()
    }

    BackHandler(enabled = isSearchActive) {
        closeSearch()
    }

    val professionalsQuery = stringResource(R.string.explore_query_professionals)
    val tasksQuery = stringResource(R.string.explore_tasks)
    val personClick = onPersonClick
    val taskClick = onTaskClick

    val visible = remember(selectedScope, resultState, availableFilterOptions, appliedFilterOptions, priceFilters) {
        deriveExploreVisibleResults(
            selectedScope = selectedScope,
            resultState = resultState,
            availableFilterOptions = availableFilterOptions,
            appliedFilterOptions = appliedFilterOptions,
            priceFilters = priceFilters,
        )
    }
    val orderedAppliedFilters = visible.orderedAppliedFilters
    val priceFilter = visible.priceFilter

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .testTag("explore_results_list"),
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        item(key = "header:explore", contentType = "header") {
            ExploreHeader(
                query = query,
                searchArea = searchArea,
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                inputTransformation = inputTransformation,
                onQueryCleared = onQueryCleared,
                onQuerySubmitted = onQuerySubmitted,
                onSearchFiltersClick = ::openFilters,
                onSearchAreaClick = {
                    closeSearch()
                    onSearchAreaClick()
                },
                selectedScope = selectedScope,
                priceFilterActive = priceFilter.isActive,
                availableFilterOptions = availableFilterOptions,
                appliedFilterOptions = appliedFilterOptions,
                onCloseSearch = ::closeSearch,
                recentSearches = recentSearches,
                onRecentSearchSelected = { recentQuery ->
                    onQuerySubmitted(recentQuery)
                    closeSearch()
                },
                onRecentSearchRemoved = onRecentSearchRemoved,
                onRecentSearchesCleared = onRecentSearchesCleared,
                categories = EXPLORE_CATEGORIES.map { stringResource(it.labelRes) },
                onCategorySelected = { category ->
                    onQuerySubmitted(category)
                    closeSearch()
                },
            )
        }

        if (!isSearchActive && normalizedQuery.isEmpty() && orderedAppliedFilters.isNotEmpty()) {
            item(key = "browse:applied-filters") {
                AppliedFilterChips(
                    options = orderedAppliedFilters,
                    onFilterRemoved = { removed ->
                        onFiltersChanged?.invoke(selectedScope, orderedAppliedFilters.filterNot { it == removed }.toSet())
                    },
                )
            }
        }
        if (!isSearchActive && priceFilter.isActive) {
            item(key = "controls:price-filter") {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = ::openFilters, modifier = Modifier.weight(1f)) {
                        val low = priceFilter.minimum.ifBlank { "0" }
                        val high = priceFilter.maximum.ifBlank { stringResource(R.string.explore_price_no_maximum) }
                        val unit = if (selectedScope == ExploreResultScope.Tasks) stringResource(R.string.explore_price_fixed) else priceFilter.unit?.let { stringResource(it.labelRes) }
                            ?: stringResource(R.string.explore_price_any_unit)
                        Text(stringResource(R.string.explore_price_summary, low, high, unit))
                    }
                    TextButton(onClick = { onPriceFilterCleared(selectedScope) }) {
                        Text(stringResource(R.string.explore_price_clear))
                    }
                }
            }
        }
        if (!isSearchActive && normalizedQuery.isNotEmpty()) {
            addSubmittedSearchResults(
                selectedScope = selectedScope,
                normalizedResultState = visible.submittedResultState,
                orderedAppliedFilters = orderedAppliedFilters,
                hasVisibleResults = visible.hasVisibleSubmittedResults,
                onRetryResults = onRetryResults,
                onEditFilters = ::openFilters,
                availableSortOptions = availableSortOptions,
                selectedSortOptions = selectedSortOptions,
                onSortChanged = onSortChanged,
                onFiltersChanged = onFiltersChanged,
                onPersonClick = personClick,
                onTaskClick = taskClick,
                onScopeSelected = { selectedScopeOrdinal = it.ordinal },
            )
        } else if (!isSearchActive && normalizedQuery.isEmpty()) {
            item(key = "browse:categories", contentType = "browse") {
                BrowseServices(
                    status = browseState.services,
                    onCategorySelected = { category ->
                        onQuerySubmitted(category)
                        closeSearch()
                    },
                    onChangeArea = ::openFilters,
                    onRetry = onRetryBrowseSection,
                )
            }
            addBrowseResultSections(
                peopleFiltered = visible.peopleHiddenByFilters,
                tasksFiltered = visible.tasksHiddenByFilters,
                people = visible.filteredPeople,
                tasks = visible.filteredTasks,
                browseState = browseState,
                onPersonClick = personClick,
                onTaskClick = taskClick,
                onChangeArea = ::openFilters,
                onRetryBrowseSection = onRetryBrowseSection,
                onViewMorePeople = {
                    onQuerySubmitted(professionalsQuery)
                    selectedScopeOrdinal = ExploreResultScope.People.ordinal
                    closeSearch()
                },
                onViewMoreTasks = {
                    onQuerySubmitted(tasksQuery)
                    selectedScopeOrdinal = ExploreResultScope.Tasks.ordinal
                    closeSearch()
                },
            )
        }
    }
}
