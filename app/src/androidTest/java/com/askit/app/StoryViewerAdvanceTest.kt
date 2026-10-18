package com.askit.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.askit.app.di.AppConfig
import com.askit.app.di.SessionGraph
import com.askit.app.home.stories.StoryViewerRoute
import com.askit.app.home.stories.StoryViewerViewModel
import com.askit.app.variant.AppVariant
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The story viewer moves to the next story when the 5 s item timer runs out. */
@RunWith(AndroidJUnit4::class)
class StoryViewerAdvanceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun timerAdvancesToTheNextStory() {
        val graph = SessionGraph(AppConfig(backendBaseUrl = null, clientPreview = true), AppVariant, previewStorage = null)
        lateinit var viewModel: StoryViewerViewModel
        compose.runOnUiThread { viewModel = StoryViewerViewModel(graph.home, SavedStateHandle()) }
        compose.waitUntil(5_000) { viewModel.uiState.value.groups.isNotEmpty() }
        val groups = viewModel.uiState.value.groups
        assertTrue("demo content needs at least two stories", groups.sumOf { it.stories.size } >= 2)
        val first = groups.first().stories.first().id
        compose.runOnUiThread { viewModel.startAt(first) }

        var dismissed = false
        compose.mainClock.autoAdvance = false
        compose.setContent {
            AskITTheme { StoryViewerRoute(viewModel = viewModel, startStoryId = first, onDismiss = { dismissed = true }) }
        }
        compose.mainClock.advanceTimeByFrame()
        assertNotNull(viewModel.uiState.value.currentStory)

        compose.mainClock.advanceTimeBy(5_500)
        compose.waitForIdle()
        assertTrue(!dismissed)
        assertNotEquals(first, viewModel.uiState.value.currentStory?.id)
    }
}
