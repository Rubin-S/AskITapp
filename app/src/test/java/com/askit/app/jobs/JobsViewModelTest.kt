package com.askit.app.jobs

import com.askit.app.R
import com.askit.app.session.SessionProfile
import com.askit.app.session.SessionProfileStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JobsViewModelTest {

    @Test
    fun applyToTask_isBlocked_withoutListedService() {
        val profile = SessionProfileStore(SessionProfile(hasListedService = false))
        val store = JobsStore(profile, seedIncomingLeads = false)

        assertEquals(
            ApplyToTaskResult.NeedsListedService,
            store.applyToTask("Repair tap", "Priya"),
        )
        assertTrue(store.jobs.value.isEmpty())
    }

    @Test
    fun applyToTask_andRequestService_createBothLeadKinds() {
        val profile = SessionProfileStore(SessionProfile(hasListedService = true))
        val store = JobsStore(profile, seedIncomingLeads = false)

        val applied = store.applyToTask("Repair tap", "Priya") as ApplyToTaskResult.Created
        val requested = store.requestService("House cleaning", "Karthik")

        assertEquals(JobKind.TaskApplication, store.job(applied.jobId)?.kind)
        assertEquals(JobKind.ServiceRequest, store.job(requested)?.kind)
        assertEquals(JobParty.Applicant, store.job(applied.jobId)?.localParty)
        assertEquals(JobStatus.Applied, store.job(applied.jobId)?.status)
    }

    @Test
    fun reject_andCancel_useOppositeHistoryCopy() {
        val profile = SessionProfileStore(SessionProfile(hasListedService = true))
        val store = JobsStore(profile, seedIncomingLeads = false)
        val incoming = store.requestService("Cleaning", "Karthik")
        store.toggleViewAsOtherParty(incoming)
        store.reject(incoming)
        val rejected = store.job(incoming)!!
        assertEquals(JobStatus.Rejected, rejected.status)
        assertTrue(rejected.inHistory)
        assertEquals(R.string.job_status_you_rejected, jobStatusLabelRes(rejected, store.visibleParty(rejected)))

        val outgoing = store.requestService("Tutoring", "Meena")
        store.cancel(outgoing)
        val canceled = store.job(outgoing)!!
        assertEquals(JobStatus.Canceled, canceled.status)
        assertEquals(R.string.job_status_you_canceled, jobStatusLabelRes(canceled, store.visibleParty(canceled)))
    }

    @Test
    fun remoteAccept_skipsOnTheWay() {
        val profile = SessionProfileStore(SessionProfile(hasListedService = true))
        val store = JobsStore(profile, seedIncomingLeads = false)
        val id = store.requestService("Laptop help", "Meena", JobWorkMode.Remote)
        store.toggleViewAsOtherParty(id)
        store.accept(id)
        assertEquals(JobStatus.Accepted, store.job(id)?.status)
        assertFalse(store.job(id)!!.stepperSteps().contains(JobStep.OnTheWay))
    }

    @Test
    fun otp_startsElapsedClock() {
        var now = 1_000L
        val profile = SessionProfileStore(SessionProfile(hasListedService = true))
        val store = JobsStore(profile, clock = { now }, seedIncomingLeads = false)
        val id = store.requestService("Laptop help", "Meena", JobWorkMode.Remote)
        val otp = store.job(id)!!.otp
        store.toggleViewAsOtherParty(id)
        store.accept(id)
        assertTrue(store.verifyOtp(id, otp))
        assertEquals(JobStatus.Started, store.job(id)?.status)
        assertEquals(1_000L, store.job(id)?.startedAtMillis)
        now = 4_000L
        val viewModel = JobsViewModel(store, profile)
        assertEquals(3_000L, viewModel.elapsedMillis(store.job(id)!!, now))
    }

    @Test
    fun complete_requiresSeeker_andLaterStillCompletes() {
        val profile = SessionProfileStore(SessionProfile(hasListedService = true))
        val store = JobsStore(profile, seedIncomingLeads = false)
        val id = store.requestService("Laptop help", "Meena", JobWorkMode.Remote)
        val otp = store.job(id)!!.otp
        store.toggleViewAsOtherParty(id)
        store.accept(id)
        assertTrue(store.verifyOtp(id, otp))
        store.requestComplete(id)
        assertFalse(store.markComplete(id))
        store.toggleViewAsOtherParty(id)
        assertTrue(store.markComplete(id))
        store.completeWithReviewDeferred(id)
        assertEquals(JobStatus.Completed, store.job(id)?.status)
        assertTrue(store.job(id)!!.inHistory)
    }

    @Test
    fun seededIncomingLeads_includeBothKinds() {
        val profile = SessionProfileStore()
        val store = JobsStore(profile)
        val kinds = store.jobs.value.map { it.kind }.toSet()
        assertTrue(JobKind.TaskApplication in kinds)
        assertTrue(JobKind.ServiceRequest in kinds)
        assertTrue(store.jobs.value.any { it.localParty == JobParty.Receiver })
    }

    @Test
    fun viewAsOtherParty_isScopedToOneJob() {
        val profile = SessionProfileStore(SessionProfile(hasListedService = true))
        val store = JobsStore(profile, seedIncomingLeads = false)
        val first = store.requestService("Cleaning", "Karthik")
        val second = store.requestService("Tutoring", "Meena")
        store.toggleViewAsOtherParty(first)
        assertTrue(store.isViewAsOther(first))
        assertFalse(store.isViewAsOther(second))
        assertEquals(JobParty.Receiver, store.visibleParty(store.job(first)!!))
        assertEquals(JobParty.Applicant, store.visibleParty(store.job(second)!!))
    }
}
