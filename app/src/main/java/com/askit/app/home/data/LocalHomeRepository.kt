package com.askit.app.home.data

import com.askit.app.home.model.FeedItem
import com.askit.app.home.model.FeedPost
import com.askit.app.home.model.Story
import com.askit.app.home.model.isAvailable
import com.askit.app.home.posts.PostStore
import com.askit.app.home.stories.StoryActionFailure
import com.askit.app.home.stories.StoryActionResult
import com.askit.app.home.stories.StorySocialStore
import com.askit.app.task.TaskRepository
import com.askit.app.task.toTaskPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/**
 * Static content placed around local data in the Home feed. Release builds always use [Empty];
 * debug and preview builds get sample content from `AppVariant.homeFeed()`.
 */
data class HomeFeedFixtures(
    /** Seeded into the [PostStore] so the usual post actions work on them. */
    val posts: List<FeedPost> = emptyList(),
    /** Page-one items placed after the account's own published posts and before the task row. */
    val pageOneLead: List<FeedItem> = emptyList(),
    /** Page-one items placed after the task row. */
    val pageOneTrail: List<FeedItem> = emptyList(),
    /** Pages 2, 3, … in order. */
    val laterPages: List<List<FeedItem>> = emptyList(),
) {
    companion object {
        val Empty = HomeFeedFixtures()
    }
}

/**
 * In-memory Home repository: locally published posts and stories plus the task row from
 * [TaskRepository]. It contains no sample content itself; anything beyond what the signed-in
 * account created comes from [fixtures], which release builds leave empty so Home shows its
 * honest empty state until a backend exists.
 */
class LocalHomeRepository(
    private val taskRepository: TaskRepository,
    initialStories: List<Story> = emptyList(),
    private val fixtures: HomeFeedFixtures = HomeFeedFixtures.Empty,
    override val storySocial: StorySocialStore = StorySocialStore(),
) : HomeRepository {

    private val storiesFlow = MutableStateFlow(initialStories)

    override val posts: PostStore = PostStore(fixtures.posts)

    override fun getStory(storyId: String): Story? = storiesFlow.value.firstOrNull { it.id == storyId && it.isAvailable() }

    override fun getStories(): Flow<List<Story>> = combine(storiesFlow, storySocial.state) { stories, social ->
        stories.filter { it.isAvailable() && it.authorId !in social.muted && it.id !in social.hiddenStoryIds }
            .map { it.copy(isOwn = social.accountId != null && it.authorId == social.accountId) }
    }

    override fun addStory(story: Story, accountId: String?): StoryActionResult {
        if (!storySocial.isCurrentAccount(accountId)) return StoryActionResult.Rejected(StoryActionFailure.SIGN_IN)
        if (story.authorId != accountId) return StoryActionResult.Rejected(StoryActionFailure.NOT_ALLOWED)
        storiesFlow.value = listOf(story) + storiesFlow.value.filter { it.id != story.id }
        return StoryActionResult.Success
    }

    override fun markStorySeen(storyId: String) {
        storiesFlow.value = storiesFlow.value.map { story ->
            if (story.id == storyId) story.copy(isSeen = true) else story
        }
    }

    override fun getFeed(page: Int): Flow<List<FeedItem>> =
        combine(taskRepository.tasks, posts.posts, posts.account) { currentTasks, _, _ ->
            val template = when {
                page == 1 -> buildList {
                    addAll(posts.publishedPosts().map(FeedItem::PostItem))
                    addAll(fixtures.pageOneLead)
                    val taskPreviews = currentTasks.map { it.toTaskPreview() }
                    if (taskPreviews.isNotEmpty()) add(FeedItem.TaskSection(id = "featured-tasks", tasks = taskPreviews))
                    addAll(fixtures.pageOneTrail)
                }
                page >= 2 -> fixtures.laterPages.getOrElse(page - 2) { emptyList() }
                else -> emptyList()
            }
            posts.project(template)
        }
}
