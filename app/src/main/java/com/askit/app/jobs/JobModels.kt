package com.askit.app.jobs

enum class JobStatus {
    Applied,
    Accepted,
    OnTheWay,
    Started,
    AwaitingSeekerComplete,
    Completed,
    Canceled,
    Rejected,
}

enum class JobKind {
    TaskApplication,
    ServiceRequest,
}

enum class JobParty {
    Applicant,
    Receiver,
}

enum class JobWorkMode {
    OnSite,
    Remote,
}

enum class JobStep {
    Applied,
    Accepted,
    OnTheWay,
    Started,
    Complete,
}

enum class JobsFilter {
    Active,
    History,
}

data class Job(
    val id: String,
    val title: String,
    val counterpartName: String,
    val counterpartAvatarUrl: String? = null,
    val kind: JobKind,
    val localParty: JobParty,
    val status: JobStatus,
    val workMode: JobWorkMode,
    val locationLabel: String = "",
    val otp: String,
    val startedAtMillis: Long? = null,
    val inHistory: Boolean = false,
)

sealed interface ApplyToTaskResult {
    data class Created(val jobId: String) : ApplyToTaskResult
    data object NeedsListedService : ApplyToTaskResult
}

fun JobParty.other(): JobParty = when (this) {
    JobParty.Applicant -> JobParty.Receiver
    JobParty.Receiver -> JobParty.Applicant
}

fun Job.visibleParty(viewAsOtherParty: Boolean): JobParty =
    if (viewAsOtherParty) localParty.other() else localParty

fun Job.isSeeker(party: JobParty): Boolean = when (kind) {
    JobKind.TaskApplication -> party == JobParty.Receiver
    JobKind.ServiceRequest -> party == JobParty.Applicant
}

fun Job.isProvider(party: JobParty): Boolean = !isSeeker(party)

fun Job.isTerminal(): Boolean = when (status) {
    JobStatus.Completed,
    JobStatus.Canceled,
    JobStatus.Rejected,
    -> true
    JobStatus.Applied,
    JobStatus.Accepted,
    JobStatus.OnTheWay,
    JobStatus.Started,
    JobStatus.AwaitingSeekerComplete,
    -> false
}

fun Job.stepperSteps(): List<JobStep> = if (workMode == JobWorkMode.Remote) {
    listOf(JobStep.Applied, JobStep.Accepted, JobStep.Started, JobStep.Complete)
} else {
    listOf(
        JobStep.Applied,
        JobStep.Accepted,
        JobStep.OnTheWay,
        JobStep.Started,
        JobStep.Complete,
    )
}

fun Job.currentStep(): JobStep = when (status) {
    JobStatus.Applied,
    JobStatus.Canceled,
    JobStatus.Rejected,
    -> JobStep.Applied
    JobStatus.Accepted -> JobStep.Accepted
    JobStatus.OnTheWay -> JobStep.OnTheWay
    JobStatus.Started,
    JobStatus.AwaitingSeekerComplete,
    -> JobStep.Started
    JobStatus.Completed -> JobStep.Complete
}
