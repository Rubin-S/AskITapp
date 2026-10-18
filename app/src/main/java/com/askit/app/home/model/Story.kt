package com.askit.app.home.model

import com.askit.app.story.StoryAudience
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

/**
 * Story audience rule until the server authorises audiences: `Everyone` stories are visible to
 * all; `CloseCircle` and `Message` stories are visible only to their author. There is no circle
 * membership or message delivery yet, so "restricted" must mean "owner only", never "everyone".
 * The same rule is used for profile highlights.
 */
fun Story.isVisibleTo(viewerId: String?): Boolean =
    content.audience == StoryAudience.Everyone || (!viewerId.isNullOrBlank() && authorId == viewerId)

/** Only public stories may be shared or forwarded to other people. */
val Story.isShareable: Boolean get() = content.audience == StoryAudience.Everyone
