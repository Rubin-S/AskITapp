package com.askit.app.home.model

data class TaskPreview(
    val id: String,
    val title: String,
    val category: String,
    val budgetLabel: String,
    val locationLabel: String,
    val posterName: String,
    val urgencyLabel: String? = null,
)
