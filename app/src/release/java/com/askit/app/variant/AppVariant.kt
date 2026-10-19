package com.askit.app.variant

import android.content.Context
import androidx.compose.runtime.Composable
import com.askit.app.creatordashboard.CreatorDashboardSample
import com.askit.app.creatordashboard.CreatorTimeRange
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
import com.askit.app.navigation.AppDestination
import com.askit.app.providerdashboard.ProviderDashboardUiState
import com.askit.app.session.SessionProfile
import com.askit.app.task.Task
import com.askit.app.task.TaskRepository

/**
 * Release implementation: no demo content at all. Until the backend exists, screens backed by
 * these values show their empty or "not available yet" states. Guarded by ReleaseVariantTest.
 */
object AppVariant : VariantContent {
    override val hasDemoContent: Boolean = false

    override fun seededTasks(): List<Task> = emptyList()
    override fun homeStories(): List<Story> = emptyList()
    override fun homeFeed(): HomeFeedFixtures = HomeFeedFixtures.Empty
    override fun explorePeople(): List<ExplorePersonResult> = emptyList()

    /** No search backend yet: say so instead of showing fixture professionals. Local tasks are real. */
    override fun exploreBrowseState(): ExploreBrowseState = ExploreBrowseState(
        services = ExploreBrowseStatus.ServerUnavailable,
        professionals = ExploreBrowseStatus.ServerUnavailable,
        tasks = ExploreBrowseStatus.Available,
    )

    override fun serviceDetail(serviceId: String): ServiceDetailData? = null
    override fun inboxSeed(): InboxSeed = InboxSeed.Empty
    override fun incomingLeads(): List<Job> = emptyList()
    override fun providerDashboardSample(profile: SessionProfile): ProviderDashboardUiState? = null
    override fun creatorDashboardSample(range: CreatorTimeRange, isServiceProvider: Boolean): CreatorDashboardSample? = null

    override fun publicProfile(userId: String): UserProfileData? = null
    override fun previewProfile(id: String): SessionProfile? = null
    override fun previewProfileStorage(context: Context): ProfileSnapshotStorage? = null
    override fun seedPreviewContent(tasks: TaskRepository, posts: PostStore, jobs: JobsStore) = Unit

    @Composable
    override fun PreviewProfileControls(onAccount: (String) -> Unit, onView: (String) -> Unit) = Unit

    @Composable
    override fun DemoBadge(signedIn: Boolean, onOpen: (AppDestination) -> Unit, onExploreDemo: () -> Unit) = Unit

    @Composable
    override fun DemoSettings(onOpen: (AppDestination) -> Unit) = Unit
}
