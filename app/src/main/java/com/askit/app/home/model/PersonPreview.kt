package com.askit.app.home.model

data class PersonPreview(
    val id: String,
    val name: String,
    val trade: String,
    val avatarUrl: String? = null,
    val rating: Double? = null,
    val completedJobsCount: Int = 0,
    val locationLabel: String,
)
