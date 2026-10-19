package com.askit.app.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.home.model.FeedPost
import com.askit.app.home.model.Story
import com.askit.app.jobs.Job
import com.askit.app.session.SessionProfile
import com.askit.app.task.Task

/** The Profile tab: your own profile, its "View as" public preview, and the owner controls the build supports. */
@Composable
fun ProfileRoute(
    profile: SessionProfile,
    jobs: List<Job>,
    tasks: List<Task>,
    posts: List<FeedPost>,
    stories: List<Story>,
    people: ProfilePeopleState,
    loadState: ProfileLoadState,
    capabilities: ProfileCapabilities,
    publicProfileUrl: String?,
    onEditProfile: () -> Unit,
    onEditListing: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProviderDashboard: (() -> Unit)?,
    onOpenCreatorDashboard: (() -> Unit)?,
    onPostTask: (() -> Unit)?,
    onOpenProfile: (String) -> Unit,
    onFollowPerson: (String, Boolean) -> Boolean,
    onOpenPost: (String) -> Unit,
    onOpenTask: (String) -> Unit,
    onSetAvatar: (String?) -> Unit,
    onSaveBanner: suspend (String?) -> Boolean,
    onSaveHighlight: suspend (ProfileHighlight) -> Boolean,
    onDeleteHighlight: suspend (String) -> Boolean,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var sectionId by rememberSaveable(profile.accountId) { mutableStateOf("") }
    var isPublicPreview by rememberSaveable(profile.accountId) { mutableStateOf(false) }
    var showPhotoSheet by rememberSaveable(profile.accountId) { mutableStateOf(false) }
    var showContact by rememberSaveable(profile.accountId) { mutableStateOf(false) }
    BackHandler(enabled = isPublicPreview) { isPublicPreview = false }
    val canEdit = capabilities.editProfile && !isPublicPreview
    val uiState = remember(profile, jobs, isPublicPreview, sectionId, people) {
        profile.toUiState(jobs = jobs, isPublicPreview = isPublicPreview, selectedSectionId = sectionId).withPeople(people)
    }
    val postIds = remember(posts) { posts.mapTo(HashSet()) { it.id } }
    if (showContact) ProfileContactDialog(profile.listing, readOnly = isPublicPreview, onDismiss = { showContact = false })
    ProfilePhotoSource(
        visible = showPhotoSheet && canEdit,
        hasPhoto = profile.avatarUrl != null,
        onDismiss = { showPhotoSheet = false },
        onPick = onSetAvatar,
        onRemove = { onSetAvatar(null) },
    )
    val actionConfig = profileActions(
        uiState,
        profile.accountId,
        buildMap {
            if (capabilities.editProfile) put(ProfileActionId.EDIT, onEditProfile)
            put(ProfileActionId.VIEW_AS) { isPublicPreview = true }
            onOpenProviderDashboard?.let { put(ProfileActionId.PROVIDER_DASHBOARD, it) }
            onOpenCreatorDashboard?.let { put(ProfileActionId.CREATOR_DASHBOARD, it) }
            onPostTask?.let { put(ProfileActionId.POST_TASK, it) }
            put(ProfileActionId.CONTACT) { showContact = true }
        },
    )
    when (loadState) {
        ProfileLoadState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(Modifier.testTag("profile_loading"))
        }

        is ProfileLoadState.Failed -> Column(
            modifier.fillMaxSize().padding(24.dp).testTag("profile_failed"),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(loadState.messageRes), color = MaterialTheme.colorScheme.onSurface)
            // Editing can only fix a blank profile where this build can save; otherwise signing out always works.
            if (capabilities.editProfile) {
                Button(onClick = onEditProfile) { Text(stringResource(R.string.profile_edit)) }
            } else {
                Button(onClick = onSignOut) { Text(stringResource(R.string.settings_logout)) }
            }
        }

        ProfileLoadState.Ready -> UniversalProfileScaffold(
            uiState = uiState,
            people = people.copy(canMutate = people.canMutate && !isPublicPreview),
            actionConfig = actionConfig,
            publicProfileUrl = publicProfileUrl,
            viewerAccountId = profile.accountId,
            onSectionSelect = { sectionId = it },
            onOpenProfile = onOpenProfile,
            onFollowPerson = onFollowPerson,
            onOpenSettings = onOpenSettings,
            onBack = if (isPublicPreview) ({ isPublicPreview = false }) else null,
            onCameraClick = if (canEdit) ({ showPhotoSheet = true }) else null,
            onSaveBanner = onSaveBanner.takeIf { canEdit },
            modifier = modifier.testTag("profile_route"),
            highlightsContent = {
                ProfileHighlights(
                    ownerId = profile.accountId,
                    viewerId = profile.accountId.takeUnless { isPublicPreview },
                    collections = profile.highlights,
                    stories = stories,
                    canManage = canEdit,
                    authorizedPostIds = postIds,
                    onSave = onSaveHighlight,
                    onDelete = onDeleteHighlight,
                    onOpenPost = onOpenPost,
                )
            },
            content = { id ->
                ProfileSections(
                    sectionId = id,
                    state = uiState,
                    posts = posts,
                    tasks = tasks,
                    onOpenPost = onOpenPost,
                    onOpenTask = onOpenTask,
                    onEditService = onEditListing.takeIf { canEdit },
                    onCreateTask = onPostTask.takeIf { !isPublicPreview },
                )
            },
        )
    }
}
