package com.askit.app.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil3.SingletonImageLoader
import com.askit.app.auth.AuthApi
import com.askit.app.auth.AuthSessionStore
import com.askit.app.auth.AuthViewModel
import com.askit.app.config.AppLinks
import com.askit.app.config.AppLinksViewModel
import com.askit.app.createpost.CreatePostViewModel
import com.askit.app.creatordashboard.CreatorDashboardViewModel
import com.askit.app.explore.ExploreViewModel
import com.askit.app.explore.FixedExploreContentSource
import com.askit.app.home.HomeViewModel
import com.askit.app.home.stories.StoryViewerViewModel
import com.askit.app.inbox.InboxViewModel
import com.askit.app.jobs.JobsViewModel
import com.askit.app.listservice.ListServiceViewModel
import com.askit.app.location.LocationPreference
import com.askit.app.posttask.PostTaskViewModel
import com.askit.app.profile.DeviceSettings
import com.askit.app.profile.LocalDataCleaner
import com.askit.app.profile.ProfileCapabilities
import com.askit.app.profile.ProfileViewModel
import com.askit.app.providerdashboard.ProviderDashboardViewModel
import com.askit.app.story.StoryViewModel
import com.askit.app.task.TaskActionsViewModel
import com.askit.app.task.TaskDiscussionViewModel
import com.askit.app.variant.AppVariant
import com.askit.app.variant.ProfileSnapshotStorage
import com.askit.app.variant.VariantContent
import java.io.File

/**
 * Hand-rolled dependency container, created once by [com.askit.app.AskITApplication].
 *
 * It holds only stateless, process-wide things (build config, variant content, preview storage).
 * Everything that holds account data lives in a [SessionGraph], which is scoped to the activity
 * so it is dropped on sign-out. [viewModelFactory] builds every screen's view model from it.
 */
class AppContainer(
    val config: AppConfig,
    val content: VariantContent,
    previewStorage: () -> ProfileSnapshotStorage?,
    private val authSessionStore: (backendBaseUrl: String) -> AuthSessionStore?,
    deviceSettings: () -> DeviceSettings = { com.askit.app.profile.InMemoryDeviceSettings() },
    private val localDataCleaner: LocalDataCleaner = LocalDataCleaner {},
) {
    constructor(
        context: Context,
        config: AppConfig = AppConfig.fromBuildConfig(),
        content: VariantContent = AppVariant,
    ) : this(
        config = config,
        content = content,
        previewStorage = {
            if (config.clientPreview) content.previewProfileStorage(context.applicationContext) else null
        },
        authSessionStore = { baseUrl -> AuthSessionStore(context.applicationContext, baseUrl) },
        deviceSettings = { LocationPreference(context.applicationContext) },
        localDataCleaner = LocalDataCleaner { clearLocalData(context.applicationContext, content.previewProfileStorage(context)) },
    )

    /** Local persona snapshots; only the client-preview build has any. */
    val previewStorage: ProfileSnapshotStorage? by lazy(previewStorage)

    /** Device preferences that are not account fields (the Location services switch). */
    private val deviceSettings: DeviceSettings by lazy(deviceSettings)

    fun newSessionGraph(): SessionGraph = SessionGraph(config, content, previewStorage)

    /** The [SessionGraph] retained by [owner] (normally the activity), created on first use. */
    fun sessionGraph(owner: ViewModelStoreOwner): SessionGraph = ViewModelProvider(
        owner,
        viewModelFactory { initializer { newSessionGraph() } },
    )[SessionGraph::class.java]

    /** One factory for every screen view model, all wired to [graph]. */
    fun viewModelFactory(graph: SessionGraph): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            AppLinksViewModel(
                config.backendBaseUrl,
                // Demo build has no app-config endpoint; give profile links a base so Copy/Share work.
                initialLinks = if (config.demoBuild) AppLinks(publicProfileBaseUrl = DEMO_PROFILE_BASE_URL) else AppLinks(),
            )
        }
        initializer { TaskDiscussionViewModel(graph.tasks, createSavedStateHandle()) }
        initializer { TaskActionsViewModel(graph.tasks, createSavedStateHandle()) }
        initializer { HomeViewModel(graph.home) }
        initializer {
            // Release has no search backend yet: no fixture people, and browse sections say so.
            ExploreViewModel(
                createSavedStateHandle(),
                FixedExploreContentSource(people = content::explorePeople, browseState = content::exploreBrowseState),
            )
        }
        initializer { PostTaskViewModel(createSavedStateHandle()) }
        initializer { ListServiceViewModel(createSavedStateHandle()) }
        initializer { CreatePostViewModel(createSavedStateHandle()) }
        initializer { StoryViewModel(createSavedStateHandle()) }
        initializer { StoryViewerViewModel(graph.home, createSavedStateHandle()) }
        initializer { JobsViewModel(graph.jobs, graph.profileStore) }
        initializer { InboxViewModel(seed = content::inboxSeed, retainInactiveAccounts = config.clientPreview) }
        initializer {
            AuthViewModel(
                clientPreview = config.clientPreview,
                api = config.backendBaseUrl?.let { AuthApi(it) },
                sessionStore = config.backendBaseUrl?.let(authSessionStore),
                profileStore = graph.profileStore,
                previewProfileLoader = { id -> previewStorage?.load(id) ?: content.previewProfile(id) },
                autoSignInPreviewOwner = !config.demoBuild,
            )
        }
        initializer {
            ProfileViewModel(
                graph.profileStore,
                capabilities = ProfileCapabilities.forBuild(config.clientPreview),
                deviceSettings = deviceSettings,
                localData = localDataCleaner,
            )
        }
        initializer { CreatorDashboardViewModel(graph.profileStore, content::creatorDashboardSample) }
        initializer { ProviderDashboardViewModel(graph.profileStore, content::providerDashboardSample) }
    }
}

/** Reserved `.example` domain: demo links can be copied and shared but never resolve to a real profile. */
private const val DEMO_PROFILE_BASE_URL = "https://demo.askit.example/u/"

/**
 * Clear app data: everything the app keeps on the device. Account sign-out (credentials) and the
 * in-memory session are handled by the caller, which restarts the app afterwards.
 */
private fun clearLocalData(context: Context, previewStorage: ProfileSnapshotStorage?) {
    previewStorage?.clearAll()
    LocationPreference.clear(context)
    File(context.filesDir, "post_photos").deleteRecursively()
    val images = SingletonImageLoader.get(context)
    images.memoryCache?.clear()
    images.diskCache?.clear()
    val imageCache = images.diskCache?.directory?.toFile()
    context.cacheDir.listFiles()?.filter { it != imageCache }?.forEach { it.deleteRecursively() }
}
