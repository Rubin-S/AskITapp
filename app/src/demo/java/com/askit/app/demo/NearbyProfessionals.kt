package com.askit.app.demo

import com.askit.app.home.model.PersonPreview

/** Shared local fixtures for nearby cards and their profile destinations. */
internal fun sampleNearbyProfessionals(): List<PersonPreview> = listOf(
    PersonPreview(
        id = "pro-lucas",
        name = "Lucas Scott",
        trade = "Master Electrician",
        rating = 4.95,
        completedJobsCount = 142,
        locationLabel = "North District",
        avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300",
    ),
    PersonPreview(
        id = "pro-amara",
        name = "Amara Chen",
        trade = "Interior Architect",
        rating = 4.92,
        completedJobsCount = 88,
        locationLabel = "Downtown",
        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
    ),
    PersonPreview(
        id = "pro-carlos",
        name = "Carlos Rivera",
        trade = "Master Plumber",
        rating = 4.88,
        completedJobsCount = 196,
        locationLabel = "Westside",
        avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
    ),
)
