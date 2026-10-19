package com.askit.app.home.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.askit.app.profile.ProfileActionId
import com.askit.app.profile.ProfileContactDialog
import com.askit.app.profile.ProfileOverflowTopBar
import com.askit.app.profile.ProfilePeopleState
import com.askit.app.profile.ProfileSections
import com.askit.app.profile.ProfileUiState
import com.askit.app.profile.UniversalProfileScaffold
import com.askit.app.profile.profileActions
import com.askit.app.profile.toUiState
import com.askit.app.profile.withPeople
import com.askit.app.task.Task

/** Sample public profiles exist only in the client-preview build; release has no profile lookup yet. */
fun getUserProfileById(userId: String): UserProfileData? =
    if (com.askit.app.BuildConfig.CLIENT_PREVIEW) com.askit.app.variant.AppVariant.publicProfile(userId) else null

/**
 * Someone else's profile. Follow, Message and Block/Report appear only when their callbacks are
 * given (signed in and supported by this build); Contact only when the listing publishes contact.
 */
@Composable
fun UserProfileScreen(
    userId: String,
    publicProfile: UserProfileData?,
    profilesAvailable: Boolean,
    viewerAccountId: String?,
    viewerPostcode: String?,
    people: ProfilePeopleState,
    posts: List<FeedPost>,
    tasks: List<Task>,
    publicProfileUrl: String?,
    isBlocked: Boolean,
    isFollowing: Boolean,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onFollowPerson: (String, Boolean) -> Boolean,
    onOpenPost: (String) -> Unit,
    onOpenTask: (String) -> Unit,
    onToggleFollow: (() -> Unit)?,
    onMessage: (() -> Unit)?,
    onModerate: ((Boolean) -> Boolean)?,
    modifier: Modifier = Modifier,
    onUnblock: (() -> Unit)? = null,
) {
    var sectionId by rememberSaveable(userId) { mutableStateOf("") }
    var showContact by rememberSaveable(userId) { mutableStateOf(false) }
    val uiState = remember(publicProfile, isFollowing, sectionId, viewerPostcode, people) {
        publicProfile?.toUiState(
            isFollowing = isFollowing,
            selectedSectionId = sectionId,
            viewerPostcode = viewerPostcode,
        )?.withPeople(people)
    }
    Scaffold(
        modifier = modifier.fillMaxSize().testTag("user_profile_screen"),
        topBar = {
            ProfileOverflowTopBar(
                uiState ?: ProfileUiState(),
                viewerAccountId,
                publicProfileUrl,
                onBack = onBack,
                onModerate = onModerate.takeIf { !isBlocked },
                onUnblock = onUnblock.takeIf { isBlocked },
            )
        },
    ) { padding ->
        if (uiState == null || isBlocked) {
            val message = when {
                isBlocked -> R.string.messages_blocked
                profilesAvailable -> R.string.user_profile_unavailable_body
                else -> R.string.user_profile_unavailable_release
            }
            Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(message))
                if (isBlocked && onUnblock != null) {
                    TextButton(onClick = onUnblock, modifier = Modifier.testTag("user_profile_unblock")) {
                        Text(stringResource(R.string.profile_unblock))
                    }
                }
            }
            return@Scaffold
        }
        if (showContact) ProfileContactDialog(uiState.listing, readOnly = false) { showContact = false }
        val actionConfig = profileActions(
            uiState,
            viewerAccountId,
            buildMap {
                onToggleFollow?.let { put(ProfileActionId.FOLLOW, it) }
                onMessage?.let { put(ProfileActionId.MESSAGE, it) }
                put(ProfileActionId.CONTACT) { showContact = true }
            },
        )
        UniversalProfileScaffold(
            uiState = uiState,
            actionConfig = actionConfig,
            onSectionSelect = { sectionId = it },
            people = people,
            showTopBar = false,
            viewerAccountId = viewerAccountId,
            authorizedPostIds = remember(posts) { posts.mapTo(HashSet()) { it.id } },
            onOpenProfile = onOpenProfile,
            onFollowPerson = onFollowPerson,
            onOpenPost = onOpenPost,
            modifier = Modifier.padding(padding),
            content = { id -> ProfileSections(id, uiState, posts, tasks, onOpenPost, onOpenTask) },
        )
    }
}
