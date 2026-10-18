package com.askit.app.explore

import androidx.annotation.StringRes
import com.askit.app.R

enum class ExploreFilterOption {
    RatingFourPlus,
    RatingFourPointFivePlus,
    ReviewsTenPlus,
    ReviewsFiftyPlus,
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
    Reviews(R.string.explore_filter_group_reviews),
    WorkLocation(R.string.explore_filter_group_work_location),
    Availability(R.string.explore_filter_group_availability),
    Status(R.string.explore_filter_group_status),
    Needed(R.string.explore_filter_group_needed),
}

internal fun ExploreFilterOption.labelRes(): Int = when (this) {
    ExploreFilterOption.RatingFourPlus -> R.string.explore_filter_rating_four_plus
    ExploreFilterOption.RatingFourPointFivePlus -> R.string.explore_filter_rating_four_point_five_plus
    ExploreFilterOption.ReviewsTenPlus -> R.string.explore_filter_reviews_ten_plus
    ExploreFilterOption.ReviewsFiftyPlus -> R.string.explore_filter_reviews_fifty_plus
    ExploreFilterOption.Remote -> R.string.explore_filter_remote
    ExploreFilterOption.AvailableToday -> R.string.explore_filter_available_today
    ExploreFilterOption.AvailableThisWeek -> R.string.explore_filter_available_this_week
    ExploreFilterOption.TaskOpen -> R.string.explore_filter_task_open
    ExploreFilterOption.TaskApplied -> R.string.explore_filter_task_applied
    ExploreFilterOption.NeededToday -> R.string.explore_filter_needed_today
    ExploreFilterOption.NeededThisWeek -> R.string.explore_filter_needed_this_week
}

internal fun ExploreFilterOption.groupFor(scope: ExploreResultScope): ExploreFilterGroup? = when (this) {
    ExploreFilterOption.RatingFourPlus, ExploreFilterOption.RatingFourPointFivePlus ->
        ExploreFilterGroup.Rating.takeUnless { scope == ExploreResultScope.Tasks }
    ExploreFilterOption.ReviewsTenPlus, ExploreFilterOption.ReviewsFiftyPlus ->
        ExploreFilterGroup.Reviews.takeUnless { scope == ExploreResultScope.Tasks }
    ExploreFilterOption.Remote -> ExploreFilterGroup.WorkLocation.takeUnless { scope == ExploreResultScope.People }
    ExploreFilterOption.AvailableToday, ExploreFilterOption.AvailableThisWeek ->
        ExploreFilterGroup.Availability.takeIf { scope == ExploreResultScope.Services || scope == ExploreResultScope.All }
    ExploreFilterOption.TaskOpen, ExploreFilterOption.TaskApplied ->
        ExploreFilterGroup.Status.takeIf { scope == ExploreResultScope.Tasks || scope == ExploreResultScope.All }
    ExploreFilterOption.NeededToday, ExploreFilterOption.NeededThisWeek ->
        ExploreFilterGroup.Needed.takeIf { scope == ExploreResultScope.Tasks || scope == ExploreResultScope.All }
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
    .distinctBy { it.groupFor(scope) }
    .toSet()

internal fun defaultExploreFilterOptions(): Map<ExploreResultScope, List<ExploreFilterOption>> =
    ExploreResultScope.entries.associateWith { scope ->
        normalizeAvailableExploreFilterOptions(scope, listOf(
            ExploreFilterOption.RatingFourPlus,
            ExploreFilterOption.RatingFourPointFivePlus,
            ExploreFilterOption.ReviewsTenPlus,
            ExploreFilterOption.ReviewsFiftyPlus,
        ))
    }
