package com.askit.app

import android.os.Bundle
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import com.askit.app.explore.ExploreRoute
import com.askit.app.explore.ExploreBrowseState
import com.askit.app.explore.ExploreBrowseSection
import com.askit.app.explore.ExploreFilterOption
import com.askit.app.explore.ExploreResultState
import com.askit.app.explore.ExploreResultScope
import com.askit.app.explore.ExploreSortOption
import com.askit.app.explore.ExploreViewModel
import com.askit.app.explore.SearchAreaRoute
import com.askit.app.explore.defaultExploreFilterOptions
import com.askit.app.posttask.PostTaskDraft
import com.askit.app.posttask.PostTaskRoute
import com.askit.app.posttask.PostTaskViewModel
import com.askit.app.listservice.ListServiceDraft
import com.askit.app.listservice.ListServiceRoute
import com.askit.app.listservice.ListServiceViewModel
import com.askit.app.createpost.CreatePostRoute
import com.askit.app.createpost.CreatePostViewModel
import com.askit.app.createpost.PostDraft
import com.askit.app.story.StoryDraft
import com.askit.app.story.StoryRoute
import com.askit.app.story.StoryViewModel
import com.askit.designsystem.empty.AskITEmptyState
import com.askit.designsystem.navigation.AskITBottomBar
import com.askit.designsystem.navigation.AskITCreateAction
import com.askit.designsystem.navigation.AskITCreateSheet
import com.askit.designsystem.navigation.AskITDestination
import com.askit.designsystem.theme.AskITTheme
import com.google.android.libraries.places.api.Places
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializePlaces()
        val exploreViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer {
                    ExploreViewModel(createSavedStateHandle())
                }
            },
        )[ExploreViewModel::class.java]
        val postTaskViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer {
                    PostTaskViewModel(createSavedStateHandle())
                }
            },
        )[PostTaskViewModel::class.java]
        val listServiceViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer {
                    ListServiceViewModel(createSavedStateHandle())
                }
            },
        )[ListServiceViewModel::class.java]
        val createPostViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer {
                    CreatePostViewModel(createSavedStateHandle())
                }
            },
        )[CreatePostViewModel::class.java]
        val storyViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer {
                    StoryViewModel(createSavedStateHandle())
                }
            },
        )[StoryViewModel::class.java]
        setContent {
            AskITTheme {
                AskITApp(
                    exploreViewModel = exploreViewModel,
                    postTaskViewModel = postTaskViewModel,
                    listServiceViewModel = listServiceViewModel,
                    createPostViewModel = createPostViewModel,
                    storyViewModel = storyViewModel,
                    onExit = ::finish,
                    availableFilterOptions = defaultExploreFilterOptions(),
                    treatUnresolvedSearchAsEmpty = true,
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

@Composable
fun AskITApp(
    exploreViewModel: ExploreViewModel,
    postTaskViewModel: PostTaskViewModel? = null,
    listServiceViewModel: ListServiceViewModel? = null,
    createPostViewModel: CreatePostViewModel? = null,
    storyViewModel: StoryViewModel? = null,
    onExit: () -> Unit = {},
    resultState: ExploreResultState = ExploreResultState.Loading,
    browseState: ExploreBrowseState = ExploreBrowseState(),
    onRetryResults: (() -> Unit)? = null,
    onRetryBrowseSection: ((ExploreBrowseSection) -> Unit)? = null,
    availableSortOptions: Map<ExploreResultScope, List<ExploreSortOption>> = emptyMap(),
    selectedSortOptions: Map<ExploreResultScope, ExploreSortOption> = emptyMap(),
    onSortChanged: ((ExploreResultScope, ExploreSortOption) -> Unit)? = null,
    onPersonClick: ((String) -> Unit)? = null,
    onTaskClick: ((String) -> Unit)? = null,
    availableFilterOptions: Map<ExploreResultScope, List<ExploreFilterOption>> = emptyMap(),
    appliedFilterOptions: Map<ExploreResultScope, Set<ExploreFilterOption>> = emptyMap(),
    onFiltersChanged: ((ExploreResultScope, Set<ExploreFilterOption>) -> Unit)? = null,
    onPostTaskCompleted: (PostTaskDraft) -> Unit = {},
    onListServiceCompleted: (ListServiceDraft) -> Unit = {},
    onCreatePostCompleted: (PostDraft) -> Unit = {},
    onStoryCompleted: (StoryDraft) -> Unit = {},
    treatUnresolvedSearchAsEmpty: Boolean = false,
) {
    val viewModelAppliedFilterOptions by exploreViewModel.appliedFilterOptions.collectAsStateWithLifecycle()
    val controlledAppliedFilterOptions = if (
        appliedFilterOptions.isEmpty() && onFiltersChanged == null
    ) {
        viewModelAppliedFilterOptions
    } else {
        appliedFilterOptions
    }
    val controlledFiltersChanged = onFiltersChanged ?: exploreViewModel::onFiltersChanged
    val resolvedPostTaskViewModel = postTaskViewModel ?: remember {
        PostTaskViewModel()
    }
    val resolvedListServiceViewModel = listServiceViewModel ?: remember {
        ListServiceViewModel()
    }
    val resolvedCreatePostViewModel = createPostViewModel ?: remember {
        CreatePostViewModel()
    }
    val resolvedStoryViewModel = storyViewModel ?: remember {
        StoryViewModel()
    }
    val navigationState = rememberAskITNavigationState()
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }
    val entryProvider = entryProvider<NavKey> {
        entry<AppDestination.Home> {
            EmptyRootDestination(
                iconRes = com.askit.designsystem.R.drawable.ic_home_outlined,
                titleRes = com.askit.designsystem.R.string.empty_home_title,
                supportingRes = com.askit.designsystem.R.string.empty_home_supporting,
                actionRes = com.askit.designsystem.R.string.empty_home_action,
                onAction = { navigationState.push(AppDestination.Story) },
            )
        }
        entry<AppDestination.Explore> {
            val exploreQuery by exploreViewModel.uiState.collectAsStateWithLifecycle()
            val resolvedResultState =
                if (
                    treatUnresolvedSearchAsEmpty &&
                    onRetryResults == null &&
                    resultState is ExploreResultState.Loading &&
                    exploreQuery.query.isNotBlank()
                ) {
                    ExploreResultState.Empty(ExploreResultState.EmptyReason.Query)
                } else {
                    resultState
                }
            ExploreRoute(
                viewModel = exploreViewModel,
                onSearchFiltersClick = { navigationState.push(AppDestination.SearchAreaDestination) },
                resultState = resolvedResultState,
                browseState = browseState,
                onRetryResults = onRetryResults,
                onRetryBrowseSection = onRetryBrowseSection,
                availableSortOptions = availableSortOptions,
                selectedSortOptions = selectedSortOptions,
                onSortChanged = onSortChanged,
                availableFilterOptions = availableFilterOptions,
                appliedFilterOptions = controlledAppliedFilterOptions,
                onFiltersChanged = controlledFiltersChanged,
                onFilterScopeChanged = exploreViewModel::onFilterScopeSelected,
                onPersonClick = onPersonClick,
                onTaskClick = onTaskClick,
            )
        }
        entry<AppDestination.SearchAreaDestination> {
            SearchAreaRoute(
                viewModel = exploreViewModel,
                onBack = { navigationState.pop() },
                availableFilterOptions = availableFilterOptions,
                appliedFilterOptions = controlledAppliedFilterOptions,
                onFiltersChanged = controlledFiltersChanged,
            )
        }
        entry<AppDestination.PostTask> {
            PostTaskRoute(
                viewModel = resolvedPostTaskViewModel,
                onBack = { navigationState.pop() },
                onCompleteDraft = onPostTaskCompleted,
            )
        }
        entry<AppDestination.ListService> {
            ListServiceRoute(
                viewModel = resolvedListServiceViewModel,
                onBack = { navigationState.pop() },
                onCompleteDraft = onListServiceCompleted,
            )
        }
        entry<AppDestination.CreatePost> {
            CreatePostRoute(
                viewModel = resolvedCreatePostViewModel,
                onBack = { navigationState.pop() },
                onCompleteDraft = onCreatePostCompleted,
            )
        }
        entry<AppDestination.Story> {
            StoryRoute(
                viewModel = resolvedStoryViewModel,
                onBack = { navigationState.pop() },
                onCompleteDraft = onStoryCompleted,
                onOpenCreateSheet = { showCreateSheet = true },
            )
        }
        entry<AppDestination.Inbox> {
            EmptyRootDestination(
                iconRes = com.askit.designsystem.R.drawable.ic_inbox_outlined,
                titleRes = com.askit.designsystem.R.string.empty_inbox_title,
                supportingRes = com.askit.designsystem.R.string.empty_inbox_supporting,
            )
        }
        entry<AppDestination.Profile> {
            EmptyRootDestination(
                iconRes = com.askit.designsystem.R.drawable.ic_person,
                titleRes = com.askit.designsystem.R.string.empty_profile_title,
                supportingRes = com.askit.designsystem.R.string.empty_profile_supporting,
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (navigationState.isAtRoot) {
                AskITBottomBar(
                    selectedDestination = navigationState.topLevelRoute.bottomBarDestination,
                    avatarUrl = null,
                    unreadCount = 0,
                    onDestinationClick = { destination ->
                        navigationState.navigate(AppDestination.fromBottomBarDestination(destination))
                    },
                    onCreateClick = { navigationState.push(AppDestination.Story) },
                )
            }
        },
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background,
        ) {
            NavDisplay(
                entries = navigationState.toEntries(entryProvider),
                modifier = Modifier.fillMaxSize(),
                onBack = {
                    if (!navigationState.goBack()) {
                        onExit()
                    }
                },
            )
        }
    }

    if (showCreateSheet) {
        AskITCreateSheet(
            onDismiss = { showCreateSheet = false },
            onActionClick = { action ->
                if (!navigationState.isAtRoot) {
                    navigationState.pop()
                }
                when (action) {
                    AskITCreateAction.PostTask -> {
                        resolvedPostTaskViewModel.startNewDraft()
                        navigationState.push(AppDestination.PostTask)
                    }
                    AskITCreateAction.AddService -> {
                        resolvedListServiceViewModel.startNewDraft()
                        navigationState.push(AppDestination.ListService)
                    }
                    AskITCreateAction.CreatePost -> {
                        resolvedCreatePostViewModel.startNewDraft()
                        navigationState.push(AppDestination.CreatePost)
                    }
                }
            },
        )
    }
}

@Composable
private fun rememberAskITNavigationState(): AskITNavigationState {
    val topLevelRoute = rememberSerializable(
        serializer = MutableStateSerializer(NavKeySerializer()),
    ) {
        mutableStateOf<NavKey>(AppDestination.Home)
    }
    val backStacks = TOP_LEVEL_ROUTES.associateWith { rememberNavBackStack(it) }

    return remember {
        AskITNavigationState(
            topLevelRouteState = topLevelRoute,
            backStacks = backStacks,
        )
    }
}

private class AskITNavigationState(
    private val topLevelRouteState: MutableState<NavKey>,
    private val backStacks: Map<AppDestination, NavBackStack<NavKey>>,
) {
    val topLevelRoute: AppDestination
        get() = topLevelRouteState.value as AppDestination

    fun navigate(route: AppDestination) {
        topLevelRouteState.value = route
    }

    fun push(route: AppDestination) {
        val currentStack = backStacks.getValue(topLevelRoute)
        if (currentStack.lastOrNull() != route) currentStack.add(route)
    }

    fun pop(): Boolean {
        val currentStack = backStacks.getValue(topLevelRoute)
        if (currentStack.size <= 1) return false
        currentStack.removeLastOrNull()
        return true
    }

    val isAtRoot: Boolean
        get() = backStacks.getValue(topLevelRoute).size == 1

    fun goBack(): Boolean {
        val currentStack = backStacks.getValue(topLevelRoute)
        if (currentStack.size > 1) {
            currentStack.removeLastOrNull()
            return true
        }
        if (topLevelRoute != AppDestination.Home) {
            topLevelRouteState.value = AppDestination.Home
            return true
        }
        return false
    }

    @Composable
    fun toEntries(entryProvider: (NavKey) -> NavEntry<NavKey>): List<NavEntry<NavKey>> {
        val decoratedEntries = backStacks.mapValues { (_, stack) ->
            rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator<NavKey>()),
                entryProvider = entryProvider,
            )
        }
        val routesInUse = if (topLevelRoute == AppDestination.Home) {
            listOf(AppDestination.Home)
        } else {
            listOf(AppDestination.Home, topLevelRoute)
        }
        return routesInUse.flatMap { decoratedEntries.getValue(it) }
    }
}

