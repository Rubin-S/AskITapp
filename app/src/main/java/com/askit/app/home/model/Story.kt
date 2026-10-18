package com.askit.app.home.model

import com.askit.app.story.StoryDraft

@kotlinx.serialization.Serializable
data class Story(
    val id: String,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val content: StoryDraft,
    val createdAtMillis: Long,
    val isSeen: Boolean = false,
    val isOwn: Boolean = false,
    val authorId: String? = null,
    val isDeleted: Boolean = false,
)

/** Stories stop opening or accepting interactions after 24 hours. */
fun Story.isAvailable(nowMillis: Long = System.currentTimeMillis()): Boolean =
    !isDeleted && nowMillis < createdAtMillis + 24 * 60 * 60 * 1000L
