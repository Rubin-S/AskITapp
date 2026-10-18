package com.askit.app.task

import android.content.Context
import com.askit.app.R
import com.askit.app.category.ASKIT_SERVICE_CATEGORIES
import com.askit.app.posttask.PostTaskBudget
import com.askit.app.posttask.PostTaskMaterialsPolicy
import com.askit.app.posttask.PostTaskTimingMode
import com.askit.app.posttask.PostTaskWorkMode
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class TaskPresentation(
    val category: String,
    val budget: String,
    val location: String,
    val timing: String,
    val requirements: List<String>,
    val posted: String,
)

fun Task.presentation(context: Context): TaskPresentation {
    val data = publicDetails
    val categoryLabel = ASKIT_SERVICE_CATEGORIES.firstOrNull { it.id == categoryId }
        ?.takeIf { categoryId != "other" }?.let { context.getString(it.labelRes) } ?: category
    if (data == null) return TaskPresentation(categoryLabel, budget, location, urgency, requirements, postedTime)
    return TaskPresentation(
        category = categoryLabel,
        budget = when (val value = data.budget) {
            is PostTaskBudget.Fixed -> context.getString(R.string.post_task_budget_fixed_value, value.amount)
            is PostTaskBudget.Range -> context.getString(R.string.post_task_budget_range_display, value.minimum, value.maximum)
            PostTaskBudget.RequestQuotes, null -> context.getString(R.string.post_task_request_quotes)
        },
        location = when (data.workMode) {
            PostTaskWorkMode.AT_MY_LOCATION -> location
            PostTaskWorkMode.AT_PROVIDER_LOCATION -> context.getString(R.string.post_task_at_provider_location)
            PostTaskWorkMode.REMOTE -> context.getString(R.string.post_task_remote)
        },
        timing = when (data.timingMode) {
            PostTaskTimingMode.ASAP -> context.getString(R.string.post_task_asap)
            PostTaskTimingMode.DATE -> data.selectedDateMillis?.let {
                formatTaskDate(it, context.resources.configuration.locales[0])
            }.orEmpty()
            PostTaskTimingMode.FLEXIBLE -> context.getString(R.string.post_task_flexible)
        },
        requirements = requirements + buildList {
            data.itemDetails?.quantityOrMeasurements?.let { add(context.getString(R.string.task_public_quantity, it)) }
            data.itemDetails?.brandOrModel?.let { add(context.getString(R.string.task_public_brand, it)) }
            data.itemDetails?.materialsPolicy?.let { add(context.getString(R.string.task_public_materials, context.getString(it.labelRes))) }
        },
        posted = context.getString(R.string.task_just_posted),
    )
}

val PostTaskMaterialsPolicy.labelRes: Int
    get() = when (this) {
        PostTaskMaterialsPolicy.REQUESTER_HAS_MATERIALS -> R.string.post_task_materials_requester
        PostTaskMaterialsPolicy.PROVIDER_BRINGS_MATERIALS -> R.string.post_task_materials_provider
        PostTaskMaterialsPolicy.DECIDE_AFTER_INSPECTION -> R.string.post_task_materials_decide_later
    }

val TaskGenderPreference.labelRes: Int
    get() = when (this) {
        TaskGenderPreference.ANY -> R.string.task_preference_none
        TaskGenderPreference.MALE -> R.string.task_preference_male
        TaskGenderPreference.FEMALE -> R.string.task_preference_female
        TaskGenderPreference.OTHER -> R.string.task_preference_other
    }

val TaskLanguage.labelRes: Int
    get() = when (this) {
        TaskLanguage.ENGLISH -> R.string.task_language_english
        TaskLanguage.TAMIL -> R.string.task_language_tamil
    }

// Material date-picker values represent midnight UTC, independent of the device timezone.
fun formatTaskDate(millis: Long, locale: Locale = Locale.getDefault()): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM, locale).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(millis))
