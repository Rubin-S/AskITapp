package com.askit.app.home.model

data class ServicePreview(
    val id: String,
    val title: String,
    val category: String,
    val providerName: String,
    val providerAvatarUrl: String? = null,
    val startingPriceLabel: String? = null,
    val rating: Double? = null,
)
