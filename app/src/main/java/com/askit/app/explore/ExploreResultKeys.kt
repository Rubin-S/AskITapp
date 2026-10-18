package com.askit.app.explore

import androidx.compose.foundation.layout.size

internal fun distinctPeopleById(people: List<ExplorePersonResult>): List<ExplorePersonResult> {
    val merged = mutableListOf<ExplorePersonResult>()
    val indicesById = linkedMapOf<String, Int>()
    people.forEach { person ->
        val stableId = person.id.trim()
        if (stableId.isEmpty()) {
            merged += person
        } else {
            val existingIndex = indicesById[stableId]
            if (existingIndex == null) {
                indicesById[stableId] = merged.size
                merged += person
            } else {
                val existing = merged[existingIndex]
                merged[existingIndex] = existing.copy(
                    additionalServices = (existing.additionalServices + person.additionalServices).distinct(),
                    matchReasons = existing.matchReasons + person.matchReasons,
                )
            }
        }
    }
    return merged
}

internal fun buildExplorePersonRenderRows(
    people: List<ExplorePersonResult>,
): List<ExplorePersonRenderRow> {
    val anonymousOccurrences = mutableMapOf<String, Int>()
    return people.map { person ->
        val stableId = person.id.trim()
        val uiKey = if (stableId.isNotEmpty()) {
            stableId
        } else {
            val fingerprint = explorePersonContentFingerprint(person)
            val occurrence = anonymousOccurrences.getOrDefault(fingerprint, 0)
            anonymousOccurrences[fingerprint] = occurrence + 1
            "anonymous-person:$fingerprint:$occurrence"
        }
        ExplorePersonRenderRow(
            result = person,
            stableId = stableId,
            uiKey = uiKey,
        )
    }
}

internal fun buildExploreTaskRenderRows(
    tasks: List<ExploreTaskResult>,
): List<ExploreTaskRenderRow> {
    val anonymousOccurrences = mutableMapOf<String, Int>()
    return tasks.map { task ->
        val stableId = task.id.trim()
        val uiKey = if (stableId.isNotEmpty()) {
            stableId
        } else {
            val fingerprint = exploreTaskContentFingerprint(task)
            val occurrence = anonymousOccurrences.getOrDefault(fingerprint, 0)
            anonymousOccurrences[fingerprint] = occurrence + 1
            "anonymous-task:$fingerprint:$occurrence"
        }
        ExploreTaskRenderRow(
            result = task,
            stableId = stableId,
            uiKey = uiKey,
        )
    }
}

private fun explorePersonContentFingerprint(person: ExplorePersonResult): String = buildString {
    appendExploreKeyPart(person.name)
    appendExploreKeyPart(person.avatarUrl)
    appendExploreKeyPart(person.primaryService)
    appendExploreKeyParts(person.additionalServices)
    appendExploreKeyPart(person.rating?.toString())
    appendExploreKeyPart(person.reviewCount.toString())
    appendExploreKeyPart(person.locationLabel)
    appendExploreKeyPart(person.priceLabel)
    appendExploreKeyPart(person.statusLabel)
    appendExploreKeyParts(person.matchReasons.map(PersonMatchReason::name).sorted())
}

private fun exploreTaskContentFingerprint(task: ExploreTaskResult): String = buildString {
    appendExploreKeyPart(task.title)
    appendExploreKeyPart(task.category)
    appendExploreKeyPart(task.summary)
    appendExploreKeyPart(task.budgetLabel)
    appendExploreKeyPart(task.locationLabel)
    appendExploreKeyPart(task.timingLabel)
    appendExploreKeyPart(task.posterName)
    appendExploreKeyPart(task.postedLabel)
    appendExploreKeyPart(task.status.name)
    appendExploreKeyPart(task.photoModels.size.toString())
    appendExploreKeyPart(task.distanceLabel)
    appendExploreKeyParts(task.scopeHighlights)
}

private fun StringBuilder.appendExploreKeyParts(values: List<String>) {
    append(values.size)
    values.forEach { value -> appendExploreKeyPart(value) }
}

private fun StringBuilder.appendExploreKeyPart(value: String?) {
    val normalized = value.orEmpty()
    append(normalized.length)
    append(':')
    append(normalized)
}

internal fun distinctTasksById(tasks: List<ExploreTaskResult>): List<ExploreTaskResult> {
    val seenIds = mutableSetOf<String>()
    return tasks.filter { task ->
        val stableId = task.id.trim()
        stableId.isEmpty() || seenIds.add(stableId)
    }
}

internal fun exploreStatusKey(status: ExploreResultState.ContentStatus): String = when (status) {
    ExploreResultState.ContentStatus.Stale -> "stale"
    ExploreResultState.ContentStatus.OfflineCached -> "offline-cached"
    is ExploreResultState.ContentStatus.PartialFailure -> "partial-${status.source.name.lowercase()}"
}

internal fun exploreRequestKey(
    query: String,
    searchArea: ExploreSearchArea,
): String = buildString {
    append(query)
    append('|')
    append(searchArea.placeId.orEmpty())
    append('|')
    append(searchArea.displayName)
    append('|')
    append(searchArea.supportingText.orEmpty())
    append('|')
    append(searchArea.latitude ?: "")
    append('|')
    append(searchArea.longitude ?: "")
    append('|')
    append(searchArea.radiusKm)
    append('|')
    append(searchArea.source.name)
}
