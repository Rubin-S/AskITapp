package com.askit.designsystem.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.askit.designsystem.R

/**
 * Universal profile identity block displaying name, optional verified community badge,
 * optional trade headline (prominently styled for Form B service providers),
 * optional bio, and locality line with location icon.
 */
@Composable
fun ProfileIdentityBlock(
    displayName: String,
    localityLine: String,
    modifier: Modifier = Modifier,
    tradeHeadline: String? = null,
    isVerified: Boolean = false,
    bio: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("profile_identity_block"),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("profile_display_name"),
            )
            if (isVerified) {
                Icon(
                    painter = painterResource(R.drawable.ic_verified),
                    contentDescription = stringResource(R.string.profile_verified_badge_desc),
                    modifier = Modifier
                        .size(18.dp)
                        .testTag("profile_verified_badge"),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (!tradeHeadline.isNullOrBlank()) {
            Text(
                text = tradeHeadline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("profile_trade_headline"),
            )
        }

        if (!bio.isNullOrBlank()) {
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("profile_bio"),
            )
        }

        Row(
            modifier = Modifier.testTag("profile_locality"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = localityLine,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Backward-compatible overload for legacy callers specifying [locationLine] and non-null [bio].
 */
@Deprecated(
    message = "Use ProfileIdentityBlock with localityLine and optional tradeHeadline/isVerified",
    replaceWith = ReplaceWith("ProfileIdentityBlock(displayName = displayName, localityLine = locationLine, modifier = modifier, bio = bio)"),
)
@Composable
fun ProfileIdentityBlock(
    displayName: String,
    bio: String,
    locationLine: String,
    modifier: Modifier = Modifier,
) {
    ProfileIdentityBlock(
        displayName = displayName,
        localityLine = locationLine,
        modifier = modifier,
        tradeHeadline = null,
        isVerified = false,
        bio = bio,
    )
}
