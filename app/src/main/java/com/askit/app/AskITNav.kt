package com.askit.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.askit.app.createpost.CreatePostViewModel
import com.askit.app.createpost.PostDraft
import com.askit.app.explore.ExploreBrowseSection
import com.askit.app.explore.ExploreBrowseState
import com.askit.app.explore.ExploreFilterOption
import com.askit.app.explore.ExploreResultScope
import com.askit.app.explore.ExploreResultState
import com.askit.app.explore.ExploreSortOption
import com.askit.app.explore.ExploreViewModel
import com.askit.app.inbox.InboxViewModel
import com.askit.app.jobs.JobsStore
import com.askit.app.jobs.JobsViewModel
import com.askit.app.profile.LocalProfileRepository
import com.askit.app.profile.ProfileViewModel
import com.askit.app.listservice.ListServiceDraft
import com.askit.app.listservice.ListServiceViewModel
import com.askit.app.navigation.AppDestination
import com.askit.app.navigation.rememberAskITNavigationState
import com.askit.app.posttask.PostTaskDraft
import com.askit.app.posttask.PostTaskViewModel
import com.askit.app.session.SessionProfileStore
import com.askit.designsystem.navigation.AskITBottomBar
import com.askit.designsystem.navigation.AskITCreateAction
import com.askit.designsystem.navigation.AskITCreateSheet

@Composable
fun AskITApp(
    exploreViewModel: ExploreViewModel,
    postTaskViewModel: PostTaskViewModel? = null,
    listServiceViewModel: ListServiceViewModel? = null,
    createPostViewModel: CreatePostViewModel? = null,
    jobsViewModel: JobsViewModel? = null,
    inboxViewModel: InboxViewModel? = null,
    profileViewModel: ProfileViewModel? = null,
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
    treatUnresolvedSearchAsEmpty: Boolean = false,
    clock: () -> Long = System::currentTimeMillis,
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
    val resolvedPostTaskViewModel = postTaskViewModel ?: remember { PostTaskViewModel() }
    val resolvedListServiceViewModel = listServiceViewModel ?: remember { ListServiceViewModel() }
    val resolvedCreatePostViewModel = createPostViewModel ?: remember { CreatePostViewModel() }
    val resolvedJobsViewModel = jobsViewModel ?: remember {
        val profile = SessionProfileStore()
        JobsViewModel(JobsStore(profile), profile)
    }
    val resolvedInboxViewModel = inboxViewModel ?: remember { InboxViewModel() }
    val resolvedProfileViewModel = profileViewModel ?: remember(resolvedJobsViewModel) {
        ProfileViewModel(LocalProfileRepository(resolvedJobsViewModel.profileStore))
    }
    val navigationState = rememberAskITNavigationState()
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }
    var showApplyGate by rememberSaveable { mutableStateOf(false) }
    val entryProvider = rememberAskITEntryProvider(
        navigationState = navigationState,
        exploreViewModel = exploreViewModel,
        postTaskViewModel = resolvedPostTaskViewModel,
        listServiceViewModel = resolvedListServiceViewModel,
        createPostViewModel = resolvedCreatePostViewModel,
        jobsViewModel = resolvedJobsViewModel,
        inboxViewModel = resolvedInboxViewModel,
        profileViewModel = resolvedProfileViewModel,
        resultState = resultState,
        browseState = browseState,
        onRetryResults = onRetryResults,
        onRetryBrowseSection = onRetryBrowseSection,
        availableSortOptions = availableSortOptions,
        selectedSortOptions = selectedSortOptions,
        onSortChanged = onSortChanged,
        onPersonClick = onPersonClick,
        onTaskClick = onTaskClick,
        availableFilterOptions = availableFilterOptions,
        appliedFilterOptions = controlledAppliedFilterOptions,
        onFiltersChanged = controlledFiltersChanged,
        treatUnresolvedSearchAsEmpty = treatUnresolvedSearchAsEmpty,
        onPostTaskCompleted = onPostTaskCompleted,
        onListServiceCompleted = onListServiceCompleted,
        onCreatePostCompleted = onCreatePostCompleted,
        onShowCreateSheet = { showCreateSheet = true },
        onApplyBlocked = { showApplyGate = true },
        clock = clock,
    )
    AskITShell(
        navigationState = navigationState,
        unreadCount = resolvedInboxViewModel.unreadCount,
        entryProvider = entryProvider,
        onExit = onExit,
        onCreateClick = { showCreateSheet = true },
        showCreateSheet = showCreateSheet,
        onDismissCreateSheet = { showCreateSheet = false },
        onCreateAction = { action ->
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
        showApplyGate = showApplyGate,
        onDismissApplyGate = { showApplyGate = false },
        onListServiceFromGate = {
            showApplyGate = false
            resolvedListServiceViewModel.startNewDraft()
            navigationState.push(AppDestination.ListService)
        },
    )
}

@Composable
private fun AskITShell(
    navigationState: com.askit.app.navigation.AskITNavigationState,
    unreadCount: Int,
    entryProvider: (NavKey) -> androidx.navigation3.runtime.NavEntry<NavKey>,
    onExit: () -> Unit,
    onCreateClick: () -> Unit,
    showCreateSheet: Boolean,
    onDismissCreateSheet: () -> Unit,
    onCreateAction: (AskITCreateAction) -> Unit,
    showApplyGate: Boolean,
    onDismissApplyGate: () -> Unit,
    onListServiceFromGate: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (navigationState.isAtRoot) {
                AskITBottomBar(
                    selectedDestination = navigationState.topLevelRoute.bottomBarDestination,
                    avatarUrl = null,
                    unreadCount = unreadCount,
                    onDestinationClick = { destination ->
                        navigationState.navigate(AppDestination.fromBottomBarDestination(destination))
                    },
                    onCreateClick = onCreateClick,
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
                    if (!navigationState.goBack()) onExit()
                },
            )
        }
    }
    if (showCreateSheet) {
        AskITCreateSheet(
            onDismiss = onDismissCreateSheet,
            onActionClick = { action ->
                onDismissCreateSheet()
                onCreateAction(action)
            },
        )
    }
    if (showApplyGate) {
        AlertDialog(
            onDismissRequest = onDismissApplyGate,
            title = { Text(stringResource(R.string.job_apply_gate_title)) },
            text = { Text(stringResource(R.string.job_apply_gate_message)) },
            confirmButton = {
                TextButton(onClick = onListServiceFromGate) {
                    Text(stringResource(R.string.job_apply_gate_action))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissApplyGate) {
                    Text(stringResource(R.string.job_apply_gate_dismiss))
                }
            },
        )
    }
}
