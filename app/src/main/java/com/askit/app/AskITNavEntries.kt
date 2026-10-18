package com.askit.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.askit.app.auth.AuthOperation
import com.askit.app.config.AppLinks
import com.askit.app.config.profileUrl
import com.askit.app.createpost.CreatePostRoute
import com.askit.app.createpost.CreatePostViewModel
import com.askit.app.createpost.PostDraft
import com.askit.app.creatordashboard.CreatorDashboardRoute
import com.askit.app.creatordashboard.CreatorDashboardViewModel
import com.askit.app.entry.EntryScreen
import com.askit.app.explore.ExploreBrowseSection
import com.askit.app.explore.ExploreBrowseState
import com.askit.app.explore.ExploreFilterOption
import com.askit.app.explore.ExploreResultScope
import com.askit.app.explore.ExploreResultState
import com.askit.app.explore.ExploreRoute
import com.askit.app.explore.ExploreSortOption
import com.askit.app.explore.ExploreViewModel
import com.askit.app.explore.SearchAreaRoute
import com.askit.app.home.HomeScreen
import com.askit.app.home.HomeViewModel
import com.askit.app.home.details.PostDetailScreen
import com.askit.app.home.details.ServiceDetailScreen
import com.askit.app.home.details.TaskDetailScreen
import com.askit.app.home.details.UserProfileScreen
import com.askit.app.home.stories.StoryViewerRoute
import com.askit.app.home.stories.StoryViewerViewModel
import com.askit.app.inbox.ChatContact
import com.askit.app.inbox.ChatThread
import com.askit.app.inbox.InboxViewModel
import com.askit.app.inbox.MessagesPane
import com.askit.app.inbox.MessagesRoute
import com.askit.app.inbox.NewMessageScreen
import com.askit.app.jobs.ApplyToTaskResult
import com.askit.app.jobs.JobsViewModel
import com.askit.app.jobs.isSeeker
import com.askit.app.jobs.ui.JobDetail
import com.askit.app.jobs.ui.JobReview
import com.askit.app.jobs.ui.JobVerifyEnter
import com.askit.app.jobs.ui.JobVerifyShare
import com.askit.app.listservice.ListServiceDraft
import com.askit.app.listservice.ListServiceRoute
import com.askit.app.listservice.ListServiceViewModel
import com.askit.app.listservice.categoryLabel
import com.askit.app.listservice.toServiceListing
import com.askit.app.navigation.AppDestination
import com.askit.app.navigation.AskITNavigationState
import com.askit.app.posttask.PostTaskDraft
import com.askit.app.posttask.PostTaskRoute
import com.askit.app.posttask.PostTaskViewModel
import com.askit.app.posttask.TaskEditUnavailableScreen
import com.askit.app.profile.EditProfileScreen
import com.askit.app.profile.ProfileReview
import com.askit.app.profile.ProfileRoute
import com.askit.app.profile.ProfileSettingsScreen
import com.askit.app.profile.ProfileViewModel
import com.askit.app.profile.toPublicProfile
import com.askit.app.story.StoryRoute
import com.askit.app.story.StoryViewModel
import com.askit.app.task.Task
import com.askit.app.task.TaskActionsViewModel
import com.askit.app.task.TaskCommentAuthor
import com.askit.app.task.TaskDiscussionViewModel
import com.askit.app.task.TaskMutationResult
import com.askit.app.task.TaskRepository
import com.askit.app.task.actionPolicy
import com.askit.app.task.toExploreTaskResult
import com.askit.app.variant.AppVariant

