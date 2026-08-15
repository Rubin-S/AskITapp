package com.askit.app.jobs

import com.askit.app.session.SessionProfileStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class JobsStore(
    private val profileStore: SessionProfileStore,
    private val clock: () -> Long = System::currentTimeMillis,
    seedIncomingLeads: Boolean = true,
) {
    private val _jobs = MutableStateFlow(
        if (seedIncomingLeads) seededJobs() else emptyList(),
    )
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    private val _viewAsOtherParty = MutableStateFlow(false)
    val viewAsOtherParty: StateFlow<Boolean> = _viewAsOtherParty.asStateFlow()

    private var nextId = 100

    fun job(id: String): Job? = _jobs.value.firstOrNull { it.id == id }

    fun visibleParty(job: Job): JobParty = job.visibleParty(_viewAsOtherParty.value)

    fun toggleViewAsOtherParty() {
        _viewAsOtherParty.update { !it }
    }

    fun applyToTask(
        title: String,
        counterpartName: String,
        workMode: JobWorkMode = JobWorkMode.OnSite,
    ): ApplyToTaskResult {
        if (!profileStore.profile.value.hasListedService) {
            return ApplyToTaskResult.NeedsListedService
        }
        return ApplyToTaskResult.Created(
            createLead(
                title = title,
                counterpartName = counterpartName,
                kind = JobKind.TaskApplication,
                localParty = JobParty.Applicant,
                workMode = workMode,
            ),
        )
    }

    fun requestService(
        title: String,
        counterpartName: String,
        workMode: JobWorkMode = JobWorkMode.OnSite,
    ): String = createLead(
        title = title,
        counterpartName = counterpartName,
        kind = JobKind.ServiceRequest,
        localParty = JobParty.Applicant,
        workMode = workMode,
    )

    fun accept(jobId: String) = update(jobId) { job ->
        if (job.status != JobStatus.Applied) return@update job
        if (visibleParty(job) != JobParty.Receiver) return@update job
        val next = if (job.workMode == JobWorkMode.Remote) {
            JobStatus.Accepted
        } else {
            JobStatus.OnTheWay
        }
        job.copy(status = next)
    }

    fun reject(jobId: String) = update(jobId) { job ->
        if (job.status != JobStatus.Applied) return@update job
        if (visibleParty(job) != JobParty.Receiver) return@update job
        job.copy(status = JobStatus.Rejected, inHistory = true)
    }

    fun cancel(jobId: String) = update(jobId) { job ->
        if (job.status != JobStatus.Applied) return@update job
        if (visibleParty(job) != JobParty.Applicant) return@update job
        job.copy(status = JobStatus.Canceled, inHistory = true)
    }

    fun verifyOtp(jobId: String, code: String): Boolean {
        val job = job(jobId) ?: return false
        if (!canEnterOtp(job)) return false
        if (code != job.otp) return false
        update(jobId) {
            it.copy(status = JobStatus.Started, startedAtMillis = clock())
        }
        return true
    }

    fun stubScan(jobId: String): Boolean {
        val job = job(jobId) ?: return false
        return verifyOtp(jobId, job.otp)
    }

    fun requestComplete(jobId: String) = update(jobId) { job ->
        if (job.status != JobStatus.Started) return@update job
        if (!job.isProvider(visibleParty(job))) return@update job
        job.copy(status = JobStatus.AwaitingSeekerComplete)
    }

    fun markComplete(jobId: String): Boolean {
        val job = job(jobId) ?: return false
        if (job.status != JobStatus.AwaitingSeekerComplete) return false
        if (!job.isSeeker(visibleParty(job))) return false
        return true
    }

    fun completeWithReviewDeferred(jobId: String) = complete(jobId)

    fun complete(jobId: String) = update(jobId) { job ->
        if (job.status != JobStatus.AwaitingSeekerComplete) return@update job
        if (!job.isSeeker(visibleParty(job))) return@update job
        job.copy(status = JobStatus.Completed, inHistory = true)
    }

    fun canEnterOtp(job: Job): Boolean {
        if (!job.isProvider(visibleParty(job))) return false
        return when (job.status) {
            JobStatus.Accepted, JobStatus.OnTheWay -> true
            JobStatus.Applied,
            JobStatus.Started,
            JobStatus.AwaitingSeekerComplete,
            JobStatus.Completed,
            JobStatus.Canceled,
            JobStatus.Rejected,
            -> false
        }
    }

    fun canShareOtp(job: Job): Boolean {
        if (!job.isSeeker(visibleParty(job))) return false
        return when (job.status) {
            JobStatus.Accepted, JobStatus.OnTheWay, JobStatus.Started -> true
            JobStatus.Applied,
            JobStatus.AwaitingSeekerComplete,
            JobStatus.Completed,
            JobStatus.Canceled,
            JobStatus.Rejected,
            -> false
        }
    }

    private fun createLead(
        title: String,
        counterpartName: String,
        kind: JobKind,
        localParty: JobParty,
        workMode: JobWorkMode,
    ): String {
        val id = "job-${nextId++}"
        val otp = ((1000..9999).random()).toString()
        _jobs.update { current ->
            current + Job(
                id = id,
                title = title,
                counterpartName = counterpartName,
                kind = kind,
                localParty = localParty,
                status = JobStatus.Applied,
                workMode = workMode,
                otp = otp,
            )
        }
        return id
    }

    private fun update(jobId: String, transform: (Job) -> Job) {
        _jobs.update { current ->
            current.map { if (it.id == jobId) transform(it) else it }
        }
    }
}

internal fun seededJobs(): List<Job> = listOf(
    Job(
        id = "lead-incoming-task",
        title = "Repair kitchen tap",
        counterpartName = "Priya",
        kind = JobKind.TaskApplication,
        localParty = JobParty.Receiver,
        status = JobStatus.Applied,
        workMode = JobWorkMode.OnSite,
        locationLabel = "Coimbatore",
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
        locationLabel = "Chennai",
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
