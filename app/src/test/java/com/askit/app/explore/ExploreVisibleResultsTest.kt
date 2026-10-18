package com.askit.app.explore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExploreVisibleResultsTest {
    private val ratingOptions = mapOf(
        ExploreResultScope.People to listOf(ExploreFilterOption.RatingFourPointFivePlus),
        ExploreResultScope.All to listOf(ExploreFilterOption.RatingFourPointFivePlus),
    )

    @Test fun noFiltersKeepsEveryResultAndReportsAQueryEmptyReason() {
        val visible = deriveExploreVisibleResults(
            selectedScope = ExploreResultScope.Tasks,
            resultState = ExploreResultState.Results(people = listOf(person("1")), tasks = emptyList()),
            availableFilterOptions = emptyMap(),
            appliedFilterOptions = emptyMap(),
            priceFilters = emptyMap(),
        )
        assertEquals(emptyList<ExploreFilterOption>(), visible.orderedAppliedFilters)
        assertEquals(listOf(person("1")), visible.filteredPeople)
        assertEquals(ExploreResultState.Empty(ExploreResultState.EmptyReason.Query), visible.submittedResultState)
        assertFalse(visible.hasVisibleSubmittedResults)
    }

    @Test fun aRatingFilterThatHidesEveryoneIsReportedAsFilteredOut() {
        val visible = deriveExploreVisibleResults(
            selectedScope = ExploreResultScope.People,
            resultState = ExploreResultState.Results(people = listOf(person("1", rating = 4.0)), tasks = emptyList()),
            availableFilterOptions = ratingOptions,
            appliedFilterOptions = mapOf(ExploreResultScope.People to setOf(ExploreFilterOption.RatingFourPointFivePlus)),
            priceFilters = emptyMap(),
        )
        assertTrue(visible.peopleHiddenByFilters)
        assertFalse(visible.tasksHiddenByFilters)
        assertEquals(ExploreResultState.Empty(ExploreResultState.EmptyReason.Filters), visible.submittedResultState)
    }

    @Test fun peopleScopeShowsOnlyIdentityMatches() {
        val identity = person("1")
        val serviceOnly = person("2", matchReasons = setOf(PersonMatchReason.Service))
        val visible = deriveExploreVisibleResults(
            selectedScope = ExploreResultScope.People,
            resultState = ExploreResultState.Results(people = listOf(identity, serviceOnly), tasks = emptyList()),
            availableFilterOptions = emptyMap(),
            appliedFilterOptions = emptyMap(),
            priceFilters = emptyMap(),
        )
        assertEquals(listOf(identity), (visible.submittedResultState as ExploreResultState.Results).people)
        assertTrue(visible.hasVisibleSubmittedResults)
    }

    @Test fun allScopeNeverCountsAsVisibleSubmittedResults() {
        val visible = deriveExploreVisibleResults(
            selectedScope = ExploreResultScope.All,
            resultState = ExploreResultState.Results(people = listOf(person("1")), tasks = listOf(task("t"))),
            availableFilterOptions = emptyMap(),
            appliedFilterOptions = emptyMap(),
            priceFilters = emptyMap(),
        )
        assertFalse(visible.hasVisibleSubmittedResults)
    }

    @Test fun appliedFiltersUnavailableForTheScopeAreIgnored() {
        val visible = deriveExploreVisibleResults(
            selectedScope = ExploreResultScope.Tasks,
            resultState = ExploreResultState.Results(people = emptyList(), tasks = listOf(task("t"))),
            availableFilterOptions = mapOf(ExploreResultScope.Tasks to listOf(ExploreFilterOption.RatingFourPlus)),
            appliedFilterOptions = mapOf(ExploreResultScope.Tasks to setOf(ExploreFilterOption.RatingFourPlus)),
            priceFilters = emptyMap(),
        )
        assertEquals(emptyList<ExploreFilterOption>(), visible.orderedAppliedFilters)
        assertTrue(visible.hasVisibleSubmittedResults)
    }

    @Test fun filterKeysChangeOnlyForTheScopeWhosePriceFilterChanged() {
        val before = exploreFilterKeys(emptyMap(), emptyMap(), emptyMap())
        val after = exploreFilterKeys(
            emptyMap(),
            emptyMap(),
            mapOf(ExploreResultScope.Tasks to ExplorePriceFilter(minimum = "100")),
        )
        assertNotEquals(before.getValue(ExploreResultScope.Tasks), after.getValue(ExploreResultScope.Tasks))
        assertEquals(before.getValue(ExploreResultScope.People), after.getValue(ExploreResultScope.People))
    }

    @Test fun sortKeysAndJoinedKeyCoverEveryScope() {
        val keys = exploreSortKeys(mapOf(ExploreResultScope.People to ExploreSortOption.Nearest))
        assertEquals("Nearest", keys.getValue(ExploreResultScope.People))
        assertEquals("", keys.getValue(ExploreResultScope.Tasks))
        assertEquals("All=|People=Nearest|Services=|Tasks=", keys.joinedScopeKey())
    }
}
