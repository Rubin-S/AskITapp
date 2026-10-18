package com.askit.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import com.askit.designsystem.dialogs.AskITAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.SideEffect
import com.askit.app.task.TaskDiscussionViewModel
import com.askit.app.task.TaskActionsViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.askit.app.createpost.CreatePostViewModel
import com.askit.app.config.AppLinks
import com.askit.app.auth.AuthDestination
import com.askit.app.auth.AuthOperation
import com.askit.app.createpost.PostDraft
import com.askit.app.creatordashboard.CreatorDashboardViewModel
import com.askit.app.explore.ExploreBrowseSection
import com.askit.app.explore.ExploreBrowseState
import com.askit.app.explore.ExploreFilterOption
import com.askit.app.explore.ExploreResultScope
import com.askit.app.explore.ExploreResultState
import com.askit.app.explore.ExploreSortOption
import com.askit.app.explore.ExploreViewModel
import com.askit.app.home.HomeViewModel
import com.askit.app.home.data.FakeHomeRepository
import com.askit.app.home.stories.StoryViewerViewModel
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
import com.askit.app.task.InMemoryTaskRepository
import com.askit.app.task.toExploreTaskResult
import com.askit.app.task.TaskRepository
import com.askit.app.story.StoryDraft
import com.askit.app.story.StoryViewModel
import com.askit.designsystem.navigation.AskITBottomBar
import com.askit.designsystem.navigation.AskITCreateAction
import com.askit.designsystem.navigation.AskITCreateSheet

