package com.askit.app.explore

/**
 * What the Explore list shows for one result scope once filters, the price filter and scope
 * normalisation have been applied. Pure, so the screen only renders it.
 */
internal data class ExploreVisibleResults(
    val orderedAppliedFilters: List<ExploreFilterOption>,
    val priceFilter: ExplorePriceFilter,
    /** Raw results after filters, before scope normalisation (used by the browse sections). */
    val filteredResultState: ExploreResultState,
    /** Results normalised and sorted for the selected scope (used by submitted searches). */
    val submittedResultState: ExploreResultState,
    val hasVisibleSubmittedResults: Boolean,
    /** True when people existed but every one of them was filtered out. */
    val peopleHiddenByFilters: Boolean,
    /** True when tasks existed but every one of them was filtered out. */
    val tasksHiddenByFilters: Boolean,
) {
    val filteredPeople: List<ExplorePersonResult>
        get() = (filteredResultState as? ExploreResultState.Results)?.people.orEmpty()

    val filteredTasks: List<ExploreTaskResult>
        get() = (filteredResultState as? ExploreResultState.Results)?.tasks.orEmpty()
}

internal fun deriveExploreVisibleResults(
    selectedScope: ExploreResultScope,
    resultState: ExploreResultState,
    availableFilterOptions: Map<ExploreResultScope, List<ExploreFilterOption>>,
    appliedFilterOptions: Map<ExploreResultScope, Set<ExploreFilterOption>>,
    priceFilters: Map<ExploreResultScope, ExplorePriceFilter>,
    sortOption: ExploreSortOption = ExploreSortOption.BestMatch,
): ExploreVisibleResults {
    val orderedAppliedFilters = normalizeAvailableExploreFilterOptions(
        scope = selectedScope,
        options = availableFilterOptions[selectedScope].orEmpty(),
    ).filter { it in appliedFilterOptions[selectedScope].orEmpty() }
    val priceFilter = priceFilters[selectedScope] ?: ExplorePriceFilter()
    val filteredResultState = filterExploreResults(resultState, orderedAppliedFilters.toSet(), priceFilter)
    val submittedResultState = normalizeExploreResultState(
        state = filteredResultState,
        scope = selectedScope,
        emptyReason = if (orderedAppliedFilters.isEmpty() && !priceFilter.isActive) {
            ExploreResultState.EmptyReason.Query
        } else {
            ExploreResultState.EmptyReason.Filters
        },
    ).let { sortExploreResults(it, sortOption) }
    val submittedResults = submittedResultState as? ExploreResultState.Results
    val hasVisibleSubmittedResults = when (selectedScope) {
        ExploreResultScope.All -> false

        ExploreResultScope.People,
        ExploreResultScope.Services,
        -> submittedResults?.people?.isNotEmpty() == true

        ExploreResultScope.Tasks -> submittedResults?.tasks?.isNotEmpty() == true
    }
    val raw = resultState as? ExploreResultState.Results
    val filtered = filteredResultState as? ExploreResultState.Results
    return ExploreVisibleResults(
        orderedAppliedFilters = orderedAppliedFilters,
        priceFilter = priceFilter,
        filteredResultState = filteredResultState,
        submittedResultState = submittedResultState,
        hasVisibleSubmittedResults = hasVisibleSubmittedResults,
        peopleHiddenByFilters = raw?.people?.isNotEmpty() == true && filtered?.people?.isEmpty() == true,
        tasksHiddenByFilters = raw?.tasks?.isNotEmpty() == true && filtered?.tasks?.isEmpty() == true,
    )
}

/** Per-scope signature of the applied filters; a change resets that scope's scroll position. */
internal fun exploreFilterKeys(
    availableFilterOptions: Map<ExploreResultScope, List<ExploreFilterOption>>,
    appliedFilterOptions: Map<ExploreResultScope, Set<ExploreFilterOption>>,
    priceFilters: Map<ExploreResultScope, ExplorePriceFilter>,
): Map<ExploreResultScope, String> = ExploreResultScope.entries.associateWith { scope ->
    normalizeAppliedExploreFilterOptions(
        scope = scope,
        availableOptions = availableFilterOptions[scope].orEmpty(),
        appliedOptions = appliedFilterOptions[scope].orEmpty(),
    ).map(ExploreFilterOption::name).sorted().joinToString(",") + priceFilters[scope].toString()
}

/** Per-scope signature of the selected sort; a change resets that scope's scroll position. */
internal fun exploreSortKeys(selectedSortOptions: Map<ExploreResultScope, ExploreSortOption>): Map<ExploreResultScope, String> =
    ExploreResultScope.entries.associateWith { scope ->
        selectedSortOptions[scope]?.name.orEmpty()
    }

internal fun Map<ExploreResultScope, String>.joinedScopeKey(): String =
    entries.joinToString("|") { (scope, value) -> "${scope.name}=$value" }
