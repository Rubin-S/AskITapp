package com.askit.app.profile

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.askit.app.R
import com.askit.app.auth.AuthViewModel
import com.askit.app.config.AppLinks
import com.askit.app.config.profileUrl
import com.askit.app.home.HomeUiState
import com.askit.app.home.HomeViewModel
import com.askit.app.home.details.UserProfileData
import com.askit.app.home.details.UserProfileScreen
import com.askit.app.home.details.getUserProfileById
import com.askit.app.home.posts.PostResult
import com.askit.app.home.posts.orderedProfilePosts
import com.askit.app.home.stories.StoryActionResult
import com.askit.app.home.stories.StoryViewerViewModel
import com.askit.app.home.stories.followerIds
import com.askit.app.home.stories.followingIds
import com.askit.app.inbox.ChatContact
import com.askit.app.inbox.InboxViewModel
import com.askit.app.jobs.JobsViewModel
import com.askit.app.navigation.AppDestination
import com.askit.app.navigation.AskITNavigationState
import com.askit.app.session.AppLanguage
import com.askit.app.task.TaskRepository
import com.askit.app.variant.AppVariant

/** What the profile screens need from the app shell. Built once in the entry provider. */
internal class ProfileNavDeps(
    val navigation: AskITNavigationState,
    val profileViewModel: ProfileViewModel,
    val authViewModel: AuthViewModel,
    val homeViewModel: HomeViewModel,
    val taskRepository: TaskRepository,
    val jobsViewModel: JobsViewModel,
    val storyViewerViewModel: StoryViewerViewModel,
    val inboxViewModel: InboxViewModel,
    val appLinks: AppLinks,
    val actions: ProfileNavActions,
)

/** Builds the deps holder. It is plain data, not a composable, so passing ViewModels into it is not forwarding. */
internal fun profileNavDeps(
    navigation: AskITNavigationState,
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    taskRepository: TaskRepository,
    jobsViewModel: JobsViewModel,
    storyViewerViewModel: StoryViewerViewModel,
    inboxViewModel: InboxViewModel,
    appLinks: AppLinks,
    actions: ProfileNavActions,
) = ProfileNavDeps(
    navigation, profileViewModel, authViewModel, homeViewModel, taskRepository, jobsViewModel, storyViewerViewModel,
    inboxViewModel, appLinks, actions,
)

/** Shell actions shared with other entries (they own the task and listing drafts). */
internal class ProfileNavActions(
    val currentAccountId: () -> String?,
    val openExternalLink: (String) -> Unit,
    val editListing: () -> Unit,
    val postTask: () -> Unit,
    val previewControls: (@Composable () -> Unit)?,
)

/** The Profile tab, Settings, Edit profile and other people's profiles. */
internal fun EntryProviderScope<NavKey>.profileEntries(deps: ProfileNavDeps) {
    entry<AppDestination.Profile> { OwnProfileEntry(deps) }
    entry<AppDestination.ProfileSettings> { SettingsEntry(deps) }
    entry<AppDestination.EditProfile> { EditProfileEntry(deps) }
    entry<AppDestination.UserProfile> { key -> UserProfileEntry(deps, key.userId) }
    entry<AppDestination.MyJobRequests> {
        val jobs by deps.jobsViewModel.jobs.collectAsStateWithLifecycle()
        MyJobRequestsScreen(
            jobs = jobs,
            onBack = { deps.navigation.pop() },
            onOpenJob = { deps.navigation.push(AppDestination.JobDetail(it)) },
            onCancelJob = { deps.jobsViewModel.store.cancel(it) },
        )
    }
}

@Composable
private fun OwnProfileEntry(deps: ProfileNavDeps) = with(deps) {
    val records by homeViewModel.posts.posts.collectAsStateWithLifecycle()
    val tasks by taskRepository.tasks.collectAsStateWithLifecycle()
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    val loadState by profileViewModel.loadState.collectAsStateWithLifecycle()
    val jobs by jobsViewModel.jobs.collectAsStateWithLifecycle()
    val home by homeViewModel.uiState.collectAsStateWithLifecycle()
    val capabilities = profileViewModel.capabilities
    val posts = remember(records, profile.accountId) { orderedProfilePosts(records.values, profile.accountId) }
    ProfileRoute(
        profile = profile,
        jobs = jobs,
        tasks = tasks,
        posts = posts,
        stories = (home as? HomeUiState.Success)?.stories.orEmpty(),
        people = rememberPeople(deps, profile.accountId),
        loadState = loadState,
        capabilities = capabilities,
        publicProfileUrl = appLinks.profileUrl(profile.accountId),
        onEditProfile = { navigation.push(AppDestination.EditProfile) },
        onEditListing = actions.editListing,
        onOpenSettings = { navigation.push(AppDestination.ProfileSettings) },
        onOpenProviderDashboard = { navigation.push(AppDestination.ProviderDashboard) }.takeIf { capabilities.dashboards },
        onOpenCreatorDashboard = { navigation.push(AppDestination.CreatorDashboard) }.takeIf { capabilities.dashboards },
        onPostTask = actions.postTask.takeIf { capabilities.postTask },
        onOpenProfile = { navigation.openProfile(it, actions.currentAccountId()) },
        onFollowPerson = { id, following -> followPerson(deps, id, following) },
        onOpenPost = { navigation.push(AppDestination.PostDetail(it)) },
        onOpenTask = { navigation.push(AppDestination.TaskDetail(it)) },
        onSetAvatar = profileViewModel::setAvatar,
        onSaveBanner = profileViewModel::setBanner,
        onSaveHighlight = profileViewModel::saveHighlight,
        onDeleteHighlight = profileViewModel::deleteHighlight,
        onSignOut = authViewModel::signOut,
    )
}

