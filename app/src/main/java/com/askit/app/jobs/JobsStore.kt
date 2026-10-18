package com.askit.app.jobs

import com.askit.app.posttask.PostTaskWorkMode
import com.askit.app.session.AccountScope
import com.askit.app.session.SessionProfileStore
import com.askit.app.task.TaskActionFailure
import com.askit.app.task.TaskRepository
import com.askit.app.task.actionPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class JobsStore(
    private val profileStore: SessionProfileStore,
    private val clock: () -> Long = System::currentTimeMillis,
    initialJobs: List<Job> = emptyList(),
    /**
     * "View as other party" is a client-preview review tool only. Party is the sole authorisation
     * input for accept/start/complete, so outside the preview build it must never flip.
     */
    val allowViewAsOtherParty: Boolean = false,
    /** Local stand-in for the server-verified start code (docs/job-start-code-contract.md). */
    private val startCodes: JobStartCodeAuthority = LocalJobStartCodeAuthority(),
    /**
     * Keep an inactive account's jobs in memory while another account is active. Only the client
     * preview does (its personas switch back and forth); production drops them on sign-out.
     */
    retainInactiveAccounts: Boolean = false,
) {
    /** Fixture jobs every account starts from (empty in release). */
    private var seedJobs: List<Job> = initialJobs

    private val _jobs = MutableStateFlow(initialJobs)
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    private val _viewAsOtherJobIds = MutableStateFlow<Set<String>>(emptySet())
    val viewAsOtherJobIds: StateFlow<Set<String>> = _viewAsOtherJobIds.asStateFlow()

    /** Ids stay unique across accounts so start-code attempt counts never collide. */
    private var nextId = 100

    private class AccountJobs(var jobs: List<Job>, var viewAsOtherJobIds: Set<String>)

    private val accounts = AccountScope(retainInactiveAccounts) { AccountJobs(seedJobs, emptySet()) }

    /** The account whose jobs [jobs] currently shows; null when signed out. */
    val activeAccountId: String? get() = accounts.accountId

    /**
     * Shows [accountId]'s jobs (null = signed out). Another account's jobs are never visible
     * here; see [AccountScope] for when the outgoing account's jobs are kept.
     */
    @Synchronized
    fun activateAccount(accountId: String?) {
        val outgoing = accounts.current
        outgoing.jobs = _jobs.value
        outgoing.viewAsOtherJobIds = _viewAsOtherJobIds.value
        if (!accounts.activate(accountId)) return
        _jobs.value = accounts.current.jobs
        _viewAsOtherJobIds.value = accounts.current.viewAsOtherJobIds
    }

    /** Adds preview-only demo jobs. Callers live in the debug/preview source set. */
    internal fun addClientPreviewJobs(jobs: List<Job>) {
        check(com.askit.app.BuildConfig.CLIENT_PREVIEW)
        seedJobs = seedJobs + jobs
        _jobs.update { existing -> existing + jobs }
    }

    fun job(id: String): Job? = _jobs.value.firstOrNull { it.id == id }

    fun isViewAsOther(jobId: String): Boolean = allowViewAsOtherParty && jobId in _viewAsOtherJobIds.value

    fun visibleParty(job: Job): JobParty = job.visibleParty(isViewAsOther(job.id))

    fun toggleViewAsOtherParty(jobId: String) {
        if (!allowViewAsOtherParty) return
        _viewAsOtherJobIds.update { ids ->
            if (jobId in ids) ids - jobId else ids + jobId
        }
    }

    fun taskApplication(taskId: String, accountId: String?): Job? = if (accountId.isNullOrBlank()) {
        null
    } else {
        _jobs.value.firstOrNull {
            it.sourceTaskId == taskId && it.applicantAccountId == accountId && it.kind == JobKind.TaskApplication
        }
    }

    @Synchronized
    fun applyToTask(repository: TaskRepository, taskId: String, accountId: String?): ApplyToTaskResult {
        if (accountId.isNullOrBlank()) return ApplyToTaskResult.Rejected(TaskActionFailure.SIGN_IN_REQUIRED)
        val task = repository.getTask(taskId) ?: return ApplyToTaskResult.Rejected(TaskActionFailure.UNAVAILABLE)
        val profile = profileStore.profile.value
        val policy = task.actionPolicy(accountId, profile.hasActiveServiceListing)
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

    fun requestService(title: String, counterpartName: String, workMode: JobWorkMode = JobWorkMode.OnSite): String = createLead(
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

    /** Provider enters the seeker's code. Attempts are limited per job; see [JobStartCodeAuthority]. */
    @Synchronized
    fun verifyStartCode(jobId: String, code: String): StartCodeCheck {
        val job = job(jobId) ?: return StartCodeCheck.Rejected(0)
        if (!canEnterOtp(job)) return StartCodeCheck.Rejected(0)
        val result = startCodes.verify(jobId, job.otp, code)
        if (result == StartCodeCheck.Verified) {
            update(jobId) { it.copy(status = JobStatus.Started, startedAtMillis = clock()) }
        }
        return result
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
        val otp = startCodes.issue()
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
