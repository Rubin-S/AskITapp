package com.askit.app.profile

import com.askit.app.auth.validAccountPostcode
import com.askit.app.home.model.Story
import com.askit.app.home.model.isVisibleTo
import com.askit.app.jobs.Job
import com.askit.app.jobs.JobStatus
import com.askit.designsystem.profile.ProfileMetricItem
import kotlinx.serialization.Serializable
import java.util.Locale
import kotlin.math.roundToInt

@Serializable
data class ProfileGalleryItem(val id: String, val uri: String, val isCarousel: Boolean = false)

@Serializable
data class SavedProfessional(val id: String, val name: String, val trade: String, val rating: String, val avatarUrl: String? = null)

@Serializable
enum class ReviewDirection { SEEKER_TO_PROVIDER, PROVIDER_TO_SEEKER }

@Serializable
data class ProfileReview(
    val id: String,
    val name: String,
    val meta: String,
    val rating: Float,
    val body: String,
    val createdAtMillis: Long,
    val jobId: String? = null,
    val reviewerAccountId: String? = null,
    val recipientAccountId: String? = null,
    val direction: ReviewDirection? = null,
    val avatarUrl: String? = null,
)

/** A server-side aggregate (average and count) for profiles whose full review list isn't loaded. */
data class RatingSummary(val average: Double, val count: Int)

/** A retained collection. Ordinary story availability remains a separate 24-hour policy. */
@Serializable
data class ProfileHighlight(
    val id: String,
    val ownerId: String,
    val name: String,
    val stories: List<Story> = emptyList(),
    val coverStoryId: String? = null,
) {
    fun validate(actorId: String) {
        require(actorId.isNotBlank() && actorId == ownerId)
        require(id.isNotBlank() && name.isNotBlank() && name.length <= HIGHLIGHT_NAME_MAX)
        require(stories.all { it.authorId == ownerId && it.id.isNotBlank() })
        require(stories.map { it.id }.distinct().size == stories.size)
        require(coverStoryId == null || stories.any { it.id == coverStoryId })
    }

    /** Without server audience authorization, restricted stories are available only to their owner. */
    fun visibleStories(viewerId: String?, authorizedPostIds: Set<String> = emptySet()): List<Story> = stories.filter {
        !it.isDeleted && it.authorId == ownerId &&
            (it.content.sharedPost == null || (it.content.sharedPost.id in authorizedPostIds && !it.content.sharedPost.archived)) &&
            (viewerId == ownerId || it.isVisibleTo(viewerId))
    }
}

const val HIGHLIGHT_NAME_MAX = 60

data class ProfilePerson(val id: String, val name: String, val avatarUrl: String? = null)

/** Followers/following lists (null = not available in this build) and the counts to show for them. */
data class ProfilePeopleState(
    val viewerId: String? = null,
    val followers: List<ProfilePerson>? = null,
    val following: List<ProfilePerson>? = null,
    val viewerFollowing: Set<String> = emptySet(),
    val canMutate: Boolean = false,
    val followerCount: Int? = null,
    val followingCount: Int? = null,
)

/**
 * Rounded distance to show on a profile, or null when it must stay private.
 * Distance is server-supplied between postcode centroids; never derive it from exact coordinates.
 */
internal fun approximateProfileDistance(consent: Boolean, viewerPostcode: String?, distanceKm: Double?): Int? = distanceKm?.takeIf {
    consent && validAccountPostcode(viewerPostcode.orEmpty()) && it.isFinite() && it >= 0
}?.roundToInt()

/** Unknown/legacy identities cannot contribute to a directional rating. */
internal fun List<ProfileReview>.receivedBy(accountId: String, direction: ReviewDirection): List<ProfileReview> = if (accountId.isBlank()) {
    emptyList()
} else {
    filter {
        it.recipientAccountId == accountId && !it.reviewerAccountId.isNullOrBlank() &&
            it.reviewerAccountId != accountId && it.direction == direction && it.rating.isFinite() && it.rating in 0f..MAX_RATING
    }.distinctBy { it.id }
}

internal fun List<Job>.profileCompletedCount(accountId: String, isProvider: Boolean): Int = if (accountId.isBlank()) {
    0
} else {
    count { it.status == JobStatus.Completed && (if (isProvider) it.providerAccountId else it.seekerAccountId) == accountId }
}

internal object ProfileMetric {
    const val RATING = "rating"
    const val COMPLETED = "completed_jobs"
    const val FOLLOWERS = "followers"
    const val FOLLOWING = "following"
}

/** Labels are added by the scaffold from the ids, so the model never carries display text. */
internal fun profileMetrics(
    accountId: String,
    provider: Boolean,
    reviews: List<ProfileReview>,
    completed: Int?,
    followers: Int,
    following: Int,
    summary: RatingSummary? = null,
): List<ProfileMetricItem> {
    val received = reviews.receivedBy(accountId, if (provider) ReviewDirection.SEEKER_TO_PROVIDER else ReviewDirection.PROVIDER_TO_SEEKER)
    val rating = summary?.takeIf { it.count > 0 && it.average.isFinite() }
        ?: received.takeIf { it.isNotEmpty() }?.let { list -> RatingSummary(list.map { it.rating }.average(), list.size) }
    return listOf(
        ProfileMetricItem(
            ProfileMetric.RATING,
            rating?.let { String.format(Locale.getDefault(), "%.1f (%d)", it.average, it.count) } ?: "—",
            "",
            com.askit.designsystem.R.drawable.ic_star_filled,
        ),
        ProfileMetricItem(ProfileMetric.COMPLETED, completed?.toString() ?: "—", ""),
        ProfileMetricItem(ProfileMetric.FOLLOWERS, followers.toString(), ""),
        ProfileMetricItem(ProfileMetric.FOLLOWING, following.toString(), ""),
    )
}

/** Counts from the people lists replace the profile's own when this build has them. */
internal fun ProfileUiState.withPeople(people: ProfilePeopleState): ProfileUiState = copy(
    metrics = metrics.map {
        when (it.id) {
            ProfileMetric.FOLLOWERS -> it.copy(value = (people.followerCount ?: people.followers?.size)?.toString() ?: it.value)
            ProfileMetric.FOLLOWING -> it.copy(value = (people.followingCount ?: people.following?.size)?.toString() ?: it.value)
            else -> it
        }
    },
)

private const val MAX_RATING = 5f
