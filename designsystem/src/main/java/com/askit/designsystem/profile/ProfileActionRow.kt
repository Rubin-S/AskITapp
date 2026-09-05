package com.askit.designsystem.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.designsystem.R
import com.askit.designsystem.jobs.AskITSecondaryButton

/**
 * Contextual configuration for ProfileActionRow:
 * - [Owner]: Owner view actions (Edit Profile, Share, View as Public)
 * - [Visitor]: Visitor view actions (Message, Follow/Following, contextual Request Service)
 */
sealed interface ProfileActionConfig {
    data class Owner(
        val onEditProfile: () -> Unit,
        val onShare: () -> Unit,
        val onViewAsPublic: () -> Unit,
    ) : ProfileActionConfig

    data class Visitor(
        val onMessage: () -> Unit,
        val isFollowing: Boolean,
        val onToggleFollow: () -> Unit,
        val onRequestService: (() -> Unit)? = null, // null for Form A community members
    ) : ProfileActionConfig
}

/**
 * Universal primary action row supporting contextual switching between Owner mode
 * and Visitor mode (Form A vs Form B).
 */
@Composable
fun ProfileActionRow(
    config: ProfileActionConfig,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("profile_action_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (config) {
            is ProfileActionConfig.Owner -> {
                AskITSecondaryButton(
                    onClick = config.onEditProfile,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_action_edit"),
                ) {
                    Text(
                        text = stringResource(R.string.profile_action_edit),
                        maxLines = 1,
                    )
                }
                AskITSecondaryButton(
                    onClick = config.onShare,
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("profile_action_share"),
                ) {
                    Text(
                        text = stringResource(R.string.profile_action_share),
                        maxLines = 1,
                    )
                }
                AskITSecondaryButton(
                    onClick = config.onViewAsPublic,
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("profile_action_view_as_public"),
                ) {
                    Text(
                        text = stringResource(R.string.profile_action_view_as_public),
                        maxLines = 1,
                    )
                }
            }

            is ProfileActionConfig.Visitor -> {
                AskITSecondaryButton(
                    onClick = config.onMessage,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_action_message"),
                ) {
                    Text(
                        text = stringResource(R.string.profile_action_message),
                        maxLines = 1,
                    )
                }
                AskITSecondaryButton(
                    onClick = config.onToggleFollow,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_action_follow"),
                ) {
                    Text(
                        text = if (config.isFollowing) {
                            stringResource(R.string.profile_action_following)
                        } else {
                            stringResource(R.string.profile_action_follow)
                        },
                        maxLines = 1,
                    )
                }
                if (config.onRequestService != null) {
                    Button(
                        onClick = config.onRequestService,
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 48.dp)
                            .testTag("profile_action_request_service"),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.profile_action_request_service),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Backward-compatible overload for legacy callers.
 */
@Deprecated(
    message = "Use ProfileActionRow(config = ProfileActionConfig.Owner(...))",
    replaceWith = ReplaceWith("ProfileActionRow(config = ProfileActionConfig.Owner(onEditProfile = onEdit, onShare = {}, onViewAsPublic = {}))"),
)
@Composable
fun ProfileActionRow(
    editLabel: String,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    availabilityLabel: String? = null,
    onAvailability: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("profile_action_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AskITSecondaryButton(
            onClick = onEdit,
            modifier = Modifier
                .weight(1f)
                .testTag("profile_edit")
                .testTag("profile_action_edit"),
        ) {
            Text(editLabel)
        }
        if (availabilityLabel != null && onAvailability != null) {
            AskITSecondaryButton(
                onClick = onAvailability,
                modifier = Modifier
                    .weight(1f)
                    .testTag("profile_availability"),
            ) {
                Text(availabilityLabel)
            }
        }
    }
}
