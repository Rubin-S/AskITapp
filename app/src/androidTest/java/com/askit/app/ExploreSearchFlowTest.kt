package com.askit.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.askit.app.explore.ExploreFilterOption
import com.askit.app.explore.ExplorePersonResult
import com.askit.app.explore.ExploreResultScope
import com.askit.app.explore.ExploreResultState
import com.askit.app.explore.ExploreRoute
import com.askit.app.explore.ExploreViewModel
import com.askit.app.explore.defaultExploreFilterOptions
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Explore: submit a search, switch the result scope, then apply and remove a filter. */
@RunWith(AndroidJUnit4::class)
class ExploreSearchFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun person(id: String, name: String, rating: Double, reviews: Int) = ExplorePersonResult(
        id = id,
        name = name,
        avatarUrl = null,
        primaryService = "Plumbing",
        additionalServices = emptyList(),
        rating = rating,
        reviewCount = reviews,
        locationLabel = "Chennai",
        priceLabel = null,
        statusLabel = null,
    )

    @Test fun searchThenScopeThenFilter() {
        val viewModel = ExploreViewModel(SavedStateHandle())
        val people = listOf(person("p1", "Priya Sharma", 4.9, 48), person("p2", "Ravi Kumar", 3.9, 5))
        var applied by mutableStateOf<Map<ExploreResultScope, Set<ExploreFilterOption>>>(emptyMap())
        compose.setContent {
            AskITTheme {
                ExploreRoute(
                    viewModel = viewModel,
                    onSearchFiltersClick = {},
                    resultState = ExploreResultState.Results(people = people, tasks = emptyList()),
                    availableFilterOptions = defaultExploreFilterOptions(),
                    appliedFilterOptions = applied,
                    onFiltersChanged = { scope, options -> applied = applied + (scope to options) },
                    onFilterScopeChanged = viewModel::onFilterScopeSelected,
                )
            }
        }

        // Search.
        compose.onNodeWithTag("explore_search_field").performClick()
        compose.onNodeWithTag("explore_search_field").performTextInput("Plumbing")
        compose.onNodeWithTag("explore_search_field").performImeAction()
        compose.waitForIdle()
        assertEquals("Plumbing", viewModel.uiState.value.query)
        compose.onNodeWithTag("explore_submitted_results").assertExists()
        assertEquals(1, compose.onAllNodesWithText("Ravi Kumar", useUnmergedTree = true).fetchSemanticsNodes().size)

        // Scope.
        compose.onNodeWithTag("explore_scope_${ExploreResultScope.People.name}").performClick()
        compose.onNodeWithTag("explore_scope_${ExploreResultScope.People.name}").assertIsSelected()
        assertEquals(ExploreResultScope.People, viewModel.filterScope.value)

        // Filter: 4.5+ hides the 3.9-rated result and shows an applied-filter chip.
        compose.runOnIdle { applied = mapOf(ExploreResultScope.People to setOf(ExploreFilterOption.RatingFourPointFivePlus)) }
        compose.onNodeWithTag("explore_applied_filters").assertExists()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("Ravi Kumar", useUnmergedTree = true).fetchSemanticsNodes().isEmpty()
        }
        assertEquals(1, compose.onAllNodesWithText("Priya Sharma", useUnmergedTree = true).fetchSemanticsNodes().size)
    }
}
