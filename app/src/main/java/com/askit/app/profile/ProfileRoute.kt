package com.askit.app.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.jobs.Job
import com.askit.app.session.SessionProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun ProfileRoute(
    profile: SessionProfile,
    jobs: List<Job>,
    onEditProfile: () -> Unit,
    onEditListing: () -> Unit,
    onSetAvatar: (String?) -> Unit,
    onSaveBanner: (String?) -> Boolean,
    modifier: Modifier = Modifier,
    tasks: List<com.askit.app.task.Task> = emptyList(),
    onOpenTask: (String) -> Unit = {},
    loadState: ProfileLoadState = ProfileLoadState.Ready,
    messages: Flow<Int> = emptyFlow(),
    onOpenSettings: () -> Unit = {},
    onOpenProviderDashboard: () -> Unit = {},
    onOpenCreatorDashboard: () -> Unit = {},
    onPostTask: () -> Unit = {},
    publicProfileUrl: String? = null,
    stories: List<com.askit.app.home.model.Story> = emptyList(),
    onSaveHighlight: (ProfileHighlight) -> Boolean = { false },
    onDeleteHighlight: (String) -> Boolean = { false },
    people: ProfilePeopleState = ProfilePeopleState(),
    onOpenProfile: (String) -> Unit = {},
    onFollowPerson: (String, Boolean) -> Boolean = { _, _ -> false },
    posts: List<com.askit.app.home.model.FeedPost> = emptyList(),
    onOpenPost: (String) -> Unit = {},
) {
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }
    val photoActions = androidx.compose.runtime.key(profile.accountId) { rememberProfilePhotoActions { onSetAvatar(it) } }
    var sectionId by rememberSaveable(profile.accountId) { mutableStateOf("") }
    var isPublicPreview by rememberSaveable(profile.accountId) { mutableStateOf(false) }
    var showPhotoSheet by rememberSaveable(profile.accountId) { mutableStateOf(false) }
    BackHandler(enabled = isPublicPreview) { isPublicPreview = false }

    LaunchedEffect(messages, resources) {
        messages.collect { res ->
            snackbarHostState.showSnackbar(resources.getString(res))
        }
    }

    val uiState = profile.toUiState(
        jobs = jobs,
        isPublicPreview = isPublicPreview,
        selectedSectionId = sectionId,
    ).withPeople(people)

    var showContact by rememberSaveable { mutableStateOf(false) }
    if (showContact) ProfileContactDialog(profile.listing, isPublicPreview, onDismiss = { showContact = false })
    val actionConfig = profileActions(
        uiState,
        profile.accountId,
        mapOf(
            ProfileActionId.EDIT to onEditProfile,
            ProfileActionId.VIEW_AS to { isPublicPreview = true },
            ProfileActionId.PROVIDER_DASHBOARD to onOpenProviderDashboard,
            ProfileActionId.CREATOR_DASHBOARD to onOpenCreatorDashboard,
            ProfileActionId.POST_TASK to onPostTask,
            ProfileActionId.CONTACT to { showContact = true },
        ),
    )

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("profile_route"),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (loadState) {
            ProfileLoadState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.testTag("profile_loading"))
            }

            is ProfileLoadState.Failed -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).testTag("profile_failed"),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(24.dp),
                ) {
                    Text(stringResource(loadState.messageRes), color = MaterialTheme.colorScheme.onSurface)
                    Button(onClick = onEditProfile) { Text(stringResource(R.string.profile_edit)) }
                }
            }

            ProfileLoadState.Ready -> UniversalProfileScaffold(
                uiState = uiState,
                people = people.copy(canMutate = people.canMutate && !isPublicPreview),
                onOpenProfile = onOpenProfile, onFollowPerson = onFollowPerson,
                actionConfig = actionConfig,
                modifier = Modifier.fillMaxSize(),
                showTopBar = true, publicProfileUrl = publicProfileUrl, viewerAccountId = profile.accountId,
                onOpenSettings = onOpenSettings,
                onCameraClick = { showPhotoSheet = true },
                onSaveBanner = onSaveBanner,
                onSectionSelected = { sectionId = it },
                onExitPreview = { isPublicPreview = false },
                highlightsContent = {
                    ProfileHighlights(
                        profile.accountId, profile.accountId.takeUnless { isPublicPreview }, profile.highlights,
                        stories, canManage = !isPublicPreview, authorizedPostIds = posts.map { it.id }.toSet(), onSave = onSaveHighlight,
                        onDelete = onDeleteHighlight, onOpenPost = onOpenPost,
                    )
                },
                content = { id ->
                    ProfileSections(id, uiState, posts, tasks, onOpenPost, onOpenTask, onEditListing, onPostTask)
                },
            )
        }
    }

    if (showPhotoSheet) {
        PhotoSourceSheet(
            onDismiss = { showPhotoSheet = false },
            onCamera = {
                showPhotoSheet = false
                photoActions.launchCamera()
            },
            onLibrary = {
                showPhotoSheet = false
                photoActions.launchLibrary()
            },
            onRemove = {
                onSetAvatar(null)
                showPhotoSheet = false
            },
        )
    }
}
