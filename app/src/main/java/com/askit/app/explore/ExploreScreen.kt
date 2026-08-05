package com.askit.app.explore

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.InputChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.askit.app.R
import com.askit.designsystem.people.AskITAvatar
import com.askit.designsystem.people.PersonResultMetadata
import com.askit.designsystem.people.PersonResultItem
import com.askit.designsystem.tasks.TaskResultItem
import com.askit.designsystem.tasks.TaskResultStatus
import kotlinx.coroutines.flow.collect
import java.util.Locale

private val EXPLORE_CATEGORIES = listOf(
    ExploreCategory(R.string.explore_category_electrician, R.drawable.service_electrician),
    ExploreCategory(R.string.explore_category_plumber, R.drawable.service_plumber),
    ExploreCategory(R.string.explore_category_cleaning, R.drawable.service_cleaning),
    ExploreCategory(R.string.explore_category_ac_repair, R.drawable.service_ac_repair),
    ExploreCategory(R.string.explore_category_home_tutor, R.drawable.service_home_tutor),
    ExploreCategory(R.string.explore_category_appliance_repair, R.drawable.service_appliance_repair),
)

private data class ExploreCategory(
    @StringRes val labelRes: Int,
    @DrawableRes val artworkRes: Int,
)

data class ExplorePersonResult(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val primaryService: String?,
    val additionalServices: List<String>,
    val rating: Double?,
    val reviewCount: Int,
    val locationLabel: String,
    val priceLabel: String?,
    val statusLabel: String?,
    val matchReasons: Set<PersonMatchReason> = emptySet(),
)

enum class PersonMatchReason {
    Identity,
    Service,
}

enum class ExploreResultScope(@StringRes val labelRes: Int) {
    All(R.string.explore_all),
    People(R.string.explore_people),
    Services(R.string.explore_services),
    Tasks(R.string.explore_tasks),
}

enum class ExploreSortOption(@StringRes val labelRes: Int) {
    BestMatch(R.string.explore_sort_best_match),
    Nearest(R.string.explore_sort_nearest),
    RatingHighToLow(R.string.explore_sort_rating_high_to_low),
    Newest(R.string.explore_sort_newest),
    DueSoon(R.string.explore_sort_due_soon),
}

data class ExploreTaskResult(
    val id: String,
    val title: String,
    val category: String,
    val summary: String?,
    val budgetLabel: String,
    val locationLabel: String,
    val timingLabel: String,
    val posterName: String,
    val postedLabel: String,
    val status: TaskResultStatus,
)

internal data class ExplorePersonRenderRow(
    val result: ExplorePersonResult,
    val stableId: String,
    val uiKey: String,
)

internal data class ExploreTaskRenderRow(
    val result: ExploreTaskResult,
    val stableId: String,
    val uiKey: String,
)

private data class ExploreListPosition(
    val index: Int,
    val offset: Int,
)

private data class ExploreListPositionState(
    val index: MutableIntState,
    val offset: MutableIntState,
)

/**
 * Keeps one LazyColumn while remembering the last viewport for each logical result scope.
 * Only primitive viewport values and request signatures are saveable; result rows remain
 * caller-owned and are never placed in saved instance state.
 */
private class ExploreScrollState(
    private val positions: Map<ExploreResultScope, ExploreListPositionState>,
    private val requestKey: MutableState<String?>,
    private val filterKeys: Map<ExploreResultScope, MutableState<String?>>,
    private val sortKeys: Map<ExploreResultScope, MutableState<String?>>,
) {
    fun position(scope: ExploreResultScope): ExploreListPosition {
        val state = positions.getValue(scope)
        return ExploreListPosition(
            index = state.index.intValue,
            offset = state.offset.intValue,
        )
    }

    fun record(scope: ExploreResultScope, index: Int, offset: Int) {
        val state = positions.getValue(scope)
        state.index.intValue = index
        state.offset.intValue = offset
    }

    fun reset(scope: ExploreResultScope) {
        val state = positions.getValue(scope)
        state.index.intValue = 0
        state.offset.intValue = 0
    }

    fun resetAll() {
        ExploreResultScope.entries.forEach(::reset)
    }

    fun synchronize(
        currentRequestKey: String,
        currentFilterKeys: Map<ExploreResultScope, String>,
        currentSortKeys: Map<ExploreResultScope, String>,
    ) {
        val requestChanged = requestKey.value != currentRequestKey
        if (requestChanged) {
            resetAll()
        } else {
            ExploreResultScope.entries.forEach { scope ->
                if (filterKeys.getValue(scope).value != currentFilterKeys.getValue(scope)) {
                    reset(scope)
                }
                if (sortKeys.getValue(scope).value != currentSortKeys.getValue(scope)) {
                    reset(scope)
                }
            }
        }

        requestKey.value = currentRequestKey
        ExploreResultScope.entries.forEach { scope ->
            filterKeys.getValue(scope).value = currentFilterKeys.getValue(scope)
            sortKeys.getValue(scope).value = currentSortKeys.getValue(scope)
        }
    }
}

