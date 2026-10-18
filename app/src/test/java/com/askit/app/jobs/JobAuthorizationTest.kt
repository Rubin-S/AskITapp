package com.askit.app.jobs

import com.askit.app.session.SessionProfileStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** "View as other party" must not work as a permission bypass outside the client preview. */
class JobAuthorizationTest {
    // A service request where this device is the provider (Receiver).
    private fun providerJob(status: JobStatus) = Job(
        id = "job-1", title = "Fix wiring", counterpartName = "Priya", kind = JobKind.ServiceRequest,
        localParty = JobParty.Receiver, status = status, workMode = JobWorkMode.OnSite, otp = "4821",
    )

    private fun store(job: Job, allowViewAs: Boolean) =
        JobsStore(SessionProfileStore(), initialJobs = listOf(job), allowViewAsOtherParty = allowViewAs)

    @Test fun viewAsIsIgnoredOutsidePreview() {
        val store = store(providerJob(JobStatus.AwaitingSeekerComplete), allowViewAs = false)
        store.toggleViewAsOtherParty("job-1")
        assertFalse(store.isViewAsOther("job-1"))
        assertTrue(store.viewAsOtherJobIds.value.isEmpty())
        assertEquals(JobParty.Receiver, store.visibleParty(store.job("job-1")!!))
    }

    @Test fun providerCannotCompleteTheirOwnJobByFlippingParty() {
        val store = store(providerJob(JobStatus.AwaitingSeekerComplete), allowViewAs = false)
        store.toggleViewAsOtherParty("job-1")
        store.complete("job-1")
        assertEquals(JobStatus.AwaitingSeekerComplete, store.job("job-1")!!.status)
        assertFalse(store.markComplete("job-1"))
    }

    @Test fun previewBuildsKeepTheReviewTool() {
        val store = store(providerJob(JobStatus.AwaitingSeekerComplete), allowViewAs = true)
        store.toggleViewAsOtherParty("job-1")
        assertTrue(store.isViewAsOther("job-1"))
        assertEquals(JobParty.Applicant, store.visibleParty(store.job("job-1")!!))
    }
}