@Composable
private fun SettingsEntry(deps: ProfileNavDeps) = with(deps) {
    val context = LocalContext.current
    val social by storyViewerViewModel.social.state.collectAsStateWithLifecycle()
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    val locationEnabled by profileViewModel.locationServicesEnabled.collectAsStateWithLifecycle()
    val records by homeViewModel.posts.posts.collectAsStateWithLifecycle()
    val postAccount by homeViewModel.posts.account.collectAsStateWithLifecycle()
    val capabilities = profileViewModel.capabilities
    ProfileSettingsScreen(
        profile = profile,
        capabilities = capabilities,
        locationServicesEnabled = locationEnabled,
        mutedStoryAuthors = social.muted.values.toList(),
        blockedAccounts = inboxViewModel.conversations.filter { it.blocked }.map { it.contact.name },
        savedPosts = remember(records, postAccount) { homeViewModel.posts.savedPosts() },
        archivedPosts = remember(records, postAccount) { homeViewModel.posts.archivedPosts() },
        previewControls = actions.previewControls,
        onBack = { navigation.pop() },
        onEditProfile = { navigation.push(AppDestination.EditProfile) },
        onEditListing = actions.editListing,
        onOpenCreatorHub = { navigation.push(AppDestination.CreatorDashboard) }.takeIf { capabilities.dashboards },
        onOpenJobRequests = { navigation.push(AppDestination.MyJobRequests) },
        onOpenLink = actions.openExternalLink,
        supportLink = appLinks.supportEmail?.let { "mailto:$it" } ?: appLinks.supportUrl,
        helpCenterLink = appLinks.helpCenterUrl,
        termsLink = appLinks.termsUrl,
        privacyPolicyLink = appLinks.privacyPolicyUrl,
        onUpdatePushNotifications = profileViewModel::updatePushNotifications,
        onUpdateJobAlerts = profileViewModel::updateJobAlerts,
        onUpdateLanguage = { language ->
            profileViewModel.updateLanguage(language)
            applyAppLanguage(language)
        },
        onUpdateLocationServices = profileViewModel::setLocationServicesEnabled,
        onUpdateWhoCanMessage = profileViewModel::updateWhoCanMessage,
        onSaveAvailability = profileViewModel::updateAvailability,
        onUnmuteStoryAuthor = { authorId ->
            val result = storyViewerViewModel.social.unmute(actions.currentAccountId(), authorId)
            if (result is StoryActionResult.Rejected) storyViewerViewModel.social.notify(result.reason.messageRes)
        },
        onOpenPost = { navigation.push(AppDestination.PostDetail(it)) },
        onRestorePost = { id ->
            val result = homeViewModel.posts.setArchived(id, actions.currentAccountId(), false)
            if (result is PostResult.Failure) Toast.makeText(context, result.messageRes, Toast.LENGTH_LONG).show()
        },
        onLogout = authViewModel::signOut,
        onClearAppData = {
            profileViewModel.resetAppData {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                // The app restarts on a fresh session, so the confirmation is a toast that outlives it.
                Toast.makeText(context.applicationContext, R.string.settings_data_cleared, Toast.LENGTH_LONG).show()
                authViewModel.signOut(restartApp = true)
            }
        },
    )
}

@Composable
private fun EditProfileEntry(deps: ProfileNavDeps) = with(deps) {
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    val language = currentAppLanguage().storedValue
    EditProfileScreen(
        profile = profile,
        appLanguage = language,
        onBack = { navigation.pop() },
        onSave = { state ->
            profileViewModel.saveIdentity(state).also { saved ->
                if (saved) {
                    if (state.language != language) applyAppLanguage(state.language)
                    navigation.pop()
                }
            }
        },
    )
}

