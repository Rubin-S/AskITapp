package com.askit.app.jobs

import androidx.lifecycle.ViewModel
import com.askit.app.session.SessionProfileStore
import kotlinx.coroutines.flow.StateFlow

class JobsViewModel(val store: JobsStore, val profileStore: SessionProfileStore) : ViewModel() {
    val jobs: StateFlow<List<Job>> = store.jobs
    val viewAsOtherJobIds: StateFlow<Set<String>> = store.viewAsOtherJobIds
    val profile = profileStore.profile

    /** Shows [accountId]'s jobs; called whenever the signed-in account changes. */
    fun activateAccount(accountId: String?) = store.activateAccount(accountId)
}
