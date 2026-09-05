package com.askit.app

import android.os.Bundle
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.askit.app.createpost.CreatePostViewModel
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
import com.askit.app.posttask.PostTaskViewModel
import com.askit.app.profile.LocalProfileRepository
import com.askit.app.profile.ProfileViewModel
import com.askit.app.session.SessionProfileStore
import com.askit.app.story.StoryViewModel
import com.askit.app.task.InMemoryTaskRepository
import com.askit.designsystem.theme.AskITTheme
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        initializePlaces()
        val taskRepository = InMemoryTaskRepository()
        val homeRepository = FakeHomeRepository(taskRepository)
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
                    val profile = SessionProfileStore()
                    JobsViewModel(JobsStore(profile), profile)
                }
            },
        )[JobsViewModel::class.java]
        val inboxViewModel = ViewModelProvider(this)[InboxViewModel::class.java]
        val storyViewerViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer { StoryViewerViewModel(homeRepository) }
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
        val sampleTasks = com.askit.app.explore.sampleExploreTasks()
        val exploreResultState = com.askit.app.explore.ExploreResultState.Results(
            people = samplePeople,
            tasks = sampleTasks,
        )
        val exploreBrowseState = com.askit.app.explore.ExploreBrowseState(
            services = com.askit.app.explore.ExploreBrowseStatus.Available,
            professionals = com.askit.app.explore.ExploreBrowseStatus.Available,
            tasks = com.askit.app.explore.ExploreBrowseStatus.Available,
        )
        val isRobolectric = android.os.Build.FINGERPRINT.contains("robolectric", ignoreCase = true)
        val initialRoute = if (intent.getBooleanExtra("extra_start_entry", false) || (!isRobolectric && !intent.getBooleanExtra("extra_start_at_home", false))) {
            com.askit.app.navigation.AppDestination.Entry
        } else {
            com.askit.app.navigation.AppDestination.Home
        }
        setContent {
            AskITTheme {
                AskITApp(
                    taskRepository = taskRepository,
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
                    availableFilterOptions = defaultExploreFilterOptions(),
                    treatUnresolvedSearchAsEmpty = false,
                    initialRoute = initialRoute,
                )
            }
        }
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
}
