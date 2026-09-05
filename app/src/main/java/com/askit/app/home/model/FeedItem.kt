package com.askit.app.home.model

sealed interface FeedItem {
    val id: String

    data class PostItem(
        val post: FeedPost,
    ) : FeedItem {
        override val id: String = post.id
    }

    data class TaskSection(
        override val id: String,
        val tasks: List<TaskPreview>,
    ) : FeedItem

    data class ServiceSection(
        override val id: String,
        val services: List<ServicePreview>,
    ) : FeedItem

    data class PeopleSection(
        override val id: String,
        val people: List<PersonPreview>,
    ) : FeedItem
}
