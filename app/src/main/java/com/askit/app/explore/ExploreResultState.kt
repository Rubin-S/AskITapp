package com.askit.app.explore

sealed interface ExploreResultState {
    data object Loading : ExploreResultState

    data class Results(
        val people: List<ExplorePersonResult>,
        val tasks: List<ExploreTaskResult>,
        val isRefreshing: Boolean = false,
        val status: ContentStatus? = null,
    ) : ExploreResultState

    data class Empty(
        val reason: EmptyReason,
    ) : ExploreResultState

    data class Failure(
        val reason: FailureReason,
    ) : ExploreResultState

    enum class EmptyReason {
        Query,
        Filters,
    }

    enum class Source {
        PeopleAndServices,
        Tasks,
    }

    sealed interface ContentStatus {
        data object Stale : ContentStatus

        data object OfflineCached : ContentStatus

        data class PartialFailure(
            val source: Source,
        ) : ContentStatus
    }

    sealed interface FailureReason {
        data object General : FailureReason

        data object Offline : FailureReason

        data class SourceUnavailable(
            val source: Source,
        ) : FailureReason
    }
}

internal fun normalizeExploreResultState(
    state: ExploreResultState,
    scope: ExploreResultScope,
    emptyReason: ExploreResultState.EmptyReason,
): ExploreResultState {
    if (state !is ExploreResultState.Results) return state

    val normalizedStatus = normalizeExploreContentStatus(listOfNotNull(state.status))
    val partialFailure = normalizedStatus as? ExploreResultState.ContentStatus.PartialFailure
    if (partialFailure != null && partialFailure.source == requiredSourceFor(scope)) {
        return ExploreResultState.Failure(
            ExploreResultState.FailureReason.SourceUnavailable(partialFailure.source),
        )
    }

    val people = when (scope) {
        ExploreResultScope.All -> if (
            partialFailure?.source == ExploreResultState.Source.PeopleAndServices
        ) {
            emptyList()
        } else {
            state.people
        }

        ExploreResultScope.People -> state.people.filter { result ->
            PersonMatchReason.Identity in result.matchReasons
        }

        ExploreResultScope.Services -> state.people.filter { result ->
            PersonMatchReason.Service in result.matchReasons && !result.primaryService.isNullOrBlank()
        }

        ExploreResultScope.Tasks -> emptyList()
    }
    val tasks = when (scope) {
        ExploreResultScope.All -> if (
            partialFailure?.source == ExploreResultState.Source.Tasks
        ) {
            emptyList()
        } else {
            state.tasks
        }

        ExploreResultScope.Tasks -> state.tasks
        ExploreResultScope.People,
        ExploreResultScope.Services,
        -> emptyList()
    }
    val hasRows = people.isNotEmpty() || tasks.isNotEmpty()
    if (!hasRows) {
        return if (normalizedStatus == ExploreResultState.ContentStatus.OfflineCached) {
            ExploreResultState.Failure(ExploreResultState.FailureReason.Offline)
        } else if (partialFailure != null && scope == ExploreResultScope.All) {
            ExploreResultState.Failure(
                ExploreResultState.FailureReason.SourceUnavailable(partialFailure.source),
            )
        } else {
            ExploreResultState.Empty(emptyReason)
        }
    }

    val status = when (partialFailure?.source) {
        ExploreResultState.Source.Tasks -> partialFailure.takeIf {
            scope == ExploreResultScope.All
        }

        ExploreResultState.Source.PeopleAndServices -> partialFailure.takeIf {
            scope == ExploreResultScope.All
        }

        null -> normalizedStatus
    }
    return state.copy(
        people = people,
        tasks = tasks,
        status = status,
    )
}

internal fun normalizeExploreContentStatus(
    statuses: Iterable<ExploreResultState.ContentStatus>,
): ExploreResultState.ContentStatus? = statuses.minByOrNull { status ->
    when (status) {
        is ExploreResultState.ContentStatus.PartialFailure -> 0
        ExploreResultState.ContentStatus.OfflineCached -> 1
        ExploreResultState.ContentStatus.Stale -> 2
    }
}

private fun requiredSourceFor(scope: ExploreResultScope): ExploreResultState.Source? = when (scope) {
    ExploreResultScope.People,
    ExploreResultScope.Services,
    -> ExploreResultState.Source.PeopleAndServices

    ExploreResultScope.Tasks -> ExploreResultState.Source.Tasks
    ExploreResultScope.All -> null
}
