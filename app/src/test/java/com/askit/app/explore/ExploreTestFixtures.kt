package com.askit.app.explore

import com.askit.designsystem.tasks.TaskResultStatus

internal fun person(
    id: String,
    name: String = "Person $id",
    rating: Double? = 4.2,
    reviewCount: Int = 12,
    primaryService: String? = "Plumbing",
    additionalServices: List<String> = emptyList(),
    matchReasons: Set<PersonMatchReason> = setOf(PersonMatchReason.Identity, PersonMatchReason.Service),
) = ExplorePersonResult(
    id = id,
    name = name,
    avatarUrl = null,
    primaryService = primaryService,
    additionalServices = additionalServices,
    rating = rating,
    reviewCount = reviewCount,
    locationLabel = "Karaikal",
    priceLabel = null,
    statusLabel = null,
    matchReasons = matchReasons,
)

internal fun task(id: String, title: String = "Task $id") = ExploreTaskResult(
    id = id,
    title = title,
    category = "Cleaning",
    summary = null,
    budgetLabel = "",
    locationLabel = "Karaikal",
    timingLabel = "Today",
    posterName = "Poster",
    postedLabel = "1h",
    status = TaskResultStatus.Open,
)

internal fun area(radiusKm: Int = 10) = ExploreSearchArea(
    placeId = "p1",
    displayName = "Karaikal",
    supportingText = "Puducherry",
    latitude = 10.9,
    longitude = 79.8,
    radiusKm = radiusKm,
    source = ExploreLocationSource.SAVED,
)
