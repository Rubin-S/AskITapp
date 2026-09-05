package com.askit.app.home.stories

import com.askit.app.home.data.FakeHomeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryViewerViewModelTest {

    @Test
    fun loadsStoriesGroupedByAuthor() = runTest {
        val viewModel = loadedViewModel()

        val state = viewModel.uiState.first { !it.isLoading }
        assertEquals(5, state.groups.size)
        assertEquals("story-1", state.currentStory?.id)
    }

    @Test
    fun startAt_positionsAtRequestedStoryAcrossGroups() = runTest {
        val viewModel = StoryViewerViewModel(FakeHomeRepository())
        viewModel.startAt("story-3")

        val state = viewModel.uiState.first { !it.isLoading && it.currentStory?.id == "story-3" }
        assertEquals("amy_deliy", state.currentGroup?.authorName)
        assertEquals(0, state.itemIndex)
    }

    @Test
    fun advanceToNext_crossesIntoNextGroupAndFinishesAtEnd() = runTest {
        val viewModel = loadedViewModel(startStoryId = "story-4")
        viewModel.uiState.first { !it.isLoading && it.currentStory?.id == "story-4" }

        // story-4 is the last item of its group -> next lands in the following group at index 0.
        assertTrue(viewModel.advanceToNext())
        val afterFirst = viewModel.uiState.value
        assertEquals("chloe_craft", afterFirst.currentGroup?.authorName)
        assertEquals("story-5", afterFirst.currentStory?.id)

        // The final group's only item was consumed -> viewer should close.
        assertFalse(viewModel.advanceToNext())
    }

    @Test
    fun backToPrevious_crossesIntoPreviousGroupTail() = runTest {
        val viewModel = loadedViewModel(startStoryId = "story-5")
        viewModel.uiState.first { !it.isLoading && it.currentStory?.id == "story-5" }

        viewModel.backToPrevious()

        val state = viewModel.uiState.value
        assertEquals("alex_electric", state.currentGroup?.authorName)
        assertEquals(0, state.itemIndex)
    }

    @Test
    fun selectGroup_movesToGroupStart() = runTest {
        val viewModel = loadedViewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.selectGroup(2)

        assertEquals(2, viewModel.uiState.value.groupIndex)
        assertEquals(0, viewModel.uiState.value.itemIndex)
    }

    @Test
    fun toggleLike_togglesCurrentStoryOnly() = runTest {
        val viewModel = loadedViewModel(startStoryId = "story-1")
        viewModel.uiState.first { !it.isLoading && it.currentStory?.id == "story-1" }

        viewModel.toggleLike()
        assertTrue(viewModel.uiState.value.isCurrentStoryLiked)

        viewModel.advanceToNext()
        assertFalse(viewModel.uiState.value.isCurrentStoryLiked)

        viewModel.toggleLike()
        viewModel.backToPrevious()
        assertTrue(viewModel.uiState.value.isCurrentStoryLiked)
    }

    private suspend fun loadedViewModel(startStoryId: String? = null): StoryViewerViewModel {
        val viewModel = StoryViewerViewModel(FakeHomeRepository())
        startStoryId?.let(viewModel::startAt)
        return viewModel
    }
}
