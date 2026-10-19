package com.askit.app.explore

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

/** Explore's sort and filter decide which professionals a seeker sees first, or at all. */
class ExploreSortFilterTest {
    @Test fun ratingSortPutsTheBestFirstBreaksTiesByReviewsAndUnratedLast() {
        val results = results(
            person("unrated", rating = null),
            person("good", rating = 4.5, reviews = 3),
            person("broken", rating = Double.NaN),
            person("best", rating = 4.9),
            person("good-more-reviews", rating = 4.5, reviews = 40),
        )
        assertEquals(
            listOf("best", "good-more-reviews", "good", "unrated", "broken"),
            ids(sortExploreResults(results, ExploreSortOption.RatingHighToLow)),
        )
        assertEquals(ids(results), ids(sortExploreResults(results, ExploreSortOption.BestMatch)))
    }

    @Test fun priceFilterKeepsOverlappingRangesAndDropsUnpricedOnes() {
        val results = results(
            person("in-range", price = 400, maxPrice = 600),
            person("overlaps-top", price = 900, maxPrice = 1_200),
            person("too-expensive", price = 1_100),
            person("unpriced"),
        )
        val filter = ExplorePriceFilter(minimum = "500", maximum = "1000")
        assertEquals(listOf("in-range", "overlaps-top"), ids(filterExploreResults(results, emptySet(), filter)))
    }

    @Test fun invalidFiltersHideEverythingAndRatingFiltersIgnoreUnrated() {
        val results = results(person("a", rating = 4.6, price = 100), person("b", rating = null, price = 100))
        val inverted = ExplorePriceFilter(minimum = "900", maximum = "100")
        assertEquals(emptyList<String>(), ids(filterExploreResults(results, emptySet(), inverted)))
        assertEquals(
            listOf("a"),
            ids(filterExploreResults(results, setOf(ExploreFilterOption.RatingFourPointFivePlus), ExplorePriceFilter())),
        )
    }

    private fun ids(state: ExploreResultState) = (state as ExploreResultState.Results).people.map { it.id }

    private fun results(vararg people: ExplorePersonResult) = ExploreResultState.Results(people.toList(), emptyList())

    private fun person(id: String, rating: Double? = 4.0, reviews: Int = 10, price: Int? = null, maxPrice: Int? = price) =
        ExplorePersonResult(
            id = id,
            name = id,
            avatarUrl = null,
            primaryService = "Plumbing",
            additionalServices = emptyList(),
            rating = rating,
            reviewCount = reviews,
            locationLabel = "Karaikal",
            priceLabel = null,
            statusLabel = null,
            listedPrice = price?.let { ExploreListedPrice(BigDecimal(it), BigDecimal(maxPrice!!), ExploreRateUnit.Visit) },
        )
}
