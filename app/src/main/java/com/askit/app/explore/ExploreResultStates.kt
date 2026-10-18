package com.askit.app.explore

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.InputChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.askit.app.R

@Composable
internal fun ExploreLoadingResultBody(selectedScope: ExploreResultScope) {
    val loadingDescription = stringResource(
        R.string.explore_loading_results_content_description,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
            .testTag("explore_result_loading"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.explore_loading_results),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.semantics {
                contentDescription = loadingDescription
            },
        )
        when (selectedScope) {
            ExploreResultScope.All -> {
                BrowseSectionSkeleton(
                    section = ExploreBrowseSection.Professionals,
                    tagPrefix = "explore_result",
                )
                BrowseSectionSkeleton(
                    section = ExploreBrowseSection.Tasks,
                    tagPrefix = "explore_result",
                )
            }

            ExploreResultScope.People,
            ExploreResultScope.Services,
            -> BrowseSectionSkeleton(
                section = ExploreBrowseSection.Professionals,
                tagPrefix = "explore_result",
            )

            ExploreResultScope.Tasks -> BrowseSectionSkeleton(
                section = ExploreBrowseSection.Tasks,
                tagPrefix = "explore_result",
            )
        }
    }
}

private data class ExploreResultStateCopy(
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val supportingTextRes: Int,
)

@Composable
private fun ExploreResultStateCard(
    testTag: String,
    iconRes: Int,
    titleRes: Int,
    supportingTextRes: Int,
    containerColor: Color,
    contentColor: Color,
    supportingTextColor: Color,
    iconTint: Color,
    actionLabelRes: Int? = null,
    actionTestTag: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
            .testTag(testTag)
            .semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(64.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(titleRes),
                    color = contentColor,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(supportingTextRes),
                    color = supportingTextColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (actionLabelRes != null && onAction != null) {
                    TextButton(
                        onClick = onAction,
                        modifier = actionTestTag?.let(Modifier::testTag) ?: Modifier,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = contentColor,
                        ),
                    ) {
                        Text(stringResource(actionLabelRes))
                    }
                }
            }
        }
    }
}

@Composable
internal fun ExploreEmptyResultBody(
    reason: ExploreResultState.EmptyReason,
    onEditFilters: () -> Unit,
) {
    val isFiltered = reason == ExploreResultState.EmptyReason.Filters
    ExploreResultStateCard(
        testTag = "explore_result_empty",
        iconRes = R.drawable.explore_results_empty,
        titleRes = if (isFiltered) {
            R.string.explore_empty_filters_title
        } else {
            R.string.explore_empty_query_title
        },
        supportingTextRes = if (isFiltered) {
            R.string.explore_empty_filters_supporting_text
        } else {
            R.string.explore_empty_query_supporting_text
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        supportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
        actionLabelRes = R.string.explore_edit_filters.takeIf { isFiltered },
        actionTestTag = "explore_edit_filters".takeIf { isFiltered },
        onAction = onEditFilters.takeIf { isFiltered },
    )
}

@Composable
internal fun ExploreFailureResultBody(
    reason: ExploreResultState.FailureReason,
    onRetryResults: (() -> Unit)?,
) {
    val copy = when (reason) {
        ExploreResultState.FailureReason.General -> {
            ExploreResultStateCopy(
                iconRes = R.drawable.explore_results_error,
                titleRes = R.string.explore_failure_general_title,
                supportingTextRes = R.string.explore_failure_general_supporting_text,
            )
        }

        ExploreResultState.FailureReason.Offline -> {
            ExploreResultStateCopy(
                iconRes = R.drawable.explore_results_offline,
                titleRes = R.string.explore_failure_offline_title,
                supportingTextRes = R.string.explore_failure_offline_supporting_text,
            )
        }

        is ExploreResultState.FailureReason.SourceUnavailable -> when (reason.source) {
            ExploreResultState.Source.PeopleAndServices -> {
                ExploreResultStateCopy(
                    iconRes = R.drawable.explore_results_server,
                    titleRes = R.string.explore_failure_people_services_title,
                    supportingTextRes = R.string.explore_failure_people_services_supporting_text,
                )
            }

            ExploreResultState.Source.Tasks -> {
                ExploreResultStateCopy(
                    iconRes = R.drawable.explore_results_server,
                    titleRes = R.string.explore_failure_tasks_title,
                    supportingTextRes = R.string.explore_failure_tasks_supporting_text,
                )
            }
        }
    }
    val isOffline = reason == ExploreResultState.FailureReason.Offline
    ExploreResultStateCard(
        testTag = "explore_result_failure",
        iconRes = copy.iconRes,
        titleRes = copy.titleRes,
        supportingTextRes = copy.supportingTextRes,
        containerColor = if (isOffline) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        },
        contentColor = if (isOffline) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            MaterialTheme.colorScheme.onErrorContainer
        },
        supportingTextColor = if (isOffline) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            MaterialTheme.colorScheme.onErrorContainer
        },
        iconTint = if (isOffline) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.error
        },
        actionLabelRes = R.string.explore_retry,
        actionTestTag = "explore_retry_results",
        onAction = onRetryResults,
    )
}

@Composable
internal fun ExploreResultStatus(
    status: ExploreResultState.ContentStatus,
    actionEnabled: Boolean,
    onAction: (() -> Unit)?,
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
            if (actionEnabled && onAction != null) {
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
internal fun AppliedFilterChips(
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
internal fun ExploreSortControl(
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
