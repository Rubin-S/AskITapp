package com.askit.app.profile

import com.askit.app.home.details.UserProfileData
import com.askit.app.jobs.Job
import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import com.askit.designsystem.profile.ProfileMetricItem
import com.askit.designsystem.profile.ProfileTabSpec
import com.askit.designsystem.R as DsR
import java.util.Locale

/**
 * Universal UI presentation model bridging own profile (SessionProfile)
 * and visitor profile (UserProfileData) into a single, cohesive presentation pipeline.
 * Adheres to the dual-identity community model:
 * - Form A (Community Member): 3 metrics, 3 tabs, member actions
 * - Form B (Service Provider): 4 metrics, 4 tabs, provider actions & trust signals
 */
data class ProfileUiState(
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val localityLine: String = "",
    val isVerified: Boolean = false,
    val isProvider: Boolean = false,
    val tradeHeadline: String? = null,
    val bio: String = "",
    val metrics: List<ProfileMetricItem> = emptyList(),
    val tabs: List<ProfileTabSpec> = emptyList(),
    val selectedTabIndex: Int = 0,
    val isPublicPreview: Boolean = false,
    val isFollowing: Boolean = false,
    val isOwner: Boolean = false,
    // Content data
    val listing: ServiceListing? = null,
    val gallery: List<ProfileGalleryItem> = emptyList(),
    val reviews: List<ProfileReview> = emptyList(),
    val lookingFor: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val languages: List<Pair<String, String>> = emptyList(),
    val licenses: List<String> = emptyList(),
    val savedProfessionals: List<SavedProfessional> = emptyList(),
    val activeJobs: List<Job> = emptyList(),
    val profileStrengthPercent: Int = 0,
)

/**
 * Maps [SessionProfile] to [ProfileUiState] according to the dual-identity community rules.
 *
 * Form A (`hasListedService == false`):
 * - 3 metrics: Activity (jobs count), Followers, Following
 * - 3 tabs: Activity, About, Reviews
 * - `isProvider = false`, `tradeHeadline = null`
 *
 * Form B (`hasListedService == true`):
 * - 4 metrics: Rating (★ and count), Completed Jobs, Followers, Following
 * - 4 tabs: Services, Showcase (Gallery), Reviews, About
 * - `isProvider = true`, `tradeHeadline = listing.category`
 */
fun SessionProfile.toUiState(
    jobs: List<Job> = emptyList(),
    isPublicPreview: Boolean = false,
    selectedTabIndex: Int = 0,
    isFollowing: Boolean = false,
): ProfileUiState {
    val isProviderMode = hasListedService

    val locality = if (city.isNotBlank() && joinedYear.isNotBlank()) {
        "$city · Joined $joinedYear"
    } else if (city.isNotBlank()) {
        city
    } else {
        "Local District · Joined 2024"
    }

    val metricsList: List<ProfileMetricItem> = if (isProviderMode) {
        val ratingString = if (reviews.isNotEmpty()) {
            val avg = reviews.map { it.rating.toDouble() }.average()
            "%.1f (%d)".format(Locale.US, avg, reviews.size)
        } else {
            "4.9 (24)"
        }
        val completedCount = jobs.profileCompletedCount()
        val completedString = if (completedCount > 0) completedCount.toString() else "48"

        listOf(
            ProfileMetricItem(
                id = "rating",
                value = ratingString,
                label = "Rating",
                iconRes = DsR.drawable.ic_star_filled,
            ),
            ProfileMetricItem(
                id = "completed_jobs",
                value = completedString,
                label = "Completed",
            ),
            ProfileMetricItem(
                id = "followers",
                value = followerCount.toString(),
                label = "Followers",
            ),
            ProfileMetricItem(
                id = "following",
                value = followingCount.toString(),
                label = "Following",
            ),
        )
    } else {
        listOf(
            ProfileMetricItem(
                id = "activity",
                value = jobs.size.toString(),
                label = "Activity",
            ),
            ProfileMetricItem(
                id = "followers",
                value = followerCount.toString(),
                label = "Followers",
            ),
            ProfileMetricItem(
                id = "following",
                value = followingCount.toString(),
                label = "Following",
            ),
        )
    }

    val tabList: List<ProfileTabSpec> = if (isProviderMode) {
        listOf(
            ProfileTabSpec("services", "Services", DsR.drawable.ic_wrench),
            ProfileTabSpec("gallery", "Showcase", DsR.drawable.ic_photo),
            ProfileTabSpec("reviews", "Reviews", DsR.drawable.ic_star_outline),
            ProfileTabSpec("about", "About", DsR.drawable.ic_person),
        )
    } else {
        listOf(
            ProfileTabSpec("activity", "Activity", DsR.drawable.ic_work),
            ProfileTabSpec("about", "About", DsR.drawable.ic_person),
            ProfileTabSpec("reviews", "Reviews", DsR.drawable.ic_star_outline),
        )
    }

    val safeTabIndex = if (tabList.isNotEmpty()) {
        selectedTabIndex.coerceIn(0, tabList.lastIndex)
    } else {
        0
    }

    return ProfileUiState(
        userId = username,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
        localityLine = locality,
        isVerified = isProviderMode,
        isProvider = isProviderMode,
        tradeHeadline = if (isProviderMode) (listing?.category ?: listing?.title ?: "Verified Specialist") else null,
        bio = bio,
        metrics = metricsList,
        tabs = tabList,
        selectedTabIndex = safeTabIndex,
        isPublicPreview = isPublicPreview,
        isFollowing = isFollowing,
        isOwner = true,
        listing = listing,
        gallery = gallery,
        reviews = reviews,
        lookingFor = lookingFor,
        skills = skills,
        languages = languages,
        licenses = licenses,
        savedProfessionals = savedProfessionals,
        activeJobs = jobs,
        profileStrengthPercent = profileStrengthPercent,
    )
}

