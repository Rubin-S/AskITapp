package com.askit.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** TalkBack and Switch Access can drive the story viewer without gestures. */
@RunWith(AndroidJUnit4::class)
class StoryViewerAccessibilityTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private val next by lazy { compose.activity.getString(R.string.story_viewer_action_next) }

    private fun setUpViewer(): StoryViewerViewModel {
        val graph = SessionGraph(AppConfig(backendBaseUrl = null, clientPreview = true), AppVariant, previewStorage = null)
        lateinit var viewModel: StoryViewerViewModel
        compose.runOnUiThread { viewModel = StoryViewerViewModel(graph.home, SavedStateHandle()) }
        compose.waitUntil(5_000) { viewModel.uiState.value.groups.isNotEmpty() }
        val first = viewModel.uiState.value.groups.first().stories.first().id
        compose.runOnUiThread { viewModel.startAt(first) }
        compose.setContent {
            AskITTheme { StoryViewerRoute(viewModel = viewModel, startStoryId = first, onDismiss = {}) }
        }
        compose.waitForIdle()
        return viewModel
    }

    private val hasNextAction = SemanticsMatcher("has a \"$next\" custom action") { node ->
        node.config.getOrNull(SemanticsActions.CustomActions).orEmpty().any { it.label == next }
    }

    @Test fun nextCustomActionAdvancesTheStory() {
        val viewModel = setUpViewer()
        val before = viewModel.uiState.value.currentStory?.id
        assertNotNull(before)
        val node = compose.onNode(hasNextAction).fetchSemanticsNode()
        val position = node.config.getOrNull(SemanticsProperties.ContentDescription)
        assertNotNull("story surface announces its position", position?.firstOrNull())
        compose.runOnUiThread {
            node.config[SemanticsActions.CustomActions].first { it.label == next }.action()
        }
        compose.waitForIdle()
        assertNotEquals(before, viewModel.uiState.value.currentStory?.id)
    }

    @Test fun closeButtonHasA48dpTouchTarget() {
        setUpViewer()
        compose.onNodeWithTag("story_close").assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
    }
}
