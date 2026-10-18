package com.askit.app.explore

import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import java.util.Locale

internal fun matchingRecentSearches(
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

internal fun matchingServiceCategories(
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

internal fun normalizedMatchValue(value: String): String =
    normalizeExploreQuery(value).lowercase(Locale.ROOT)

internal fun predictiveCategoryText(
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