@Serializable
private sealed interface AppDestination : NavKey {
    val bottomBarDestination: AskITDestination

    @Serializable
    data object Home : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data object Explore : AppDestination {
        override val bottomBarDestination = AskITDestination.Explore
    }

    @Serializable
    data object Inbox : AppDestination {
        override val bottomBarDestination = AskITDestination.Inbox
    }

    @Serializable
    data object Profile : AppDestination {
        override val bottomBarDestination = AskITDestination.Profile
    }

    @Serializable
    data object SearchAreaDestination : AppDestination {
        override val bottomBarDestination = AskITDestination.Explore
    }

    @Serializable
    data object PostTask : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data object ListService : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data object CreatePost : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data object Story : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    companion object {
        fun fromBottomBarDestination(destination: AskITDestination): AppDestination = when (destination) {
            AskITDestination.Home -> Home
            AskITDestination.Explore -> Explore
            AskITDestination.Inbox -> Inbox
            AskITDestination.Profile -> Profile
        }
    }
}

private val TOP_LEVEL_ROUTES = listOf(
    AppDestination.Home,
    AppDestination.Explore,
    AppDestination.Inbox,
    AppDestination.Profile,
)

@Composable
private fun EmptyRootDestination(
    @DrawableRes iconRes: Int,
    @StringRes titleRes: Int,
    @StringRes supportingRes: Int,
    @StringRes actionRes: Int? = null,
    onAction: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        AskITEmptyState(
            iconRes = iconRes,
            title = stringResource(titleRes),
            supporting = stringResource(supportingRes),
            actionLabel = actionRes?.let { stringResource(it) },
            onAction = onAction,
        )
    }
}
