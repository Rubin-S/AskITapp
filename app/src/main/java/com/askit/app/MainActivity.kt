package com.askit.app

import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.askit.app.createpost.CreatePostViewModel
import com.askit.app.config.AppLinksViewModel
import com.askit.app.creatordashboard.CreatorDashboardViewModel
import com.askit.app.explore.ExploreViewModel
import com.askit.app.explore.defaultExploreFilterOptions
import com.askit.app.home.HomeViewModel
import com.askit.app.home.data.FakeHomeRepository
import com.askit.app.home.stories.StoryViewerViewModel
import com.askit.app.inbox.InboxViewModel
import com.askit.app.jobs.JobsStore
import com.askit.app.jobs.JobsViewModel
import com.askit.app.listservice.ListServiceViewModel
import com.askit.app.task.TaskDiscussionViewModel
import com.askit.app.task.TaskActionsViewModel
import com.askit.app.posttask.PostTaskViewModel
import com.askit.app.profile.LocalProfileRepository
import com.askit.app.profile.ProfileViewModel
import com.askit.app.session.SessionProfileStore
import com.askit.app.story.StoryViewModel
import com.askit.app.task.InMemoryTaskRepository
import com.askit.designsystem.theme.AskITTheme
import com.google.android.libraries.places.api.Places