@Composable
private fun rememberExploreScrollState(): ExploreScrollState {
    val allIndex = rememberSaveable { mutableIntStateOf(0) }
    val allOffset = rememberSaveable { mutableIntStateOf(0) }
    val peopleIndex = rememberSaveable { mutableIntStateOf(0) }
    val peopleOffset = rememberSaveable { mutableIntStateOf(0) }
    val servicesIndex = rememberSaveable { mutableIntStateOf(0) }
    val servicesOffset = rememberSaveable { mutableIntStateOf(0) }
    val tasksIndex = rememberSaveable { mutableIntStateOf(0) }
    val tasksOffset = rememberSaveable { mutableIntStateOf(0) }

    val requestKey = rememberSaveable { mutableStateOf<String?>(null) }
    val allFilterKey = rememberSaveable { mutableStateOf<String?>(null) }
    val peopleFilterKey = rememberSaveable { mutableStateOf<String?>(null) }
    val servicesFilterKey = rememberSaveable { mutableStateOf<String?>(null) }
    val tasksFilterKey = rememberSaveable { mutableStateOf<String?>(null) }
    val allSortKey = rememberSaveable { mutableStateOf<String?>(null) }
    val peopleSortKey = rememberSaveable { mutableStateOf<String?>(null) }
    val servicesSortKey = rememberSaveable { mutableStateOf<String?>(null) }
    val tasksSortKey = rememberSaveable { mutableStateOf<String?>(null) }

    return remember {
        ExploreScrollState(
            positions = mapOf(
                ExploreResultScope.All to ExploreListPositionState(allIndex, allOffset),
                ExploreResultScope.People to ExploreListPositionState(peopleIndex, peopleOffset),
                ExploreResultScope.Services to ExploreListPositionState(
                    servicesIndex,
                    servicesOffset,
                ),
                ExploreResultScope.Tasks to ExploreListPositionState(tasksIndex, tasksOffset),
            ),
            requestKey = requestKey,
            filterKeys = mapOf(
                ExploreResultScope.All to allFilterKey,
                ExploreResultScope.People to peopleFilterKey,
                ExploreResultScope.Services to servicesFilterKey,
                ExploreResultScope.Tasks to tasksFilterKey,
            ),
            sortKeys = mapOf(
                ExploreResultScope.All to allSortKey,
                ExploreResultScope.People to peopleSortKey,
                ExploreResultScope.Services to servicesSortKey,
                ExploreResultScope.Tasks to tasksSortKey,
            ),
        )
    }
}

@Composable
fun ExploreRoute(
    viewModel: ExploreViewModel,
    onSearchFiltersClick: () -> Unit,
    resultState: ExploreResultState = ExploreResultState.Loading,
    onRetryResults: () -> Unit = {},
    availableSortOptions: Map<ExploreResultScope, List<ExploreSortOption>> = emptyMap(),
    selectedSortOptions: Map<ExploreResultScope, ExploreSortOption> = emptyMap(),
    onSortChanged: ((ExploreResultScope, ExploreSortOption) -> Unit)? = null,
    onPersonClick: ((String) -> Unit)? = null,
    onTaskClick: ((String) -> Unit)? = null,
    availableFilterOptions: Map<ExploreResultScope, List<ExploreFilterOption>> = emptyMap(),
    appliedFilterOptions: Map<ExploreResultScope, Set<ExploreFilterOption>> = emptyMap(),
    onFiltersChanged: ((ExploreResultScope, Set<ExploreFilterOption>) -> Unit)? = null,
    onFilterScopeChanged: (ExploreResultScope) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ExploreScreen(
        query = uiState.query,
        searchArea = uiState.searchArea,
        recentSearches = uiState.recentSearches,
        onQueryChanged = viewModel::onQueryChanged,
        onQueryCleared = viewModel::onQueryCleared,
        onQuerySubmitted = viewModel::submitQuery,
        onRecentSearchRemoved = viewModel::removeRecentSearch,
        onRecentSearchesCleared = viewModel::clearRecentSearches,
        onSearchFiltersClick = onSearchFiltersClick,
        resultState = resultState,
        onRetryResults = onRetryResults,
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
    onRetryResults: () -> Unit = {},
    availableSortOptions: Map<ExploreResultScope, List<ExploreSortOption>> = emptyMap(),
    selectedSortOptions: Map<ExploreResultScope, ExploreSortOption> = emptyMap(),
    onSortChanged: ((ExploreResultScope, ExploreSortOption) -> Unit)? = null,
    onPersonClick: ((String) -> Unit)? = null,
    onTaskClick: ((String) -> Unit)? = null,
    availableFilterOptions: Map<ExploreResultScope, List<ExploreFilterOption>> = emptyMap(),
    appliedFilterOptions: Map<ExploreResultScope, Set<ExploreFilterOption>> = emptyMap(),
    onFiltersChanged: ((ExploreResultScope, Set<ExploreFilterOption>) -> Unit)? = null,
    onFilterScopeChanged: (ExploreResultScope) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val scrollState = rememberExploreScrollState()
    var isSearchActive by remember { mutableStateOf(false) }
    val normalizedQuery = normalizeExploreQuery(query)
    var selectedScopeOrdinal by rememberSaveable(normalizedQuery) {
        mutableIntStateOf(ExploreResultScope.All.ordinal)
    }
    val selectedScope = ExploreResultScope.entries[selectedScopeOrdinal]
    val currentFilterKeys = ExploreResultScope.entries.associateWith { scope ->
        normalizeAppliedExploreFilterOptions(
            scope = scope,
            availableOptions = availableFilterOptions[scope].orEmpty(),
            appliedOptions = appliedFilterOptions[scope].orEmpty(),
        ).map(ExploreFilterOption::name).sorted().joinToString(",")
    }
    val currentSortKeys = ExploreResultScope.entries.associateWith { scope ->
        selectedSortOptions[scope]?.name.orEmpty()
    }
    val filterKey = currentFilterKeys.entries.joinToString("|") { (scope, value) ->
        "${scope.name}=$value"
    }
    val sortKey = currentSortKeys.entries.joinToString("|") { (scope, value) ->
        "${scope.name}=$value"
    }
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
        isSearchActive = false
    }

    fun openFilters() {
        closeSearch()
        onFilterScopeChanged(selectedScope)
        onSearchFiltersClick()
    }

    BackHandler(enabled = isSearchActive) {
        closeSearch()
    }

    val personClick = onPersonClick
    val taskClick = onTaskClick

    val orderedAppliedFilters = normalizeAvailableExploreFilterOptions(
        scope = selectedScope,
        options = availableFilterOptions[selectedScope].orEmpty(),
    ).filter { it in appliedFilterOptions[selectedScope].orEmpty() }
    val normalizedSubmittedResultState = normalizeExploreResultState(
        state = resultState,
        scope = selectedScope,
        emptyReason = if (orderedAppliedFilters.isEmpty()) {
            ExploreResultState.EmptyReason.Query
        } else {
            ExploreResultState.EmptyReason.Filters
        },
    )
    val submittedResults = normalizedSubmittedResultState as? ExploreResultState.Results
    val hasVisibleSubmittedResults = when (selectedScope) {
        ExploreResultScope.All -> false
        ExploreResultScope.People,
        ExploreResultScope.Services,
        -> submittedResults?.people?.isNotEmpty() == true

        ExploreResultScope.Tasks -> submittedResults?.tasks?.isNotEmpty() == true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .testTag("explore_results_list"),
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        item(key = "header:explore", contentType = "header") {
            ExploreHeader(
                query = query,
                searchArea = searchArea,
                onQueryChanged = onQueryChanged,
                onQueryCleared = onQueryCleared,
                onQuerySubmitted = onQuerySubmitted,
                onSearchFiltersClick = ::openFilters,
                selectedScope = selectedScope,
                availableFilterOptions = availableFilterOptions,
                appliedFilterOptions = appliedFilterOptions,
                onSearchFocused = { isSearchActive = true },
                onCloseSearch = ::closeSearch,
            )
        }

        if (isSearchActive && normalizedQuery.isEmpty()) {
            item(key = "search:active", contentType = "search") {
                ActiveSearchContent(
                    recentSearches = recentSearches,
                    onRecentSearchSelected = { recentQuery ->
                        onQuerySubmitted(recentQuery)
                        closeSearch()
                    },
                    onRecentSearchRemoved = onRecentSearchRemoved,
                    onRecentSearchesCleared = onRecentSearchesCleared,
                    onCategorySelected = { category ->
                        onQuerySubmitted(category)
                        closeSearch()
                    },
                )
            }
        } else if (isSearchActive && normalizedQuery.isNotEmpty()) {
            item(key = "search:typed", contentType = "search") {
                TypedSearchSuggestions(
                    query = normalizedQuery,
                    recentSearches = recentSearches,
                    categories = EXPLORE_CATEGORIES.map { stringResource(it.labelRes) },
                    onSuggestionSelected = { suggestion ->
                        onQuerySubmitted(suggestion)
                        closeSearch()
                    },
                )
            }
        } else if (!isSearchActive && normalizedQuery.isNotEmpty()) {
            addSubmittedSearchResults(
                selectedScope = selectedScope,
                normalizedResultState = normalizedSubmittedResultState,
                orderedAppliedFilters = orderedAppliedFilters,
                hasVisibleResults = hasVisibleSubmittedResults,
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
                    onCategorySelected = { category ->
                        onQuerySubmitted(category)
                        closeSearch()
                    },
                )
            }
            addBrowseResultSections(
                people = people,
                tasks = tasks,
                onPersonClick = personClick,
                onTaskClick = taskClick,
            )
        }
    }
}

