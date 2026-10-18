package com.askit.app.screenshots

import androidx.compose.foundation.layout.Column
import com.askit.app.explore.AppliedFilterChips
import com.askit.app.explore.BrowseSectionStateCard
import com.askit.app.explore.BrowseServices
import com.askit.app.explore.ExploreBrowseSection
import com.askit.app.explore.ExploreBrowseStatus
import com.askit.app.explore.ExploreEmptyResultBody
import com.askit.app.explore.ExploreFailureResultBody
import com.askit.app.explore.ExploreFilterOption
import com.askit.app.explore.ExploreResultState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-mdpi")
class ExploreScreenshotTest {
    @Test fun browseServices() = snapshot("explore_browse_services") {
        BrowseServices(status = ExploreBrowseStatus.Available, onCategorySelected = {}, onChangeArea = {}, onRetry = {})
    }

    @Test fun browseSectionOffline() = snapshot("explore_browse_offline") {
        BrowseSectionStateCard(
            section = ExploreBrowseSection.Professionals,
            status = ExploreBrowseStatus.Offline,
            onChangeArea = {},
            onRetry = {},
        )
    }

    @Test fun filteredResultsWithChips() = snapshot("explore_filtered_empty") {
        Column {
            AppliedFilterChips(
                options = listOf(ExploreFilterOption.RatingFourPlus, ExploreFilterOption.AvailableToday),
                onFilterRemoved = {},
            )
            ExploreEmptyResultBody(reason = ExploreResultState.EmptyReason.Filters, onEditFilters = {})
        }
    }

    @Test fun resultsOffline() = snapshot("explore_results_offline") {
        ExploreFailureResultBody(reason = ExploreResultState.FailureReason.Offline, onRetryResults = {})
    }
}
