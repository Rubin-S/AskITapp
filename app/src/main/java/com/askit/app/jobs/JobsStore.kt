package com.askit.app.jobs

import com.askit.app.session.SessionProfileStore
import com.askit.app.task.TaskRepository
import com.askit.app.task.TaskActionFailure
import com.askit.app.task.actionPolicy
import com.askit.app.posttask.PostTaskWorkMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class JobsStore(
    private val profileStore: SessionProfileStore,
    private val clock: () -> Long = System::currentTimeMillis,
    initialJobs: List<Job> = emptyList(),
) {
    private val _jobs = MutableStateFlow(initialJobs)
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    private val _viewAsOtherJobIds = MutableStateFlow<Set<String>>(emptySet())
    val viewAsOtherJobIds: StateFlow<Set<String>> = _viewAsOtherJobIds.asStateFlow()

    private var nextId = 100

    /** Adds preview-only demo jobs. Callers live in the debug/preview source set. */
    internal fun addClientPreviewJobs(jobs: List<Job>) {
        check(com.askit.app.BuildConfig.CLIENT_PREVIEW)
        _jobs.update { existing -> existing + jobs }
    }

    fun job(id: String): Job? = _jobs.value.firstOrNull { it.id == id }

    fun isViewAsOther(jobId: String): Boolean = jobId in _viewAsOtherJobIds.value

    fun visibleParty(job: Job): JobParty = job.visibleParty(isViewAsOther(job.id))

    fun toggleViewAsOtherParty(jobId: String) {
        _viewAsOtherJobIds.update { ids ->
            if (jobId in ids) ids - jobId else ids + jobId
        }
    }

    fun taskApplication(taskId: String, accountId: String?): Job? =
        if (accountId.isNullOrBlank()) null else _jobs.value.firstOrNull {
            it.sourceTaskId == taskId && it.applicantAccountId == accountId && it.kind == JobKind.TaskApplication
        }

    @Synchronized
    fun applyToTask(repository: TaskRepository, taskId: String, accountId: String?): ApplyToTaskResult {
        if (accountId.isNullOrBlank()) return ApplyToTaskResult.Rejected(TaskActionFailure.SIGN_IN_REQUIRED)
        val task = repository.getTask(taskId) ?: return ApplyToTaskResult.Rejected(TaskActionFailure.UNAVAILABLE)
        val profile = profileStore.profile.value
        val policy = task.actionPolicy(accountId, profile.hasListedService && profile.listing != null)
        if (policy.isOwner) return ApplyToTaskResult.Rejected(TaskActionFailure.NOT_ALLOWED)
        if (!policy.showApply) return ApplyToTaskResult.NeedsListedService
        taskApplication(taskId, accountId)?.let { return ApplyToTaskResult.AlreadyApplied(it.id) }
        if (!policy.canApply) return ApplyToTaskResult.Rejected(TaskActionFailure.UNAVAILABLE)
        return ApplyToTaskResult.Created(
            createLead(
                title = task.title, counterpartName = task.posterName,
                kind = JobKind.TaskApplication, localParty = JobParty.Applicant,
                workMode = if (task.publicDetails?.workMode == PostTaskWorkMode.REMOTE) JobWorkMode.Remote else JobWorkMode.OnSite,
                sourceTaskId = taskId, applicantAccountId = accountId, providerAccountId = accountId, seekerAccountId = task.posterId,
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

    private fun createLead(
        title: String,
        counterpartName: String,
        kind: JobKind,
        localParty: JobParty,
        workMode: JobWorkMode,
        sourceTaskId: String? = null,
        applicantAccountId: String? = null,
        providerAccountId: String? = null,
        seekerAccountId: String? = null,
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
                sourceTaskId = sourceTaskId,
                applicantAccountId = applicantAccountId, providerAccountId = providerAccountId, seekerAccountId = seekerAccountId,
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