@Composable
private fun ExploreHeader(
    query: String,
    searchArea: ExploreSearchArea,
    onQueryChanged: (String) -> Unit,
    onQueryCleared: () -> Unit,
    onQuerySubmitted: (String) -> Unit,
    onSearchFiltersClick: () -> Unit,
    selectedScope: ExploreResultScope,
    availableFilterOptions: Map<ExploreResultScope, List<ExploreFilterOption>>,
    appliedFilterOptions: Map<ExploreResultScope, Set<ExploreFilterOption>>,
    onSearchFocused: () -> Unit,
    onCloseSearch: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier
                    .weight(1f)
                    .testTag("explore_search_field")
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) onSearchFocused()
                    },
                placeholder = {
                    Text(
                        text = stringResource(R.string.explore_search_placeholder),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                    )
                },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = onQueryCleared) {
                            Icon(
                                painter = painterResource(R.drawable.ic_clear),
                                contentDescription = stringResource(R.string.explore_clear_search),
                            )
                        }
                    }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search,
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onQuerySubmitted(query)
                        onCloseSearch()
                    },
                ),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
            SearchFiltersButton(
                appliedFilterCount = normalizeAppliedExploreFilterOptions(
                    scope = selectedScope,
                    availableOptions = availableFilterOptions[selectedScope].orEmpty(),
                    appliedOptions = appliedFilterOptions[selectedScope].orEmpty(),
                ).size,
                onClick = onSearchFiltersClick,
            )
        }
        SearchAreaSummary(searchArea)
    }
}

@Composable
private fun ActiveSearchContent(
    recentSearches: List<String>,
    onRecentSearchSelected: (String) -> Unit,
    onRecentSearchRemoved: (String) -> Unit,
    onRecentSearchesCleared: () -> Unit,
    onCategorySelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    ) {
        if (recentSearches.isNotEmpty()) {
            RecentSearches(
                recentSearches = recentSearches,
                onQuerySelected = onRecentSearchSelected,
                onQueryRemoved = onRecentSearchRemoved,
                onClearAll = onRecentSearchesCleared,
            )
            Spacer(Modifier.height(24.dp))
        }
        SuggestedCategories(onCategorySelected)
    }
}

@Composable
private fun BrowseServices(
    onCategorySelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .testTag("explore_browse_services"),
    ) {
        Text(
            text = stringResource(R.string.explore_browse_services),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("explore_browse_category_row"),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = EXPLORE_CATEGORIES,
                key = { "category:${it.labelRes}" },
                contentType = { "category" },
            ) { categoryDefinition ->
                val category = stringResource(categoryDefinition.labelRes)
                ServiceCategoryTile(
                    category = category,
                    artworkRes = categoryDefinition.artworkRes,
                    onClick = { onCategorySelected(category) },
                )
            }
        }
    }
}

