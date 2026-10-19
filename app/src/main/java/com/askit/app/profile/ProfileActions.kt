package com.askit.app.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.askit.app.R
import com.askit.app.auth.validAccountEmail
import com.askit.app.auth.validAccountPhone
import com.askit.app.session.ServiceListing
import com.askit.designsystem.profile.ProfileAction
import com.askit.designsystem.profile.ProfileActionConfig

enum class ProfileActionId { EDIT, VIEW_AS, PROVIDER_DASHBOARD, CREATOR_DASHBOARD, POST_TASK, FOLLOW, CONTACT, MESSAGE }

/** The buttons a profile can offer, before dropping the ones this build or this profile can't support. */
internal fun profileActionIds(provider: Boolean, owner: Boolean): List<ProfileActionId> = when {
    owner && provider -> listOf(ProfileActionId.EDIT, ProfileActionId.VIEW_AS, ProfileActionId.PROVIDER_DASHBOARD)
    owner -> listOf(ProfileActionId.EDIT, ProfileActionId.VIEW_AS, ProfileActionId.CREATOR_DASHBOARD, ProfileActionId.POST_TASK)
    provider -> listOf(ProfileActionId.FOLLOW, ProfileActionId.CONTACT, ProfileActionId.MESSAGE)
    else -> listOf(ProfileActionId.FOLLOW, ProfileActionId.MESSAGE)
}

internal fun canMutateProfileVisitor(viewerId: String?, ownerId: String, publicPreview: Boolean): Boolean =
    !publicPreview && !viewerId.isNullOrBlank() && ownerId.isNotBlank() && viewerId != ownerId

/** Contact is offered only when the listing actually publishes a valid phone or email. */
internal val ServiceListing?.hasPublishedContact: Boolean
    get() = this != null && (validAccountPhone(phoneNumber) || validAccountEmail(email))

/** [profileActionIds] minus Contact without published contact, and minus actions this build gave no callback for. */
internal fun visibleProfileActionIds(profile: ProfileUiState, available: Set<ProfileActionId>): List<ProfileActionId> =
    profileActionIds(profile.isProvider, profile.isOwnerView)
        .filter { it != ProfileActionId.CONTACT || profile.listing.hasPublishedContact }
        .filter { profile.isPublicPreview || it in available }

/**
 * Builds the action row. An action without a callback is dropped rather than shown as a button that
 * can't work: release passes no callbacks for what its backend can't do yet. In "View as" the
 * visitor buttons are shown disabled, as a preview.
 */
@Composable
internal fun profileActions(profile: ProfileUiState, viewerId: String?, callbacks: Map<ProfileActionId, () -> Unit>): ProfileActionConfig {
    val owner = profile.isOwnerView
    val visitorCanAct = canMutateProfileVisitor(viewerId, profile.userId, profile.isPublicPreview)
    return ProfileActionConfig(
        visibleProfileActionIds(profile, callbacks.keys).map { action ->
            val label = when (action) {
                ProfileActionId.EDIT -> R.string.profile_edit_title
                ProfileActionId.VIEW_AS -> R.string.profile_view_as
                ProfileActionId.PROVIDER_DASHBOARD -> R.string.profile_my_dashboard
                ProfileActionId.CREATOR_DASHBOARD -> R.string.profile_view_dashboard
                ProfileActionId.POST_TASK -> R.string.profile_post_task
                ProfileActionId.FOLLOW -> if (profile.isFollowing) R.string.profile_unfollow else R.string.story_follow
                ProfileActionId.CONTACT -> R.string.profile_contact
                ProfileActionId.MESSAGE -> R.string.profile_message
            }
            ProfileAction(
                action.name.lowercase(),
                stringResource(label),
                callbacks[action] ?: {},
                owner || visitorCanAct || (action == ProfileActionId.CONTACT && !profile.isPublicPreview),
            )
        },
    )
}
