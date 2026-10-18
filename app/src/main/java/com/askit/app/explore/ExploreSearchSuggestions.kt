package com.askit.app.explore

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.askit.app.R
import java.util.Locale

@Composable
internal fun TypedSearchSuggestions(
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
private fun SuggestionRow(text: AnnotatedString, @DrawableRes iconRes: Int, actionLabel: String, onClick: () -> Unit) {
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

@Composable
internal fun RecentSearches(
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
private fun RecentSearchRow(query: String, onSelect: () -> Unit, onRemove: () -> Unit) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SuggestedCategories(onCategorySelected: (String) -> Unit) {
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
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("explore_suggested_categories_flow"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EXPLORE_CATEGORIES.forEach { categoryDefinition ->
                val category = stringResource(categoryDefinition.labelRes)
                SuggestionChip(
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                )
            }
        }
    }
}
