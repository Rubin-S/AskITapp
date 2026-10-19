package com.askit.app.jobs

import com.askit.app.session.SessionProfileStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.security.SecureRandom

/**
 * The start code proves the provider is on site, and party is the only authorization input for
 * job transitions, so both must hold up against guessing and against "View as other party".
 */
class JobSecurityTest {
    private fun job(party: JobParty, status: JobStatus) = Job(
        id = "job-1",
        title = "Fix wiring",
        counterpartName = "Priya",
        // In a service request the provider is the Receiver.
        kind = JobKind.ServiceRequest,
        localParty = party,
        status = status,
        workMode = JobWorkMode.OnSite,
        otp = "4821",
    )

    private fun store(job: Job, allowViewAs: Boolean = false, now: Long = 5_000L) = JobsStore(
        SessionProfileStore(),
        clock = { now },
        initialJobs = listOf(job),
        allowViewAsOtherParty = allowViewAs,
    )

    @Test fun codesAreFourDigitsFromTheSecureGeneratorWithLeadingZeros() {
        val fixed = object : SecureRandom() {
            override fun nextInt(bound: Int) = 7
        }
        assertEquals("0007", LocalJobStartCodeAuthority(fixed).issue())
    }

    @Test fun codeEntryLocksAfterFiveWrongAttemptsEvenForTheRightCode() {
        val authority = LocalJobStartCodeAuthority()
        repeat(4) { attempt -> assertEquals(StartCodeCheck.Rejected(4 - attempt), authority.verify("job-1", "4821", "0000")) }
        assertEquals(StartCodeCheck.LockedOut, authority.verify("job-1", "4821", "0000"))
        assertEquals(StartCodeCheck.LockedOut, authority.verify("job-1", "4821", "4821"))
        assertEquals(StartCodeCheck.Verified, authority.verify("job-2", "4821", "4821"))
    }

    @Test fun onlyTheProviderWithTheRightCodeStartsTheJob() {
        val provider = store(job(JobParty.Receiver, JobStatus.OnTheWay))
        assertEquals(StartCodeCheck.Rejected(4), provider.verifyStartCode("job-1", "1234"))
        assertEquals(JobStatus.OnTheWay, provider.job("job-1")!!.status)
        assertEquals(StartCodeCheck.Verified, provider.verifyStartCode("job-1", "4821"))
        assertEquals(JobStatus.Started, provider.job("job-1")!!.status)
        assertEquals(5_000L, provider.job("job-1")!!.startedAtMillis)

        val seeker = store(job(JobParty.Applicant, JobStatus.OnTheWay))
        assertEquals(StartCodeCheck.Rejected(0), seeker.verifyStartCode("job-1", "4821"))
        assertNull(seeker.job("job-1")!!.startedAtMillis)
    }

    @Test fun viewAsOtherPartyCannotBypassAuthorizationOutsideThePreview() {
        val store = store(job(JobParty.Receiver, JobStatus.AwaitingSeekerComplete))
        store.toggleViewAsOtherParty("job-1")
        assertFalse(store.isViewAsOther("job-1"))
        store.complete("job-1")
        assertEquals(JobStatus.AwaitingSeekerComplete, store.job("job-1")!!.status)
        assertFalse(store.markComplete("job-1"))
    }

    @Test fun onlyTheReceiverCanAcceptOrRejectAndOnlyTheApplicantCanCancel() {
        val applicant = store(job(JobParty.Applicant, JobStatus.Applied))
        applicant.accept("job-1")
        applicant.reject("job-1")
        assertEquals(JobStatus.Applied, applicant.job("job-1")!!.status)
        applicant.cancel("job-1")
        assertEquals(JobStatus.Canceled, applicant.job("job-1")!!.status)

        val receiver = store(job(JobParty.Receiver, JobStatus.Applied))
        receiver.cancel("job-1")
        assertEquals(JobStatus.Applied, receiver.job("job-1")!!.status)
        receiver.accept("job-1")
        assertEquals(JobStatus.OnTheWay, receiver.job("job-1")!!.status)
    }
}