class MainActivity : AppCompatActivity() {
    private lateinit var appLinksViewModel: AppLinksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        initializePlaces()
        appLinksViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer { AppLinksViewModel(BuildConfig.BACKEND_BASE_URL.takeIf { it.isNotBlank() }) }
            },
        )[AppLinksViewModel::class.java]
        val repositories = ViewModelProvider(
            this,
            viewModelFactory { initializer { Repositories(applicationContext) } },
        )[Repositories::class.java]
        val taskRepository = repositories.tasks
        val taskDiscussionViewModel = ViewModelProvider(
            this,
            viewModelFactory { initializer { TaskDiscussionViewModel(taskRepository, createSavedStateHandle()) } },
        )[TaskDiscussionViewModel::class.java]
        val taskActionsViewModel = ViewModelProvider(
            this,
            viewModelFactory { initializer { TaskActionsViewModel(taskRepository, createSavedStateHandle()) } },
        )[TaskActionsViewModel::class.java]
        val homeRepository = repositories.home
        val homeViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer { HomeViewModel(homeRepository) }
            },
        )[HomeViewModel::class.java]
        val exploreViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer { ExploreViewModel(createSavedStateHandle()) }
            },
        )[ExploreViewModel::class.java]
        val postTaskViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer { PostTaskViewModel(createSavedStateHandle()) }
            },
        )[PostTaskViewModel::class.java]
        val listServiceViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer { ListServiceViewModel(createSavedStateHandle()) }
            },
        )[ListServiceViewModel::class.java]
        val createPostViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer { CreatePostViewModel(createSavedStateHandle()) }
            },
        )[CreatePostViewModel::class.java]
        val storyViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer { StoryViewModel(createSavedStateHandle()) }
            },
        )[StoryViewModel::class.java]
        val jobsViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer {
                    val previewStorage = if (BuildConfig.CLIENT_PREVIEW) com.askit.app.preview.PreviewProfileStorage(applicationContext) else null
                    val profile = SessionProfileStore(persist = { previewStorage?.save(it) ?: it })
                    JobsViewModel(JobsStore(profile), profile)
                }
            },
        )[JobsViewModel::class.java]
        val authViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer {
                    val backendUrl = BuildConfig.BACKEND_BASE_URL.takeIf { it.isNotBlank() }
                    com.askit.app.auth.AuthViewModel(
                        clientPreview = BuildConfig.CLIENT_PREVIEW,
                        api = backendUrl?.let { com.askit.app.auth.AuthApi(it) },
                        sessionStore = backendUrl?.let { com.askit.app.auth.AuthSessionStore(applicationContext, it) },
                        profileStore = jobsViewModel.profileStore,
                        previewProfileLoader = { id -> com.askit.app.preview.PreviewProfileStorage(applicationContext).load(id) },
                    )
                }
            },
        )[com.askit.app.auth.AuthViewModel::class.java]
        if (BuildConfig.CLIENT_PREVIEW && !repositories.previewSeeded) {
            repositories.previewSeeded = true
            com.askit.app.preview.seedPreviewContent(taskRepository, homeViewModel.posts, jobsViewModel.store)
        }
        val inboxViewModel = ViewModelProvider(this)[InboxViewModel::class.java]
        val storyViewerViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer { StoryViewerViewModel(homeRepository, createSavedStateHandle()) }
            },
        )[StoryViewerViewModel::class.java]
        val profileViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer {
                    ProfileViewModel(LocalProfileRepository(jobsViewModel.profileStore))
                }
            },
        )[ProfileViewModel::class.java]
        val creatorDashboardViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer {
                    CreatorDashboardViewModel(jobsViewModel.profileStore)
                }
            },
        )[CreatorDashboardViewModel::class.java]
        val providerDashboardViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer {
                    com.askit.app.providerdashboard.ProviderDashboardViewModel(jobsViewModel.profileStore)
                }
            },
        )[com.askit.app.providerdashboard.ProviderDashboardViewModel::class.java]
        val samplePeople = com.askit.app.explore.sampleExplorePeople()
        val exploreResultState = com.askit.app.explore.ExploreResultState.Results(
            people = samplePeople,
            tasks = emptyList(),
        )
        val exploreBrowseState = com.askit.app.explore.ExploreBrowseState(
            services = com.askit.app.explore.ExploreBrowseStatus.Available,
            professionals = com.askit.app.explore.ExploreBrowseStatus.Available,
            tasks = com.askit.app.explore.ExploreBrowseStatus.Available,
        )
        val initialRoute = if (BuildConfig.CLIENT_PREVIEW || (BuildConfig.DEBUG &&
            intent.getBooleanExtra("extra_start_at_home", false) &&
            !intent.getBooleanExtra("extra_start_entry", false))
        ) {
            com.askit.app.navigation.AppDestination.Home
        } else {
            com.askit.app.navigation.AppDestination.Entry
        }
        if (initialRoute == com.askit.app.navigation.AppDestination.Entry) authViewModel.restoreSession()
        setContent {
            val appLinks by appLinksViewModel.links.collectAsStateWithLifecycle()
            AskITTheme {
                // Consume system-bar/cutout insets once for every route. Screens handle their own IME.
                Box(
                    Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout)),
                ) {
                    AskITApp(
                        appLinks = appLinks,
                        authViewModel = authViewModel,
                        taskRepository = taskRepository,
                        taskDiscussionViewModel = taskDiscussionViewModel,
                        taskActionsViewModel = taskActionsViewModel,
                        homeViewModel = homeViewModel,
                        exploreViewModel = exploreViewModel,
                        postTaskViewModel = postTaskViewModel,
                        listServiceViewModel = listServiceViewModel,
                        createPostViewModel = createPostViewModel,
                        creatorDashboardViewModel = creatorDashboardViewModel,
                        providerDashboardViewModel = providerDashboardViewModel,
                        storyViewModel = storyViewModel,
                        jobsViewModel = jobsViewModel,
                        inboxViewModel = inboxViewModel,
                        storyViewerViewModel = storyViewerViewModel,
                        profileViewModel = profileViewModel,
                        resultState = exploreResultState,
                        browseState = exploreBrowseState,
                        onExit = ::finish,
                        onSignedOut = {
                            if (BuildConfig.CLIENT_PREVIEW) {
                                finishAndRemoveTask()
                            } else {
                                startActivity(Intent.makeRestartActivityTask(componentName).putExtra("extra_start_entry", true))
                                finish()
                            }
                        },
                        availableFilterOptions = defaultExploreFilterOptions(),
                        treatUnresolvedSearchAsEmpty = false,
                        initialRoute = initialRoute,
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appLinksViewModel.refresh()
    }

    private fun initializePlaces() {
        val apiKey = packageManager
            .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            .metaData
            ?.getString("com.google.android.geo.API_KEY")
            .orEmpty()
        if (apiKey.isBlank() || apiKey == "DEFAULT_API_KEY" || Places.isInitialized()) return
        runCatching {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        }
    }

    private class Repositories(context: android.content.Context) : ViewModel() {
        var previewSeeded = false
        val tasks = InMemoryTaskRepository()
        private val previewStorage = if (BuildConfig.CLIENT_PREVIEW) com.askit.app.preview.PreviewProfileStorage(context) else null
        val home = FakeHomeRepository(tasks, storySocial = com.askit.app.home.stories.StorySocialStore(
            initialRelationships = runCatching { previewStorage?.loadRelationships().orEmpty() }.getOrDefault(emptyMap()),
            persistRelationships = { previewStorage?.saveRelationships(it) },
        ))
    }
}
