package com.askit.app.explore

/** What one Explore search asks for. Filters and sort are applied on the device, so they are not part of it. */
data class ExploreSearchRequest(val query: String, val searchArea: ExploreSearchArea)

/**
 * Where Explore's remote content comes from: people/service search results and the browse
 * sections shown before a query is submitted. Local tasks are merged in on the screen, so a
 * source only answers for what has to come from a backend.
 *
 * Implementations return failure states ([ExploreResultState.Failure],
 * [ExploreResultState.ContentStatus.PartialFailure], [ExploreBrowseStatus.Offline],
 * [ExploreBrowseStatus.ServerUnavailable]) instead of throwing; the view model offers Retry for them.
 */
interface ExploreContentSource {
    suspend fun search(request: ExploreSearchRequest): ExploreResultState

    suspend fun loadBrowseSection(section: ExploreBrowseSection, searchArea: ExploreSearchArea): ExploreBrowseStatus
}

/**
 * Source backed by fixed content: demo fixtures in debug/preview, nothing in release. When the
 * professionals section is unavailable (release has no search backend yet), searches report the
 * people/services source as unavailable so the screen says so and offers Retry, instead of
 * pretending there were simply no matches.
 */
class FixedExploreContentSource(
    private val people: () -> List<ExplorePersonResult>,
    private val browseState: () -> ExploreBrowseState,
) : ExploreContentSource {
    override suspend fun search(request: ExploreSearchRequest): ExploreResultState {
        val peopleStatus = browseState().professionals
        val peopleUnavailable = peopleStatus == ExploreBrowseStatus.ServerUnavailable ||
            peopleStatus == ExploreBrowseStatus.Offline
        return ExploreResultState.Results(
            people = if (peopleUnavailable) emptyList() else people(),
            tasks = emptyList(),
            status = ExploreResultState.ContentStatus.PartialFailure(ExploreResultState.Source.PeopleAndServices)
                .takeIf { peopleUnavailable },
        )
    }

    override suspend fun loadBrowseSection(section: ExploreBrowseSection, searchArea: ExploreSearchArea): ExploreBrowseStatus =
        browseState().statusOf(section)
}

internal fun ExploreBrowseState.statusOf(section: ExploreBrowseSection): ExploreBrowseStatus = when (section) {
    ExploreBrowseSection.Services -> services
    ExploreBrowseSection.Professionals -> professionals
    ExploreBrowseSection.Tasks -> tasks
}

internal fun ExploreBrowseState.withStatus(section: ExploreBrowseSection, status: ExploreBrowseStatus): ExploreBrowseState = when (section) {
    ExploreBrowseSection.Services -> copy(services = status)
    ExploreBrowseSection.Professionals -> copy(professionals = status)
    ExploreBrowseSection.Tasks -> copy(tasks = status)
}

/**
 * Merges the device's live task list into remote results. Tasks never come from the remote
 * source, so a remote failure or loading state is shown as is.
 */
internal fun ExploreResultState.withLocalTasks(tasks: List<ExploreTaskResult>): ExploreResultState = when (this) {
    is ExploreResultState.Results -> copy(tasks = tasks)
    else -> this
}
