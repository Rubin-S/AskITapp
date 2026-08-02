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
import androidx.compose.ui.Modifier
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
import com.askit.app.explore.ExplorePersonResult
import com.askit.app.explore.ExploreFilterOption
import com.askit.app.explore.ExploreResultScope
import com.askit.app.explore.ExploreSortOption
import com.askit.app.explore.ExploreTaskResult
import com.askit.app.explore.ExploreViewModel
import com.askit.app.explore.SearchAreaRoute
import com.askit.app.explore.defaultExploreFilterOptions
import com.askit.designsystem.navigation.AskITBottomBar
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
        setContent {
            AskITTheme {
                AskITApp(
                    exploreViewModel = exploreViewModel,
                    onExit = ::finish,
                    availableFilterOptions = defaultExploreFilterOptions(),
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
    onExit: () -> Unit = {},
    submittedPeople: List<ExplorePersonResult> = emptyList(),
    submittedTasks: List<ExploreTaskResult> = emptyList(),
    availableSortOptions: Map<ExploreResultScope, List<ExploreSortOption>> = emptyMap(),
    selectedSortOptions: Map<ExploreResultScope, ExploreSortOption> = emptyMap(),
    onSortChanged: ((ExploreResultScope, ExploreSortOption) -> Unit)? = null,
    onPersonClick: ((String) -> Unit)? = null,
    onTaskClick: ((String) -> Unit)? = null,
    availableFilterOptions: Map<ExploreResultScope, List<ExploreFilterOption>> = emptyMap(),
    appliedFilterOptions: Map<ExploreResultScope, Set<ExploreFilterOption>> = emptyMap(),
    onFiltersChanged: ((ExploreResultScope, Set<ExploreFilterOption>) -> Unit)? = null,
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
    val navigationState = rememberAskITNavigationState()
    val entryProvider = entryProvider<NavKey> {
        entry<AppDestination.Home> {
            EmptyRootDestination()
        }
        entry<AppDestination.Explore> {
            ExploreRoute(
                viewModel = exploreViewModel,
                onSearchFiltersClick = { navigationState.push(AppDestination.SearchAreaDestination) },
                submittedPeople = submittedPeople,
                submittedTasks = submittedTasks,
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
        entry<AppDestination.Inbox> {
            EmptyRootDestination()
        }
        entry<AppDestination.Profile> {
            EmptyRootDestination()
        }
    }
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }

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
                    onCreateClick = { showCreateSheet = true },
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
            onActionClick = {},
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
private fun EmptyRootDestination() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {}
}
