package com.askit.app.explore

import androidx.annotation.StringRes
import com.askit.app.R
import com.askit.app.category.ASKIT_SERVICE_CATEGORIES
import com.askit.designsystem.tasks.TaskResultStatus

internal val EXPLORE_CATEGORIES = ASKIT_SERVICE_CATEGORIES

data class ExplorePersonResult(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val primaryService: String?,
    val additionalServices: List<String>,
    val rating: Double?,
    val reviewCount: Int,
    val locationLabel: String,
    val priceLabel: String?,
    val statusLabel: String?,
    val matchReasons: Set<PersonMatchReason> = emptySet(),
    val listedPrice: ExploreListedPrice? = null,
)

enum class PersonMatchReason {
    Identity,
    Service,
}

enum class ExploreResultScope(@StringRes val labelRes: Int) {
    All(R.string.explore_all),
    People(R.string.explore_people),
    Services(R.string.explore_services),
    Tasks(R.string.explore_tasks),
}

enum class ExploreSortOption(@StringRes val labelRes: Int) {
    BestMatch(R.string.explore_sort_best_match),
    Nearest(R.string.explore_sort_nearest),
    RatingHighToLow(R.string.explore_sort_rating_high_to_low),
    Newest(R.string.explore_sort_newest),
    DueSoon(R.string.explore_sort_due_soon),
    PriceLowToHigh(R.string.explore_sort_price_low_to_high),
}

data class ExploreTaskResult(
    val id: String,
    val title: String,
    val category: String,
    val summary: String?,
    val budgetLabel: String,
    val locationLabel: String,
    val timingLabel: String,
    val posterName: String,
    val postedLabel: String,
    val status: TaskResultStatus,
    val photoModels: List<Any> = emptyList(),
    val distanceLabel: String? = null,
    val scopeHighlights: List<String> = emptyList(),
    val sourceTask: com.askit.app.task.Task? = null,
)

internal data class ExplorePersonRenderRow(val result: ExplorePersonResult, val stableId: String, val uiKey: String)

internal data class ExploreTaskRenderRow(val result: ExploreTaskResult, val stableId: String, val uiKey: String)
