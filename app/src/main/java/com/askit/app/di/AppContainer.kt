package com.askit.app.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.askit.app.auth.AuthApi
import com.askit.app.auth.AuthSessionStore
import com.askit.app.auth.AuthViewModel
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
import com.askit.app.posttask.PostTaskViewModel
import com.askit.app.profile.LocalProfileRepository
import com.askit.app.profile.ProfileViewModel
import com.askit.app.providerdashboard.ProviderDashboardViewModel
import com.askit.app.story.StoryViewModel
import com.askit.app.task.TaskActionsViewModel
import com.askit.app.task.TaskDiscussionViewModel
import com.askit.app.variant.AppVariant
import com.askit.app.variant.ProfileSnapshotStorage
import com.askit.app.variant.VariantContent

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
    )

    /** Local persona snapshots; only the client-preview build has any. */
    val previewStorage: ProfileSnapshotStorage? by lazy(previewStorage)

    fun newSessionGraph(): SessionGraph = SessionGraph(config, content, previewStorage)

    /** The [SessionGraph] retained by [owner] (normally the activity), created on first use. */
    fun sessionGraph(owner: ViewModelStoreOwner): SessionGraph = ViewModelProvider(
        owner,
        viewModelFactory { initializer { newSessionGraph() } },
    )[SessionGraph::class.java]

    /** One factory for every screen view model, all wired to [graph]. */
    fun viewModelFactory(graph: SessionGraph): ViewModelProvider.Factory = viewModelFactory {
        initializer { AppLinksViewModel(config.backendBaseUrl) }
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
            )
        }
        initializer {
            ProfileViewModel(LocalProfileRepository(graph.profileStore), signupAvailable = config.clientPreview)
        }
        initializer { CreatorDashboardViewModel(graph.profileStore, content::creatorDashboardSample) }
        initializer { ProviderDashboardViewModel(graph.profileStore, content::providerDashboardSample) }
    }
}
