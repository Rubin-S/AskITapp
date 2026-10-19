package com.askit.app.preview

import com.askit.app.home.details.UserProfileData
import com.askit.app.home.details.UserReviewData

internal fun previewPublicProfile(userId: String): UserProfileData? = (
    when (userId) {
        "member-1", "user-1" -> UserProfileData(
            id = userId,
            username = "rahul_verma",
            name = "Rahul Verma",
            trade = "",
            avatarUrl = null,
            isProvider = false,
            rating = 0.0,
            completedJobsCount = 0,
            followerCount = 84,
            followingCount = 31,
            location = "Mission Street, Puducherry",
            joinedYear = "2023",
            bio = "Active community member seeking home renovation and electronics assistance.",
            skills = emptyList(),
            reviews = emptyList(),
        )

        "pro-1" -> UserProfileData(
            id = userId,
            username = "priya_electric",
            name = "Priya Sharma",
            trade = "Certified Electrician",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
            rating = 4.9,
            completedJobsCount = 48,
            location = "Muthialpet, Puducherry",
            joinedYear = "2022",
            bio = "Certified electrician with 8+ years of residential and commercial wiring experience. Specializing in inverter repairs, fuse box upgrades, ceiling fan installation, and safety audits.",
            skills = listOf(
                "Inverter Repair",
                "Ceiling Fan Installation",
                "Wiring & Rewiring",
                "Switchboard Upgrades",
                "Emergency Diagnostics",
            ),
            reviews = listOf(
                UserReviewData(
                    reviewerName = "Karthik V.",
                    rating = 5.0,
                    date = "2 days ago",
                    comment = "Priya fixed our inverter and installed 2 new ceiling fans in under an hour. Super clean work!",
                ),
                UserReviewData(
                    reviewerName = "Meera R.",
                    rating = 4.9,
                    date = "1 week ago",
                    comment = "Diagnosed the tripping circuit issue promptly. Highly recommended.",
                ),
            ),
        )

        "pro-2" -> UserProfileData(
            id = userId,
            username = "arun_plumbing",
            name = "Arun Kumar",
            trade = "Master Plumber",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
            rating = 4.8,
            completedJobsCount = 62,
            location = "Muthialpet, Puducherry",
            joinedYear = "2021",
            bio = "Licensed plumber handling leak detection, pipeline replacements, sanitary fittings, and water heater servicing across Gandhipuram.",
            skills = listOf("Pipe Fitting", "Leak Detection", "Geyser Repair", "Drain Cleaning", "Bathroom Fixtures"),
            reviews = listOf(
                UserReviewData(
                    reviewerName = "Suresh K.",
                    rating = 5.0,
                    date = "Yesterday",
                    comment = "Arun found the hidden pipe leakage in 15 minutes. Transparent pricing.",
                ),
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
                    rating = person.rating ?: 0.0,
                    completedJobsCount = person.completedJobsCount,
                    location = "",
                )
            }
    }
    )?.let { profile ->
    profile.copy(
        listing = if (profile.isProvider) {
            com.askit.app.session.ServiceListing(
                title = profile.trade, category = profile.trade, description = profile.bio,
                quoteLabel = "", coverage = profile.location, coverageHint = "", hours = "", hoursHint = "",
                response = "", responseHint = "", tags = profile.skills, experience = "",
            )
        } else {
            null
        },
    )
}
