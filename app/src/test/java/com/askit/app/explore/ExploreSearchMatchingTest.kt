package com.askit.app.explore

import org.junit.Assert.assertEquals
import org.junit.Test

class ExploreSearchMatchingTest {
    @Test fun recentSearchesPutTheExactMatchFirstAndKeepTwo() {
        val matches = matchingRecentSearches(
            recentSearches = listOf("Plumbing repair", "plumber near me", "Plumber", "Painter"),
            normalizedQuery = "plumber",
        )
        assertEquals(listOf("Plumber", "plumber near me"), matches)
    }

    @Test fun categoriesSkipRecentSuggestionsAndCapAtThree() {
        val matches = matchingServiceCategories(
            categories = listOf("Cleaning", "Car wash", "Carpentry", "Catering", "Cooking"),
            normalizedQuery = "c",
            recentSuggestions = listOf("Cleaning"),
        )
        assertEquals(listOf("Car wash", "Carpentry", "Catering"), matches)
    }

    @Test fun matchingIgnoresCaseAndExtraWhitespace() {
        assertEquals("deep cleaning", normalizedMatchValue("  Deep   Cleaning "))
        assertEquals(listOf("Deep Cleaning"), matchingServiceCategories(listOf("Deep Cleaning"), "clean", emptyList()))
    }

    @Test fun predictiveTextKeepsTheCategoryWordingWhenItIsNotAPrefixMatch() {
        assertEquals("Painting", predictiveCategoryText("Painting", "xyz").text)
        assertEquals("Painting", predictiveCategoryText("Painting", "pai").text)
    }
}
