package com.askit.app.home.details

import com.askit.app.profile.ProfileHighlight
import com.askit.app.profile.ProfileReview
import com.askit.app.profile.RatingSummary
import com.askit.app.session.ServiceListing

/** Another person's public profile, as the server (or the preview fixtures) describe it. */
data class UserProfileData(
    val id: String,
    val username: String,
    val name: String,
    val trade: String = "",
    val avatarUrl: String? = null,
    val directionalReviews: List<ProfileReview> = emptyList(),
    /** Aggregate shown when the full review list isn't loaded (e.g. "4.9 (48)" as on Explore). */
    val ratingSummary: RatingSummary? = null,
    val completedAsProvider: Int? = null,
    val completedAsSeeker: Int? = null,
    val location: String = "",
    val joinedYear: String = "",
    val bio: String = "",
    val isProvider: Boolean = false,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val isVerified: Boolean = false,
    val bannerUrl: String? = null,
    val highlights: List<ProfileHighlight> = emptyList(),
    val listing: ServiceListing? = null,
    val discloseApproximateLocation: Boolean = false,
    val postcodeDistanceKm: Double? = null,
)
