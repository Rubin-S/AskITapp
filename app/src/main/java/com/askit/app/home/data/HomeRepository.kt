package com.askit.app.home.data

import com.askit.app.home.model.FeedItem
import com.askit.app.home.model.Story
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getStories(): Flow<List<Story>>
    fun getFeed(page: Int = 1): Flow<List<FeedItem>>
    fun addStory(story: Story) {}
    fun markStorySeen(storyId: String) {}
}