@Composable
internal fun rememberAskITEntryProvider(
    navigationState: AskITNavigationState,
    authViewModel: com.askit.app.auth.AuthViewModel,
    appLinks: AppLinks,
    taskRepository: TaskRepository,
    taskDiscussionViewModel: TaskDiscussionViewModel,
    taskActionsViewModel: TaskActionsViewModel,
    discussionAccountId: String?,
    homeViewModel: HomeViewModel,
    exploreViewModel: ExploreViewModel,
    postTaskViewModel: PostTaskViewModel,
    listServiceViewModel: ListServiceViewModel,
    createPostViewModel: CreatePostViewModel,
    creatorDashboardViewModel: CreatorDashboardViewModel,
    providerDashboardViewModel: com.askit.app.providerdashboard.ProviderDashboardViewModel,
    storyViewModel: StoryViewModel,
    jobsViewModel: JobsViewModel,
    inboxViewModel: InboxViewModel,
    profileViewModel: ProfileViewModel,
    storyViewerViewModel: StoryViewerViewModel,
    resultState: ExploreResultState,
    browseState: ExploreBrowseState,
    onRetryResults: (() -> Unit)?,
    onRetryBrowseSection: ((ExploreBrowseSection) -> Unit)?,
    availableSortOptions: Map<ExploreResultScope, List<ExploreSortOption>>,
    selectedSortOptions: Map<ExploreResultScope, ExploreSortOption>,
    onSortChanged: ((ExploreResultScope, ExploreSortOption) -> Unit)?,
    onPersonClick: ((String) -> Unit)?,
    onTaskClick: ((String) -> Unit)?,
    availableFilterOptions: Map<ExploreResultScope, List<ExploreFilterOption>>,
    appliedFilterOptions: Map<ExploreResultScope, Set<ExploreFilterOption>>,
    onFiltersChanged: ((ExploreResultScope, Set<ExploreFilterOption>) -> Unit)?,
    treatUnresolvedSearchAsEmpty: Boolean,
    onPostTaskCompleted: (PostTaskDraft) -> Unit,
    onListServiceCompleted: (ListServiceDraft) -> Unit,
    onCreatePostCompleted: (PostDraft) -> Unit,
    onShowCreateSheet: () -> Unit,
    onApplyBlocked: () -> Unit,
    clock: () -> Long,
): (NavKey) -> NavEntry<NavKey> {
    val context = LocalContext.current
    val linkUnavailableMessage = stringResource(R.string.external_link_unavailable)
    val openExternalLink: (String) -> Unit = { address ->
        val uri = address.toUri()
        val action = if (uri.scheme == "mailto") Intent.ACTION_SENDTO else Intent.ACTION_VIEW
        try {
            context.startActivity(Intent(action, uri))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, linkUnavailableMessage, Toast.LENGTH_SHORT).show()
        } catch (_: SecurityException) {
            Toast.makeText(context, linkUnavailableMessage, Toast.LENGTH_SHORT).show()
        }
    }

    // Read the current session at action time, rather than trusting a rendered button.
    fun currentTaskAccountId(): String? = authViewModel.session?.userId?.takeIf { id ->
        id.isNotBlank() && !authViewModel.state.value.signedOut &&
            authViewModel.state.value.operation !in setOf(AuthOperation.SignOut, AuthOperation.RestoreSession, AuthOperation.LoadAccount)
    }
    fun saveTask(draft: PostTaskDraft, editTaskId: String? = null) {
        if (!BuildConfig.CLIENT_PREVIEW) {
            postTaskViewModel.showSubmissionError(R.string.task_action_unavailable)
            return
        }
        val id = currentTaskAccountId()
        if (id == null || !postTaskViewModel.isActiveAccount(id)) {
            postTaskViewModel.showSubmissionError(R.string.task_action_sign_in)
            return
        }
        if (postTaskViewModel.editingTaskId != editTaskId) {
            postTaskViewModel.showSubmissionError(R.string.task_action_unavailable)
            return
        }
        val profile = profileViewModel.profile.value
        val result = if (editTaskId == null) {
            taskRepository.createTask(draft, TaskCommentAuthor(id, profile.displayName, profile.avatarUrl))
        } else {
            taskRepository.updateTask(editTaskId, id, draft)
        }
        when (result) {
            is TaskMutationResult.Rejected -> postTaskViewModel.showSubmissionError(result.reason.messageRes)

            is TaskMutationResult.Success -> {
                if (editTaskId == null) onPostTaskCompleted(draft)
                postTaskViewModel.startNewDraft()
                navigationState.pop()
            }
        }
    }
    val profileRelationships by storyViewerViewModel.social.relationships.collectAsStateWithLifecycle()
    fun peopleFor(id: String): com.askit.app.profile.ProfilePeopleState {
        if (!BuildConfig.CLIENT_PREVIEW) return com.askit.app.profile.ProfilePeopleState(viewerId = currentTaskAccountId())
        fun person(accountId: String): com.askit.app.profile.ProfilePerson {
            val account = runCatching { AppVariant.previewProfileStorage(context)?.load(accountId) }.getOrNull()
            val sample = com.askit.app.home.details.getUserProfileById(accountId)
            return com.askit.app.profile.ProfilePerson(
                accountId,
                account?.displayName ?: sample?.name ?: accountId,
                account?.avatarUrl ?: sample?.avatarUrl,
            )
        }
        return com.askit.app.profile.ProfilePeopleState(
            currentTaskAccountId(),
            profileRelationships.filterValues { id in it }.keys.map(::person),
            profileRelationships[id].orEmpty().map(::person),
            profileRelationships[currentTaskAccountId()].orEmpty(),
            canMutate = true,
        )
    }
    fun openProfileDashboard(route: AppDestination) {
        if (BuildConfig.CLIENT_PREVIEW) {
            navigationState.push(route)
        } else {
            Toast.makeText(context, R.string.profile_action_unavailable, Toast.LENGTH_LONG).show()
        }
    }
    fun openProfile(id: String) {
        if (id == currentTaskAccountId()) {
            navigationState.navigateToRoot(AppDestination.Profile)
        } else {
            navigationState.push(AppDestination.UserProfile(id))
        }
    }
    fun followPerson(id: String, following: Boolean): Boolean {
        if (!BuildConfig.CLIENT_PREVIEW) {
            Toast.makeText(context, R.string.profile_save_unavailable, Toast.LENGTH_LONG).show()
            return false
        }
        return storyViewerViewModel.social.setFollowing(
            currentTaskAccountId(),
            id,
            following,
        ) is com.askit.app.home.stories.StoryActionResult.Success
    }
    return entryProvider<NavKey> {
        entry<AppDestination.Entry> {
            EntryScreen(
                onOpenTerms = { openExternalLink(appLinks.termsUrl) },
                onOpenPrivacyPolicy = { openExternalLink(appLinks.privacyPolicyUrl) },
                onGetStarted = {
                    navigationState.push(AppDestination.AuthPhone)
                },
                onLogin = {
                    navigationState.push(AppDestination.AuthPhone)
                },
            )
        }
        entry<AppDestination.AuthPhone> {
            val authState by authViewModel.state.collectAsStateWithLifecycle()
            val activity = LocalActivity.current
            DisposableEffect(authViewModel) {
                onDispose {
                    // Rotation, dark-mode and locale changes dispose this composition too. The request
                    // lives in viewModelScope and must survive them; only leaving the screen cancels it.
                    if (com.askit.app.auth.shouldCancelAuthRequestOnDispose(activity?.isChangingConfigurations == true)) {
                        authViewModel.cancelPendingRequest(AuthOperation.RequestCode)
                    }
                }
            }
            com.askit.app.auth.AuthPhoneScreen(
                onOpenTerms = { openExternalLink(appLinks.termsUrl) },
                onOpenPrivacyPolicy = { openExternalLink(appLinks.privacyPolicyUrl) },
                onBack = {
                    navigationState.pop()
                },
                onGetOtp = authViewModel::requestCode,
                isSubmitting = authState.isSubmitting,
                errorMessage = authState.errorRes?.let { stringResource(it) },
            )
        }
        entry<AppDestination.AuthOtp> { key ->
            val authState by authViewModel.state.collectAsStateWithLifecycle()
            val resendSeconds by produceState(0L, authState.resendAvailableAtMillis) {
                do {
                    val remaining = (authState.resendAvailableAtMillis - android.os.SystemClock.elapsedRealtime()).coerceAtLeast(0)
                    value = remaining / 1000 + if (remaining % 1000 == 0L) 0 else 1
                    if (value > 0) kotlinx.coroutines.delay(1000)
                } while (value > 0)
            }
            val activity = LocalActivity.current
            DisposableEffect(authViewModel) {
                onDispose {
                    // See AuthPhone: a consumed OTP or a sent SMS must not be lost to a rotation.
                    if (com.askit.app.auth.shouldCancelAuthRequestOnDispose(activity?.isChangingConfigurations == true)) {
                        authViewModel.cancelPendingRequest(AuthOperation.ResendCode, AuthOperation.VerifyCode, AuthOperation.SaveSession)
                    }
                }
            }
            com.askit.app.auth.AuthOtpScreen(
                phoneNumber = key.phoneNumber,
                onBack = {
                    navigationState.pop()
                },
                onEditPhone = {
                    navigationState.pop()
                },
                onVerify = authViewModel::verifyCode,
                onResend = authViewModel::resendCode,
                resendCountdownSeconds = resendSeconds,
                challengeId = authState.challenge?.challengeId,
                isResending = authState.isResending,
                isSubmitting = authState.isSubmitting,
                errorMessage = authState.errorRes?.let { stringResource(it) },
            )
        }
        entry<AppDestination.FormA> { key ->
            BackHandler(onBack = authViewModel::signOut)
            com.askit.app.auth.FormAScreen(
                phoneNumber = key.phoneNumber,
                onBack = authViewModel::signOut,
                signupAvailable = profileViewModel.canSignUp,
                onSubmit = { draft ->
                    val result = profileViewModel.completeSignup(draft) { uri ->
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            com.askit.app.auth.isSignupPhotoReadable(context.contentResolver, uri)
                        }
                    }
                    if (result == com.askit.app.auth.SignupResult.Saved) {
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(
                                com.askit.app.session.AppLanguage.fromStoredValue(draft.preferredLanguage).languageTag,
                            ),
                        )
                        navigationState.clearToHome()
                    }
                    result
                },
            )
        }
        entry<AppDestination.Home> {
            val social by storyViewerViewModel.social.state.collectAsStateWithLifecycle()
            HomeScreen(
                followingIds = social.following,
                followingAccountId = social.accountId,
                onTogglePersonFollow = { person ->
                    val store = storyViewerViewModel.social
                    val result = store.setFollowing(
                        currentTaskAccountId(),
                        person.id,
                        person.id !in store.state.value.following,
                    )
                    if (result is com.askit.app.home.stories.StoryActionResult.Rejected) {
                        store.notify(
                            if (result.reason == com.askit.app.home.stories.StoryActionFailure.SIGN_IN) {
                                R.string.nearby_follow_sign_in
                            } else {
                                R.string.nearby_follow_unavailable
                            },
                        )
                    }
                },
                viewModel = homeViewModel,
                currentAccountId = ::currentTaskAccountId,
                onEditPost = { navigationState.push(AppDestination.EditPost(it)) },
                onAddStoryClick = { post ->
                    if (post != null) {
                        storyViewModel.startReshareDraft(post)
                    } else {
                        storyViewModel.startNewDraft()
                    }
                    navigationState.push(AppDestination.Story)
                },
                onStoryClick = { story ->
                    navigationState.push(AppDestination.StoryViewer(startStoryId = story.id))
                },
                onTaskClick = { task ->
                    navigationState.push(AppDestination.TaskDetail(taskId = task.id))
                },
                onServiceClick = { service ->
                    navigationState.push(AppDestination.ServiceDetail(serviceId = service.id))
                },
                onPersonClick = { person ->
                    navigationState.push(AppDestination.UserProfile(userId = person.id))
                },
            )
        }
        entry<AppDestination.Explore> {
            val exploreQuery by exploreViewModel.uiState.collectAsStateWithLifecycle()
            val repositoryTasks by taskRepository.tasks.collectAsStateWithLifecycle()
            val mappedExploreTasks = repositoryTasks.map { it.toExploreTaskResult() }
            val effectiveResultState = when (resultState) {
                is ExploreResultState.Results -> resultState.copy(tasks = mappedExploreTasks)

                ExploreResultState.Loading -> if (treatUnresolvedSearchAsEmpty && exploreQuery.query.isNotBlank()) {
                    ExploreResultState.Empty(ExploreResultState.EmptyReason.Query)
                } else {
                    ExploreResultState.Results(
                        people = emptyList(),
                        tasks = mappedExploreTasks,
                    )
                }

                else -> resultState
            }
            ExploreRoute(
                viewModel = exploreViewModel,
                onSearchFiltersClick = { navigationState.push(AppDestination.SearchAreaDestination) },
                onSearchAreaClick = { navigationState.push(AppDestination.ExploreLocationDestination) },
                resultState = effectiveResultState,
                browseState = browseState,
                onRetryResults = onRetryResults,
                onRetryBrowseSection = onRetryBrowseSection,
                availableSortOptions = availableSortOptions,
                selectedSortOptions = selectedSortOptions,
                onSortChanged = onSortChanged,
                availableFilterOptions = availableFilterOptions,
                appliedFilterOptions = appliedFilterOptions,
                onFiltersChanged = onFiltersChanged,
                onFilterScopeChanged = exploreViewModel::onFilterScopeSelected,
                onPersonClick = onPersonClick ?: { personId ->
                    navigationState.push(AppDestination.UserProfile(userId = personId))
                },
                onTaskClick = onTaskClick ?: { taskId ->
                    navigationState.push(AppDestination.TaskDetail(taskId = taskId))
                },
            )
        }
        entry<AppDestination.ExploreLocationDestination> {
            SearchAreaRoute(
                viewModel = exploreViewModel,
                onBack = { navigationState.pop() },
                showFilterControls = false,
            )
        }
        entry<AppDestination.SearchAreaDestination> {
            SearchAreaRoute(
                viewModel = exploreViewModel,
                onBack = { navigationState.pop() },
                availableFilterOptions = availableFilterOptions,
                appliedFilterOptions = appliedFilterOptions,
                onFiltersChanged = onFiltersChanged,
            )
        }
        entry<AppDestination.PostTask> {
            PostTaskRoute(
                viewModel = postTaskViewModel,
                onBack = {
                    postTaskViewModel.startNewDraft()
                    navigationState.pop()
                },
                onCompleteDraft = { draft -> saveTask(draft) },
            )
        }
        entry<AppDestination.EditTask> { key ->
            val tasks by taskRepository.tasks.collectAsStateWithLifecycle()
            val task = tasks.firstOrNull { it.id == key.taskId }
            val draft = taskRepository.editableDraft(key.taskId, discussionAccountId)
            if (!task.actionPolicy(discussionAccountId).isOwner || draft == null ||
                !postTaskViewModel.isActiveAccount(discussionAccountId)
            ) {
                TaskEditUnavailableScreen(onBack = {
                    postTaskViewModel.clearTask(key.taskId)
                    navigationState.pop()
                })
            } else {
                LaunchedEffect(key.taskId, discussionAccountId) { postTaskViewModel.beginEdit(key.taskId, draft) }
                PostTaskRoute(
                    viewModel = postTaskViewModel,
                    onBack = {
                        postTaskViewModel.startNewDraft()
                        navigationState.pop()
                    },
                    onCompleteDraft = { updated -> saveTask(updated, key.taskId) },
                )
            }
        }
        entry<AppDestination.ListService> {
            val profile by profileViewModel.profile.collectAsStateWithLifecycle()
            ListServiceRoute(
                viewModel = listServiceViewModel,
                profile = profile,
                onBack = { navigationState.pop() },
                onCompleteDraft = { draft ->
                    if (!com.askit.app.BuildConfig.CLIENT_PREVIEW) {
                        android.widget.Toast.makeText(context, R.string.profile_save_unavailable, android.widget.Toast.LENGTH_LONG).show()
                        return@ListServiceRoute
                    }
                    jobsViewModel.profileStore.applyListing(
                        listing = draft.toServiceListing(draft.categoryLabel(context::getString)),
                        draft = draft,
                    )
                    onListServiceCompleted(draft)
                },
            )
        }
        entry<AppDestination.CreatePost> {
            CreatePostRoute(
                viewModel = createPostViewModel,
                onBack = {
                    createPostViewModel.startNewDraft()
                    navigationState.pop()
                },
                onCompleteDraft = { draft ->
                    when (
                        val result = homeViewModel.posts.publish(
                            draft,
                            currentTaskAccountId(),
                            jobsViewModel.profileStore.profile.value,
                        )
                    ) {
                        is com.askit.app.home.posts.PostResult.Failure -> android.widget.Toast.makeText(
                            context,
                            result.messageRes,
                            android.widget.Toast.LENGTH_LONG,
                        ).show()

                        is com.askit.app.home.posts.PostResult.Success -> {
                            createPostViewModel.retainPublishedCrops()
                            createPostViewModel.startNewDraft()
                            onCreatePostCompleted(draft)
                            navigationState.clearToHome()
                        }
                    }
                },
            )
        }
        entry<AppDestination.EditPost> { key ->
            val records by homeViewModel.posts.posts.collectAsStateWithLifecycle()
            val account by homeViewModel.posts.account.collectAsStateWithLifecycle()
            val post = records[key.postId]
            val draft = homeViewModel.posts.editableDraft(key.postId, account.accountId)
            if (post == null || draft == null || !homeViewModel.posts.isOwner(post, account.accountId)) {
                androidx.compose.foundation.layout.Column(androidx.compose.ui.Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
                    androidx.compose.material3.Text(stringResource(R.string.post_edit_unavailable))
                    androidx.compose.material3.TextButton(onClick = {
                        createPostViewModel.startNewDraft()
                        navigationState.pop()
                    }) {
                        androidx.compose.material3.Text(stringResource(R.string.explore_back))
                    }
                }
            } else {
                LaunchedEffect(key.postId, account.accountId) { createPostViewModel.beginEdit(key.postId, draft) }
                CreatePostRoute(
                    viewModel = createPostViewModel,
                    onBack = {
                        createPostViewModel.startNewDraft()
                        navigationState.pop()
                    },
                    onCompleteDraft = { updated ->
                        when (val result = homeViewModel.posts.update(key.postId, updated, currentTaskAccountId())) {
                            is com.askit.app.home.posts.PostResult.Failure -> android.widget.Toast.makeText(
                                context,
                                result.messageRes,
                                android.widget.Toast.LENGTH_LONG,
                            ).show()

                            is com.askit.app.home.posts.PostResult.Success -> {
                                createPostViewModel.retainPublishedCrops()
                                createPostViewModel.startNewDraft()
                                navigationState.pop()
                            }
                        }
                    },
                )
            }
        }
        entry<AppDestination.CreatorDashboard> {
            CreatorDashboardRoute(
                viewModel = creatorDashboardViewModel,
                onBack = { navigationState.pop() },
                onCreatePost = { _ ->
                    createPostViewModel.startNewDraft()
                    navigationState.push(AppDestination.CreatePost)
                },
                onOpenChat = { conversationId ->
                    inboxViewModel.openThread(conversationId)
                    navigationState.push(AppDestination.ChatThread(conversationId))
                },
                onOpenPostDetail = { postId ->
                    navigationState.push(AppDestination.PostDetail(postId))
                },
                onOpenProfile = { userId ->
                    navigationState.push(AppDestination.UserProfile(userId))
                },
            )
        }
        entry<AppDestination.ProviderDashboard> {
            com.askit.app.providerdashboard.ProviderDashboardRoute(
                viewModel = providerDashboardViewModel,
                onBack = { navigationState.pop() },
                onOpenJob = { jobId ->
                    navigationState.push(AppDestination.JobDetail(jobId))
                },
                onOpenChat = { conversationId ->
                    inboxViewModel.openThread(conversationId)
                    navigationState.push(AppDestination.ChatThread(conversationId))
                },
                onEditProfile = {
                    navigationState.push(AppDestination.EditProfile)
                },
                onUploadWork = {
                    createPostViewModel.startNewDraft()
                    navigationState.push(AppDestination.CreatePost)
                },
                onManageAlerts = {
                    navigationState.navigate(AppDestination.Inbox)
                },
            )
        }
        entry<AppDestination.Inbox> {
            val jobs by jobsViewModel.jobs.collectAsStateWithLifecycle()
            val viewAsOtherJobIds by jobsViewModel.viewAsOtherJobIds.collectAsStateWithLifecycle()
            MessagesRoute(
                conversations = inboxViewModel.conversations,
                jobs = jobs,
                viewAsOtherJobIds = viewAsOtherJobIds,
                onCompose = { navigationState.push(AppDestination.NewMessage) },
                onOpenChat = { id ->
                    inboxViewModel.openThread(id)
                    navigationState.push(AppDestination.ChatThread(id))
                },
                onOpenJob = { navigationState.push(AppDestination.JobDetail(it)) },
                onAcceptJob = { jobsViewModel.store.accept(it) },
                onDeclineJob = { jobsViewModel.store.reject(it) },
                onCancelJob = { jobsViewModel.store.cancel(it) },
                initialPane = MessagesPane.Chats,
            )
        }
        entry<AppDestination.Profile> {
            val profilePostRecords by homeViewModel.posts.posts.collectAsStateWithLifecycle()
            val profileTasks by taskRepository.tasks.collectAsStateWithLifecycle()
            val profile by profileViewModel.profile.collectAsStateWithLifecycle()
            val loadState by profileViewModel.loadState.collectAsStateWithLifecycle()
            val jobs by jobsViewModel.jobs.collectAsStateWithLifecycle()
            val profileHome by homeViewModel.uiState.collectAsStateWithLifecycle()
            ProfileRoute(
                people = peopleFor(profile.accountId), onOpenProfile = ::openProfile, onFollowPerson = ::followPerson,
                stories = (profileHome as? com.askit.app.home.HomeUiState.Success)?.stories.orEmpty(),
                onSaveHighlight = { profileViewModel.saveHighlight(profile.accountId, it) },
                onDeleteHighlight = { profileViewModel.deleteHighlight(profile.accountId, it) },
                posts = com.askit.app.home.posts.orderedProfilePosts(profilePostRecords.values, profile.accountId),
                onOpenPost = { navigationState.push(AppDestination.PostDetail(it)) },
                profile = profile,
                tasks = profileTasks, onOpenTask = { navigationState.push(AppDestination.TaskDetail(it)) },
                jobs = jobs,
                loadState = loadState,
                messages = profileViewModel.messages,
                onEditProfile = { navigationState.push(AppDestination.EditProfile) },
                onEditListing = {
                    val draft = jobsViewModel.profileStore.profile.value.listingDraft
                    if (draft != null) {
                        listServiceViewModel.loadDraft(draft)
                    } else {
                        listServiceViewModel.startNewDraft()
                    }
                    navigationState.push(AppDestination.ListService)
                },
                onSetAvatar = { uri -> profileViewModel.setAvatar(profile.accountId, uri) },
                onSaveBanner = { uri -> profileViewModel.setBanner(profile.accountId, uri) },
                onOpenSettings = { navigationState.push(AppDestination.ProfileSettings) },
                onOpenProviderDashboard = { openProfileDashboard(AppDestination.ProviderDashboard) },
                onOpenCreatorDashboard = { openProfileDashboard(AppDestination.CreatorDashboard) },
                onPostTask = {
                    postTaskViewModel.startNewDraft()
                    navigationState.push(AppDestination.PostTask)
                },
                publicProfileUrl = appLinks.profileUrl(profile.accountId),
            )
        }
        entry<AppDestination.ProfileSettings> {
            val storySocial by storyViewerViewModel.social.state.collectAsStateWithLifecycle()
            val profile by profileViewModel.profile.collectAsStateWithLifecycle()
            val postRecords by homeViewModel.posts.posts.collectAsStateWithLifecycle()
            val postAccount by homeViewModel.posts.account.collectAsStateWithLifecycle()
            ProfileSettingsScreen(
                previewControls = if (BuildConfig.CLIENT_PREVIEW) {
                    (
                        {
                            AppVariant.PreviewProfileControls(
                                onAccount = {
                                    authViewModel.selectPreviewAccount(it)
                                    navigationState.navigate(AppDestination.Profile)
                                },
                                onView = { navigationState.push(AppDestination.UserProfile(it)) },
                            )
                        }
                        )
                } else {
                    null
                },
                savedPosts = remember(postRecords, postAccount) { homeViewModel.posts.savedPosts() },
                archivedPosts = remember(postRecords, postAccount) { homeViewModel.posts.archivedPosts() },
                onOpenPost = { navigationState.push(AppDestination.PostDetail(it)) },
                onRestorePost = { id ->
                    val result = homeViewModel.posts.setArchived(id, currentTaskAccountId(), false)
                    if (result is com.askit.app.home.posts.PostResult.Failure) {
                        Toast.makeText(
                            context,
                            result.messageRes,
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                },
                profile = profile,
                mutedStoryAuthors = storySocial.muted.values.toList(),
                onUnmuteStoryAuthor = { authorId ->
                    val result = storyViewerViewModel.social.unmute(currentTaskAccountId(), authorId)
                    if (result is com.askit.app.home.stories.StoryActionResult.Rejected) {
                        storyViewerViewModel.social.notify(
                            result.reason.messageRes,
                        )
                    }
                },
                onBack = { navigationState.pop() },
                onNavigateToEditProfile = { navigationState.push(AppDestination.EditProfile) },
                onNavigateToListService = {
                    val draft = jobsViewModel.profileStore.profile.value.listingDraft
                    if (draft != null) {
                        listServiceViewModel.loadDraft(draft)
                    } else {
                        listServiceViewModel.startNewDraft()
                    }
                    navigationState.push(AppDestination.ListService)
                },
                onNavigateToCreatePost = {
                    createPostViewModel.startNewDraft()
                    navigationState.push(AppDestination.CreatePost)
                },
                onNavigateToJobRequests = {
                    navigationState.navigate(AppDestination.Inbox)
                },
                onContactSupport = {
                    openExternalLink(appLinks.supportEmail?.let { Uri.fromParts("mailto", it, null).toString() } ?: appLinks.supportUrl)
                },
                onOpenHelpCenter = { openExternalLink(appLinks.helpCenterUrl) },
                onOpenTerms = { openExternalLink(appLinks.termsUrl) },
                onOpenPrivacyPolicy = { openExternalLink(appLinks.privacyPolicyUrl) },
                onUpdatePushNotifications = profileViewModel::updatePushNotifications,
                onUpdateJobAlerts = profileViewModel::updateJobAlerts,
                onUpdateLanguage = { language ->
                    profileViewModel.updateLanguage(language)
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(com.askit.app.session.AppLanguage.fromStoredValue(language).languageTag),
                    )
                },
                onUpdateLocationServices = profileViewModel::updateLocationServices,
                onUpdateWhoCanMessage = { option ->
                    if (!profileViewModel.updateWhoCanMessage(option)) {
                        val message = if (BuildConfig.CLIENT_PREVIEW) R.string.profile_action_error else R.string.profile_save_unavailable
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                },
                onSaveAvailability = profileViewModel::updateAvailability,
                onLogout = authViewModel::signOut,
                onClearAppData = {
                    // Clearing local data ends the session too: staying signed in with a blank
                    // profile left the account half-broken until the next sign-in.
                    profileViewModel.resetAppData()
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                    authViewModel.signOut()
                },
            )
        }
        entry<AppDestination.EditProfile> {
            val profile by profileViewModel.profile.collectAsStateWithLifecycle()
            EditProfileScreen(
                profile = profile,
                onBack = { navigationState.pop() },
                onSave = { state ->
                    profileViewModel.saveIdentity(state).also { saved ->
                        if (saved) {
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(
                                    com.askit.app.session.AppLanguage.fromStoredValue(state.language).languageTag,
                                ),
                            )
                            navigationState.pop()
                        }
                    }
                },
            )
        }
        entry<AppDestination.NewMessage> {
            NewMessageScreen(
                contacts = inboxViewModel.store.contacts,
                onBack = { navigationState.pop() },
                onSelectContact = select@{ contact ->
                    val id = inboxViewModel.startConversation(contact) ?: return@select
                    navigationState.pop()
                    navigationState.push(AppDestination.ChatThread(id))
                },
            )
        }
        entry<AppDestination.ChatThread> { key ->
            val conversation = inboxViewModel.store.conversation(key.conversationId) ?: return@entry
            inboxViewModel.conversations
            ChatThread(
                conversation = conversation,
                messages = inboxViewModel.store.messages(key.conversationId),
                onBack = { navigationState.pop() },
                onSendText = { inboxViewModel.sendText(key.conversationId, it) },
                onSendPhoto = { inboxViewModel.sendPhoto(key.conversationId, it) },
                resolveStory = storyViewerViewModel::availableStory,
                onOpenStory = { id ->
                    if (storyViewerViewModel.availableStory(id) != null) {
                        navigationState.push(AppDestination.StoryViewer(id))
                    } else {
                        storyViewerViewModel.social.notify(R.string.story_shared_unavailable)
                    }
                },
                onMuteChanged = { inboxViewModel.setMuted(key.conversationId, it) },
                onBlock = { inboxViewModel.block(key.conversationId) },
                onReport = { inboxViewModel.report(key.conversationId) },
            )
        }
        entry<AppDestination.JobDetail> { key ->
            val jobs by jobsViewModel.jobs.collectAsStateWithLifecycle()
            val viewAsOtherJobIds by jobsViewModel.viewAsOtherJobIds.collectAsStateWithLifecycle()
            val job = jobs.firstOrNull { it.id == key.jobId } ?: return@entry
            JobDetail(
                job = job,
                store = jobsViewModel.store,
                viewAsOtherParty = job.id in viewAsOtherJobIds,
                clock = clock,
                onBack = { navigationState.pop() },
                onShareCode = { navigationState.push(AppDestination.JobVerifyShare(key.jobId)) },
                onEnterCode = { navigationState.push(AppDestination.JobVerifyEnter(key.jobId)) },
                onReview = {
                    if (jobsViewModel.store.markComplete(key.jobId)) {
                        navigationState.push(AppDestination.JobReview(key.jobId))
                    }
                },
            )
        }
        entry<AppDestination.JobVerifyShare> { key ->
            val job = jobsViewModel.store.job(key.jobId) ?: return@entry
            if (!job.isSeeker(jobsViewModel.store.visibleParty(job))) {
                LaunchedEffect(key.jobId) { navigationState.pop() }
                return@entry
            }
            JobVerifyShare(job = job, onBack = { navigationState.pop() })
        }
        entry<AppDestination.JobVerifyEnter> { key ->
            JobVerifyEnter(
                jobId = key.jobId,
                store = jobsViewModel.store,
                onBack = { navigationState.pop() },
                onVerified = { navigationState.pop() },
            )
        }
        entry<AppDestination.JobReview> { key ->
            JobReview(
                jobId = key.jobId,
                store = jobsViewModel.store,
                onFinished = { navigationState.pop() },
                onReviewSubmitted = review@{ rating, comment ->
                    if (!BuildConfig.CLIENT_PREVIEW) return@review false
                    val actor = currentTaskAccountId() ?: return@review false
                    val job = jobsViewModel.store.job(key.jobId) ?: return@review false
                    if (jobsViewModel.store.visibleParty(job) != job.localParty) return@review false
                    val direction = when (actor) {
                        job.seekerAccountId -> com.askit.app.profile.ReviewDirection.SEEKER_TO_PROVIDER
                        job.providerAccountId -> com.askit.app.profile.ReviewDirection.PROVIDER_TO_SEEKER
                        else -> return@review false
                    }
                    val recipient =
                        (if (actor == job.seekerAccountId) job.providerAccountId else job.seekerAccountId) ?: return@review false
                    val storage = AppVariant.previewProfileStorage(context) ?: return@review false
                    runCatching {
                        val target = storage.load(recipient)
                            ?: (
                                if (recipient in
                                    setOf("preview-owner", "preview-visitor", "preview-seeker")
                                ) {
                                    AppVariant.previewProfile(recipient)
                                } else {
                                    null
                                }
                                )
                            ?: return@review false
                        val review = ProfileReview(
                            "review-${job.id}-$actor", profileViewModel.profile.value.displayName,
                            job.title, rating.toFloat(), comment, clock(), jobId = job.id,
                            reviewerAccountId = actor, recipientAccountId = recipient, direction = direction,
                        )
                        storage.save(target.copy(reviews = target.reviews.filterNot { it.id == review.id } + review))
                    }.isSuccess
                },
            )
        }
        entry<AppDestination.TaskDetail> { key ->
            val profile by profileViewModel.profile.collectAsStateWithLifecycle()
            val discussionAuthor = discussionAccountId?.let { TaskCommentAuthor(it, profile.displayName, profile.avatarUrl) }
            val serviceProfile by jobsViewModel.profileStore.profile.collectAsStateWithLifecycle()
            val jobs by jobsViewModel.jobs.collectAsStateWithLifecycle()
            val hasApplied =
                discussionAccountId != null && jobs.any { it.sourceTaskId == key.taskId && it.applicantAccountId == discussionAccountId }
            TaskDetailScreen(
                taskId = key.taskId,
                taskRepository = taskRepository,
                discussionViewModel = taskDiscussionViewModel,
                discussionAuthor = discussionAuthor,
                currentDiscussionAuthor = {
                    currentTaskAccountId()?.let { id ->
                        val currentProfile = profileViewModel.profile.value
                        TaskCommentAuthor(id, currentProfile.displayName, currentProfile.avatarUrl)
                    }
                },
                actionsViewModel = taskActionsViewModel,
                accountId = discussionAccountId,
                currentAccountId = ::currentTaskAccountId,
                hasServiceListing = serviceProfile.hasActiveServiceListing,
                hasApplied = hasApplied,
                onEdit = { task ->
                    val currentId = currentTaskAccountId()
                    val draft = taskRepository.editableDraft(task.id, currentId)
                    if (draft == null || !postTaskViewModel.isActiveAccount(currentId)) {
                        taskActionsViewModel.feedback(
                            task.id,
                            if (currentId ==
                                null
                            ) {
                                R.string.task_action_sign_in
                            } else {
                                R.string.task_edit_unavailable
                            },
                        )
                    } else {
                        postTaskViewModel.beginEdit(task.id, draft)
                        navigationState.push(AppDestination.EditTask(task.id))
                    }
                },
                onDeleted = { taskId ->
                    postTaskViewModel.clearTask(taskId)
                    taskDiscussionViewModel.clearTask(taskId)
                    navigationState.pop()
                },
                onBack = { navigationState.pop() },
                onMessage = { task ->
                    task.posterId?.takeIf { it.isNotBlank() }?.let { posterId ->
                        val conversationId = inboxViewModel.startConversation(
                            ChatContact(posterId, task.posterName, task.posterAvatarUrl),
                        ) ?: return@let
                        inboxViewModel.openThread(conversationId)
                        navigationState.push(AppDestination.ChatThread(conversationId))
                    }
                },
                onApply = { appliedTask ->
                    when (
                        val result = jobsViewModel.store.applyToTask(taskRepository, appliedTask.id, currentTaskAccountId())
                    ) {
                        ApplyToTaskResult.NeedsListedService -> onApplyBlocked()

                        is ApplyToTaskResult.Rejected -> taskActionsViewModel.feedback(appliedTask.id, result.reason.messageRes)

                        is ApplyToTaskResult.AlreadyApplied -> taskActionsViewModel.feedback(appliedTask.id, R.string.task_applied)

                        is ApplyToTaskResult.Created -> navigationState.navigateAndPush(
                            AppDestination.Inbox,
                            AppDestination.JobDetail(result.jobId),
                        )
                    }
                },
            )
        }
        entry<AppDestination.ServiceDetail> { key ->
            ServiceDetailScreen(
                serviceId = key.serviceId,
                onBack = { navigationState.pop() },
                onChat = chat@{ providerId ->
                    val service = com.askit.app.home.details.getServiceDetailById(key.serviceId)
                        ?: return@chat
                    val conversationId = inboxViewModel.startConversation(
                        ChatContact(providerId, service.providerName, service.providerAvatarUrl),
                    ) ?: return@chat
                    inboxViewModel.openThread(conversationId)
                    navigationState.push(AppDestination.ChatThread(conversationId))
                },
                onRequestService = request@{
                    val service = com.askit.app.home.details.getServiceDetailById(key.serviceId)
                        ?: return@request
                    val jobId = jobsViewModel.store.requestService(
                        title = service.title,
                        counterpartName = service.providerName,
                    )
                    navigationState.navigateAndPush(
                        AppDestination.Inbox,
                        AppDestination.JobDetail(jobId),
                    )
                },
            )
        }
        entry<AppDestination.UserProfile> { key ->
            if (key.userId == currentTaskAccountId()) {
                LaunchedEffect(key.userId) { navigationState.navigateToRoot(AppDestination.Profile) }
                return@entry
            }
            val profilePostRecords by homeViewModel.posts.posts.collectAsStateWithLifecycle()
            val profileTasks by taskRepository.tasks.collectAsStateWithLifecycle()
            val social by storyViewerViewModel.social.state.collectAsStateWithLifecycle()
            val currentProfile by profileViewModel.profile.collectAsStateWithLifecycle()
            val publicProfile = remember(key.userId, currentProfile) {
                if (com.askit.app.BuildConfig.CLIENT_PREVIEW) {
                    runCatching {
                        val account = AppVariant.previewProfileStorage(context)?.load(key.userId)
                            ?: if (key.userId in
                                setOf("preview-owner", "preview-visitor", "preview-seeker")
                            ) {
                                AppVariant.previewProfile(key.userId)
                            } else {
                                null
                            }
                        account?.toPublicProfile() ?: com.askit.app.home.details.getUserProfileById(key.userId)
                    }.getOrNull()
                } else {
                    null
                }
            }
            UserProfileScreen(
                tasks = profileTasks, onOpenTask = { navigationState.push(AppDestination.TaskDetail(it)) },
                publicProfileUrl = appLinks.profileUrl(key.userId),
                isBlocked = inboxViewModel.conversations.any { it.contact.id == key.userId && it.blocked },
                onModerate = moderate@{ report ->
                    val actor = currentTaskAccountId()
                    if (!BuildConfig.CLIENT_PREVIEW ||
                        !com.askit.app.profile.canMutateProfileVisitor(actor, key.userId, false)
                    ) {
                        return@moderate false
                    }
                    val target = publicProfile ?: return@moderate false
                    inboxViewModel.moderate(ChatContact(target.id, target.name, target.avatarUrl), report)
                    if (!report) followPerson(target.id, false)
                    true
                },
                people = peopleFor(key.userId), onOpenProfile = ::openProfile, onFollowPerson = ::followPerson,
                publicProfile = publicProfile,
                viewerAccountId = currentTaskAccountId(),
                viewerPostcode = currentProfile.pincode,
                posts = com.askit.app.home.posts.orderedProfilePosts(profilePostRecords.values, key.userId),
                onOpenPost = { navigationState.push(AppDestination.PostDetail(it)) },
                userId = key.userId,
                isFollowing = key.userId in social.following,
                onToggleFollow = { followPerson(key.userId, key.userId !in social.following) },
                onBack = { navigationState.pop() },
                onMessage = message@{
                    if (!BuildConfig.CLIENT_PREVIEW) {
                        Toast.makeText(context, R.string.profile_action_unavailable, Toast.LENGTH_LONG).show()
                        return@message
                    }
                    val target = publicProfile ?: return@message
                    val conversationId =
                        inboxViewModel.startConversation(ChatContact(target.id, target.name, target.avatarUrl)) ?: return@message
                    inboxViewModel.openThread(conversationId)
                    navigationState.push(AppDestination.ChatThread(conversationId))
                },
            )
        }
        entry<AppDestination.StoryViewer> { key ->
            val storyAccount by storyViewerViewModel.social.state.collectAsStateWithLifecycle()
            var sharingId by rememberSaveable(storyAccount.accountId) { mutableStateOf<String?>(null) }
            sharingId?.let { id ->
                com.askit.app.home.stories.StorySendSheet(
                    storyId = id,
                    contacts = inboxViewModel.store.contacts.filter { inboxViewModel.store.conversation(it.id)?.blocked != true },
                    onDismiss = { sharingId = null },
                    onSend = { contact ->
                        val account = currentTaskAccountId()
                        val story = storyViewerViewModel.availableStory(id)
                        if (!storyViewerViewModel.social.isCurrentAccount(account)) {
                            storyViewerViewModel.social.notify(R.string.story_action_sign_in)
                            false
                        } else {
                            story != null && inboxViewModel.sendStory(account, contact, story)
                        }
                    },
                )
            }
            LaunchedEffect(key.startStoryId) {
                storyViewerViewModel.startAt(key.startStoryId)
            }
            StoryViewerRoute(
                viewModel = storyViewerViewModel,
                shareStoryId = sharingId,
                onShareStory = { id ->
                    when {
                        !storyViewerViewModel.social.isCurrentAccount(currentTaskAccountId()) ->
                            storyViewerViewModel.social.notify(R.string.story_action_sign_in)

                        storyViewerViewModel.availableStory(id) == null ->
                            storyViewerViewModel.social.notify(R.string.story_shared_unavailable)

                        else -> sharingId = id
                    }
                },
                startStoryId = key.startStoryId,
                onDismiss = {
                    storyViewerViewModel.closeViewer()
                    navigationState.pop()
                },
                currentAccountId = ::currentTaskAccountId,
                onViewPost = { postId ->
                    navigationState.push(AppDestination.PostDetail(postId = postId))
                },
                onAddStory = {
                    storyViewModel.startNewDraft()
                    navigationState.push(AppDestination.Story)
                },
                onStorySeen = { storyId ->
                    homeViewModel.markStorySeen(storyId)
                },
            )
        }
        entry<AppDestination.Story> {
            StoryRoute(
                viewModel = storyViewModel,
                onBack = { navigationState.pop() },
                onPublishDraft = { draft ->
                    homeViewModel.addStoryFromDraft(draft, jobsViewModel.profileStore.profile.value, currentTaskAccountId())
                },
                onOpenCreateSheet = onShowCreateSheet,
            )
        }
        entry<AppDestination.PostDetail> { key ->
            PostDetailScreen(
                postId = key.postId,
                store = homeViewModel.posts,
                currentAccountId = ::currentTaskAccountId,
                onEditPost = { navigationState.push(AppDestination.EditPost(it)) },
                onBack = { navigationState.pop() },
            )
        }
    }
}