@Composable
private fun ServiceCategoryTile(
    category: String,
    @DrawableRes artworkRes: Int,
    onClick: () -> Unit,
) {
    val searchLabel = stringResource(R.string.explore_search_category, category)

    Card(
        modifier = Modifier
            .width(120.dp)
            .heightIn(min = 136.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = searchLabel,
                onClick = onClick,
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(artworkRes),
                contentDescription = null,
                modifier = Modifier.size(72.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = category,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.addSubmittedSearchResults(
    selectedScope: ExploreResultScope,
    normalizedResultState: ExploreResultState,
    orderedAppliedFilters: List<ExploreFilterOption>,
    hasVisibleResults: Boolean,
    onRetryResults: () -> Unit,
    onEditFilters: () -> Unit,
    availableSortOptions: Map<ExploreResultScope, List<ExploreSortOption>>,
    selectedSortOptions: Map<ExploreResultScope, ExploreSortOption>,
    onSortChanged: ((ExploreResultScope, ExploreSortOption) -> Unit)?,
    onFiltersChanged: ((ExploreResultScope, Set<ExploreFilterOption>) -> Unit)?,
    onPersonClick: ((String) -> Unit)?,
    onTaskClick: ((String) -> Unit)?,
    onScopeSelected: (ExploreResultScope) -> Unit,
) {
    stickyHeader(key = "controls:scope-tabs", contentType = "control") {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("explore_submitted_results"),
            color = MaterialTheme.colorScheme.background,
        ) {
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedScope.ordinal,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("explore_result_tabs"),
                edgePadding = 0.dp,
                minTabWidth = 0.dp,
            ) {
                ExploreResultScope.entries.forEach { scope ->
                    Tab(
                        selected = selectedScope == scope,
                        onClick = { onScopeSelected(scope) },
                        text = {
                            Text(
                                text = stringResource(scope.labelRes),
                                maxLines = 1,
                            )
                        },
                    )
                }
            }
        }
    }

    if (orderedAppliedFilters.isNotEmpty()) {
        item(key = "controls:filters", contentType = "control") {
            AppliedFilterChips(
                options = orderedAppliedFilters,
                onFilterRemoved = { option ->
                    onFiltersChanged?.invoke(
                        selectedScope,
                        orderedAppliedFilters.filterNot { it == option }.toSet(),
                    )
                },
            )
        }
    }

    val options = availableSortOptions[selectedScope].orEmpty()
    val selectedOption = selectedSortOptions[selectedScope]
    if (
        hasVisibleResults &&
        options.size >= 2 &&
        selectedOption != null &&
        selectedOption in options &&
        onSortChanged != null
    ) {
        item(key = "controls:sort:${selectedScope.name}", contentType = "control") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                ExploreSortControl(
                    scope = selectedScope,
                    options = options,
                    selectedOption = selectedOption,
                    onOptionSelected = { option ->
                        onSortChanged(selectedScope, option)
                    },
                )
            }
        }
    }

    addExploreResultBody(
        selectedScope = selectedScope,
        state = normalizedResultState,
        onRetryResults = onRetryResults,
        onEditFilters = onEditFilters,
        onPersonClick = onPersonClick,
        onTaskClick = onTaskClick,
    )
}

private fun LazyListScope.addBrowseResultSections(
    people: List<ExplorePersonResult>,
    tasks: List<ExploreTaskResult>,
    onPersonClick: ((String) -> Unit)?,
    onTaskClick: ((String) -> Unit)?,
) {
    addPersonResultSection(
        sectionKey = "browse:people",
        heading = R.string.explore_nearby_professionals,
        sectionTag = "explore_nearby_professionals",
        people = people,
        onClick = onPersonClick,
    )
    addTaskResultSection(
        sectionKey = "browse:tasks",
        heading = R.string.explore_open_tasks_nearby,
        sectionTag = "explore_open_tasks_nearby",
        tasks = tasks,
        onClick = onTaskClick,
    )
}

private fun LazyListScope.addExploreResultBody(
    selectedScope: ExploreResultScope,
    state: ExploreResultState,
    onRetryResults: () -> Unit,
    onEditFilters: () -> Unit,
    onPersonClick: ((String) -> Unit)?,
    onTaskClick: ((String) -> Unit)?,
) {
    when (state) {
        ExploreResultState.Loading -> {
            item(key = "body:loading", contentType = "progress") {
                ExploreLoadingResultBody()
            }
        }

        is ExploreResultState.Empty -> {
            item(key = "body:empty", contentType = "blocking") {
                ExploreEmptyResultBody(
                    reason = state.reason,
                    onEditFilters = onEditFilters,
                )
            }
        }

        is ExploreResultState.Failure -> {
            item(key = "body:failure", contentType = "blocking") {
                ExploreFailureResultBody(
                    reason = state.reason,
                    onRetryResults = onRetryResults,
                )
            }
        }

        is ExploreResultState.Results -> {
            if (state.isRefreshing) {
                item(key = "progress:refreshing", contentType = "progress") {
                    val refreshingDescription = stringResource(
                        R.string.explore_refreshing_results_content_description,
                    )
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .testTag("explore_result_refreshing_indicator")
                            .semantics {
                                contentDescription = refreshingDescription
                            },
                    )
                }
            }
            state.status?.let { status ->
                item(
                    key = "status:${exploreStatusKey(status)}",
                    contentType = "status",
                ) {
                    ExploreResultStatus(
                        status = status,
                        actionEnabled = !state.isRefreshing,
                        onAction = onRetryResults,
                    )
                }
            }

            when (selectedScope) {
                ExploreResultScope.All -> {
                    addPersonResultSection(
                        sectionKey = "submitted:people",
                        heading = R.string.explore_people_and_services,
                        sectionTag = "explore_submitted_people",
                        people = state.people,
                        onClick = onPersonClick,
                    )
                    addTaskResultSection(
                        sectionKey = "submitted:tasks",
                        heading = R.string.explore_tasks,
                        sectionTag = "explore_submitted_tasks",
                        tasks = state.tasks,
                        onClick = onTaskClick,
                    )
                }

                ExploreResultScope.People -> {
                    addPersonResultSection(
                        sectionKey = "submitted:people",
                        heading = null,
                        sectionTag = "explore_submitted_people",
                        people = state.people,
                        onClick = onPersonClick,
                    )
                }

                ExploreResultScope.Services -> {
                    addServiceResultRows(
                        people = state.people,
                        onClick = onPersonClick,
                    )
                }

                ExploreResultScope.Tasks -> {
                    addTaskResultSection(
                        sectionKey = "submitted:tasks",
                        heading = null,
                        sectionTag = "explore_submitted_tasks",
                        tasks = state.tasks,
                        onClick = onTaskClick,
                    )
                }
            }
        }
    }
}

