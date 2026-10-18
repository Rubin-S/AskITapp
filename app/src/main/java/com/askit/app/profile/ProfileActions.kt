package com.askit.app.profile

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.askit.app.R
import com.askit.designsystem.profile.ProfileAction
import com.askit.designsystem.profile.ProfileActionConfig

enum class ProfileActionId { EDIT, VIEW_AS, PROVIDER_DASHBOARD, CREATOR_DASHBOARD, POST_TASK, FOLLOW, CONTACT, MESSAGE }

internal fun profileActionIds(provider: Boolean, owner: Boolean): List<ProfileActionId> = when {
    owner && provider -> listOf(ProfileActionId.EDIT, ProfileActionId.VIEW_AS, ProfileActionId.PROVIDER_DASHBOARD)
    owner -> listOf(ProfileActionId.EDIT, ProfileActionId.CREATOR_DASHBOARD, ProfileActionId.POST_TASK)
    provider -> listOf(ProfileActionId.FOLLOW, ProfileActionId.CONTACT, ProfileActionId.MESSAGE)
    else -> listOf(ProfileActionId.FOLLOW, ProfileActionId.MESSAGE)
}

internal fun canMutateProfileVisitor(viewerId: String?, ownerId: String, publicPreview: Boolean): Boolean =
    !publicPreview && !viewerId.isNullOrBlank() && ownerId.isNotBlank() && viewerId != ownerId

@Composable
internal fun profileActions(profile: ProfileUiState, viewerId: String?, callbacks: Map<ProfileActionId, () -> Unit>): ProfileActionConfig {
    val context = LocalContext.current
    val owner = profile.isOwner && !profile.isPublicPreview
    val enabled = owner || canMutateProfileVisitor(viewerId, profile.userId, profile.isPublicPreview)
    return ProfileActionConfig(
        profileActionIds(profile.isProvider, owner).map { action ->
            val label = stringResource(
                when (action) {
                    ProfileActionId.EDIT -> R.string.profile_edit_title
                    ProfileActionId.VIEW_AS -> R.string.profile_view_as
                    ProfileActionId.PROVIDER_DASHBOARD -> R.string.profile_my_dashboard
                    ProfileActionId.CREATOR_DASHBOARD -> R.string.profile_view_dashboard
                    ProfileActionId.POST_TASK -> R.string.profile_post_task
                    ProfileActionId.FOLLOW -> if (profile.isFollowing) R.string.profile_unfollow else R.string.story_follow
                    ProfileActionId.CONTACT -> R.string.profile_contact
                    ProfileActionId.MESSAGE -> R.string.profile_message
                },
            )
            ProfileAction(
                action.name.lowercase(),
                label,
                callbacks[action] ?: { Toast.makeText(context, R.string.profile_action_unavailable, Toast.LENGTH_LONG).show() },
                enabled || action == ProfileActionId.CONTACT,
            )
        },
    )
}
