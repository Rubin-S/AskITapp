package com.askit.app.explore

import androidx.lifecycle.SavedStateHandle
import com.askit.app.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExploreViewModelRetryTest {
    @get:Rule val main = MainDispatcherRule()

    /** Answers each call with the next queued response; a call with nothing queued suspends until [release]. */
    private class ScriptedSource : ExploreContentSource {
        val searchResponses = ArrayDeque<ExploreResultState>()
        val browseResponses = mutableMapOf<ExploreBrowseSection, ArrayDeque<ExploreBrowseStatus>>()
        val requests = mutableListOf<ExploreSearchRequest>()
        val browseCalls = mutableListOf<ExploreBrowseSection>()
        var gate: CompletableDeferred<Unit>? = null

        override suspend fun search(request: ExploreSearchRequest): ExploreResultState {
            requests += request
            gate?.await()
            return searchResponses.removeFirst()
        }

        override suspend fun loadBrowseSection(section: ExploreBrowseSection, searchArea: ExploreSearchArea): ExploreBrowseStatus {
            browseCalls += section
            gate?.await()
            return browseResponses[section]?.removeFirstOrNull() ?: ExploreBrowseStatus.Available
        }
    }

    private val people = listOf(person("p1"), person("p2"))

    private fun source(
        vararg search: ExploreResultState,
        professionals: List<ExploreBrowseStatus> = listOf(ExploreBrowseStatus.Available),
    ) = ScriptedSource().apply {
        searchResponses.addAll(search)
        browseResponses[ExploreBrowseSection.Services] = ArrayDeque(listOf(ExploreBrowseStatus.Available))
        browseResponses[ExploreBrowseSection.Professionals] = ArrayDeque(professionals)
        browseResponses[ExploreBrowseSection.Tasks] = ArrayDeque(listOf(ExploreBrowseStatus.Available))
    }

    @Test fun failedSearchRetriesWithTheSameQueryAndAreaThenShowsResults() = runTest {
        val source = source(
            // initial load, area change, the failing search, the retry
            ExploreResultState.Results(emptyList(), emptyList()),
            ExploreResultState.Results(emptyList(), emptyList()),
            ExploreResultState.Failure(ExploreResultState.FailureReason.Offline),
            ExploreResultState.Results(people, emptyList()),
        )
        val vm = ExploreViewModel(SavedStateHandle(), source)
        vm.onSearchAreaApplied(area(radiusKm = 25))
        vm.onFiltersChanged(ExploreResultScope.People, setOf(ExploreFilterOption.RatingFourPlus))
        vm.submitQuery("  plumber ")
        assertEquals(ExploreResultState.Failure(ExploreResultState.FailureReason.Offline), vm.resultState.value)

        source.gate = CompletableDeferred()
        vm.retryResults()
        assertEquals(ExploreResultState.Loading, vm.resultState.value)
        source.gate!!.complete(Unit)

        assertEquals(ExploreResultState.Results(people, emptyList()), vm.resultState.value)
        val retried = source.requests.last()
        assertEquals("plumber", retried.query)
        assertEquals(25, retried.searchArea.radiusKm)
        assertEquals(source.requests[source.requests.size - 2], retried)
        // Device-side filters are untouched by a retry.
        assertEquals(setOf(ExploreFilterOption.RatingFourPlus), vm.appliedFilterOptions.value[ExploreResultScope.People])
        assertEquals("plumber", vm.uiState.value.query)
    }

    @Test fun retryingAPartialFailureKeepsRowsAndShowsRefreshing() = runTest {
        val partial = ExploreResultState.Results(
            people = emptyList(),
            tasks = emptyList(),
            status = ExploreResultState.ContentStatus.PartialFailure(ExploreResultState.Source.PeopleAndServices),
        )
        val source = source(partial, ExploreResultState.Results(people, emptyList()))
        val vm = ExploreViewModel(SavedStateHandle(), source)
        assertEquals(partial, vm.resultState.value)

        source.gate = CompletableDeferred()
        vm.retryResults()
        assertEquals(partial.copy(isRefreshing = true), vm.resultState.value)
        source.gate!!.complete(Unit)
        assertEquals(ExploreResultState.Results(people, emptyList()), vm.resultState.value)
    }

    @Test fun browseSectionRetryReloadsOnlyThatSection() = runTest {
        val source = source(
            ExploreResultState.Results(emptyList(), emptyList()),
            professionals = listOf(ExploreBrowseStatus.ServerUnavailable, ExploreBrowseStatus.Available),
        )
        val vm = ExploreViewModel(SavedStateHandle(), source)
        assertEquals(ExploreBrowseStatus.ServerUnavailable, vm.browseState.value.professionals)
        source.browseCalls.clear()

        source.gate = CompletableDeferred()
        vm.retryBrowseSection(ExploreBrowseSection.Professionals)
        assertEquals(ExploreBrowseStatus.Loading, vm.browseState.value.professionals)
        assertEquals(ExploreBrowseStatus.Available, vm.browseState.value.services)
        source.gate!!.complete(Unit)

        assertEquals(ExploreBrowseStatus.Available, vm.browseState.value.professionals)
        assertEquals(listOf(ExploreBrowseSection.Professionals), source.browseCalls)
    }

    @Test fun fixedSourceReportsUnavailablePeopleSoTheScreenOffersRetry() = runTest {
        val unavailable = FixedExploreContentSource(
            people = { people },
            browseState = { ExploreBrowseState(professionals = ExploreBrowseStatus.ServerUnavailable) },
        )
        val result = unavailable.search(ExploreSearchRequest("plumber", area())) as ExploreResultState.Results
        assertTrue(result.people.isEmpty())
        assertEquals(
            ExploreResultState.ContentStatus.PartialFailure(ExploreResultState.Source.PeopleAndServices),
            result.status,
        )
        val available = FixedExploreContentSource(
            people = { people },
            browseState = { ExploreBrowseState(professionals = ExploreBrowseStatus.Available) },
        )
        assertEquals(ExploreResultState.Results(people, emptyList()), available.search(ExploreSearchRequest("", area())))
    }

    @Test fun localTasksMergeIntoResultsButNotIntoFailuresOrLoading() {
        val tasks = listOf(task("t1"))
        assertEquals(tasks, (ExploreResultState.Results(people, emptyList()).withLocalTasks(tasks) as ExploreResultState.Results).tasks)
        assertEquals(ExploreResultState.Loading, ExploreResultState.Loading.withLocalTasks(tasks))
        val failure = ExploreResultState.Failure(ExploreResultState.FailureReason.General)
        assertEquals(failure, failure.withLocalTasks(tasks))
    }

    @Test fun sortSelectionSurvivesRecreationAndResetsOnANewQuery() = runTest {
        val handle = SavedStateHandle()
        val source = source(*Array(4) { ExploreResultState.Results(people, emptyList()) })
        val vm = ExploreViewModel(handle, source)
        vm.submitQuery("plumber")
        vm.sort.select(ExploreResultScope.Tasks, ExploreSortOption.Newest)
        // Unsupported for the tab: ignored rather than stored.
        vm.sort.select(ExploreResultScope.People, ExploreSortOption.DueSoon)
        assertEquals(mapOf(ExploreResultScope.Tasks to ExploreSortOption.Newest), vm.sort.selected.value)

        val recreated = ExploreViewModel(handle, source)
        assertEquals(mapOf(ExploreResultScope.Tasks to ExploreSortOption.Newest), recreated.sort.selected.value)

        recreated.submitQuery("Plumber") // same query, different case: keeps the sort
        assertEquals(ExploreSortOption.Newest, recreated.sort.selected.value[ExploreResultScope.Tasks])
        recreated.submitQuery("electrician")
        assertTrue(recreated.sort.selected.value.isEmpty())
    }
}
