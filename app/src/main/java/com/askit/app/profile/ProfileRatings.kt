package com.askit.app.profile

import com.askit.app.jobs.Job
import com.askit.app.jobs.JobStatus
import com.askit.designsystem.profile.ProfileMetricItem
import java.util.Locale
import kotlinx.serialization.Serializable

@Serializable
enum class ReviewDirection { SEEKER_TO_PROVIDER, PROVIDER_TO_SEEKER }

/** Unknown/legacy identities cannot contribute to a directional rating. */
internal fun List<ProfileReview>.receivedBy(accountId: String, direction: ReviewDirection): List<ProfileReview> =
    if (accountId.isBlank()) emptyList() else filter {
        it.recipientAccountId == accountId && !it.reviewerAccountId.isNullOrBlank() &&
            it.reviewerAccountId != accountId && it.direction == direction && it.rating.isFinite() && it.rating in 0f..5f
    }.distinctBy { it.id }

internal fun List<Job>.profileCompletedCount(accountId: String, isProvider: Boolean): Int =
    if (accountId.isBlank()) 0 else count {
        it.status == JobStatus.Completed && (if (isProvider) it.providerAccountId else it.seekerAccountId) == accountId
    }

internal fun profileMetrics(accountId: String, provider: Boolean, reviews: List<ProfileReview>, completed: Int?, followers: Int, following: Int): List<ProfileMetricItem> {
    val received = reviews.receivedBy(accountId, if (provider) ReviewDirection.SEEKER_TO_PROVIDER else ReviewDirection.PROVIDER_TO_SEEKER)
    val rating = if (received.isEmpty()) "—" else String.format(Locale.US, "%.1f (%d)", received.map { it.rating }.average(), received.size)
    return listOf(
        ProfileMetricItem("rating", rating, "Rating", com.askit.designsystem.R.drawable.ic_star_filled),
        ProfileMetricItem("completed_jobs", completed?.toString() ?: "—", "Completed"),
        ProfileMetricItem("followers", followers.toString(), "Followers"),
        ProfileMetricItem("following", following.toString(), "Following"),
    )
}

data class ProfilePerson(val id: String, val name: String, val avatarUrl: String? = null)
data class ProfilePeopleState(
    val viewerId: String? = null,
    val followers: List<ProfilePerson>? = null,
    val following: List<ProfilePerson>? = null,
    val viewerFollowing: Set<String> = emptySet(),
    val canMutate: Boolean = false,
)

internal fun ProfileUiState.withPeople(people: ProfilePeopleState): ProfileUiState = copy(metrics = metrics.map {
    when (it.id) {
        "followers" -> it.copy(value = people.followers?.size?.toString() ?: it.value)
        "following" -> it.copy(value = people.following?.size?.toString() ?: it.value)
        else -> it
    }
})
