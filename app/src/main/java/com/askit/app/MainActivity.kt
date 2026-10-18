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
import com.askit.app.auth.AuthViewModel
import com.askit.app.createpost.CreatePostViewModel
import com.askit.app.providerdashboard.ProviderDashboardViewModel
import com.askit.app.config.AppLinksViewModel
import com.askit.app.creatordashboard.CreatorDashboardViewModel
import com.askit.app.explore.ExploreViewModel
import com.askit.app.explore.defaultExploreFilterOptions
import com.askit.app.home.HomeViewModel
import com.askit.app.home.stories.StoryViewerViewModel
import com.askit.app.inbox.InboxViewModel
import com.askit.app.jobs.JobsViewModel
import com.askit.app.listservice.ListServiceViewModel
import com.askit.app.task.TaskDiscussionViewModel
import com.askit.app.task.TaskActionsViewModel
import com.askit.app.posttask.PostTaskViewModel
import com.askit.app.profile.ProfileViewModel
import com.askit.app.story.StoryViewModel
import com.askit.designsystem.theme.AskITTheme
import com.google.android.libraries.places.api.Places

class MainActivity : AppCompatActivity() {
    private lateinit var appLinksViewModel: AppLinksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        initializePlaces()
        val container = (application as AskITApplication).container
        val graph = container.sessionGraph(this)
        val viewModels = ViewModelProvider(this, container.viewModelFactory(graph))
        appLinksViewModel = viewModels[AppLinksViewModel::class.java]
        val taskRepository = graph.tasks
        val taskDiscussionViewModel = viewModels[TaskDiscussionViewModel::class.java]
        val taskActionsViewModel = viewModels[TaskActionsViewModel::class.java]
        val homeViewModel = viewModels[HomeViewModel::class.java]
        val exploreViewModel = viewModels[ExploreViewModel::class.java]
        val postTaskViewModel = viewModels[PostTaskViewModel::class.java]
        val listServiceViewModel = viewModels[ListServiceViewModel::class.java]
        val createPostViewModel = viewModels[CreatePostViewModel::class.java]
        val storyViewModel = viewModels[StoryViewModel::class.java]
        val jobsViewModel = viewModels[JobsViewModel::class.java]
        val authViewModel = viewModels[AuthViewModel::class.java]
        if (container.config.clientPreview && !graph.previewSeeded) {
            graph.previewSeeded = true
            container.content.seedPreviewContent(taskRepository, homeViewModel.posts, jobsViewModel.store)
        }
        val inboxViewModel = viewModels[InboxViewModel::class.java]
        val storyViewerViewModel = viewModels[StoryViewerViewModel::class.java]
        val profileViewModel = viewModels[ProfileViewModel::class.java]
        val creatorDashboardViewModel = viewModels[CreatorDashboardViewModel::class.java]
        val providerDashboardViewModel = viewModels[ProviderDashboardViewModel::class.java]
        // Release has no search backend yet: no fixture people, and browse sections say so.
        val exploreResultState = com.askit.app.explore.ExploreResultState.Results(
            people = container.content.explorePeople(),
            tasks = emptyList(),
        )
        val exploreBrowseState = container.content.exploreBrowseState()
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
}
