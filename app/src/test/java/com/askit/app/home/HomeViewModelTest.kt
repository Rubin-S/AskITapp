package com.askit.app.home

import com.askit.app.home.data.FakeHomeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelTest {

    @Test
    fun homeViewModel_loadsDataSuccessfully() = runTest {
        val repository = FakeHomeRepository()
        val viewModel = HomeViewModel(repository)

        val state = viewModel.uiState.first { it is HomeUiState.Success }
        assertTrue(state is HomeUiState.Success)

        val success = state as HomeUiState.Success
        assertEquals(5, success.stories.size)
        assertEquals(5, success.feedItems.size)
        assertEquals(1, success.currentPage)
        assertTrue(success.hasMore)
        assertFalse(success.isLoadingMore)
        assertFalse(success.isRefreshing)
    }

    @Test
    fun homeViewModel_loadMore_appendsNextPage() = runTest {
        val repository = FakeHomeRepository()
        val viewModel = HomeViewModel(repository)

        viewModel.uiState.first { it is HomeUiState.Success }
        viewModel.loadMore()

        val state = viewModel.uiState.first { (it as? HomeUiState.Success)?.currentPage == 2 }
        val success = state as HomeUiState.Success
        assertEquals(9, success.feedItems.size)
        assertEquals(2, success.currentPage)
        assertTrue(success.hasMore)
    }

    @Test
    fun homeViewModel_refresh_reloadsPageOne() = runTest {
        val repository = FakeHomeRepository()
        val viewModel = HomeViewModel(repository)

        viewModel.uiState.first { it is HomeUiState.Success }
        viewModel.loadMore()
        viewModel.uiState.first { (it as? HomeUiState.Success)?.currentPage == 2 }

        viewModel.refresh()
        val state = viewModel.uiState.first { (it as? HomeUiState.Success)?.currentPage == 1 }
        val success = state as HomeUiState.Success
        assertEquals(5, success.feedItems.size)
    }

    @Test
    fun homeViewModel_addStoryFromDraft_prependsUserStory() = runTest {
        val repository = FakeHomeRepository()
        val viewModel = HomeViewModel(repository)
        viewModel.uiState.first { it is HomeUiState.Success }

        val samplePost = com.askit.app.home.data.getFeedPostById("post-2")
        val draft = com.askit.app.story.StoryDraft(
            sharedPost = samplePost,
            reshareCardStyle = com.askit.app.story.StoryReshareCardStyle.FullCard,
        )

        viewModel.addStoryFromDraft(draft)

        val state = viewModel.uiState.value as HomeUiState.Success
        val firstStory = state.stories.first()
        assertEquals("You", firstStory.authorName)
        assertEquals(samplePost, firstStory.sharedPost)
        assertFalse(firstStory.isSeen)
    }

    @Test
    fun homeViewModel_markStorySeen_updatesStoryIsSeenTrue() = runTest {
        val repository = FakeHomeRepository()
        val viewModel = HomeViewModel(repository)
        viewModel.uiState.first { it is HomeUiState.Success }

        val unseenStory = (viewModel.uiState.value as HomeUiState.Success).stories.first { !it.isSeen }
        viewModel.markStorySeen(unseenStory.id)

        val updatedStory = (viewModel.uiState.value as HomeUiState.Success).stories.first { it.id == unseenStory.id }
        assertTrue(updatedStory.isSeen)
    }
}
