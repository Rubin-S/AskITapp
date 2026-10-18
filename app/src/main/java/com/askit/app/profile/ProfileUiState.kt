package com.askit.app.profile

import com.askit.app.home.details.UserProfileData
import com.askit.app.jobs.Job
import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import com.askit.designsystem.navigation.SectionTab
import com.askit.designsystem.profile.ProfileMetricItem

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
    val tabs: List<SectionTab> = emptyList(),
    val selectedSectionId: String = "",
    val isPublicPreview: Boolean = false,
    val isFollowing: Boolean = false,
    val isOwner: Boolean = false,
    val listing: ServiceListing? = null,
    val reviews: List<ProfileReview> = emptyList(),
    val joinedYear: String = "",
) {
    val canEditMedia: Boolean get() = isOwner && !isPublicPreview
    val displayUsername: String get() = username.removePrefix("@").ifBlank { displayName }
}

fun SessionProfile.toUiState(
    jobs: List<Job> = emptyList(),
    isPublicPreview: Boolean = false,
    selectedSectionId: String = "",
    isFollowing: Boolean = false,
): ProfileUiState {
    val isProviderMode = listing != null

    val metricsList = profileMetrics(
        accountId,
        isProviderMode,
        reviews,
        jobs.profileCompletedCount(accountId, isProviderMode),
        followerCount,
        followingCount,
    )

    val tabList = profileTabs(isProviderMode)

    val safeSectionId = selectedSectionId.takeIf { id -> tabList.any { it.id == id } } ?: tabList.first().id

    return ProfileUiState(
        userId = accountId,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
        bannerUrl = bannerUrl, highlights = highlights,
        localityLine = city,
        joinedYear = joinedYear,
        isVerified = isVerified,
        isProvider = isProviderMode,
        tradeHeadline = if (isProviderMode) listing.title else title.takeIf(String::isNotBlank),
        bio = if (isProviderMode) listing.description else bio,
        metrics = metricsList,
        tabs = tabList,
        selectedSectionId = safeSectionId,
        isPublicPreview = isPublicPreview,
        isFollowing = isFollowing,
        isOwner = true,
        listing = listing,
        reviews = reviews,
    )
}

fun UserProfileData.toUiState(
    selectedSectionId: String = "",
    viewerPostcode: String? = null,
    isFollowing: Boolean = false,
): ProfileUiState {
    val metricsList = profileMetrics(
        id,
        isProvider,
        directionalReviews,
        if (isProvider) completedAsProvider else completedAsSeeker,
        followerCount,
        followingCount,
    )

    val tabList = profileTabs(isProvider)

    val safeSectionId = selectedSectionId.takeIf { id -> tabList.any { it.id == id } } ?: tabList.first().id

    val mappedReviews = directionalReviews

    val providerListing = listing

    return ProfileUiState(
        userId = id,
        username = username,
        displayName = name,
        avatarUrl = avatarUrl, bannerUrl = bannerUrl, highlights = highlights,
        approximateDistanceKm = approximateProfileDistance(discloseApproximateLocation, viewerPostcode, postcodeDistanceKm),
        localityLine = location,
        joinedYear = joinedYear,
        isVerified = isVerified,
        isProvider = isProvider,
        tradeHeadline = providerListing?.title ?: trade.takeIf(String::isNotBlank),
        bio = providerListing?.description ?: bio,
        metrics = metricsList,
        tabs = tabList,
        selectedSectionId = safeSectionId,
        isPublicPreview = false,
        isFollowing = isFollowing,
        isOwner = false,
        listing = providerListing,
        reviews = mappedReviews,
    )
}

private fun profileTabs(isProvider: Boolean): List<SectionTab> = if (isProvider) {
    listOf(
        SectionTab("about", "About"),
        SectionTab("showcase", "Showcase"),
        SectionTab("services", "My services"),
        SectionTab("tasks", "Post your task"),
    )
} else {
    listOf(
        SectionTab("posts", "My post"),
        SectionTab("tasks", "Post your task"),
    )
}

/** Public adapter deliberately excludes private Form A contact/postcode fields. */
internal fun SessionProfile.toPublicProfile() = UserProfileData(
    id = accountId, username = username, name = displayName, avatarUrl = avatarUrl,
    trade = listing?.title ?: title, bio = listing?.description ?: bio, isProvider = listing != null,
    location = city, joinedYear = joinedYear, skills = skills, isVerified = isVerified,
    followerCount = followerCount, followingCount = followingCount,
    bannerUrl = bannerUrl, highlights = highlights, listing = listing, directionalReviews = reviews,
    discloseApproximateLocation = discloseApproximateLocation,
)
