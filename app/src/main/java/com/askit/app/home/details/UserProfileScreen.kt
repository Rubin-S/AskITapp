package com.askit.app.home.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import com.askit.app.profile.UniversalProfileScaffold
import com.askit.app.profile.withPeople
import com.askit.app.profile.toUiState

data class UserReviewData(
    val reviewerName: String,
    val rating: Double,
    val date: String,
    val comment: String,
)

data class UserProfileData(
    val id: String,
    val username: String,
    val name: String,
    val trade: String = "",
    val avatarUrl: String? = null,
    val directionalReviews: List<com.askit.app.profile.ProfileReview> = emptyList(),
    val completedAsProvider: Int? = null,
    val completedAsSeeker: Int? = null,
    val rating: Double = 0.0,
    val completedJobsCount: Int = 0,
    val location: String = "Local District",
    val joinedYear: String = "",
    val bio: String = "",
    val skills: List<String> = emptyList(),
    val reviews: List<UserReviewData> = emptyList(),
    val isProvider: Boolean = true,
    val activityCount: Int = 12,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val isVerified: Boolean = false,
    val bannerUrl: String? = null,
    val highlights: List<com.askit.app.profile.ProfileHighlight> = emptyList(),
    val listing: com.askit.app.session.ServiceListing? = null,
    val discloseApproximateLocation: Boolean = false,
    val postcodeDistanceKm: Double? = null,
)

/** Sample public profiles exist only in the client-preview build; release has no profile lookup yet. */
fun getUserProfileById(userId: String): UserProfileData? =
    if (com.askit.app.BuildConfig.CLIENT_PREVIEW) com.askit.app.variant.AppVariant.publicProfile(userId) else null

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    posts: List<com.askit.app.home.model.FeedPost> = emptyList(),
    onOpenPost: (String) -> Unit = {},
    onMessage: () -> Unit = {},
    tasks: List<com.askit.app.task.Task> = emptyList(),
    onOpenTask: (String) -> Unit = {},
    publicProfileUrl: String? = null,
    onModerate: (Boolean) -> Boolean = { false },
    isBlocked: Boolean = false,
    viewerPostcode: String? = null,
    viewerAccountId: String? = null,
    publicProfile: UserProfileData? = null,
    people: com.askit.app.profile.ProfilePeopleState = com.askit.app.profile.ProfilePeopleState(),
    onOpenProfile: (String) -> Unit = {},
    onFollowPerson: (String, Boolean) -> Boolean = { _, _ -> false },
    isFollowing: Boolean? = null,
    onToggleFollow: (() -> Unit)? = null,
) {
    val user = publicProfile ?: remember(userId) { getUserProfileById(userId) }
    val following = isFollowing ?: false
    var sectionId by rememberSaveable(userId) { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("user_profile_screen"),
        topBar = {
            com.askit.app.profile.ProfileOverflowTopBar(user?.toUiState() ?: com.askit.app.profile.ProfileUiState(),
                viewerAccountId, publicProfileUrl, onBack = onBack, onModerate = onModerate)
        },
    ) { padding ->
        if (user == null || isBlocked) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(if (isBlocked) R.string.messages_blocked else R.string.user_profile_unavailable_body))
            }
            return@Scaffold
        }
        val uiState = user.toUiState(isFollowing = following, selectedSectionId = sectionId, viewerPostcode = viewerPostcode).withPeople(people)
        var showContact by rememberSaveable(userId) { mutableStateOf(false) }
        if (showContact) com.askit.app.profile.ProfileContactDialog(uiState.listing, false) { showContact = false }
        val actionConfig = com.askit.app.profile.profileActions(uiState, viewerAccountId, mapOf(
            com.askit.app.profile.ProfileActionId.FOLLOW to { onToggleFollow?.invoke() },
            com.askit.app.profile.ProfileActionId.MESSAGE to onMessage,
            com.askit.app.profile.ProfileActionId.CONTACT to { showContact = true },
        ))

        UniversalProfileScaffold(
            uiState = uiState, people = people, onOpenProfile = onOpenProfile, onFollowPerson = onFollowPerson,
            actionConfig = actionConfig,
            showTopBar = false,
            isVisitor = true,
            viewerAccountId = viewerAccountId,
            authorizedPostIds = posts.map { it.id }.toSet(),
            onOpenPost = onOpenPost,
            modifier = Modifier.padding(padding),
            onSectionSelected = { sectionId = it },
            content = { id ->
                com.askit.app.profile.ProfileSections(id, uiState, posts, tasks, onOpenPost, onOpenTask)
            },
        )
    }
}
