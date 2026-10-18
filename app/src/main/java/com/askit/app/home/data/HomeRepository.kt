package com.askit.app.home.data

import com.askit.app.home.model.FeedItem
import com.askit.app.home.model.Story
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    val posts: com.askit.app.home.posts.PostStore
    val storySocial: com.askit.app.home.stories.StorySocialStore
    fun getStory(storyId: String): Story?
    fun getStories(): Flow<List<Story>>
    fun getFeed(page: Int = 1): Flow<List<FeedItem>>
    fun addStory(story: Story, accountId: String?): com.askit.app.home.stories.StoryActionResult
    fun markStorySeen(storyId: String)
}
