package com.askit.app.variant

import android.content.Context
import androidx.compose.runtime.Composable
import com.askit.app.BuildConfig
import com.askit.app.creatordashboard.CreatorDashboardSample
import com.askit.app.creatordashboard.CreatorTimeRange
import com.askit.app.demo.CreatorDashboardFixtures
import com.askit.app.demo.HomeFixtures
import com.askit.app.demo.sampleExplorePeople
import com.askit.app.demo.sampleInboxSeed
import com.askit.app.demo.sampleIncomingLeads
import com.askit.app.demo.sampleProviderDashboard
import com.askit.app.demo.sampleServiceDetail
import com.askit.app.demo.sampleTasks
import com.askit.app.explore.ExploreBrowseState
import com.askit.app.explore.ExploreBrowseStatus
import com.askit.app.explore.ExplorePersonResult
import com.askit.app.home.data.HomeFeedFixtures
import com.askit.app.home.details.ServiceDetailData
import com.askit.app.home.details.UserProfileData
import com.askit.app.home.model.Story
import com.askit.app.home.posts.PostStore
import com.askit.app.inbox.InboxSeed
import com.askit.app.jobs.Job
import com.askit.app.jobs.JobsStore
import com.askit.app.preview.PreviewProfileStorage
import com.askit.app.preview.previewPublicProfile
import com.askit.app.providerdashboard.ProviderDashboardUiState
import com.askit.app.session.SessionProfile
import com.askit.app.task.Task
import com.askit.app.task.TaskRepository

/**
 * Debug + preview implementation: sample content so the UI can be explored without a backend.
 * This source set (`src/demo/java`) is never compiled into release.
 */
object AppVariant : VariantContent {
    override val hasDemoContent: Boolean = true

    override fun seededTasks(): List<Task> = sampleTasks()
    override fun homeStories(): List<Story> = HomeFixtures.stories()
    override fun homeFeed(): HomeFeedFixtures = HomeFixtures.feed()
    override fun explorePeople(): List<ExplorePersonResult> = sampleExplorePeople()
    override fun exploreBrowseState(): ExploreBrowseState = ExploreBrowseState(
        services = ExploreBrowseStatus.Available,
        professionals = ExploreBrowseStatus.Available,
        tasks = ExploreBrowseStatus.Available,
    )
    override fun serviceDetail(serviceId: String): ServiceDetailData? = sampleServiceDetail(serviceId)
    override fun inboxSeed(): InboxSeed = sampleInboxSeed()
    override fun incomingLeads(): List<Job> = sampleIncomingLeads()
    override fun providerDashboardSample(profile: SessionProfile): ProviderDashboardUiState = sampleProviderDashboard(profile)
    override fun creatorDashboardSample(range: CreatorTimeRange, isServiceProvider: Boolean): CreatorDashboardSample =
        CreatorDashboardFixtures.sample(range, isServiceProvider)

    override fun publicProfile(userId: String): UserProfileData? = previewPublicProfile(userId)
    override fun previewProfile(id: String): SessionProfile? = com.askit.app.preview.previewProfile(id)
    override fun previewProfileStorage(context: Context): ProfileSnapshotStorage? =
        if (BuildConfig.CLIENT_PREVIEW) PreviewProfileStorage(context) else null
    override fun seedPreviewContent(tasks: TaskRepository, posts: PostStore, jobs: JobsStore) =
        com.askit.app.preview.seedPreviewContent(tasks, posts, jobs)

    @Composable
    override fun PreviewProfileControls(onAccount: (String) -> Unit, onView: (String) -> Unit) =
        com.askit.app.preview.PreviewProfileControls(onAccount, onView)
}
