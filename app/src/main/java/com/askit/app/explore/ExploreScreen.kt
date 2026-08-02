package com.askit.app.explore

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
    var isSearchActive by remember { mutableStateOf(false) }
    val normalizedQuery = normalizeExploreQuery(query)
    var selectedScopeOrdinal by rememberSaveable(normalizedQuery) {
        mutableIntStateOf(ExploreResultScope.All.ordinal)
    }
    val selectedScope = ExploreResultScope.entries[selectedScopeOrdinal]

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        item(key = "explore_header") {
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
            item(key = "active_search_content") {
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
            item(key = "typed_search_suggestions") {
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
            item(key = "submitted_search_results") {
                SubmittedSearchResults(
                    selectedScope = selectedScope,
                    resultState = resultState,
                    onRetryResults = onRetryResults,
                    onEditFilters = ::openFilters,
                    availableSortOptions = availableSortOptions,
                    selectedSortOptions = selectedSortOptions,
                    onSortChanged = onSortChanged,
                    availableFilterOptions = availableFilterOptions,
                    appliedFilterOptions = appliedFilterOptions,
                    onFiltersChanged = onFiltersChanged,
                    onPersonClick = personClick,
                    onTaskClick = taskClick,
                    onScopeSelected = { selectedScopeOrdinal = it.ordinal },
                )
            }
        } else if (!isSearchActive && normalizedQuery.isEmpty()) {
            item(key = "browse_services") {
                BrowseServices(
                    onCategorySelected = { category ->
                        onQuerySubmitted(category)
                        closeSearch()
                    },
                )
            }

            if (people.isNotEmpty() && personClick != null) {
                item(key = "nearby_professionals") {
                    ExploreResultSection(
                        heading = stringResource(R.string.explore_nearby_professionals),
                        testTag = "explore_nearby_professionals",
                    ) {
                        PersonResultRows(people.take(4), personClick)
                    }
                }
            }

            if (tasks.isNotEmpty() && taskClick != null) {
                item(key = "open_tasks_nearby") {
                    ExploreResultSection(
                        heading = stringResource(R.string.explore_open_tasks_nearby),
                        testTag = "explore_open_tasks_nearby",
                    ) {
                        TaskResultRows(tasks.take(4), taskClick)
                    }
                }
            }
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
            items(EXPLORE_CATEGORIES, key = { it.labelRes }) { categoryDefinition ->
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

@Composable
private fun SubmittedSearchResults(
    selectedScope: ExploreResultScope,
    resultState: ExploreResultState,
    onRetryResults: () -> Unit,
    onEditFilters: () -> Unit,
    availableSortOptions: Map<ExploreResultScope, List<ExploreSortOption>>,
    selectedSortOptions: Map<ExploreResultScope, ExploreSortOption>,
    onSortChanged: ((ExploreResultScope, ExploreSortOption) -> Unit)?,
    availableFilterOptions: Map<ExploreResultScope, List<ExploreFilterOption>>,
    appliedFilterOptions: Map<ExploreResultScope, Set<ExploreFilterOption>>,
    onFiltersChanged: ((ExploreResultScope, Set<ExploreFilterOption>) -> Unit)?,
    onPersonClick: ((String) -> Unit)?,
    onTaskClick: ((String) -> Unit)?,
    onScopeSelected: (ExploreResultScope) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("explore_submitted_results"),
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

        val orderedAppliedFilters = normalizeAvailableExploreFilterOptions(
            scope = selectedScope,
            options = availableFilterOptions[selectedScope].orEmpty(),
        ).filter {
            it in appliedFilterOptions[selectedScope].orEmpty()
        }
        if (orderedAppliedFilters.isNotEmpty()) {
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

        val normalizedResultState = normalizeExploreResultState(
            state = resultState,
            scope = selectedScope,
            emptyReason = if (orderedAppliedFilters.isEmpty()) {
                ExploreResultState.EmptyReason.Query
            } else {
                ExploreResultState.EmptyReason.Filters
            },
        )
        val results = normalizedResultState as? ExploreResultState.Results
        val hasVisibleResults = when (selectedScope) {
            ExploreResultScope.All -> false
            ExploreResultScope.People,
            ExploreResultScope.Services,
            -> results?.people?.isNotEmpty() == true

            ExploreResultScope.Tasks -> results?.tasks?.isNotEmpty() == true
        }
        val options = availableSortOptions[selectedScope].orEmpty()
        val selectedOption = selectedSortOptions[selectedScope]
        val sortCallback = onSortChanged
        if (
            hasVisibleResults &&
            options.size >= 2 &&
            selectedOption != null &&
            selectedOption in options &&
            sortCallback != null
        ) {
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
                        sortCallback(selectedScope, option)
                    },
                )
            }
        }

        ExploreResultBody(
            selectedScope = selectedScope,
            state = normalizedResultState,
            onRetryResults = onRetryResults,
            onEditFilters = onEditFilters,
            onPersonClick = onPersonClick,
            onTaskClick = onTaskClick,
        )
    }
}

@Composable
private fun ExploreResultBody(
    selectedScope: ExploreResultScope,
    state: ExploreResultState,
    onRetryResults: () -> Unit,
    onEditFilters: () -> Unit,
    onPersonClick: ((String) -> Unit)?,
    onTaskClick: ((String) -> Unit)?,
) {
    when (state) {
        ExploreResultState.Loading -> {
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

        is ExploreResultState.Empty -> {
            val isFiltered = state.reason == ExploreResultState.EmptyReason.Filters
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

        is ExploreResultState.Failure -> {
            val titleRes: Int
            val supportingTextRes: Int
            when (val reason = state.reason) {
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

        is ExploreResultState.Results -> {
            if (state.isRefreshing) {
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
            state.status?.let { status ->
                ExploreResultStatus(
                    status = status,
                    actionEnabled = !state.isRefreshing,
                    onAction = onRetryResults,
                )
            }
            when (selectedScope) {
                ExploreResultScope.All -> {
                    if (state.people.isNotEmpty()) {
                        ExploreResultSection(
                            heading = stringResource(R.string.explore_people_and_services),
                            testTag = "explore_submitted_people",
                        ) {
                            PersonResultRows(state.people, onPersonClick)
                        }
                    }
                    if (state.tasks.isNotEmpty()) {
                        ExploreResultSection(
                            heading = stringResource(R.string.explore_tasks),
                            testTag = "explore_submitted_tasks",
                        ) {
                            TaskResultRows(state.tasks, onTaskClick)
                        }
                    }
                }

                ExploreResultScope.People -> {
                    if (state.people.isNotEmpty()) {
                        ExploreResultSection(
                            heading = null,
                            testTag = "explore_submitted_people",
                        ) {
                            PersonResultRows(state.people, onPersonClick)
                        }
                    }
                }

                ExploreResultScope.Services -> {
                    if (state.people.isNotEmpty()) {
                        ServiceResultRows(state.people, onPersonClick)
                    }
                }

                ExploreResultScope.Tasks -> {
                    if (state.tasks.isNotEmpty()) {
                        ExploreResultSection(
                            heading = null,
                            testTag = "explore_submitted_tasks",
                        ) {
                            TaskResultRows(state.tasks, onTaskClick)
                        }
                    }
                }
            }
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
            key = ExploreFilterOption::name,
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
private fun PersonResultRows(
    people: List<ExplorePersonResult>,
    onClick: ((String) -> Unit)?,
) {
    people.forEachIndexed { index, person ->
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
            onClick = onClick?.let { click -> { click(person.id) } },
        )
    }
}

@Composable
private fun ServiceResultRows(
    people: List<ExplorePersonResult>,
    onClick: ((String) -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
            .testTag("explore_submitted_services"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        people.forEach { person ->
            CompactServiceResultCard(
                result = person,
                onClick = onClick?.let { click -> { click(person.id) } },
            )
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
private fun TaskResultRows(
    tasks: List<ExploreTaskResult>,
    onClick: ((String) -> Unit)?,
) {
    tasks.forEachIndexed { index, task ->
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
            onClick = onClick?.let { click -> { click(task.id) } },
        )
    }
}

@Composable
private fun ExploreResultSection(
    heading: String?,
    testTag: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
            .testTag(testTag),
    ) {
        if (heading != null) {
            Text(
                text = heading,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(12.dp))
        }
        content()
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
            items(EXPLORE_CATEGORIES, key = { it.labelRes }) { categoryDefinition ->
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