/**
 * Maps [UserProfileData] (visitor view) to [ProfileUiState] according to dual-identity rules.
 */
fun UserProfileData.toUiState(
    selectedTabIndex: Int = 0,
    isFollowing: Boolean = false,
): ProfileUiState {
    val locality = if (location.isNotBlank() && memberSince.isNotBlank()) {
        "$location · $memberSince"
    } else {
        location.ifBlank { memberSince }
    }

    val metricsList: List<ProfileMetricItem> = if (isProvider) {
        val ratingString = if (reviews.isNotEmpty()) {
            "%.1f (%d)".format(Locale.US, rating, reviews.size)
        } else {
            "%.1f".format(Locale.US, rating)
        }
        listOf(
            ProfileMetricItem(
                id = "rating",
                value = ratingString,
                label = "Rating",
                iconRes = DsR.drawable.ic_star_filled,
            ),
            ProfileMetricItem(
                id = "completed_jobs",
                value = completedJobsCount.toString(),
                label = "Completed",
            ),
            ProfileMetricItem(
                id = "followers",
                value = followerCount.toString(),
                label = "Followers",
            ),
            ProfileMetricItem(
                id = "following",
                value = followingCount.toString(),
                label = "Following",
            ),
        )
    } else {
        listOf(
            ProfileMetricItem(
                id = "activity",
                value = activityCount.toString(),
                label = "Activity",
            ),
            ProfileMetricItem(
                id = "followers",
                value = followerCount.toString(),
                label = "Followers",
            ),
            ProfileMetricItem(
                id = "following",
                value = followingCount.toString(),
                label = "Following",
            ),
        )
    }

    val tabList: List<ProfileTabSpec> = if (isProvider) {
        listOf(
            ProfileTabSpec("services", "Services", DsR.drawable.ic_wrench),
            ProfileTabSpec("gallery", "Showcase", DsR.drawable.ic_photo),
            ProfileTabSpec("reviews", "Reviews", DsR.drawable.ic_star_outline),
            ProfileTabSpec("about", "About", DsR.drawable.ic_person),
        )
    } else {
        listOf(
            ProfileTabSpec("activity", "Activity", DsR.drawable.ic_work),
            ProfileTabSpec("about", "About", DsR.drawable.ic_person),
            ProfileTabSpec("reviews", "Reviews", DsR.drawable.ic_star_outline),
        )
    }

    val safeTabIndex = if (tabList.isNotEmpty()) {
        selectedTabIndex.coerceIn(0, tabList.lastIndex)
    } else {
        0
    }

    val mappedReviews = reviews.mapIndexed { index, r ->
        ProfileReview(
            id = "review_$index",
            name = r.reviewerName,
            meta = r.date,
            rating = r.rating.toFloat(),
            body = r.comment,
            createdAtMillis = 0L,
        )
    }

    val providerListing = if (isProvider && trade.isNotBlank()) {
        ServiceListing(
            title = trade,
            category = trade,
            description = bio,
            quoteLabel = "Visit / Quote",
            coverage = location,
            coverageHint = "Service Area",
            hours = "Mon - Sat 9AM - 6PM",
            hoursHint = "Working Hours",
            response = "< 1 hr",
            responseHint = "Response Time",
            tags = skills,
            experience = memberSince,
            live = true,
        )
    } else {
        null
    }

    return ProfileUiState(
        userId = id,
        username = username,
        displayName = name,
        avatarUrl = avatarUrl,
        localityLine = locality,
        isVerified = isProvider,
        isProvider = isProvider,
        tradeHeadline = if (isProvider && trade.isNotBlank()) trade else null,
        bio = bio,
        metrics = metricsList,
        tabs = tabList,
        selectedTabIndex = safeTabIndex,
        isPublicPreview = false,
        isFollowing = isFollowing,
        isOwner = false,
        listing = providerListing,
        gallery = emptyList(),
        reviews = mappedReviews,
        lookingFor = emptyList(),
        skills = skills,
        languages = emptyList(),
        licenses = emptyList(),
        savedProfessionals = emptyList(),
        activeJobs = emptyList(),
        profileStrengthPercent = if (isProvider) 95 else 70,
    )
}