private fun LazyListScope.addPersonResultSection(
    sectionKey: String,
    @StringRes heading: Int?,
    sectionTag: String,
    people: List<ExplorePersonResult>,
    onClick: ((String) -> Unit)?,
) {
    val uniquePeople = distinctPeopleById(people)
    if (uniquePeople.isEmpty()) return

    if (heading != null) {
        item(key = "section:$sectionKey", contentType = "section") {
            ExploreResultSectionHeading(
                heading = stringResource(heading),
                testTag = sectionTag,
            )
        }
    }
    addPersonResultRows(
        itemKeyPrefix = sectionKey,
        people = uniquePeople,
        onClick = onClick,
        firstRowTag = sectionTag.takeIf { heading == null },
        addTopPadding = heading == null,
    )
}

private fun LazyListScope.addTaskResultSection(
    sectionKey: String,
    @StringRes heading: Int?,
    sectionTag: String,
    tasks: List<ExploreTaskResult>,
    onClick: ((String) -> Unit)?,
) {
    val uniqueTasks = distinctTasksById(tasks)
    if (uniqueTasks.isEmpty()) return

    if (heading != null) {
        item(key = "section:$sectionKey", contentType = "section") {
            ExploreResultSectionHeading(
                heading = stringResource(heading),
                testTag = sectionTag,
            )
        }
    }
    addTaskResultRows(
        itemKeyPrefix = sectionKey,
        tasks = uniqueTasks,
        onClick = onClick,
        firstRowTag = sectionTag.takeIf { heading == null },
        addTopPadding = heading == null,
    )
}

private fun LazyListScope.addPersonResultRows(
    itemKeyPrefix: String,
    people: List<ExplorePersonResult>,
    onClick: ((String) -> Unit)?,
    firstRowTag: String?,
    addTopPadding: Boolean,
) {
    buildExplorePersonRenderRows(people).forEachIndexed { index, row ->
        val person = row.result
        item(
            key = "person:$itemKeyPrefix:${row.uiKey}",
            contentType = "person",
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (index == 0 && addTopPadding) Modifier.padding(top = 24.dp) else Modifier)
                    .then(if (index == 0 && firstRowTag != null) Modifier.testTag(firstRowTag) else Modifier),
            ) {
                if (index > 0) HorizontalDivider()
                PersonResultItem(
                    name = person.name,
                    avatarUrl = person.avatarUrl,
                    primaryService = person.primaryService,
                    additionalServices = person.additionalServices,
                    rating = person.rating,
                    reviewCount = person.reviewCount,
                    locationLabel = person.locationLabel,
                    priceLabel = person.priceLabel,
                    statusLabel = person.statusLabel,
                    onClick = onClick
                        ?.takeIf { row.stableId.isNotEmpty() }
                        ?.let { click -> { click(row.stableId) } },
                )
            }
        }
    }
}

private fun LazyListScope.addServiceResultRows(
    people: List<ExplorePersonResult>,
    onClick: ((String) -> Unit)?,
) {
    buildExplorePersonRenderRows(distinctPeopleById(people)).forEachIndexed { index, row ->
        val person = row.result
        item(
            key = "service:person:${row.uiKey}",
            contentType = "service",
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (index == 0) 24.dp else 12.dp)
                    .then(
                        if (index == 0) {
                            Modifier.testTag("explore_submitted_services")
                        } else {
                            Modifier
                        },
                    ),
            ) {
                CompactServiceResultCard(
                    result = person,
                    onClick = onClick
                        ?.takeIf { row.stableId.isNotEmpty() }
                        ?.let { click -> { click(row.stableId) } },
                )
            }
        }
    }
}

private fun LazyListScope.addTaskResultRows(
    itemKeyPrefix: String,
    tasks: List<ExploreTaskResult>,
    onClick: ((String) -> Unit)?,
    firstRowTag: String?,
    addTopPadding: Boolean,
) {
    buildExploreTaskRenderRows(tasks).forEachIndexed { index, row ->
        val task = row.result
        item(
            key = "task:$itemKeyPrefix:${row.uiKey}",
            contentType = "task",
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (index == 0 && addTopPadding) Modifier.padding(top = 24.dp) else Modifier)
                    .then(if (index == 0 && firstRowTag != null) Modifier.testTag(firstRowTag) else Modifier),
            ) {
                if (index > 0) HorizontalDivider()
                TaskResultItem(
                    title = task.title,
                    category = task.category,
                    summary = task.summary,
                    budgetLabel = task.budgetLabel,
                    locationLabel = task.locationLabel,
                    timingLabel = task.timingLabel,
                    posterName = task.posterName,
                    postedLabel = task.postedLabel,
                    status = task.status,
                    onClick = onClick
                        ?.takeIf {
                            task.status != TaskResultStatus.Unavailable && row.stableId.isNotEmpty()
                        }
                        ?.let { click -> { click(row.stableId) } },
                )
            }
        }
    }
}

@Composable
private fun ExploreResultSectionHeading(
    heading: String,
    testTag: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
            .testTag(testTag),
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(12.dp))
    }
}

private fun distinctPeopleById(people: List<ExplorePersonResult>): List<ExplorePersonResult> {
    val merged = mutableListOf<ExplorePersonResult>()
    val indicesById = linkedMapOf<String, Int>()
    people.forEach { person ->
        val stableId = person.id.trim()
        if (stableId.isEmpty()) {
            merged += person
        } else {
            val existingIndex = indicesById[stableId]
            if (existingIndex == null) {
                indicesById[stableId] = merged.size
                merged += person
            } else {
                val existing = merged[existingIndex]
                merged[existingIndex] = existing.copy(
                    additionalServices = (existing.additionalServices + person.additionalServices).distinct(),
                    matchReasons = existing.matchReasons + person.matchReasons,
                )
            }
        }
    }
    return merged
}