@Composable
private fun UserProfileEntry(deps: ProfileNavDeps, userId: String) = with(deps) {
    if (userId == actions.currentAccountId()) {
        LaunchedEffect(userId) { navigation.openProfile(userId, actions.currentAccountId()) }
        return@with
    }
    val context = LocalContext.current
    val records by homeViewModel.posts.posts.collectAsStateWithLifecycle()
    val tasks by taskRepository.tasks.collectAsStateWithLifecycle()
    val social by storyViewerViewModel.social.state.collectAsStateWithLifecycle()
    val viewer by profileViewModel.profile.collectAsStateWithLifecycle()
    val capabilities = profileViewModel.capabilities
    val publicProfile = remember(userId, viewer.accountId) { if (capabilities.social) previewPublicProfile(context, userId) else null }
    val posts = remember(records, userId) { orderedProfilePosts(records.values, userId) }
    val canMutate = capabilities.social && canMutateProfileVisitor(actions.currentAccountId(), userId, false)
    val toggleFollow: () -> Unit = { followPerson(deps, userId, userId !in social.following) }
    val message: () -> Unit = message@{
        val target = publicProfile ?: return@message
        val conversationId = inboxViewModel.startConversation(ChatContact(target.id, target.name, target.avatarUrl)) ?: return@message
        inboxViewModel.openThread(conversationId)
        navigation.push(AppDestination.ChatThread(conversationId))
    }
    val moderate: (Boolean) -> Boolean = { report ->
        val target = publicProfile
        if (target != null && canMutateProfileVisitor(actions.currentAccountId(), userId, false)) {
            inboxViewModel.moderate(ChatContact(target.id, target.name, target.avatarUrl), report)
            if (!report) followPerson(deps, target.id, false)
            true
        } else {
            false
        }
    }
    UserProfileScreen(
        userId = userId,
        publicProfile = publicProfile,
        profilesAvailable = capabilities.social,
        viewerAccountId = actions.currentAccountId(),
        viewerPostcode = viewer.pincode,
        people = rememberPeople(deps, userId),
        posts = posts,
        tasks = tasks,
        publicProfileUrl = appLinks.profileUrl(userId),
        isBlocked = inboxViewModel.conversations.any { it.contact.id == userId && it.blocked },
        isFollowing = userId in social.following,
        onBack = { navigation.pop() },
        onOpenProfile = { navigation.openProfile(it, actions.currentAccountId()) },
        onFollowPerson = { id, following -> followPerson(deps, id, following) },
        onOpenPost = { navigation.push(AppDestination.PostDetail(it)) },
        onOpenTask = { navigation.push(AppDestination.TaskDetail(it)) },
        onToggleFollow = toggleFollow.takeIf { canMutate },
        onMessage = message.takeIf { canMutate },
        onModerate = moderate.takeIf { canMutate },
        onUnblock = { inboxViewModel.unblock(userId) }.takeIf { canMutate },
    )
}

private fun followPerson(deps: ProfileNavDeps, id: String, following: Boolean): Boolean = deps.profileViewModel.capabilities.social &&
    deps.storyViewerViewModel.social.setFollowing(deps.actions.currentAccountId(), id, following) is StoryActionResult.Success

/** Followers and following. Only the preview has people lists; release shows the profile's own counts. */
@Composable
private fun rememberPeople(deps: ProfileNavDeps, id: String): ProfilePeopleState {
    val context = LocalContext.current
    val relationships by deps.storyViewerViewModel.social.relationships.collectAsStateWithLifecycle()
    val viewer = deps.actions.currentAccountId()
    val social = deps.profileViewModel.capabilities.social
    return remember(relationships, id, viewer, social) {
        if (social) previewPeople(context, relationships, id, viewer) else ProfilePeopleState(viewerId = viewer)
    }
}

private fun previewPeople(context: Context, relationships: Map<String, Set<String>>, id: String, viewer: String?): ProfilePeopleState {
    val storage = AppVariant.previewProfileStorage(context)
    fun person(accountId: String): ProfilePerson {
        val account = runCatching { storage?.load(accountId) }.getOrNull()
        val sample = getUserProfileById(accountId)
        return ProfilePerson(accountId, account?.displayName ?: sample?.name ?: accountId, account?.avatarUrl ?: sample?.avatarUrl)
    }
    val followers = relationships.followerIds(id).map(::person)
    val following = relationships.followingIds(id).map(::person)
    // Demo people carry the same baseline counts as Explore; local follows add to them.
    val sample = getUserProfileById(id)
    return ProfilePeopleState(
        viewerId = viewer,
        followers = followers,
        following = following,
        viewerFollowing = relationships[viewer].orEmpty(),
        canMutate = true,
        followerCount = (sample?.followerCount ?: 0) + followers.size,
        followingCount = (sample?.followingCount ?: 0) + following.size,
    )
}

/** Preview only: a stored persona, a seeded persona or a demo fixture, as a public profile. */
private fun previewPublicProfile(context: Context, userId: String): UserProfileData? = runCatching {
    val account = AppVariant.previewProfileStorage(context)?.load(userId)
        ?: AppVariant.previewProfile(userId).takeIf { userId in PREVIEW_PERSONAS }
    account?.toPublicProfile() ?: getUserProfileById(userId)
}.getOrNull()

private val PREVIEW_PERSONAS = setOf("preview-owner", "preview-visitor", "preview-seeker")

internal fun applyAppLanguage(storedValue: String) = AppCompatDelegate.setApplicationLocales(
    LocaleListCompat.forLanguageTags(AppLanguage.fromStoredValue(storedValue).languageTag),
)
