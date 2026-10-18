package com.askit.app.jobs

import app.cash.turbine.test
import com.askit.app.session.SessionProfileStore
import com.askit.app.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class JobsViewModelFlowTest {
    @get:Rule val main = MainDispatcherRule()

    private fun viewModel(retain: Boolean = false, now: Long = 10_000L): JobsViewModel {
        val profiles = SessionProfileStore()
        return JobsViewModel(JobsStore(profiles, clock = { now }, retainInactiveAccounts = retain), profiles)
    }

    @Test fun jobsFlowFollowsTheSignedInAccountAndEmptiesOnSignOut() = runTest {
        val vm = viewModel()
        vm.jobs.test {
            assertTrue(awaitItem().isEmpty())
            vm.activateAccount("a")
            val id = vm.store.requestService("Fix tap", "Ravi")
            assertEquals(listOf(id), awaitItem().map(Job::id))
            vm.activateAccount(null)
            assertTrue(awaitItem().isEmpty())
            // Signing back in to the same account does not resurrect its jobs in production.
            vm.activateAccount("a")
            expectNoEvents()
            assertTrue(vm.jobs.value.isEmpty())
        }
    }

    @Test fun switchingAccountsNeverEmitsTheOtherAccountsJobs() = runTest {
        val vm = viewModel(retain = true)
        vm.activateAccount("a")
        val aJob = vm.store.requestService("Fix tap", "Ravi")
        vm.jobs.test {
            assertEquals(listOf(aJob), awaitItem().map(Job::id))
            vm.activateAccount("b")
            assertTrue(awaitItem().isEmpty())
            val bJob = vm.store.requestService("Paint wall", "Meena")
            assertEquals(listOf(bJob), awaitItem().map(Job::id))
            vm.activateAccount("a")
            assertEquals(listOf(aJob), awaitItem().map(Job::id))
        }
    }

    @Test fun cancellingMovesAJobFromActiveToHistory() = runTest {
        val vm = viewModel()
        vm.activateAccount("a")
        val id = vm.store.requestService("Fix tap", "Ravi")
        vm.jobs.test {
            assertEquals(JobStatus.Applied, awaitItem().single().status)
            assertEquals(listOf(id), vm.filtered(JobsFilter.Active).map(Job::id))
            vm.store.cancel(id)
            val cancelled = awaitItem().single()
            assertEquals(JobStatus.Canceled, cancelled.status)
            assertTrue(vm.filtered(JobsFilter.Active).isEmpty())
            assertEquals(listOf(id), vm.filtered(JobsFilter.History).map(Job::id))
        }
    }

    @Test fun viewAsOtherPartyStaysOffOutsideThePreview() = runTest {
        val vm = viewModel()
        vm.activateAccount("a")
        val id = vm.store.requestService("Fix tap", "Ravi")
        vm.viewAsOtherJobIds.test {
            assertTrue(awaitItem().isEmpty())
            vm.store.toggleViewAsOtherParty(id)
            expectNoEvents()
        }
        assertTrue(!vm.store.isViewAsOther(id))
    }

    @Test fun elapsedTimeIsNeverNegativeAndNullBeforeStart() {
        val vm = viewModel()
        vm.activateAccount("a")
        val job = vm.store.job(vm.store.requestService("Fix tap", "Ravi"))!!
        assertNull(vm.elapsedMillis(job, 5_000L))
        assertEquals(0L, vm.elapsedMillis(job.copy(startedAtMillis = 9_000L), 5_000L))
        assertEquals(1_000L, vm.elapsedMillis(job.copy(startedAtMillis = 9_000L), 10_000L))
    }
}
