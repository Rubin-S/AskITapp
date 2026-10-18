package com.askit.app.explore

import androidx.annotation.StringRes
import com.askit.app.R
import com.askit.app.posttask.PostTaskBudget
import java.math.BigDecimal

enum class ExploreRateUnit(@StringRes val labelRes: Int) {
    Hour(R.string.explore_price_hour),
    Visit(R.string.explore_price_visit),
    Service(R.string.explore_price_service),
    Fixed(R.string.explore_price_fixed),
}

/** Numeric public pricing, independent of the localized display label. */
data class ExploreListedPrice(val minimum: BigDecimal, val maximum: BigDecimal, val unit: ExploreRateUnit)

data class ExplorePriceFilter(val minimum: String = "", val maximum: String = "", val unit: ExploreRateUnit? = null) {
    val hasBounds: Boolean get() = minimum.isNotBlank() || maximum.isNotBlank()
    val isActive: Boolean get() = hasBounds || unit != null
    val isValid: Boolean get() {
        fun valid(value: String) = value.isBlank() ||
            (value.matches(Regex("[0-9]{1,9}(\\.[0-9]{1,2})?")) && value.toBigDecimalOrNull() != null)
        if (!valid(minimum) || !valid(maximum)) return false
        val low = minimum.toBigDecimalOrNull()
        val high = maximum.toBigDecimalOrNull()
        return low == null || high == null || low <= high
    }

    // Range overlap includes listings whose advertised budget/rate can fit the requested range.
    fun matchesRange(low: BigDecimal?, high: BigDecimal?): Boolean {
        if (!isValid) return false
        if (!hasBounds) return true
        if (low == null || high == null || low < BigDecimal.ZERO || high < low) return false
        return (minimum.toBigDecimalOrNull()?.let { high >= it } ?: true) &&
            (maximum.toBigDecimalOrNull()?.let { low <= it } ?: true)
    }
}

internal fun filterExploreResults(
    state: ExploreResultState,
    options: Set<ExploreFilterOption>,
    price: ExplorePriceFilter,
): ExploreResultState {
    if (state !is ExploreResultState.Results) return state
    val rating = when {
        ExploreFilterOption.RatingFourPointFivePlus in options -> 4.5
        ExploreFilterOption.RatingFourPlus in options -> 4.0
        else -> null
    }
    val reviews = when {
        ExploreFilterOption.ReviewsFiftyPlus in options -> 50
        ExploreFilterOption.ReviewsTenPlus in options -> 10
        else -> null
    }
    return state.copy(
        people = state.people.filter { person ->
            (rating == null || person.rating?.let { it.isFinite() && it >= rating } == true) &&
                (reviews == null || person.reviewCount >= reviews) &&
                (price.unit == null || person.listedPrice?.unit == price.unit) &&
                price.matchesRange(person.listedPrice?.minimum, person.listedPrice?.maximum)
        },
        tasks = state.tasks.filter { task ->
            val details = task.sourceTask?.publicDetails
            val budget = details?.budget ?: task.sourceTask?.listedBudget
            val low = when (budget) {
                is PostTaskBudget.Fixed -> budget.amount.toBigDecimalOrNull()
                is PostTaskBudget.Range -> budget.minimum.toBigDecimalOrNull()
                else -> null
            }
            val high = when (budget) {
                is PostTaskBudget.Fixed -> low
                is PostTaskBudget.Range -> budget.maximum.toBigDecimalOrNull()
                else -> null
            }
            price.matchesRange(low, high)
        },
    )
}
