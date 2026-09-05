package com.askit.app.home.model

sealed interface PostMedia {
    data class SinglePhoto(
        val url: String,
        val contentDescription: String? = null,
    ) : PostMedia

    data class Carousel(
        val urls: List<String>,
    ) : PostMedia

    data class BeforeAfter(
        val beforeUrl: String,
        val afterUrl: String,
    ) : PostMedia
}

data class PostPoll(
    val question: String,
    val options: List<String>,
    val closingSummary: String = "Poll ends soon",
)

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
)
