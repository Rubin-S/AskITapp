package com.askit.app.explore

import com.askit.app.posttask.PostTaskBudget
import com.askit.app.posttask.PostTaskTimingMode
import java.math.BigDecimal

/**
 * Sort options each result tab can honestly support with the fields its results carry.
 *
 * - All mixes people and tasks in two sections, so it keeps the source order (no control).
 * - People: rating is the only comparable field (identity matches have no comparable price).
 * - Services: rating and listed price.
 * - Tasks: posting time, the requested date, and budget.
 *
 * [ExploreSortOption.Nearest] is offered nowhere yet: no result carries coordinates or a numeric
 * distance, so "nearest" would be a guess. Add it here once results carry a distance.
 */
internal fun exploreAvailableSortOptions(): Map<ExploreResultScope, List<ExploreSortOption>> = mapOf(
    ExploreResultScope.People to listOf(ExploreSortOption.BestMatch, ExploreSortOption.RatingHighToLow),
    ExploreResultScope.Services to listOf(
        ExploreSortOption.BestMatch,
        ExploreSortOption.RatingHighToLow,
        ExploreSortOption.PriceLowToHigh,
    ),
    ExploreResultScope.Tasks to listOf(
        ExploreSortOption.BestMatch,
        ExploreSortOption.Newest,
        ExploreSortOption.DueSoon,
        ExploreSortOption.PriceLowToHigh,
    ),
)

/** The option shown as selected for [scope]: the saved one when the tab supports it, else Best match. */
internal fun effectiveExploreSortOption(
    scope: ExploreResultScope,
    selected: Map<ExploreResultScope, ExploreSortOption>,
    available: Map<ExploreResultScope, List<ExploreSortOption>> = exploreAvailableSortOptions(),
): ExploreSortOption = selected[scope]?.takeIf { it in available[scope].orEmpty() } ?: ExploreSortOption.BestMatch

/**
 * Orders submitted [state] for [option]. Every sort is stable, so ties (and Best match) keep the
 * source's relevance order; results missing the sorted field go last rather than being dropped.
 */
internal fun sortExploreResults(state: ExploreResultState, option: ExploreSortOption): ExploreResultState {
    if (state !is ExploreResultState.Results || option == ExploreSortOption.BestMatch) return state
    return state.copy(people = sortPeople(state.people, option), tasks = sortTasks(state.tasks, option))
}

private fun sortPeople(people: List<ExplorePersonResult>, option: ExploreSortOption): List<ExplorePersonResult> = when (option) {
    ExploreSortOption.RatingHighToLow -> people.sortedWith(
        compareByDescending<ExplorePersonResult> { it.rating?.takeIf(Double::isFinite) != null }
            .thenByDescending { it.rating?.takeIf(Double::isFinite) ?: 0.0 }
            .thenByDescending { it.reviewCount },
    )

    ExploreSortOption.PriceLowToHigh -> people.sortedWith(nullsLastBy { it.listedPrice?.minimum })

    ExploreSortOption.BestMatch,
    ExploreSortOption.Nearest,
    ExploreSortOption.Newest,
    ExploreSortOption.DueSoon,
    -> people
}

private fun sortTasks(tasks: List<ExploreTaskResult>, option: ExploreSortOption): List<ExploreTaskResult> = when (option) {
    ExploreSortOption.Newest -> tasks.sortedWith(
        compareByDescending<ExploreTaskResult> { it.sourceTask != null }
            .thenByDescending { it.sourceTask?.createdAtMillis ?: 0L },
    )

    ExploreSortOption.DueSoon -> tasks.sortedWith(compareBy(::dueRank).thenBy(::dueAtMillis))

    ExploreSortOption.PriceLowToHigh -> tasks.sortedWith(nullsLastBy(ExploreTaskResult::lowestBudget))

    ExploreSortOption.BestMatch,
    ExploreSortOption.Nearest,
    ExploreSortOption.RatingHighToLow,
    -> tasks
}

/** ASAP first, then dated tasks, then flexible or unknown timing. */
private fun dueRank(task: ExploreTaskResult): Int {
    val details = task.sourceTask?.publicDetails ?: return 2
    return when (details.timingMode) {
        PostTaskTimingMode.ASAP -> 0
        PostTaskTimingMode.DATE -> if (details.selectedDateMillis != null) 1 else 2
        PostTaskTimingMode.FLEXIBLE -> 2
    }
}

private fun dueAtMillis(task: ExploreTaskResult): Long {
    val source = task.sourceTask ?: return Long.MAX_VALUE
    val details = source.publicDetails ?: return Long.MAX_VALUE
    return when (details.timingMode) {
        // Equal urgency: whoever asked first has waited longest.
        PostTaskTimingMode.ASAP -> source.createdAtMillis
        PostTaskTimingMode.DATE -> details.selectedDateMillis ?: Long.MAX_VALUE
        PostTaskTimingMode.FLEXIBLE -> Long.MAX_VALUE
    }
}

private fun ExploreTaskResult.lowestBudget(): BigDecimal? =
    when (val budget = sourceTask?.publicDetails?.budget ?: sourceTask?.listedBudget) {
        is PostTaskBudget.Fixed -> budget.amount.toBigDecimalOrNull()
        is PostTaskBudget.Range -> budget.minimum.toBigDecimalOrNull()
        else -> null
    }

private fun <T> nullsLastBy(selector: (T) -> BigDecimal?): Comparator<T> =
    compareBy<T> { selector(it) == null }.thenBy { selector(it) ?: BigDecimal.ZERO }
