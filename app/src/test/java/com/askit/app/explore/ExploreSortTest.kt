package com.askit.app.explore

import com.askit.app.posttask.PostTaskBudget
import com.askit.app.posttask.PostTaskTimingMode
import com.askit.app.posttask.PostTaskWorkMode
import com.askit.app.task.Task
import com.askit.app.task.TaskProviderPreferences
import com.askit.app.task.TaskPublicDetails
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExploreSortTest {
    private fun priced(id: String, minimum: String?) = person(id).copy(
        listedPrice = minimum?.let { ExploreListedPrice(BigDecimal(it), BigDecimal(it), ExploreRateUnit.Hour) },
    )

    private fun sourceTask(
        id: String,
        createdAt: Long = 0L,
        timing: PostTaskTimingMode? = null,
        dateMillis: Long? = null,
        budget: PostTaskBudget? = null,
    ) = task(id).copy(
        sourceTask = Task(
            id = id,
            title = id,
            category = "Cleaning",
            budget = "",
            location = "",
            posterName = "Poster",
            urgency = "",
            postedTime = "",
            description = "",
            createdAtMillis = createdAt,
            publicDetails = timing?.let {
                TaskPublicDetails(
                    workMode = PostTaskWorkMode.AT_MY_LOCATION,
                    timingMode = it,
                    selectedDateMillis = dateMillis,
                    budget = budget,
                    itemDetails = null,
                    preferences = TaskProviderPreferences(),
                )
            },
            listedBudget = budget,
        ),
    )

    private fun ExploreResultState.peopleIds() = (this as ExploreResultState.Results).people.map { it.id }
    private fun ExploreResultState.taskIds() = (this as ExploreResultState.Results).tasks.map { it.id }

    @Test fun bestMatchKeepsTheSourceOrder() {
        val state = ExploreResultState.Results(listOf(person("b"), person("a")), listOf(task("2"), task("1")))
        assertEquals(state, sortExploreResults(state, ExploreSortOption.BestMatch))
    }

    @Test fun ratingSortsHighToLowWithReviewTieBreakAndUnratedLast() {
        val state = ExploreResultState.Results(
            people = listOf(
                person("unrated", rating = null),
                person("four", rating = 4.0, reviewCount = 3),
                person("fiveFew", rating = 5.0, reviewCount = 2),
                person("nan", rating = Double.NaN),
                person("fiveMany", rating = 5.0, reviewCount = 40),
            ),
            tasks = emptyList(),
        )
        assertEquals(
            listOf("fiveMany", "fiveFew", "four", "unrated", "nan"),
            sortExploreResults(state, ExploreSortOption.RatingHighToLow).peopleIds(),
        )
    }

    @Test fun priceSortsPeopleLowToHighWithUnpricedLast() {
        val state = ExploreResultState.Results(
            people = listOf(priced("none", null), priced("500", "500"), priced("150", "150"), priced("150b", "150.00")),
            tasks = emptyList(),
        )
        assertEquals(
            listOf("150", "150b", "500", "none"),
            sortExploreResults(state, ExploreSortOption.PriceLowToHigh).peopleIds(),
        )
    }

    @Test fun newestSortsTasksByPostingTime() {
        val state = ExploreResultState.Results(
            people = emptyList(),
            tasks = listOf(task("plain"), sourceTask("old", createdAt = 10), sourceTask("new", createdAt = 30), sourceTask("mid", createdAt = 20)),
        )
        assertEquals(listOf("new", "mid", "old", "plain"), sortExploreResults(state, ExploreSortOption.Newest).taskIds())
    }

    @Test fun dueSoonPutsAsapFirstThenDatesThenFlexible() {
        val state = ExploreResultState.Results(
            people = emptyList(),
            tasks = listOf(
                sourceTask("flexible", timing = PostTaskTimingMode.FLEXIBLE),
                sourceTask("friday", timing = PostTaskTimingMode.DATE, dateMillis = 5_000),
                sourceTask("asapLater", createdAt = 200, timing = PostTaskTimingMode.ASAP),
                sourceTask("tuesday", timing = PostTaskTimingMode.DATE, dateMillis = 1_000),
                task("unknown"),
                sourceTask("asapEarlier", createdAt = 100, timing = PostTaskTimingMode.ASAP),
            ),
        )
        assertEquals(
            listOf("asapEarlier", "asapLater", "tuesday", "friday", "flexible", "unknown"),
            sortExploreResults(state, ExploreSortOption.DueSoon).taskIds(),
        )
    }

    @Test fun priceSortsTasksByLowestBudgetWithQuotesLast() {
        val state = ExploreResultState.Results(
            people = emptyList(),
            tasks = listOf(
                sourceTask("quotes", timing = PostTaskTimingMode.ASAP, budget = PostTaskBudget.RequestQuotes),
                sourceTask("range", timing = PostTaskTimingMode.ASAP, budget = PostTaskBudget.Range("300", "900")),
                sourceTask("fixed", timing = PostTaskTimingMode.ASAP, budget = PostTaskBudget.Fixed("1200")),
            ),
        )
        assertEquals(listOf("range", "fixed", "quotes"), sortExploreResults(state, ExploreSortOption.PriceLowToHigh).taskIds())
    }

    @Test fun eachTabOnlyOffersSortsItsResultsCanSupport() {
        val available = exploreAvailableSortOptions()
        assertTrue(available[ExploreResultScope.All].orEmpty().size < 2)
        ExploreResultScope.entries.forEach { scope ->
            assertFalse(ExploreSortOption.Nearest in available[scope].orEmpty())
            available[scope]?.let { assertEquals(ExploreSortOption.BestMatch, it.first()) }
        }
        assertFalse(ExploreSortOption.Newest in available[ExploreResultScope.People].orEmpty())
        assertTrue(ExploreSortOption.DueSoon in available.getValue(ExploreResultScope.Tasks))
        assertEquals(
            ExploreSortOption.BestMatch,
            effectiveExploreSortOption(ExploreResultScope.People, mapOf(ExploreResultScope.People to ExploreSortOption.DueSoon)),
        )
    }

    @Test fun visibleResultsApplyTheSortAfterFiltering() {
        val state = ExploreResultState.Results(
            people = listOf(person("low", rating = 4.1), person("high", rating = 4.9), person("hidden", rating = 3.0)),
            tasks = emptyList(),
        )
        val visible = deriveExploreVisibleResults(
            selectedScope = ExploreResultScope.People,
            resultState = state,
            availableFilterOptions = defaultExploreFilterOptions(),
            appliedFilterOptions = mapOf(ExploreResultScope.People to setOf(ExploreFilterOption.RatingFourPlus)),
            priceFilters = emptyMap(),
            sortOption = ExploreSortOption.RatingHighToLow,
        )
        assertEquals(listOf("high", "low"), visible.submittedResultState.peopleIds())
    }
}
