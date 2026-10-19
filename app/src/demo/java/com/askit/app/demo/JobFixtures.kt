package com.askit.app.demo

import com.askit.app.jobs.Job
import com.askit.app.jobs.JobKind
import com.askit.app.jobs.JobParty
import com.askit.app.jobs.JobStatus
import com.askit.app.jobs.JobWorkMode

/** Sample incoming leads for debug/preview builds. Not compiled into release. */
internal fun sampleIncomingLeads(): List<Job> = listOf(
    Job(
        id = "lead-incoming-task",
        title = "Repair kitchen tap",
        counterpartName = "Priya",
        kind = JobKind.TaskApplication,
        localParty = JobParty.Receiver,
        status = JobStatus.Applied,
        workMode = JobWorkMode.OnSite,
        locationLabel = "White Town, Puducherry",
        otp = "4821",
    ),
    Job(
        id = "lead-incoming-service",
        title = "House cleaning",
        counterpartName = "Karthik",
        kind = JobKind.ServiceRequest,
        localParty = JobParty.Receiver,
        status = JobStatus.Applied,
        workMode = JobWorkMode.OnSite,
        locationLabel = "Lawspet, Puducherry",
        otp = "7390",
    ),
    Job(
        id = "lead-remote-applied",
        title = "Laptop setup help",
        counterpartName = "Meena",
        kind = JobKind.ServiceRequest,
        localParty = JobParty.Applicant,
        status = JobStatus.Applied,
        workMode = JobWorkMode.Remote,
        otp = "1560",
    ),
)

/** Two demo jobs that let preview reviewers exercise start-code entry and review. */
internal fun clientPreviewJobs(): List<Job> = listOf(
    Job(
        "preview-verify", "Demo: start an electrical repair", "Priya", kind = JobKind.ServiceRequest,
        localParty = JobParty.Receiver, status = JobStatus.OnTheWay, workMode = JobWorkMode.OnSite,
        locationLabel = "White Town, Puducherry", otp = "1234", providerAccountId = "preview-owner", seekerAccountId = "preview-visitor",
    ),
    Job(
        "preview-review", "Demo: completed lighting installation", "Karthik", kind = JobKind.ServiceRequest,
        localParty = JobParty.Applicant, status = JobStatus.AwaitingSeekerComplete, workMode = JobWorkMode.OnSite,
        locationLabel = "White Town, Puducherry", otp = "1234", providerAccountId = "preview-visitor", seekerAccountId = "preview-owner",
    ),
)
