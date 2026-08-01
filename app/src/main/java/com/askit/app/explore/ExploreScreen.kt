package com.askit.app.explore

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.askit.app.R

private val SUGGESTED_CATEGORY_RESOURCES = listOf(
    R.string.explore_category_electrician,
    R.string.explore_category_plumber,
    R.string.explore_category_cleaning,
    R.string.explore_category_ac_repair,
    R.string.explore_category_home_tutor,
    R.string.explore_category_appliance_repair,
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
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchActive by remember { mutableStateOf(false) }

    fun closeSearch() {
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
        isSearchActive = false
    }

    BackHandler(enabled = isSearchActive) {
        closeSearch()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
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
                        if (focusState.isFocused) isSearchActive = true
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
                        closeSearch()
                    },
                ),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
            SearchFiltersButton(
                searchArea = searchArea,
                onClick = {
                    closeSearch()
                    onSearchFiltersClick()
                },
            )
        }
        SearchAreaSummary(searchArea)

        if (isSearchActive && query.isBlank()) {
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
    }
}

@Composable
private fun ColumnScope.ActiveSearchContent(
    recentSearches: List<String>,
    onRecentSearchSelected: (String) -> Unit,
    onRecentSearchRemoved: (String) -> Unit,
    onRecentSearchesCleared: () -> Unit,
    onCategorySelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .imePadding()
            .verticalScroll(rememberScrollState())
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
            items(SUGGESTED_CATEGORY_RESOURCES, key = { it }) { categoryResource ->
                val category = stringResource(categoryResource)
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
