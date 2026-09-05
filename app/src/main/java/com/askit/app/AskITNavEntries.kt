package com.askit.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.askit.app.explore.ExploreBrowseSection
import com.askit.app.explore.ExploreBrowseState
import com.askit.app.explore.ExploreFilterOption
import com.askit.app.explore.ExploreResultScope
import com.askit.app.explore.ExploreResultState
import com.askit.app.explore.ExploreRoute
import com.askit.app.explore.ExploreSortOption
import com.askit.app.explore.ExploreViewModel
import com.askit.app.explore.SearchAreaRoute
import com.askit.app.creatordashboard.CreatorDashboardRoute
import com.askit.app.creatordashboard.CreatorDashboardViewModel
import com.askit.app.createpost.CreatePostRoute
import com.askit.app.createpost.CreatePostViewModel
import com.askit.app.createpost.PostDraft
import com.askit.app.entry.EntryScreen
import com.askit.app.home.HomeScreen
import com.askit.app.home.HomeViewModel
import com.askit.app.home.details.PostDetailScreen
import com.askit.app.home.details.ServiceDetailScreen
import com.askit.app.home.details.TaskDetailScreen
import com.askit.app.home.details.UserProfileScreen
import com.askit.app.home.stories.StoryViewerRoute
import com.askit.app.home.stories.StoryViewerViewModel
import com.askit.app.inbox.ChatThread
import com.askit.app.inbox.InboxViewModel
import com.askit.app.inbox.MessagesPane
import com.askit.app.inbox.MessagesRoute
import com.askit.app.inbox.NewMessageScreen
import com.askit.app.jobs.ApplyToTaskResult
import com.askit.app.jobs.JobWorkMode
import com.askit.app.jobs.JobsViewModel
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
import com.askit.app.profile.EditProfileScreen
import com.askit.app.profile.ProfileReview
import com.askit.app.profile.ProfileRoute
import com.askit.app.profile.ProfileSettingsScreen
import com.askit.app.profile.ProfileViewModel
import com.askit.app.story.StoryDraft
import com.askit.app.story.StoryRoute
import com.askit.app.story.StoryViewModel
import com.askit.app.task.Task
import com.askit.app.task.TaskRepository
import com.askit.app.task.toExploreTaskResult

