package com.askit.app

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.askit.app.auth.AuthViewModel
import com.askit.app.createpost.CreatePostViewModel
import com.askit.app.creatordashboard.CreatorDashboardViewModel
import com.askit.app.di.AppConfig
import com.askit.app.di.AppContainer
import com.askit.app.explore.ExploreViewModel
import com.askit.app.home.HomeViewModel
import com.askit.app.home.stories.StoryViewerViewModel
import com.askit.app.inbox.InboxViewModel
import com.askit.app.jobs.Job
import com.askit.app.jobs.JobsViewModel
import com.askit.app.listservice.ListServiceViewModel
import com.askit.app.navigation.AppDestination
import com.askit.app.posttask.PostTaskViewModel
import com.askit.app.profile.ProfileViewModel
import com.askit.app.providerdashboard.ProviderDashboardViewModel
import com.askit.app.story.StoryViewModel
import com.askit.app.task.TaskActionsViewModel
import com.askit.app.task.TaskDiscussionViewModel
import com.askit.app.variant.AppVariant
import com.askit.designsystem.theme.AskITTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Signing out through the real [AskITApp] wiring drops the account's jobs and inbox. Uses the
 * client-preview configuration so the app starts signed in as a local persona without a backend.
 */
@RunWith(AndroidJUnit4::class)
class SignOutClearsAccountDataTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun signOutClearsJobsAndInbox() {
        val activity = compose.activity
        val container = AppContainer(activity, AppConfig(backendBaseUrl = null, clientPreview = true), AppVariant)
        lateinit var auth: AuthViewModel
        lateinit var jobs: JobsViewModel
        lateinit var inbox: InboxViewModel
        compose.runOnUiThread {
            val graph = container.newSessionGraph()
            val vms = ViewModelProvider(activity, container.viewModelFactory(graph))
            auth = vms[AuthViewModel::class.java]
            jobs = vms[JobsViewModel::class.java]
            inbox = vms[InboxViewModel::class.java]
            activity.setContent {
                AskITTheme {
                    AskITApp(
                        exploreViewModel = vms[ExploreViewModel::class.java],
                        taskRepository = graph.tasks,
                        taskDiscussionViewModel = vms[TaskDiscussionViewModel::class.java],
                        taskActionsViewModel = vms[TaskActionsViewModel::class.java],
                        homeViewModel = vms[HomeViewModel::class.java],
                        postTaskViewModel = vms[PostTaskViewModel::class.java],
                        listServiceViewModel = vms[ListServiceViewModel::class.java],
                        createPostViewModel = vms[CreatePostViewModel::class.java],
                        creatorDashboardViewModel = vms[CreatorDashboardViewModel::class.java],
                        providerDashboardViewModel = vms[ProviderDashboardViewModel::class.java],
                        storyViewModel = vms[StoryViewModel::class.java],
                        jobsViewModel = jobs,
                        inboxViewModel = inbox,
                        profileViewModel = vms[ProfileViewModel::class.java],
                        storyViewerViewModel = vms[StoryViewerViewModel::class.java],
                        authViewModel = auth,
                        initialRoute = AppDestination.Home,
                    )
                }
            }
        }
        compose.waitForIdle()
        val owner = checkNotNull(auth.session?.userId)
        assertEquals(owner, jobs.store.activeAccountId)
        val jobId = compose.runOnIdle { jobs.store.requestService("Fix tap", "Ravi") }
        assertTrue(jobs.jobs.value.any { it.id == jobId })
        assertTrue("the preview persona starts with seeded threads", inbox.conversations.isNotEmpty())

        compose.runOnUiThread { auth.signOut() }
        compose.waitForIdle()

        assertEquals(null, auth.session)
        assertEquals(null, jobs.store.activeAccountId)
        assertFalse(jobs.jobs.value.map(Job::id).contains(jobId))
        assertTrue(inbox.conversations.isEmpty())
        assertEquals(0, inbox.unreadCount)
    }
}
