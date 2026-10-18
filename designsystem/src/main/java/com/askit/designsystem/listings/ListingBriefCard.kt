package com.askit.designsystem.listings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.askit.designsystem.people.AskITAvatar
import com.askit.designsystem.tasks.TaskPhotoMedia

/** Shared public listing hierarchy. Adapters own domain formatting and navigation. */
@Composable
fun ListingBriefCard(
    title: String,
    category: String,
    summary: String?,
    priceLabel: String?,
    locationLabel: String?,
    authorName: String?,
    metadata: List<String>,
    photoModels: List<Any>,
    onClickLabel: String,
    modifier: Modifier = Modifier,
    authorAvatarUrl: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp)
            .semantics(mergeDescendants = true) {}
            .then(if (onClick == null) Modifier else Modifier.clickable(
                role = Role.Button,
                onClickLabel = onClickLabel,
                onClick = onClick,
            )),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        if (photoModels.isNotEmpty()) TaskPhotoMedia(photoModels = photoModels)
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            category.trim().takeIf(String::isNotEmpty)?.let {
                Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                title.trim(), style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
            summary?.trim()?.takeIf(String::isNotEmpty)?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            priceLabel?.trim()?.takeIf(String::isNotEmpty)?.let {
                Text(it, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
            locationLabel?.trim()?.takeIf(String::isNotEmpty)?.let { ListingMetadata(it) }
            authorName?.trim()?.takeIf(String::isNotEmpty)?.let { name ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (authorAvatarUrl != null) AskITAvatar(
                        avatarUrl = authorAvatarUrl, avatarSize = 24.dp, fallbackIconSize = 16.dp,
                    )
                    Text(name, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f))
                }
            }
            val secondary = metadata.map(String::trim).filter(String::isNotEmpty).distinct().joinToString(" · ")
            if (secondary.isNotEmpty()) Text(
                secondary, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ListingMetadata(value: String) {
    Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
