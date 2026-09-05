package com.askit.app.home.model

data class Story(
    val id: String,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val mediaUrl: String,
    val caption: String? = null,
    val createdAtMillis: Long,
    val isSeen: Boolean = false,
    val sharedPost: FeedPost? = null,
) {
    val isOwn: Boolean get() = authorName == "You"
}
