package com.askit.app.explore

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
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
    val primaryService: String,
    val additionalServices: List<String>,
    val rating: Double?,
    val reviewCount: Int,
    val locationLabel: String,
    val priceLabel: String?,
    val statusLabel: String?,
)

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
    onPersonClick: ((String) -> Unit)? = null,
    onTaskClick: ((String) -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    var isSearchActive by remember { mutableStateOf(false) }
    val normalizedQuery = normalizeExploreQuery(query)

    fun closeSearch() {
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
        isSearchActive = false
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
                onSearchFiltersClick = {
                    closeSearch()
                    onSearchFiltersClick()
                },
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
                        people.take(4).forEachIndexed { index, person ->
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
                                onClick = { personClick(person.id) },
                            )
                        }
                    }
                }
            }

            if (tasks.isNotEmpty() && taskClick != null) {
                item(key = "open_tasks_nearby") {
                    ExploreResultSection(
                        heading = stringResource(R.string.explore_open_tasks_nearby),
                        testTag = "explore_open_tasks_nearby",
                    ) {
                        tasks.take(4).forEachIndexed { index, task ->
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
                                onClick = { taskClick(task.id) },
                            )
                        }
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
                searchArea = searchArea,
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
private fun ExploreResultSection(
    heading: String,
    testTag: String,
    content: @Composable () -> Unit,
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
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp,
        ) {
            Column {
                content()
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
    searchArea: ExploreSearchArea,
    onClick: () -> Unit,
) {
    val radius = pluralStringResource(
        R.plurals.explore_radius_kilometres,
        searchArea.radiusKm,
        searchArea.radiusKm,
    )
    val contentDescription = stringResource(
        R.string.explore_search_filters_content_description,
        searchArea.displayName,
        radius,
    )

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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .testTag("explore_location_summary"),
        )
    }
}
