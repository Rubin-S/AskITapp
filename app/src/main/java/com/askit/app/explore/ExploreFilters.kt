package com.askit.app.explore

import androidx.annotation.StringRes
import com.askit.app.R

enum class ExploreFilterOption {
    RatingFourPlus,
    Remote,
    AvailableToday,
    AvailableThisWeek,
    TaskOpen,
    TaskApplied,
    NeededToday,
    NeededThisWeek,
}

internal enum class ExploreFilterGroup(@StringRes val labelRes: Int) {
    Rating(R.string.explore_filter_group_rating),
    WorkLocation(R.string.explore_filter_group_work_location),
    Availability(R.string.explore_filter_group_availability),
    Status(R.string.explore_filter_group_status),
    Needed(R.string.explore_filter_group_needed),
}

internal fun ExploreFilterOption.labelRes(): Int = when (this) {
    ExploreFilterOption.RatingFourPlus -> R.string.explore_filter_rating_four_plus
    ExploreFilterOption.Remote -> R.string.explore_filter_remote
    ExploreFilterOption.AvailableToday -> R.string.explore_filter_available_today
    ExploreFilterOption.AvailableThisWeek -> R.string.explore_filter_available_this_week
    ExploreFilterOption.TaskOpen -> R.string.explore_filter_task_open
    ExploreFilterOption.TaskApplied -> R.string.explore_filter_task_applied
    ExploreFilterOption.NeededToday -> R.string.explore_filter_needed_today
    ExploreFilterOption.NeededThisWeek -> R.string.explore_filter_needed_this_week
}

internal fun ExploreFilterOption.groupFor(scope: ExploreResultScope): ExploreFilterGroup? = when (scope) {
    ExploreResultScope.All,
    ExploreResultScope.People,
    -> null

    ExploreResultScope.Services -> when (this) {
        ExploreFilterOption.RatingFourPlus -> ExploreFilterGroup.Rating
        ExploreFilterOption.Remote -> ExploreFilterGroup.WorkLocation
        ExploreFilterOption.AvailableToday,
        ExploreFilterOption.AvailableThisWeek,
        -> ExploreFilterGroup.Availability

        ExploreFilterOption.TaskOpen,
        ExploreFilterOption.TaskApplied,
        ExploreFilterOption.NeededToday,
        ExploreFilterOption.NeededThisWeek,
        -> null
    }

    ExploreResultScope.Tasks -> when (this) {
        ExploreFilterOption.Remote -> ExploreFilterGroup.WorkLocation
        ExploreFilterOption.TaskOpen,
        ExploreFilterOption.TaskApplied,
        -> ExploreFilterGroup.Status
        ExploreFilterOption.NeededToday,
        ExploreFilterOption.NeededThisWeek,
        -> ExploreFilterGroup.Needed

        ExploreFilterOption.RatingFourPlus,
        ExploreFilterOption.AvailableToday,
        ExploreFilterOption.AvailableThisWeek,
        -> null
    }
}

internal fun normalizeAvailableExploreFilterOptions(
    scope: ExploreResultScope,
    options: List<ExploreFilterOption>,
): List<ExploreFilterOption> = options
    .asSequence()
    .filter { it.groupFor(scope) != null }
    .distinct()
    .toList()

internal fun normalizeAppliedExploreFilterOptions(
    scope: ExploreResultScope,
    availableOptions: List<ExploreFilterOption>,
    appliedOptions: Set<ExploreFilterOption>,
): Set<ExploreFilterOption> = normalizeAvailableExploreFilterOptions(scope, availableOptions)
    .filter { it in appliedOptions }
    .toSet()

internal fun defaultExploreFilterOptions(): Map<ExploreResultScope, List<ExploreFilterOption>> = mapOf(
    ExploreResultScope.Services to listOf(
        ExploreFilterOption.RatingFourPlus,
        ExploreFilterOption.Remote,
        ExploreFilterOption.AvailableToday,
        ExploreFilterOption.AvailableThisWeek,
    ),
    ExploreResultScope.Tasks to listOf(
        ExploreFilterOption.Remote,
        ExploreFilterOption.TaskOpen,
        ExploreFilterOption.TaskApplied,
        ExploreFilterOption.NeededToday,
        ExploreFilterOption.NeededThisWeek,
    ),
)
