package com.askit.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.askit.app.createpost.CreatePostRoute
import com.askit.app.createpost.CreatePostViewModel
import com.askit.app.createpost.PostDraft
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
import com.askit.app.navigation.AppDestination
import com.askit.app.navigation.AskITNavigationState
import com.askit.app.posttask.PostTaskDraft
import com.askit.app.posttask.PostTaskRoute
import com.askit.app.posttask.PostTaskViewModel
import com.askit.app.profile.EditProfileScreen
import com.askit.app.profile.LocalProfileRepository
import com.askit.app.profile.ProfileReview
import com.askit.app.profile.ProfileRoute
import com.askit.app.profile.ProfileViewModel

@Composable
internal fun rememberAskITEntryProvider(
    navigationState: AskITNavigationState,
    exploreViewModel: ExploreViewModel,
    postTaskViewModel: PostTaskViewModel,
    listServiceViewModel: ListServiceViewModel,
    createPostViewModel: CreatePostViewModel,
    jobsViewModel: JobsViewModel,
    inboxViewModel: InboxViewModel,
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
    val profileViewModel = remember(jobsViewModel) {
        ProfileViewModel(LocalProfileRepository(jobsViewModel.profileStore))
    }
    return entryProvider<NavKey> {
    entry<AppDestination.Home> {
        EmptyRootDestination(
            iconRes = com.askit.designsystem.R.drawable.ic_home_outlined,
            titleRes = com.askit.designsystem.R.string.empty_home_title,
            supportingRes = com.askit.designsystem.R.string.empty_home_supporting,
            actionRes = com.askit.designsystem.R.string.empty_home_action,
            onAction = onShowCreateSheet,
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
            appliedFilterOptions = appliedFilterOptions,
            onFiltersChanged = onFiltersChanged,
            onFilterScopeChanged = exploreViewModel::onFilterScopeSelected,
            onPersonClick = onPersonClick,
            onTaskClick = onTaskClick,
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
            onCompleteDraft = onPostTaskCompleted,
        )
    }
    entry<AppDestination.ListService> {
        ListServiceRoute(
            viewModel = listServiceViewModel,
            onBack = { navigationState.pop() },
            onCompleteDraft = {
                jobsViewModel.profileStore.markServiceListed()
                onListServiceCompleted(it)
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
    entry<AppDestination.Inbox> {
        val jobs by jobsViewModel.jobs.collectAsStateWithLifecycle()
        val viewAsOther by jobsViewModel.viewAsOtherParty.collectAsStateWithLifecycle()
        MessagesRoute(
            conversations = inboxViewModel.conversations,
            jobs = jobs,
            viewAsOtherParty = viewAsOther,
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
        val viewAsOther by jobsViewModel.viewAsOtherParty.collectAsStateWithLifecycle()
        ProfileRoute(
            profile = profile,
            jobs = jobs,
            viewAsOtherParty = viewAsOther,
            loadState = loadState,
            messages = profileViewModel.messages,
            onEditProfile = { navigationState.push(AppDestination.EditProfile) },
            onEditListing = {
                listServiceViewModel.startNewDraft()
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
        val viewAsOther by jobsViewModel.viewAsOtherParty.collectAsStateWithLifecycle()
        ChatThread(
            conversation = conversation,
            messages = inboxViewModel.store.messages(key.conversationId),
            viewAsOtherParty = viewAsOther,
            onBack = { navigationState.pop() },
            onSendText = { inboxViewModel.sendText(key.conversationId, it) },
            onSendPhoto = { inboxViewModel.sendPhoto(key.conversationId, it) },
            onMuteChanged = { inboxViewModel.setMuted(key.conversationId, it) },
            onViewAsOtherParty = { jobsViewModel.store.toggleViewAsOtherParty() },
        )
    }
    entry<AppDestination.JobDetail> { key ->
        val jobs by jobsViewModel.jobs.collectAsStateWithLifecycle()
        val viewAsOther by jobsViewModel.viewAsOtherParty.collectAsStateWithLifecycle()
        val job = jobs.firstOrNull { it.id == key.jobId } ?: return@entry
        viewAsOther
        JobDetail(
            job = job,
            store = jobsViewModel.store,
            viewAsOtherParty = viewAsOther,
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
    }
}
