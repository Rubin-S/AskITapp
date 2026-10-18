package com.askit.app.task

import com.askit.app.explore.ExploreTaskResult
import com.askit.app.home.model.TaskPreview
import com.askit.designsystem.tasks.TaskResultStatus
import com.askit.app.posttask.PostTaskBudget
import com.askit.app.posttask.PostTaskDraft
import com.askit.app.posttask.PostTaskItemDetails
import com.askit.app.posttask.PostTaskTimingMode
import com.askit.app.posttask.PostTaskWorkMode

/** Public fields only: exact addresses, coordinates and access instructions stay in the draft. */
data class TaskPublicDetails(
    val workMode: PostTaskWorkMode,
    val timingMode: PostTaskTimingMode,
    val selectedDateMillis: Long?,
    val budget: PostTaskBudget?,
    val itemDetails: PostTaskItemDetails?,
    val preferences: TaskProviderPreferences,
)

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
    val posterId: String? = null,
    val publicDetails: TaskPublicDetails? = null,
    // Legacy listings can expose a numeric budget without fabricating missing public details.
    val listedBudget: PostTaskBudget? = publicDetails?.budget,
)

fun Task.toTaskPreview(): TaskPreview = TaskPreview(
    id = id,
    title = title,
    category = category,
    budgetLabel = budget,
    locationLabel = location,
    posterName = posterName,
    urgencyLabel = urgency,
    sourceTask = this,
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
    status = TaskResultStatus.entries.firstOrNull { it.name.equals(status, ignoreCase = true) }
        ?: TaskResultStatus.Unavailable,
    scopeHighlights = requirements,
    photoModels = photos,
    sourceTask = this,
)

fun PostTaskDraft.toPublicTask(id: String, posterName: String): Task {
    require(preferences.isValid)
    return Task(
        id = id,
        title = title,
        category = customCategory?.takeIf(String::isNotBlank) ?: categoryId,
        categoryId = categoryId,
        budget = when (val value = budget) {
            is PostTaskBudget.Fixed -> "₹${value.amount}"
            is PostTaskBudget.Range -> "₹${value.minimum}–₹${value.maximum}"
            PostTaskBudget.RequestQuotes, null -> "Request quotes"
        },
        location = when (workMode) {
            PostTaskWorkMode.AT_MY_LOCATION -> location?.publicAreaLabel.orEmpty()
            PostTaskWorkMode.AT_PROVIDER_LOCATION -> "At provider’s location"
            PostTaskWorkMode.REMOTE -> "Remote"
        },
        posterName = posterName,
        urgency = when (timingMode) {
            PostTaskTimingMode.ASAP -> "As soon as possible"
            PostTaskTimingMode.DATE -> selectedDateMillis?.let(::formatTaskDate).orEmpty()
            PostTaskTimingMode.FLEXIBLE -> "Flexible"
        },
        postedTime = "Just now",
        description = details,
        photos = photos.toList(),
        publicDetails = TaskPublicDetails(
            workMode = workMode,
            timingMode = timingMode,
            selectedDateMillis = selectedDateMillis,
            budget = budget,
            itemDetails = itemDetails,
            preferences = preferences.copy(languages = preferences.languages.toSet()),
        ),
    )
}