@Composable
internal fun rememberAskITEntryProvider(
    navigationState: AskITNavigationState,
    taskRepository: TaskRepository,
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
    return entryProvider<NavKey> {
    entry<AppDestination.Entry> {
        EntryScreen(
            onGetStarted = {
                navigationState.push(AppDestination.AuthPhone)
            },
            onLogin = {
                navigationState.push(AppDestination.AuthPhone)
            },
        )
    }
    entry<AppDestination.AuthPhone> {
        com.askit.app.auth.AuthPhoneScreen(
            onBack = {
                navigationState.pop()
            },
            onGetOtp = { phoneNumber ->
                navigationState.push(AppDestination.AuthOtp(phoneNumber = phoneNumber))
            },
        )
    }
    entry<AppDestination.AuthOtp> { key ->
        com.askit.app.auth.AuthOtpScreen(
            phoneNumber = key.phoneNumber,
            onBack = {
                navigationState.pop()
            },
            onEditPhone = {
                navigationState.pop()
            },
            onVerifySuccess = {
                navigationState.push(AppDestination.FormA(phoneNumber = key.phoneNumber))
            },
        )
    }
    entry<AppDestination.FormA> { key ->
        com.askit.app.auth.FormAScreen(
            phoneNumber = key.phoneNumber,
            onBack = {
                navigationState.pop()
            },
            onSubmitSuccess = { _, _, _, _ ->
                navigationState.clearToHome()
            },
            initialInterests = emptyList(),
        )
    }
    entry<AppDestination.Home> {
        HomeScreen(
            viewModel = homeViewModel,
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
            onApplyToTask = { task ->
                when (
                    val result = jobsViewModel.store.applyToTask(
                        title = task.title,
                        counterpartName = task.posterName,
                        workMode = JobWorkMode.OnSite,
                    )
                ) {
                    ApplyToTaskResult.NeedsListedService -> onApplyBlocked()
                    is ApplyToTaskResult.Created -> navigationState.navigateAndPush(
                        AppDestination.Inbox,
                        AppDestination.JobDetail(result.jobId),
                    )
                }
            },
            onRequestService = { person ->
                val jobId = jobsViewModel.store.requestService(
                    title = person.primaryService ?: person.name,
                    counterpartName = person.name,
                )
                navigationState.navigateAndPush(
                    AppDestination.Inbox,
                    AppDestination.JobDetail(jobId),
                )
            },
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
            onBack = { navigationState.pop() },
            onCompleteDraft = { draft ->
                onPostTaskCompleted(draft)
                navigationState.pop()
            },
        )
    }
    entry<AppDestination.ListService> {
        ListServiceRoute(
            viewModel = listServiceViewModel,
            onBack = { navigationState.pop() },
            onCompleteDraft = { draft ->
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
            onBack = { navigationState.pop() },
            onCompleteDraft = { draft ->
                profileViewModel.appendGallery(draft.photos.mapNotNull { it.uri })
                onCreatePostCompleted(draft)
            },
        )
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
        val profile by profileViewModel.profile.collectAsStateWithLifecycle()
        val loadState by profileViewModel.loadState.collectAsStateWithLifecycle()
        val jobs by jobsViewModel.jobs.collectAsStateWithLifecycle()
        ProfileRoute(
            profile = profile,
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
            onUploadWork = {
                createPostViewModel.startNewDraft()
                navigationState.push(AppDestination.CreatePost)
            },
            onOpenJob = { navigationState.push(AppDestination.JobDetail(it)) },
            onViewAllJobs = { navigationState.navigate(AppDestination.Inbox) },
            onSaveAbout = profileViewModel::updateAbout,
            onSaveLookingFor = profileViewModel::updateLookingFor,
            onSaveSkills = profileViewModel::updateSkills,
            onSaveAvailability = profileViewModel::updateAvailability,
            onSetAvatar = profileViewModel::setAvatar,
            onAddLicense = profileViewModel::addLicense,
            onUsernameCopied = profileViewModel::notifyCopied,
            onOpenSettings = { navigationState.push(AppDestination.ProfileSettings) },
            onOpenProviderDashboard = { navigationState.push(AppDestination.ProviderDashboard) },
        )
    }
    entry<AppDestination.ProfileSettings> {
        val profile by profileViewModel.profile.collectAsStateWithLifecycle()
        ProfileSettingsScreen(
            profile = profile,
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
            onNavigateToSupportChat = {
                navigationState.push(AppDestination.NewMessage)
            },
            onUpdatePhoneNumber = profileViewModel::updatePhoneNumber,
            onUpdatePushNotifications = profileViewModel::updatePushNotifications,
            onUpdateJobAlerts = profileViewModel::updateJobAlerts,
            onUpdateLanguage = profileViewModel::updateLanguage,
            onUpdateLocationServices = profileViewModel::updateLocationServices,
            onUpdateWhoCanMessage = profileViewModel::updateWhoCanMessage,
            onSaveAvailability = profileViewModel::updateAvailability,
            onLogout = {
                navigationState.navigateAndPush(AppDestination.Home, AppDestination.Entry)
            },
            onClearAppData = profileViewModel::resetAppData,
        )
    }
    entry<AppDestination.EditProfile> {
        val profile by profileViewModel.profile.collectAsStateWithLifecycle()
        EditProfileScreen(
            profile = profile,
            onBack = { navigationState.pop() },
            onSave = { state ->
                profileViewModel.saveIdentity(state)
                navigationState.pop()
            },
        )
    }
    entry<AppDestination.NewMessage> {
        NewMessageScreen(
            contacts = inboxViewModel.store.contacts,
            onBack = { navigationState.pop() },
            onSelectContact = { contact ->
                val id = inboxViewModel.startConversation(contact)
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
            onReviewSubmitted = { rating, comment ->
                val job = jobsViewModel.store.job(key.jobId)
                if (job != null) {
                    profileViewModel.appendReview(
                        ProfileReview(
                            id = "rev-${job.id}",
                            name = job.counterpartName,
                            meta = job.title,
                            rating = rating.toFloat(),
                            body = comment,
                            createdAtMillis = clock(),
                            jobId = job.id,
                        ),
                    )
                }
            },
        )
    }
    entry<AppDestination.TaskDetail> { key ->
        TaskDetailScreen(
            taskId = key.taskId,
            taskRepository = taskRepository,
            onBack = { navigationState.pop() },
            onApply = { appliedTask ->
                when (
                    val result = jobsViewModel.store.applyToTask(
                        title = appliedTask.title,
                        counterpartName = appliedTask.posterName,
                        workMode = JobWorkMode.OnSite,
                    )
                ) {
                    ApplyToTaskResult.NeedsListedService -> onApplyBlocked()
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
            onRequestService = {
                val service = com.askit.app.home.details.getServiceDetailById(key.serviceId)
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
        UserProfileScreen(
            userId = key.userId,
            onBack = { navigationState.pop() },
            onMessage = {
                navigationState.push(AppDestination.NewMessage)
            },
            onRequestService = {
                val profile = com.askit.app.home.details.getUserProfileById(key.userId)
                val jobId = jobsViewModel.store.requestService(
                    title = profile.trade,
                    counterpartName = profile.name,
                )
                navigationState.navigateAndPush(
                    AppDestination.Inbox,
                    AppDestination.JobDetail(jobId),
                )
            },
        )
    }
    entry<AppDestination.StoryViewer> { key ->
        LaunchedEffect(key.startStoryId) {
            storyViewerViewModel.startAt(key.startStoryId)
        }
        StoryViewerRoute(
            viewModel = storyViewerViewModel,
            onDismiss = { navigationState.pop() },
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
            onCompleteDraft = { draft ->
                homeViewModel.addStoryFromDraft(draft)
                navigationState.pop()
            },
            onOpenCreateSheet = onShowCreateSheet,
        )
    }
    entry<AppDestination.PostDetail> { key ->
        PostDetailScreen(
            postId = key.postId,
            onBack = { navigationState.pop() },
        )
    }
    }
}
