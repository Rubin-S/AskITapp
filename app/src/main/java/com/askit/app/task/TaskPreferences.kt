package com.askit.app.task

enum class TaskGenderPreference { ANY, MALE, FEMALE, OTHER }
enum class TaskLanguage { ENGLISH, TAMIL }

data class TaskProviderPreferences(
    val minimumAge: Int? = null,
    val maximumAge: Int? = null,
    val gender: TaskGenderPreference = TaskGenderPreference.ANY,
    val languages: Set<TaskLanguage> = emptySet(),
) {
    val isValid: Boolean
        get() = (minimumAge == null || minimumAge in 18..100) &&
            (maximumAge == null || maximumAge in 18..100) &&
            (minimumAge == null || maximumAge == null || minimumAge <= maximumAge)
}
