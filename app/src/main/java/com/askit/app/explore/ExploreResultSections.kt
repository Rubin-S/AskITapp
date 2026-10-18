package com.askit.app.explore

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.designsystem.navigation.AskITSectionTabs
import com.askit.designsystem.navigation.SectionTab

@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.addSubmittedSearchResults(
    selectedScope: ExploreResultScope,
    normalizedResultState: ExploreResultState,
    orderedAppliedFilters: List<ExploreFilterOption>,
    hasVisibleResults: Boolean,
    onRetryResults: (() -> Unit)?,
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
            AskITSectionTabs(
                tabs = ExploreResultScope.entries.map { SectionTab(it.name, stringResource(it.labelRes)) },
                selectedId = selectedScope.name,
                onSelect = { id -> onScopeSelected(ExploreResultScope.entries.first { it.name == id }) },
                modifier = Modifier.testTag("explore_result_tabs"),
                tabTagPrefix = "explore_scope_",
            )
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
    val selectedOption = effectiveExploreSortOption(selectedScope, selectedSortOptions, availableSortOptions)
    if (
        hasVisibleResults &&
        options.size >= 2 &&
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

internal fun LazyListScope.addBrowseResultSections(
    peopleFiltered: Boolean = false,
    tasksFiltered: Boolean = false,
    people: List<ExplorePersonResult>,
    tasks: List<ExploreTaskResult>,
    browseState: ExploreBrowseState,
    onPersonClick: ((String) -> Unit)?,
    onTaskClick: ((String) -> Unit)?,
    onChangeArea: () -> Unit,
    onRetryBrowseSection: ((ExploreBrowseSection) -> Unit)?,
    onViewMorePeople: (() -> Unit)? = null,
    onViewMoreTasks: (() -> Unit)? = null,
) {
    addPersonResultSection(
        sectionKey = "browse:people",
        filteredEmpty = peopleFiltered,
        heading = R.string.explore_nearby_professionals,
        sectionTag = "explore_nearby_professionals",
        people = people,
        onClick = onPersonClick,
        browseStatus = browseState.professionals,
        onBrowseChangeArea = onChangeArea,
        onBrowseRetry = onRetryBrowseSection?.let { retry ->
            { retry(ExploreBrowseSection.Professionals) }
        },
        onViewMore = onViewMorePeople,
    )
    addTaskResultSection(
        sectionKey = "browse:tasks",
        filteredEmpty = tasksFiltered,
        heading = R.string.explore_open_tasks_nearby,
        sectionTag = "explore_open_tasks_nearby",
        tasks = tasks,
        onClick = onTaskClick,
        browseStatus = browseState.tasks,
        onBrowseChangeArea = onChangeArea,
        onBrowseRetry = onRetryBrowseSection?.let { retry ->
            { retry(ExploreBrowseSection.Tasks) }
        },
        onViewMore = onViewMoreTasks,
    )
}

private fun LazyListScope.addExploreResultBody(
    selectedScope: ExploreResultScope,
    state: ExploreResultState,
    onRetryResults: (() -> Unit)?,
    onEditFilters: () -> Unit,
    onPersonClick: ((String) -> Unit)?,
    onTaskClick: ((String) -> Unit)?,
) {
    when (state) {
        ExploreResultState.Loading -> {
            item(key = "body:loading", contentType = "progress") {
                ExploreLoadingResultBody(selectedScope)
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
    filteredEmpty: Boolean = false,
    browseStatus: ExploreBrowseStatus? = null,
    onBrowseChangeArea: (() -> Unit)? = null,
    onBrowseRetry: (() -> Unit)? = null,
    onViewMore: (() -> Unit)? = null,
) {
    val uniquePeople = distinctPeopleById(people)
    if (uniquePeople.isEmpty() && browseStatus == null) return

    if (heading != null) {
        item(key = "section:$sectionKey", contentType = "section") {
            ExploreResultSectionHeading(
                heading = stringResource(heading),
                testTag = sectionTag,
                onViewMore = onViewMore.takeIf { uniquePeople.isNotEmpty() },
            )
        }
    }
    if (uniquePeople.isEmpty()) {
        item(
            key = "$sectionKey:state:${browseStatus?.name}",
            contentType = "browse-state",
        ) {
            if (filteredEmpty) {
                ExploreEmptyResultBody(ExploreResultState.EmptyReason.Filters, onBrowseChangeArea ?: {})
            } else {
                BrowseSectionStateCard(
                    section = ExploreBrowseSection.Professionals,
                    status = browseStatus ?: ExploreBrowseStatus.Empty,
                    onChangeArea = onBrowseChangeArea,
                    onRetry = onBrowseRetry,
                )
            }
        }
        return
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
    filteredEmpty: Boolean = false,
    browseStatus: ExploreBrowseStatus? = null,
    onBrowseChangeArea: (() -> Unit)? = null,
    onBrowseRetry: (() -> Unit)? = null,
    onViewMore: (() -> Unit)? = null,
) {
    val uniqueTasks = distinctTasksById(tasks)
    if (uniqueTasks.isEmpty() && browseStatus == null) return

    if (heading != null) {
        item(key = "section:$sectionKey", contentType = "section") {
            ExploreResultSectionHeading(
                heading = stringResource(heading),
                testTag = sectionTag,
                onViewMore = onViewMore.takeIf { uniqueTasks.isNotEmpty() },
            )
        }
    }
    if (uniqueTasks.isEmpty()) {
        item(
            key = "$sectionKey:state:${browseStatus?.name}",
            contentType = "browse-state",
        ) {
            if (filteredEmpty) {
                ExploreEmptyResultBody(ExploreResultState.EmptyReason.Filters, onBrowseChangeArea ?: {})
            } else {
                BrowseSectionStateCard(
                    section = ExploreBrowseSection.Tasks,
                    status = browseStatus ?: ExploreBrowseStatus.Empty,
                    onChangeArea = onBrowseChangeArea,
                    onRetry = onBrowseRetry,
                )
            }
        }
        return
    }
    addTaskResultRows(
        itemKeyPrefix = sectionKey,
        tasks = uniqueTasks,
        onClick = onClick,
        firstRowTag = sectionTag.takeIf { heading == null },
        addTopPadding = heading == null,
    )
}
