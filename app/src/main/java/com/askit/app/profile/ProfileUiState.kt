package com.askit.app.profile

import com.askit.app.home.details.UserProfileData
import com.askit.app.jobs.Job
import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import com.askit.app.session.isAvailableOn
import com.askit.designsystem.profile.ProfileMetricItem
import java.time.LocalDate

/** Section tab ids; the scaffold picks owner or visitor labels for them. */
internal object ProfileTab {
    const val ABOUT = "about"
    const val SHOWCASE = "showcase"
    const val SERVICES = "services"
    const val POSTS = "posts"
    const val TASKS = "tasks"

    fun forRole(isProvider: Boolean) = if (isProvider) listOf(ABOUT, SHOWCASE, SERVICES, TASKS) else listOf(POSTS, TASKS)
}

data class ProfileUiState(
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val highlights: List<ProfileHighlight> = emptyList(),
    val approximateDistanceKm: Int? = null,
    val localityLine: String = "",
    val isVerified: Boolean = false,
    val isProvider: Boolean = false,
    val tradeHeadline: String? = null,
    val bio: String = "",
    val metrics: List<ProfileMetricItem> = emptyList(),
    val tabs: List<String> = emptyList(),
    val selectedSectionId: String = "",
    val isPublicPreview: Boolean = false,
    val isFollowing: Boolean = false,
    val isOwner: Boolean = false,
    val listing: ServiceListing? = null,
    val reviews: List<ProfileReview> = emptyList(),
    val joinedYear: String = "",
) {
    /** The owner looking at their own profile (not "View as"): the only case that shows owner controls and copy. */
    val isOwnerView: Boolean get() = isOwner && !isPublicPreview
    val displayUsername: String get() = username.removePrefix("@").ifBlank { displayName }
}

fun SessionProfile.toUiState(
    jobs: List<Job> = emptyList(),
    isPublicPreview: Boolean = false,
    selectedSectionId: String = "",
    today: LocalDate = LocalDate.now(),
): ProfileUiState {
    val provider = listing != null
    return ProfileUiState(
        userId = accountId,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
        bannerUrl = bannerUrl,
        highlights = highlights,
        localityLine = city,
        joinedYear = joinedYear,
        isVerified = isVerified,
        isProvider = provider,
        tradeHeadline = listing?.title ?: title.takeIf(String::isNotBlank),
        bio = listing?.description ?: bio,
        metrics = profileMetrics(
            accountId,
            provider,
            reviews,
            jobs.profileCompletedCount(accountId, provider),
            followerCount,
            followingCount,
        ),
        tabs = ProfileTab.forRole(provider),
        selectedSectionId = selectedSectionId.validIn(ProfileTab.forRole(provider)),
        isPublicPreview = isPublicPreview,
        isOwner = true,
        listing = effectiveListing(today),
        reviews = reviews,
    )
}

fun UserProfileData.toUiState(
    selectedSectionId: String = "",
    viewerPostcode: String? = null,
    isFollowing: Boolean = false,
): ProfileUiState = ProfileUiState(
    userId = id,
    username = username,
    displayName = name,
    avatarUrl = avatarUrl,
    bannerUrl = bannerUrl,
    highlights = highlights,
    approximateDistanceKm = approximateProfileDistance(discloseApproximateLocation, viewerPostcode, postcodeDistanceKm),
    localityLine = location,
    joinedYear = joinedYear,
    isVerified = isVerified,
    isProvider = isProvider,
    tradeHeadline = listing?.title ?: trade.takeIf(String::isNotBlank),
    bio = listing?.description ?: bio,
    metrics = profileMetrics(
        id,
        isProvider,
        directionalReviews,
        if (isProvider) completedAsProvider else completedAsSeeker,
        followerCount,
        followingCount,
        ratingSummary,
    ),
    tabs = ProfileTab.forRole(isProvider),
    selectedSectionId = selectedSectionId.validIn(ProfileTab.forRole(isProvider)),
    isFollowing = isFollowing,
    listing = listing,
    reviews = directionalReviews,
)

private fun String.validIn(tabs: List<String>) = takeIf { it in tabs } ?: tabs.first()

/** The listing as others see it today: paused while an availability window covers [today]. */
private fun SessionProfile.effectiveListing(today: LocalDate): ServiceListing? =
    listing?.let { it.copy(live = it.live && availability.isAvailableOn(today)) }

/** Public adapter: never carries Form A contact or postcode, nor listing contact, whatever the writers did. */
internal fun SessionProfile.toPublicProfile(today: LocalDate = LocalDate.now()) = UserProfileData(
    id = accountId, username = username, name = displayName, avatarUrl = avatarUrl,
    trade = listing?.title ?: title, bio = listing?.description ?: bio, isProvider = listing != null,
    location = city, joinedYear = joinedYear, isVerified = isVerified,
    followerCount = followerCount, followingCount = followingCount,
    bannerUrl = bannerUrl, highlights = highlights, listing = effectiveListing(today)?.copy(phoneNumber = "", email = ""),
    directionalReviews = reviews, discloseApproximateLocation = discloseApproximateLocation,
)
