package com.askit.app.jobs

import androidx.lifecycle.ViewModel
import com.askit.app.session.SessionProfileStore
import kotlinx.coroutines.flow.StateFlow

class JobsViewModel(
    val store: JobsStore,
    val profileStore: SessionProfileStore,
) : ViewModel() {
    val jobs: StateFlow<List<Job>> = store.jobs
    val viewAsOtherJobIds: StateFlow<Set<String>> = store.viewAsOtherJobIds
    val profile = profileStore.profile

    fun filtered(filter: JobsFilter): List<Job> = jobs.value.filter { job ->
        when (filter) {
            JobsFilter.Active -> !job.inHistory
            JobsFilter.History -> job.inHistory
        }
    }

    fun elapsedMillis(job: Job, nowMillis: Long): Long? {
        val started = job.startedAtMillis ?: return null
        return (nowMillis - started).coerceAtLeast(0L)
    }
}