@Composable
fun AskITApp(
    exploreViewModel: ExploreViewModel,
    taskRepository: TaskRepository? = null,
    taskDiscussionViewModel: TaskDiscussionViewModel,
    taskActionsViewModel: TaskActionsViewModel,
    homeViewModel: HomeViewModel? = null,
    postTaskViewModel: PostTaskViewModel? = null,
    listServiceViewModel: ListServiceViewModel? = null,
    createPostViewModel: CreatePostViewModel? = null,
    creatorDashboardViewModel: CreatorDashboardViewModel? = null,
    providerDashboardViewModel: com.askit.app.providerdashboard.ProviderDashboardViewModel? = null,
    storyViewModel: StoryViewModel? = null,
    jobsViewModel: JobsViewModel? = null,
    inboxViewModel: InboxViewModel? = null,
    profileViewModel: ProfileViewModel? = null,
    storyViewerViewModel: StoryViewerViewModel,
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
    clock: () -> Long = System::currentTimeMillis,
    initialRoute: AppDestination = AppDestination.Home,
    authViewModel: com.askit.app.auth.AuthViewModel? = null,
    onSignedOut: () -> Unit = onExit,
    appLinks: AppLinks = AppLinks(),
) {
    val resolvedAuthViewModel = authViewModel ?: remember { com.askit.app.auth.AuthViewModel() }
    val authState by resolvedAuthViewModel.state.collectAsStateWithLifecycle()
    val resolvedTaskRepository = taskRepository ?: remember { InMemoryTaskRepository() }
    val resolvedDiscussionViewModel = taskDiscussionViewModel
    val resolvedTaskActionsViewModel = taskActionsViewModel
    val resolvedPostTaskViewModel = postTaskViewModel ?: remember { PostTaskViewModel() }
    val fallbackHomeRepository = remember(resolvedTaskRepository) { FakeHomeRepository(resolvedTaskRepository) }
    val resolvedHomeViewModel = homeViewModel ?: remember(fallbackHomeRepository) { HomeViewModel(fallbackHomeRepository) }
    val resolvedCreatePostViewModel = createPostViewModel ?: remember { CreatePostViewModel() }
    val resolvedStoryViewerViewModel = storyViewerViewModel
    val storySnackbar = remember { SnackbarHostState() }
    val storyFeedback by resolvedStoryViewerViewModel.social.feedback.collectAsStateWithLifecycle()
    val storyContext = LocalContext.current
    val resources = LocalResources.current
    val undoLabel = stringResource(R.string.story_undo)
    LaunchedEffect(storyFeedback?.id) {
        val feedback = storyFeedback ?: return@LaunchedEffect
        val result = storySnackbar.showSnackbar(
            resources.getString(feedback.messageRes),
            actionLabel = undoLabel.takeIf { feedback.undo != null },
            withDismissAction = true,
            duration = if (feedback.undo != null) SnackbarDuration.Long else SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed && feedback.undo != null) {
            val currentId = resolvedAuthViewModel.session?.userId?.takeUnless {
                resolvedAuthViewModel.state.value.signedOut || resolvedAuthViewModel.state.value.operation in
                    setOf(AuthOperation.SignOut, AuthOperation.RestoreSession, AuthOperation.LoadAccount)
            }
            resolvedStoryViewerViewModel.social.undo(currentId, feedback.undo)
        }
        resolvedStoryViewerViewModel.social.consumeFeedback(feedback.id)
    }
    val postHideFeedback by resolvedHomeViewModel.posts.hideFeedback.collectAsStateWithLifecycle()
    LaunchedEffect(postHideFeedback?.id) {
        val feedback = postHideFeedback ?: return@LaunchedEffect
        val result = storySnackbar.showSnackbar(
            resources.getString(R.string.post_hidden_from_home),
            actionLabel = undoLabel,
            withDismissAction = true,
            duration = SnackbarDuration.Long,
        )
        if (result == SnackbarResult.ActionPerformed) {
            val currentId = resolvedAuthViewModel.session?.userId?.takeUnless {
                resolvedAuthViewModel.state.value.signedOut || resolvedAuthViewModel.state.value.operation in
                    setOf(AuthOperation.SignOut, AuthOperation.RestoreSession, AuthOperation.LoadAccount)
            }
            val restored = resolvedHomeViewModel.posts.undoHidden(currentId, feedback)
            if (restored is com.askit.app.home.posts.PostResult.Failure) {
                storySnackbar.showSnackbar(resources.getString(restored.messageRes))
            }
        }
        resolvedHomeViewModel.posts.consumeHideFeedback(feedback.id)
    }
    val discussionAccountId = resolvedAuthViewModel.session?.userId?.takeUnless {
        authState.signedOut || authState.operation == com.askit.app.auth.AuthOperation.SignOut
    }
    val isResolvingDiscussionAccount = discussionAccountId == null &&
        (authState.operation == AuthOperation.RestoreSession || authState.operation == AuthOperation.LoadAccount)
    SideEffect {
        // A temporarily unknown identity during restoration is not a sign-out.
        if (!isResolvingDiscussionAccount && (authState.operation != AuthOperation.SignOut || authState.signedOut)) {
            resolvedDiscussionViewModel.activateAccount(discussionAccountId)
            resolvedTaskActionsViewModel.activateAccount(discussionAccountId)
            resolvedPostTaskViewModel.activateAccount(discussionAccountId)
            resolvedStoryViewerViewModel.activateAccount(discussionAccountId)
            resolvedHomeViewModel.posts.activateAccount(discussionAccountId)
            resolvedCreatePostViewModel.activateAccount(discussionAccountId)
        }
    }

    LaunchedEffect(authState.signedOut) {
        if (authState.signedOut) onSignedOut()
    }
    if (authState.operation == com.askit.app.auth.AuthOperation.SignOut || authState.signedOut) {
        BackHandler { }
        Surface(modifier = Modifier.fillMaxSize().testTag("auth_logout_screen")) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val errorRes = authState.errorRes
                if (errorRes == null) {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.auth_logging_out))
                } else {
                    Text(stringResource(errorRes))
                    TextButton(onClick = resolvedAuthViewModel::signOut, modifier = Modifier.testTag("auth_logout_retry")) {
                        Text(stringResource(R.string.explore_retry))
                    }
                }
            }
        }
        return
    }
    val viewModelAppliedFilterOptions by exploreViewModel.appliedFilterOptions.collectAsStateWithLifecycle()
    val controlledAppliedFilterOptions = if (
        appliedFilterOptions.isEmpty() && onFiltersChanged == null
    ) {
        viewModelAppliedFilterOptions
    } else {
        appliedFilterOptions
    }
    val controlledFiltersChanged = onFiltersChanged ?: exploreViewModel::onFiltersChanged
    val resolvedListServiceViewModel = listServiceViewModel ?: remember { ListServiceViewModel() }
    val resolvedStoryViewModel = storyViewModel ?: remember { StoryViewModel() }
    val resolvedJobsViewModel = jobsViewModel ?: remember {
        val profile = resolvedAuthViewModel.profileStore
        JobsViewModel(JobsStore(profile), profile)
    }
    val resolvedCreatorDashboardViewModel = creatorDashboardViewModel
        ?: remember(resolvedJobsViewModel) {
            CreatorDashboardViewModel(resolvedJobsViewModel.profileStore)
        }
    val resolvedProviderDashboardViewModel = providerDashboardViewModel
        ?: remember(resolvedJobsViewModel) {
            com.askit.app.providerdashboard.ProviderDashboardViewModel(resolvedJobsViewModel.profileStore)
        }
    val resolvedInboxViewModel = inboxViewModel ?: remember { InboxViewModel() }
    val messagingProfiles = remember(storyContext) { com.askit.app.preview.PreviewProfileStorage(storyContext) }
    LaunchedEffect(resolvedInboxViewModel) {
        resolvedInboxViewModel.feedback.collect { message ->
            android.widget.Toast.makeText(storyContext, message, android.widget.Toast.LENGTH_LONG).show()
        }
    }
    SideEffect {
        resolvedInboxViewModel.configureAccess { sender, recipient, isContact ->
            val session = resolvedAuthViewModel.session
            val current = resolvedJobsViewModel.profileStore.profile.value
            val state = resolvedAuthViewModel.state.value
            if (!BuildConfig.CLIENT_PREVIEW || session?.userId != sender || current.accountId != sender ||
                state.signedOut || state.operation in setOf(AuthOperation.SignOut, AuthOperation.RestoreSession, AuthOperation.LoadAccount)) {
                com.askit.app.inbox.MessageAccess.Unavailable
            } else runCatching {
                val target = messagingProfiles.load(recipient)
                    ?: if (recipient in setOf("preview-owner", "preview-visitor", "preview-seeker")) com.askit.app.preview.previewProfile(recipient) else null
                val preference = target?.whoCanMessage ?: "Everyone".takeIf {
                    com.askit.app.home.details.getUserProfileById(recipient) != null ||
                        com.askit.app.inbox.seededContacts().any { it.id == recipient }
                }
                com.askit.app.inbox.messagingAccess(sender, recipient, preference, isContact, current.isVerified)
            }.getOrDefault(com.askit.app.inbox.MessageAccess.Unavailable)
        }
        if (!isResolvingDiscussionAccount && (authState.operation != AuthOperation.SignOut || authState.signedOut)) {
            resolvedInboxViewModel.activateAccount(discussionAccountId)
        }
    }
    val resolvedProfileViewModel = profileViewModel ?: remember(resolvedJobsViewModel) {
        ProfileViewModel(LocalProfileRepository(resolvedJobsViewModel.profileStore))
    }

    val navigationState = rememberAskITNavigationState(initialRoute = initialRoute)
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }
    var showApplyGate by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(authState.destination) {
        val destination = authState.destination ?: return@LaunchedEffect
        when (destination) {
            AuthDestination.Code -> navigationState.push(AppDestination.AuthOtp(authState.phoneNumber))
            AuthDestination.Signup -> navigationState.resetForAuthentication(AppDestination.FormA(authState.phoneNumber))
            AuthDestination.Home -> navigationState.resetForAuthentication(AppDestination.Home)
            AuthDestination.Entry -> navigationState.resetForAuthentication(AppDestination.Entry)
        }
        showCreateSheet = false
        showApplyGate = false
        resolvedAuthViewModel.navigationHandled()
    }
    if (authState.operation == AuthOperation.RestoreSession || authState.operation == AuthOperation.LoadAccount ||
        authState.destination != null
    ) {
        BackHandler { }
        Surface(modifier = Modifier.fillMaxSize().testTag("auth_account_gate")) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val errorRes = authState.errorRes
                if (errorRes == null) {
                    CircularProgressIndicator()
                    Text(stringResource(if (authState.operation == AuthOperation.RestoreSession) {
                        R.string.auth_restoring_session
                    } else R.string.auth_loading_account))
                } else {
                    Text(stringResource(errorRes))
                    if (errorRes != R.string.auth_session_expired) {
                        TextButton(onClick = resolvedAuthViewModel::retryAccountLoad, modifier = Modifier.testTag("auth_account_retry")) {
                            Text(stringResource(R.string.explore_retry))
                        }
                    }
                }
                TextButton(onClick = resolvedAuthViewModel::signOut, modifier = Modifier.testTag("auth_account_sign_in_again")) {
                    Text(stringResource(R.string.auth_sign_in_again))
                }
            }
        }
        return
    }
    val sharedTasks by resolvedTaskRepository.tasks.collectAsStateWithLifecycle()
    val sharedResultState = if (resultState is ExploreResultState.Results) {
        resultState.copy(tasks = sharedTasks.map { it.toExploreTaskResult() })
    } else {
        resultState
    }
    val entryProvider = rememberAskITEntryProvider(
        navigationState = navigationState,
        authViewModel = resolvedAuthViewModel,
        appLinks = appLinks,
        taskRepository = resolvedTaskRepository,
        taskDiscussionViewModel = resolvedDiscussionViewModel,
        taskActionsViewModel = resolvedTaskActionsViewModel,
        discussionAccountId = discussionAccountId,
        homeViewModel = resolvedHomeViewModel,
        exploreViewModel = exploreViewModel,
        postTaskViewModel = resolvedPostTaskViewModel,
        listServiceViewModel = resolvedListServiceViewModel,
        createPostViewModel = resolvedCreatePostViewModel,
        creatorDashboardViewModel = resolvedCreatorDashboardViewModel,
        providerDashboardViewModel = resolvedProviderDashboardViewModel,
        storyViewModel = resolvedStoryViewModel,
        jobsViewModel = resolvedJobsViewModel,
        inboxViewModel = resolvedInboxViewModel,
        profileViewModel = resolvedProfileViewModel,
        storyViewerViewModel = resolvedStoryViewerViewModel,
        resultState = sharedResultState,
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
    val profileState by resolvedProfileViewModel.profile.collectAsStateWithLifecycle()
    AskITShell(
        storySnackbar = storySnackbar,
        navigationState = navigationState,
        avatarUrl = profileState.avatarUrl,
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
                AskITCreateAction.CreatorDashboard -> {
                    navigationState.push(AppDestination.CreatorDashboard)
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
    storySnackbar: SnackbarHostState,
    navigationState: com.askit.app.navigation.AskITNavigationState,
    avatarUrl: String?,
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
        snackbarHost = { SnackbarHost(storySnackbar, modifier = Modifier.safeDrawingPadding()) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (navigationState.isAtRoot) {
                AskITBottomBar(
                    selectedDestination = navigationState.topLevelRoute.bottomBarDestination,
                    avatarUrl = avatarUrl,
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
                .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
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
        AskITAlertDialog(
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
