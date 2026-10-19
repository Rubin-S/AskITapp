package com.askit.app.preview

import com.askit.app.home.details.UserProfileData
import com.askit.app.profile.ProfileReview
import com.askit.app.profile.RatingSummary
import com.askit.app.profile.ReviewDirection

/** A demo review received by [recipient] as a provider; the aggregate on the profile comes from Explore. */
private fun review(recipient: String, index: Int, reviewer: String, rating: Float, meta: String, body: String) = ProfileReview(
    id = "$recipient-review-$index", name = reviewer, meta = meta, rating = rating, body = body, createdAtMillis = 0,
    reviewerAccountId = "demo-reviewer-$index", recipientAccountId = recipient, direction = ReviewDirection.SEEKER_TO_PROVIDER,
)

private val skillsById = mapOf(
    "pro-1" to listOf("Inverter Repair", "Ceiling Fan Installation", "Wiring & Rewiring", "Switchboard Upgrades", "Emergency Diagnostics"),
    "pro-2" to listOf("Pipe Fitting", "Leak Detection", "Geyser Repair", "Drain Cleaning", "Bathroom Fixtures"),
)

internal fun previewPublicProfile(userId: String): UserProfileData? = (
    when (userId) {
        "member-1", "user-1" -> UserProfileData(
            id = userId,
            username = "rahul_verma",
            name = "Rahul Verma",
            trade = "",
            avatarUrl = null,
            isProvider = false,
            followerCount = 84,
            followingCount = 31,
            location = "Mission Street, Puducherry",
            joinedYear = "2023",
            bio = "Active community member seeking home renovation and electronics assistance.",
        )

        "pro-1" -> UserProfileData(
            id = userId,
            username = "priya_electric",
            name = "Priya Sharma",
            trade = "Certified Electrician",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
            isProvider = true,
            ratingSummary = RatingSummary(4.9, 48),
            completedAsProvider = 48,
            location = "Muthialpet, Puducherry",
            joinedYear = "2022",
            bio = "Certified electrician with 8+ years of residential and commercial wiring experience. " +
                "Specializing in inverter repairs, fuse box upgrades, ceiling fan installation, and safety audits.",
            directionalReviews = listOf(
                review(
                    userId,
                    1,
                    "Karthik V.",
                    5f,
                    "2 days ago",
                    "Priya fixed our inverter and installed 2 new ceiling fans in under an hour. Super clean work!",
                ),
                review(userId, 2, "Meera R.", 4.8f, "1 week ago", "Diagnosed the tripping circuit issue promptly. Highly recommended."),
            ),
        )

        "pro-2" -> UserProfileData(
            id = userId,
            username = "arun_plumbing",
            name = "Arun Kumar",
            trade = "Master Plumber",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
            isProvider = true,
            ratingSummary = RatingSummary(4.8, 62),
            completedAsProvider = 62,
            location = "Muthialpet, Puducherry",
            joinedYear = "2021",
            bio = "Licensed plumber handling leak detection, pipeline replacements, sanitary fittings, " +
                "and water heater servicing across Gandhipuram.",
            directionalReviews = listOf(
                review(userId, 1, "Suresh K.", 4.8f, "Yesterday", "Arun found the hidden pipe leakage in 15 minutes. Transparent pricing."),
            ),
        )

        else -> com.askit.app.demo.sampleNearbyProfessionals()
            .firstOrNull { it.id == userId }?.let { person ->
                UserProfileData(
                    id = person.id,
                    username = person.id.removePrefix("pro-"),
                    name = person.name,
                    trade = person.trade,
                    avatarUrl = person.avatarUrl,
                    isProvider = true,
                    ratingSummary = person.rating?.let { RatingSummary(it, person.completedJobsCount) },
                    completedAsProvider = person.completedJobsCount,
                )
            }
    }
    )?.let { profile ->
    profile.copy(
        listing = if (profile.isProvider) {
            com.askit.app.session.ServiceListing(
                title = profile.trade, category = profile.trade, description = profile.bio,
                quoteLabel = "", coverage = profile.location, coverageHint = "", hours = "", hoursHint = "",
                response = "", responseHint = "", tags = skillsById[profile.id].orEmpty(), experience = "",
            )
        } else {
            null
        },
    )
}
