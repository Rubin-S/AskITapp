package com.askit.app.explore

import com.askit.designsystem.inputs.AskITInputDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.askit.app.R
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ExploreHeader(
    query: String,
    searchArea: ExploreSearchArea,
    textFieldState: TextFieldState,
    searchBarState: SearchBarState,
    inputTransformation: InputTransformation,
    onQueryCleared: () -> Unit,
    onQuerySubmitted: (String) -> Unit,
    onSearchFiltersClick: () -> Unit,
    onSearchAreaClick: () -> Unit,
    selectedScope: ExploreResultScope,
    priceFilterActive: Boolean,
    availableFilterOptions: Map<ExploreResultScope, List<ExploreFilterOption>>,
    appliedFilterOptions: Map<ExploreResultScope, Set<ExploreFilterOption>>,
    onCloseSearch: () -> Unit,
    recentSearches: List<String>,
    onRecentSearchSelected: (String) -> Unit,
    onRecentSearchRemoved: (String) -> Unit,
    onRecentSearchesCleared: () -> Unit,
    categories: List<String>,
    onCategorySelected: (String) -> Unit,
) {
    val appliedFilterCount = normalizeAppliedExploreFilterOptions(
        scope = selectedScope,
        availableOptions = availableFilterOptions[selectedScope].orEmpty(),
        appliedOptions = appliedFilterOptions[selectedScope].orEmpty(),
    ).size + if (priceFilterActive) 1 else 0
    val filterContentDescription = if (appliedFilterCount == 0) {
        stringResource(R.string.explore_search_filters_content_description)
    } else {
        pluralStringResource(
            R.plurals.explore_search_filters_applied_content_description,
            appliedFilterCount,
            appliedFilterCount,
        )
    }

    val context = LocalContext.current
    val inputQuery = textFieldState.text.toString()
    val isSearchActive = searchBarState.currentValue == SearchBarValue.Expanded
    val inputFieldColors = SearchBarDefaults.inputFieldColors(
        focusedContainerColor = AskITInputDefaults.containerColor,
        unfocusedContainerColor = AskITInputDefaults.containerColor,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val searchBarColors = SearchBarDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surface,
        dividerColor = MaterialTheme.colorScheme.outlineVariant,
        inputFieldColors = inputFieldColors,
    )
    val headerScope = rememberCoroutineScope()
    fun inputField(isExpandedField: Boolean): @Composable () -> Unit = {
        val isVisible = isExpandedField == isSearchActive
        val leadingContent: @Composable () -> Unit = {
            if (isExpandedField) {
                IconButton(onClick = onCloseSearch) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.explore_back),
                        modifier = Modifier.size(24.dp),
                    )
                }
            } else {
                val radius = pluralStringResource(R.plurals.explore_radius_km, searchArea.radiusKm, searchArea.radiusKm)
                val areaDescription = stringResource(
                    R.string.explore_change_area_description, searchArea.displayName, radius,
                )
                TextButton(
                    onClick = onSearchAreaClick,
                    modifier = Modifier.heightIn(min = 48.dp).widthIn(max = 120.dp)
                        .testTag("explore_location_action")
                        .semantics { contentDescription = areaDescription },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_location_on),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        searchArea.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
            }
        }
        val trailingContent: @Composable () -> Unit = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (inputQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            textFieldState.setTextAndPlaceCursorAtEnd("")
                            onQueryCleared()
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_clear),
                            contentDescription = stringResource(R.string.explore_clear_search),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                BadgedBox(
                    badge = {
                        if (appliedFilterCount > 0) {
                            Badge(
                                modifier = Modifier.clearAndSetSemantics {},
                            ) {
                                Text(
                                    text = appliedFilterCount.toString(),
                                    modifier = Modifier.clearAndSetSemantics {},
                                )
                            }
                        }
                    },
                ) {
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
                        IconButton(
                            onClick = onSearchFiltersClick,
                            modifier = Modifier
                                .size(48.dp)
                                .then(
                                    if (isVisible) {
                                        Modifier.testTag("explore_search_filter_action")
                                    } else {
                                        Modifier.clearAndSetSemantics {}
                                    },
                                ),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_filter_list),
                                contentDescription = filterContentDescription,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }
        }
        if (isExpandedField) {
            SearchBarDefaults.InputField(
                textFieldState,
                searchBarState,
                onSearch = {
                    onQuerySubmitted(it)
                    onCloseSearch()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isVisible) {
                            Modifier.testTag("explore_search_field")
                        } else {
                            Modifier.clearAndSetSemantics {}
                        },
                    )
                    .border(
                        width = if (isExpandedField) 2.dp else 1.dp,
                        color = if (isExpandedField) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = AskITInputDefaults.Shape,
                    ),
                shape = AskITInputDefaults.Shape,
                colors = inputFieldColors,
                placeholder = {
                    Text(
                        text = stringResource(if (isExpandedField) R.string.explore_search_placeholder else R.string.explore_search_short_placeholder),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                leadingIcon = leadingContent,
                trailingIcon = trailingContent,
                inputTransformation = inputTransformation,
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AskITInputDefaults.Shape,
                color = AskITInputDefaults.containerColor,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    leadingContent()
                    Box(
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp)
                            .clickable(role = Role.Button) {
                                headerScope.launch { searchBarState.animateToExpanded() }
                            }
                            .testTag("explore_search_field")
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            inputQuery.ifEmpty { stringResource(R.string.explore_search_short_placeholder) },
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (inputQuery.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    trailingContent()
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        SearchBar(
            state = searchBarState,
            inputField = inputField(false),
            modifier = Modifier.fillMaxWidth(),
            shape = AskITInputDefaults.Shape,
            colors = searchBarColors,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        )
    }

    ExpandedFullScreenSearchBar(
        state = searchBarState,
        inputField = inputField(true),
        colors = searchBarColors,
    ) {
        CompositionLocalProvider(LocalContext provides context) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                if (normalizeExploreQuery(query).isEmpty()) {
                    ActiveSearchContent(
                        recentSearches = recentSearches,
                        onRecentSearchSelected = onRecentSearchSelected,
                        onRecentSearchRemoved = onRecentSearchRemoved,
                        onRecentSearchesCleared = onRecentSearchesCleared,
                        onCategorySelected = onCategorySelected,
                    )
                } else {
                    TypedSearchSuggestions(
                        query = normalizeExploreQuery(query),
                        recentSearches = recentSearches,
                        categories = categories,
                        onSuggestionSelected = { suggestion ->
                            onQuerySubmitted(suggestion)
                            onCloseSearch()
                        },
                    )
                }
            }
        }
    }
}
