package com.askit.app.task

import com.askit.app.explore.ExploreTaskResult
import com.askit.app.home.model.TaskPreview
import com.askit.designsystem.tasks.TaskResultStatus

data class Task(
    val id: String,
    val title: String,
    val category: String,
    val categoryId: String? = null,
    val budget: String,
    val location: String,
    val posterName: String,
    val posterAvatarUrl: String? = null,
    val urgency: String,
    val postedTime: String,
    val description: String,
    val requirements: List<String> = emptyList(),
    val photos: List<String> = emptyList(),
    val status: String = "Open",
    val createdAtMillis: Long = System.currentTimeMillis(),
)

fun Task.toTaskPreview(): TaskPreview = TaskPreview(
    id = id,
    title = title,
    category = category,
    budgetLabel = budget,
    locationLabel = location,
    posterName = posterName,
    urgencyLabel = urgency,
)

fun Task.toExploreTaskResult(): ExploreTaskResult = ExploreTaskResult(
    id = id,
    title = title,
    category = category,
    summary = description,
    budgetLabel = budget,
    locationLabel = location,
    timingLabel = urgency,
    posterName = posterName,
    postedLabel = postedTime,
    status = TaskResultStatus.Open,
    scopeHighlights = requirements,
)
