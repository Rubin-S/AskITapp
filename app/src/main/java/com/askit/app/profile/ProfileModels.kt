package com.askit.app.profile

import kotlinx.serialization.Serializable

@Serializable
data class ProfileGalleryItem(
    val id: String,
    val uri: String,
    val isCarousel: Boolean = false,
)

@Serializable
data class SavedProfessional(
    val id: String,
    val name: String,
    val trade: String,
    val rating: String,
    val avatarUrl: String? = null,
)

@Serializable
data class ProfileReview(
    val id: String,
    val name: String,
    val meta: String,
    val rating: Float,
    val body: String,
    val createdAtMillis: Long,
    val jobId: String? = null,
    val reviewerAccountId: String? = null,
    val recipientAccountId: String? = null,
    val direction: ReviewDirection? = null,
    val avatarUrl: String? = null,
)
