package com.askit.app.home.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface PostMedia {
    @Serializable
    data class SinglePhoto(
        val url: String,
        val contentDescription: String? = null,
        val aspectRatio: Float = 4f / 3f,
    ) : PostMedia

    @Serializable
    data class Carousel(
        val urls: List<String>,
        val aspectRatio: Float = 4f / 3f,
    ) : PostMedia

    @Serializable
    data class BeforeAfter(
        val beforeUrl: String,
        val afterUrl: String,
        val aspectRatio: Float = 4f / 3f,
    ) : PostMedia
}

@Serializable
data class PostPoll(
    val question: String,
    val options: List<String>,
    val closingSummary: String = "Poll ends soon",
)

@Serializable
data class FeedPost(
    val id: String,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val locationLabel: String? = null,
    val content: String,
    val media: PostMedia? = null,
    val poll: PostPoll? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val authorId: String? = null,
    val hideLikeCount: Boolean = false,
    val commentsEnabled: Boolean = true,
    val archived: Boolean = false,
    val pinnedAtMillis: Long? = null,
)