internal fun buildExplorePersonRenderRows(
    people: List<ExplorePersonResult>,
): List<ExplorePersonRenderRow> {
    val anonymousOccurrences = mutableMapOf<String, Int>()
    return people.map { person ->
        val stableId = person.id.trim()
        val uiKey = if (stableId.isNotEmpty()) {
            stableId
        } else {
            val fingerprint = explorePersonContentFingerprint(person)
            val occurrence = anonymousOccurrences.getOrDefault(fingerprint, 0)
            anonymousOccurrences[fingerprint] = occurrence + 1
            "anonymous-person:$fingerprint:$occurrence"
        }
        ExplorePersonRenderRow(
            result = person,
            stableId = stableId,
            uiKey = uiKey,
        )
    }
}

internal fun buildExploreTaskRenderRows(
    tasks: List<ExploreTaskResult>,
): List<ExploreTaskRenderRow> {
    val anonymousOccurrences = mutableMapOf<String, Int>()
    return tasks.map { task ->
        val stableId = task.id.trim()
        val uiKey = if (stableId.isNotEmpty()) {
            stableId
        } else {
            val fingerprint = exploreTaskContentFingerprint(task)
            val occurrence = anonymousOccurrences.getOrDefault(fingerprint, 0)
            anonymousOccurrences[fingerprint] = occurrence + 1
            "anonymous-task:$fingerprint:$occurrence"
        }
        ExploreTaskRenderRow(
            result = task,
            stableId = stableId,
            uiKey = uiKey,
        )
    }
}

private fun explorePersonContentFingerprint(person: ExplorePersonResult): String = buildString {
    appendExploreKeyPart(person.name)
    appendExploreKeyPart(person.avatarUrl)
    appendExploreKeyPart(person.primaryService)
    appendExploreKeyParts(person.additionalServices)
    appendExploreKeyPart(person.rating?.toString())
    appendExploreKeyPart(person.reviewCount.toString())
    appendExploreKeyPart(person.locationLabel)
    appendExploreKeyPart(person.priceLabel)
    appendExploreKeyPart(person.statusLabel)
    appendExploreKeyParts(person.matchReasons.map(PersonMatchReason::name).sorted())
}

private fun exploreTaskContentFingerprint(task: ExploreTaskResult): String = buildString {
    appendExploreKeyPart(task.title)
    appendExploreKeyPart(task.category)
    appendExploreKeyPart(task.summary)
    appendExploreKeyPart(task.budgetLabel)
    appendExploreKeyPart(task.locationLabel)
    appendExploreKeyPart(task.timingLabel)
    appendExploreKeyPart(task.posterName)
    appendExploreKeyPart(task.postedLabel)
    appendExploreKeyPart(task.status.name)
}

private fun StringBuilder.appendExploreKeyParts(values: List<String>) {
    append(values.size)
    values.forEach { value -> appendExploreKeyPart(value) }
}

private fun StringBuilder.appendExploreKeyPart(value: String?) {
    val normalized = value.orEmpty()
    append(normalized.length)
    append(':')
    append(normalized)
}

private fun distinctTasksById(tasks: List<ExploreTaskResult>): List<ExploreTaskResult> {
    val seenIds = mutableSetOf<String>()
    return tasks.filter { task ->
        val stableId = task.id.trim()
        stableId.isEmpty() || seenIds.add(stableId)
    }
}

private fun exploreStatusKey(status: ExploreResultState.ContentStatus): String = when (status) {
    ExploreResultState.ContentStatus.Stale -> "stale"
    ExploreResultState.ContentStatus.OfflineCached -> "offline-cached"
    is ExploreResultState.ContentStatus.PartialFailure -> "partial-${status.source.name.lowercase()}"
}

private fun exploreRequestKey(
    query: String,
    searchArea: ExploreSearchArea,
): String = buildString {
    append(query)
    append('|')
    append(searchArea.placeId.orEmpty())
    append('|')
    append(searchArea.displayName)
    append('|')
    append(searchArea.supportingText.orEmpty())
    append('|')
    append(searchArea.latitude ?: "")
    append('|')
    append(searchArea.longitude ?: "")
    append('|')
    append(searchArea.radiusKm)
    append('|')
    append(searchArea.source.name)
}

