package com.askit.app.profile

import com.askit.app.jobs.Job
import com.askit.app.jobs.JobKind
import com.askit.app.jobs.JobParty
import com.askit.app.jobs.JobStatus
import com.askit.app.jobs.JobWorkMode
import org.junit.Assert.assertEquals
import org.junit.Test

class MyJobRequestsTest {
    private fun job(id: String, kind: JobKind, party: JobParty) = Job(
        id = id,
        title = id,
        counterpartName = "x",
        kind = kind,
        localParty = party,
        status = JobStatus.values().first(),
        workMode = JobWorkMode.values().first(),
        otp = "0000",
    )

    @Test fun onlyJobsWhereYouAreTheSeekerAreListed() {
        val jobs = listOf(
            job("requested-service", JobKind.ServiceRequest, JobParty.Applicant),
            job("service-for-someone", JobKind.ServiceRequest, JobParty.Receiver),
            job("my-task-applicant", JobKind.TaskApplication, JobParty.Receiver),
            job("applied-to-task", JobKind.TaskApplication, JobParty.Applicant),
        )
        assertEquals(listOf("requested-service", "my-task-applicant"), myJobRequests(jobs).map { it.id })
    }
}
