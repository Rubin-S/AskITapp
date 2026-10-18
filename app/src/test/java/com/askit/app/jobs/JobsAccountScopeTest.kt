package com.askit.app.jobs

import com.askit.app.session.SessionProfileStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JobsAccountScopeTest {
    private fun store(retain: Boolean, seed: List<Job> = emptyList()) =
        JobsStore(SessionProfileStore(), initialJobs = seed, retainInactiveAccounts = retain)

    @Test fun signingOutClearsTheAccountsJobs() {
        val store = store(retain = false)
        store.activateAccount("a")
        store.requestService("Fix tap", "Ravi")
        assertEquals(1, store.jobs.value.size)
        store.activateAccount(null)
        assertTrue(store.jobs.value.isEmpty())
        store.activateAccount("a")
        assertTrue(store.jobs.value.isEmpty())
    }

    @Test fun anotherAccountNeverSeesTheFirstAccountsJobs() {
        val store = store(retain = true)
        store.activateAccount("a")
        val id = store.requestService("Fix tap", "Ravi")
        store.activateAccount("b")
        assertTrue(store.jobs.value.none { it.id == id })
        assertEquals(null, store.job(id))
    }

    @Test fun clientPreviewKeepsEachPersonasJobsAcrossSwitches() {
        val store = store(retain = true)
        store.activateAccount("a")
        val id = store.requestService("Fix tap", "Ravi")
        store.activateAccount("b")
        val other = store.requestService("Paint wall", "Meena")
        store.activateAccount("a")
        assertEquals(listOf(id), store.jobs.value.map(Job::id))
        assertNotEquals(id, other)
        assertEquals("a", store.activeAccountId)
    }

    @Test fun everyAccountStartsFromTheSeedJobs() {
        val seed = Job(
            id = "seed-1",
            title = "Seed",
            counterpartName = "Fixture",
            kind = JobKind.ServiceRequest,
            localParty = JobParty.Receiver,
            status = JobStatus.Applied,
            workMode = JobWorkMode.OnSite,
            otp = "0000",
        )
        val store = store(retain = false, seed = listOf(seed))
        store.activateAccount("a")
        assertEquals(listOf("seed-1"), store.jobs.value.map(Job::id))
        store.activateAccount("b")
        assertEquals(listOf("seed-1"), store.jobs.value.map(Job::id))
    }
}
