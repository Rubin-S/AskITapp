package com.askit.app.variant

import android.content.Context
import androidx.compose.runtime.Composable
import com.askit.app.creatordashboard.CreatorDashboardSample
import com.askit.app.creatordashboard.CreatorTimeRange
import com.askit.app.explore.ExploreBrowseState
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
 * Everything that differs between a build that may show demo content and a release build.
 *
 * Production code depends only on this interface. The single implementation, `AppVariant`, is
 * supplied per build type:
 * - `src/demo/java` (compiled into **debug** and **preview**) returns sample feed, people, chats,
 *   jobs, dashboards and the client-preview personas.
 * - `src/release/java` (compiled into **release** only) returns nothing, so release screens fall
 *   back to their honest empty/"not available yet" states.
 *
 * Preview-only members are additionally gated by `BuildConfig.CLIENT_PREVIEW` at the call site, so
 * debug builds get sample content but not the preview account switcher.
 */
interface VariantContent {
    /** True only where demo fixtures are compiled in. Always false in release. */
    val hasDemoContent: Boolean

    fun seededTasks(): List<Task>
    fun homeStories(): List<Story>
    fun homeFeed(): HomeFeedFixtures
    fun explorePeople(): List<ExplorePersonResult>
    fun exploreBrowseState(): ExploreBrowseState
    fun serviceDetail(serviceId: String): ServiceDetailData?
    fun inboxSeed(): InboxSeed
    fun incomingLeads(): List<Job>
    fun providerDashboardSample(profile: SessionProfile): ProviderDashboardUiState?
    fun creatorDashboardSample(range: CreatorTimeRange, isServiceProvider: Boolean): CreatorDashboardSample?

    // --- Client-preview only (callers also check BuildConfig.CLIENT_PREVIEW) ---

    fun publicProfile(userId: String): UserProfileData?
    fun previewProfile(id: String): SessionProfile?
    fun previewProfileStorage(context: Context): ProfileSnapshotStorage?
    fun seedPreviewContent(tasks: TaskRepository, posts: PostStore, jobs: JobsStore)

    @Composable
    fun PreviewProfileControls(onAccount: (String) -> Unit, onView: (String) -> Unit)

    // --- Demo build only (render nothing unless BuildConfig.DEMO_BUILD) ---

    /** "DEMO" chip drawn over every screen; opens a menu that jumps to any screen. */
    @Composable
    fun DemoBadge(signedIn: Boolean, onOpen: (AppDestination) -> Unit, onExploreDemo: () -> Unit)

    /** Settings rows: the screen menu and "Reset demo data". */
    @Composable
    fun DemoSettings(onOpen: (AppDestination) -> Unit)
}

/** Durable per-account profile snapshots. Only the client-preview build has an implementation. */
interface ProfileSnapshotStorage {
    fun loadRelationships(): Map<String, Set<String>>
    fun saveRelationships(relationships: Map<String, Set<String>>)
    fun load(accountId: String): SessionProfile?
    fun save(profile: SessionProfile): SessionProfile

    /** Deletes every stored persona, relationship and retained media file (Clear app data). */
    fun clearAll()
}