@Composable
private fun ExploreLoadingResultBody() {
    val loadingDescription = stringResource(
        R.string.explore_loading_results_content_description,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
            .testTag("explore_result_loading"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .testTag("explore_result_loading_indicator")
                .semantics {
                    contentDescription = loadingDescription
                },
        )
        Text(
            text = stringResource(R.string.explore_loading_results),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun ExploreEmptyResultBody(
    reason: ExploreResultState.EmptyReason,
    onEditFilters: () -> Unit,
) {
    val isFiltered = reason == ExploreResultState.EmptyReason.Filters
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
            .testTag("explore_result_empty")
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(
                if (isFiltered) {
                    R.string.explore_empty_filters_title
                } else {
                    R.string.explore_empty_query_title
                },
            ),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(
                if (isFiltered) {
                    R.string.explore_empty_filters_supporting_text
                } else {
                    R.string.explore_empty_query_supporting_text
                },
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (isFiltered) {
            OutlinedButton(
                onClick = onEditFilters,
                modifier = Modifier.testTag("explore_edit_filters"),
            ) {
                Text(stringResource(R.string.explore_edit_filters))
            }
        }
    }
}

@Composable
private fun ExploreFailureResultBody(
    reason: ExploreResultState.FailureReason,
    onRetryResults: () -> Unit,
) {
    val titleRes: Int
    val supportingTextRes: Int
    when (reason) {
        ExploreResultState.FailureReason.General -> {
            titleRes = R.string.explore_failure_general_title
            supportingTextRes = R.string.explore_failure_general_supporting_text
        }

        ExploreResultState.FailureReason.Offline -> {
            titleRes = R.string.explore_failure_offline_title
            supportingTextRes = R.string.explore_failure_offline_supporting_text
        }

        is ExploreResultState.FailureReason.SourceUnavailable -> when (reason.source) {
            ExploreResultState.Source.PeopleAndServices -> {
                titleRes = R.string.explore_failure_people_services_title
                supportingTextRes = R.string.explore_failure_people_services_supporting_text
            }

            ExploreResultState.Source.Tasks -> {
                titleRes = R.string.explore_failure_tasks_title
                supportingTextRes = R.string.explore_failure_tasks_supporting_text
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
            .testTag("explore_result_failure")
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(titleRes),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(supportingTextRes),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
        OutlinedButton(
            onClick = onRetryResults,
            modifier = Modifier.testTag("explore_retry_results"),
        ) {
            Text(stringResource(R.string.explore_retry))
        }
    }
}

@Composable
private fun ExploreResultStatus(
    status: ExploreResultState.ContentStatus,
    actionEnabled: Boolean,
    onAction: () -> Unit,
) {
    val messageRes = when (status) {
        ExploreResultState.ContentStatus.Stale -> R.string.explore_status_stale
        ExploreResultState.ContentStatus.OfflineCached -> R.string.explore_status_offline_cached
        is ExploreResultState.ContentStatus.PartialFailure -> when (status.source) {
            ExploreResultState.Source.PeopleAndServices -> {
                R.string.explore_status_people_services_partial_failure
            }

            ExploreResultState.Source.Tasks -> R.string.explore_status_tasks_partial_failure
        }
    }
    val actionRes = if (status == ExploreResultState.ContentStatus.Stale) {
        R.string.explore_refresh
    } else {
        R.string.explore_retry
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .testTag("explore_result_status")
            .semantics { liveRegion = LiveRegionMode.Polite },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(messageRes),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (actionEnabled) {
                TextButton(
                    onClick = onAction,
                    modifier = Modifier.testTag("explore_result_status_action"),
                ) {
                    Text(stringResource(actionRes))
                }
            }
        }
    }
}

@Composable
private fun AppliedFilterChips(
    options: List<ExploreFilterOption>,
    onFilterRemoved: (ExploreFilterOption) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("explore_applied_filters"),
        contentPadding = PaddingValues(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = options,
            key = { "filter:${it.name}" },
            contentType = { "filter" },
        ) { option ->
            val label = stringResource(option.labelRes())
            InputChip(
                selected = true,
                onClick = { onFilterRemoved(option) },
                label = { Text(label) },
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_clear),
                        contentDescription = stringResource(
                            R.string.explore_remove_filter,
                            label,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun ExploreSortControl(
    scope: ExploreResultScope,
    options: List<ExploreSortOption>,
    selectedOption: ExploreSortOption,
    onOptionSelected: (ExploreSortOption) -> Unit,
) {
    var expanded by remember(scope) { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.testTag("explore_sort_control"),
        ) {
            Text(
                text = stringResource(
                    R.string.explore_sort_by,
                    stringResource(selectedOption.labelRes),
                ),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag("explore_sort_menu"),
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes)) },
                    onClick = {
                        expanded = false
                        if (!isSelected) onOptionSelected(option)
                    },
                    modifier = Modifier.semantics {
                        selected = isSelected
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
private fun CompactServiceResultCard(
    result: ExplorePersonResult,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val service = result.primaryService?.trim().orEmpty()
    val provider = result.name.trim()
    val status = result.statusLabel?.trim()?.takeIf(String::isNotEmpty)
    val additionalServiceCount = result.additionalServices.count { additionalService ->
        val normalized = additionalService.trim()
        normalized.isNotEmpty() && !normalized.equals(service, ignoreCase = true)
    }
    val clickLabel = stringResource(R.string.explore_view_service, service)

    val cardModifier = modifier
        .fillMaxWidth()
        .testTag("explore_service_result_card")
        .then(
            if (onClick == null) {
                Modifier
            } else {
                Modifier.clickable(
                    role = Role.Button,
                    onClickLabel = clickLabel,
                    onClick = onClick,
                )
            },
        )

    Card(
        modifier = cardModifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = service,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                    ) {
                        AskITAvatar(
                            avatarUrl = result.avatarUrl,
                            avatarSize = 40.dp,
                            fallbackIconSize = 28.dp,
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = provider,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (status != null) {
                        Text(
                            text = status,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    PersonResultMetadata(
                        rating = result.rating,
                        reviewCount = result.reviewCount,
                        locationLabel = result.locationLabel,
                        price = result.priceLabel,
                    )
                }
            }
            if (additionalServiceCount > 0) {
                Text(
                    text = pluralStringResource(
                        R.plurals.explore_additional_services,
                        additionalServiceCount,
                        additionalServiceCount,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun TypedSearchSuggestions(
    query: String,
    recentSearches: List<String>,
    categories: List<String>,
    onSuggestionSelected: (String) -> Unit,
) {
    val normalizedQuery = query.lowercase(Locale.ROOT)
    val recentSuggestions = remember(normalizedQuery, recentSearches) {
        matchingRecentSearches(recentSearches, normalizedQuery)
    }
    val serviceSuggestions = remember(normalizedQuery, recentSuggestions, categories) {
        matchingServiceCategories(categories, normalizedQuery, recentSuggestions)
    }
    val hasExactVisibleSuggestion = (recentSuggestions + serviceSuggestions)
        .any { normalizedMatchValue(it) == normalizedQuery }
    val directSearchVisible = !hasExactVisibleSuggestion

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .testTag("explore_typed_suggestions"),
    ) {
        if (recentSuggestions.isNotEmpty()) {
            SuggestionSectionHeading(stringResource(R.string.explore_recent_searches))
            recentSuggestions.forEach { recentQuery ->
                SuggestionRow(
                    text = AnnotatedString(recentQuery),
                    iconRes = R.drawable.ic_history,
                    actionLabel = stringResource(
                        R.string.explore_search_recent_query,
                        recentQuery,
                    ),
                    onClick = { onSuggestionSelected(recentQuery) },
                )
            }
        }

        if (serviceSuggestions.isNotEmpty()) {
            if (recentSuggestions.isNotEmpty()) Spacer(Modifier.height(24.dp))
            SuggestionSectionHeading(stringResource(R.string.explore_services))
            serviceSuggestions.forEach { category ->
                SuggestionRow(
                    text = predictiveCategoryText(category, normalizedQuery),
                    iconRes = R.drawable.ic_search,
                    actionLabel = stringResource(R.string.explore_search_category, category),
                    onClick = { onSuggestionSelected(category) },
                )
            }
        }

        if (directSearchVisible) {
            SuggestionRow(
                text = AnnotatedString(
                    stringResource(R.string.explore_search_typed_query, query),
                ),
                iconRes = R.drawable.ic_search,
                actionLabel = stringResource(
                    R.string.explore_search_typed_query_action,
                    query,
                ),
                onClick = { onSuggestionSelected(query) },
            )
        }
    }
}

@Composable
private fun SuggestionSectionHeading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun SuggestionRow(
    text: AnnotatedString,
    @DrawableRes iconRes: Int,
    actionLabel: String,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .testTag("explore_typed_suggestion_row")
            .clickable(
                role = Role.Button,
                onClickLabel = actionLabel,
                onClick = onClick,
            ),
        leadingContent = {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        },
        headlineContent = {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
    )
}

private fun matchingRecentSearches(
    recentSearches: List<String>,
    normalizedQuery: String,
): List<String> {
    val matches = recentSearches.filter { recentQuery ->
        matchStrength(normalizedMatchValue(recentQuery), normalizedQuery) != null
    }
    val exactMatch = matches.firstOrNull { normalizedMatchValue(it) == normalizedQuery }
    return buildList {
        exactMatch?.let(::add)
        matches.filterNot { normalizedMatchValue(it) == normalizedQuery }.let(::addAll)
    }.take(2)
}

private fun matchingServiceCategories(
    categories: List<String>,
    normalizedQuery: String,
    recentSuggestions: List<String>,
): List<String> {
    val usedValues = recentSuggestions
        .mapTo(mutableSetOf(), ::normalizedMatchValue)
    return buildList {
        categories.forEach { category ->
            val normalizedCategory = normalizedMatchValue(category)
            if (
                size < 3 &&
                normalizedCategory !in usedValues &&
                matchStrength(normalizedCategory, normalizedQuery) != null
            ) {
                add(category)
                usedValues += normalizedCategory
            }
        }
    }
}

private fun matchStrength(normalizedLabel: String, normalizedQuery: String): Int? {
    if (normalizedLabel.isEmpty() || normalizedQuery.isEmpty()) return null
    return when {
        normalizedLabel == normalizedQuery -> 0
        normalizedLabel.startsWith(normalizedQuery) -> 1
        normalizedLabel.split(' ').any { it.startsWith(normalizedQuery) } -> 2
        normalizedLabel.contains(normalizedQuery) -> 3
        else -> null
    }
}

private fun normalizedMatchValue(value: String): String =
    normalizeExploreQuery(value).lowercase(Locale.ROOT)

private fun predictiveCategoryText(
    category: String,
    normalizedQuery: String,
): AnnotatedString {
    if (
        !category.startsWith(normalizedQuery, ignoreCase = true) ||
        normalizedQuery.isEmpty() ||
        normalizedQuery.length > category.length
    ) {
        return AnnotatedString(category)
    }

    return buildAnnotatedString {
        append(category.substring(0, normalizedQuery.length))
        withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
            append(category.substring(normalizedQuery.length))
        }
    }
}

@Composable
private fun RecentSearches(
    recentSearches: List<String>,
    onQuerySelected: (String) -> Unit,
    onQueryRemoved: (String) -> Unit,
    onClearAll: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("explore_recent_searches"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.explore_recent_searches),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            if (recentSearches.size >= 2) {
                TextButton(onClick = onClearAll) {
                    Text(stringResource(R.string.explore_clear_all))
                }
            }
        }
        recentSearches.forEach { recentQuery ->
            RecentSearchRow(
                query = recentQuery,
                onSelect = { onQuerySelected(recentQuery) },
                onRemove = { onQueryRemoved(recentQuery) },
            )
        }
    }
}

@Composable
private fun RecentSearchRow(
    query: String,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
) {
    val searchAction = stringResource(R.string.explore_search_recent_query, query)
    val removeAction = stringResource(R.string.explore_remove_recent_search, query)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(
                    role = Role.Button,
                    onClickLabel = searchAction,
                    onClick = onSelect,
                )
                .semantics { contentDescription = searchAction },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_history),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = query,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
        }
        IconButton(onClick = onRemove) {
            Icon(
                painter = painterResource(R.drawable.ic_clear),
                contentDescription = removeAction,
            )
        }
    }
}

@Composable
private fun SuggestedCategories(onCategorySelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("explore_suggested_categories"),
    ) {
        Text(
            text = stringResource(R.string.explore_suggested_categories),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = EXPLORE_CATEGORIES,
                key = { "category:${it.labelRes}" },
                contentType = { "category" },
            ) { categoryDefinition ->
                val category = stringResource(categoryDefinition.labelRes)
                SuggestionChip(
                    onClick = { onCategorySelected(category) },
                    label = {
                        Text(
                            text = category,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SearchFiltersButton(
    appliedFilterCount: Int,
    onClick: () -> Unit,
) {
    val contentDescription = if (appliedFilterCount == 0) {
        stringResource(R.string.explore_search_filters_content_description)
    } else {
        pluralStringResource(
            R.plurals.explore_search_filters_applied_content_description,
            appliedFilterCount,
            appliedFilterCount,
        )
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above,
        ),
        tooltip = {
            PlainTooltip {
                Text(stringResource(R.string.explore_filters_tooltip))
            }
        },
        state = rememberTooltipState(),
    ) {
        OutlinedIconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .testTag("explore_filter_button"),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_filter_list),
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun SearchAreaSummary(searchArea: ExploreSearchArea) {
    val radius = pluralStringResource(
        R.plurals.explore_radius_km,
        searchArea.radiusKm,
        searchArea.radiusKm,
    )
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_location_on),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(
                R.string.explore_location_summary,
                searchArea.displayName,
                radius,
            ),
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .testTag("explore_location_summary"),
        )
    }
}
