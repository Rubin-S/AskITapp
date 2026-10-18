package com.askit.app.jobs

import com.askit.app.session.SessionProfileStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JobStartCodeTest {
    @Test fun codesAreFourDigitsFromTheSecureGenerator() {
        val authority = LocalJobStartCodeAuthority()
        val codes = List(500) { authority.issue() }
        assertTrue(codes.all { it.length == 4 && it.all(Char::isDigit) })
        assertTrue("codes should vary", codes.toSet().size > 100)
    }

    @Test fun entryLocksAfterFiveWrongAttempts() {
        val authority = LocalJobStartCodeAuthority()
        repeat(4) { attempt ->
            assertEquals(StartCodeCheck.Rejected(4 - attempt), authority.verify("job", "4821", "0000"))
        }
        assertEquals(StartCodeCheck.LockedOut, authority.verify("job", "4821", "0000"))
        // Even the right code is refused once locked: no unlimited guessing.
        assertEquals(StartCodeCheck.LockedOut, authority.verify("job", "4821", "4821"))
        // Attempts are per job.
        assertEquals(StartCodeCheck.Verified, authority.verify("other", "4821", "4821"))
    }

    @Test fun storeStartsTheJobOnlyWithTheRightCodeBeforeLockout() {
        val job = Job(
            id = "job-1", title = "Fix wiring", counterpartName = "Priya", kind = JobKind.ServiceRequest,
            localParty = JobParty.Receiver, status = JobStatus.OnTheWay, workMode = JobWorkMode.OnSite, otp = "4821",
        )
        val locked = JobsStore(SessionProfileStore(), clock = { 42L }, initialJobs = listOf(job))
        repeat(5) { locked.verifyStartCode("job-1", "1111") }
        assertEquals(StartCodeCheck.LockedOut, locked.verifyStartCode("job-1", "4821"))
        assertEquals(JobStatus.OnTheWay, locked.job("job-1")!!.status)
        assertNull(locked.job("job-1")!!.startedAtMillis)

        val fresh = JobsStore(SessionProfileStore(), clock = { 42L }, initialJobs = listOf(job))
        assertNotEquals(StartCodeCheck.Verified, fresh.verifyStartCode("job-1", "1111"))
        assertEquals(StartCodeCheck.Verified, fresh.verifyStartCode("job-1", "4821"))
        assertEquals(JobStatus.Started, fresh.job("job-1")!!.status)
        assertEquals(42L, fresh.job("job-1")!!.startedAtMillis)
    }
}
