package com.askit.app.di

import androidx.lifecycle.ViewModel
import com.askit.app.home.data.LocalHomeRepository
import com.askit.app.home.stories.StorySocialStore
import com.askit.app.jobs.JobsStore
import com.askit.app.session.SessionProfileStore
import com.askit.app.task.InMemoryTaskRepository
import com.askit.app.variant.ProfileSnapshotStorage
import com.askit.app.variant.VariantContent

/**
 * The stateful, in-memory part of the graph: repositories and stores shared by several
 * view models. It is retained across configuration changes (it is a [ViewModel] owned by the
 * activity) and dropped with the activity, which is what happens on sign-out.
 */
class SessionGraph(
    config: AppConfig,
    content: VariantContent,
    previewStorage: ProfileSnapshotStorage?,
) : ViewModel() {
    /** Set once demo content has been seeded into this graph (client-preview builds only). */
    var previewSeeded = false

    val tasks = InMemoryTaskRepository(content.seededTasks())

    val home = LocalHomeRepository(
        taskRepository = tasks,
        initialStories = content.homeStories(),
        fixtures = content.homeFeed(),
        storySocial = StorySocialStore(
            initialRelationships = runCatching { previewStorage?.loadRelationships().orEmpty() }.getOrDefault(emptyMap()),
            persistRelationships = { previewStorage?.saveRelationships(it) },
        ),
    )

    /** The signed-in account's profile; auth, jobs, profile and dashboards all read this one store. */
    val profileStore = SessionProfileStore(persist = { previewStorage?.save(it) ?: it })

    val jobs = JobsStore(
        profileStore,
        initialJobs = content.incomingLeads(),
        allowViewAsOtherParty = config.clientPreview,
        retainInactiveAccounts = config.clientPreview,
    )
}
