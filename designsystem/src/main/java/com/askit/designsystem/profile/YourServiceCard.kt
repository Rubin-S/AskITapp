package com.askit.designsystem.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.askit.designsystem.jobs.AskITSecondaryButton

@Composable
fun YourServiceCard(
    eyebrow: String,
    liveLabel: String,
    live: Boolean,
    title: String,
    category: String,
    experience: String,
    description: String,
    fromLabel: String,
    quoteKind: String,
    quoteValue: String,
    coverageLabel: String,
    coverageValue: String,
    coverageHint: String,
    hoursLabel: String,
    hoursValue: String,
    hoursHint: String,
    responseLabel: String,
    responseValue: String,
    responseHint: String,
    tags: List<String>,
    editLabel: String,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("profile_your_service"),
        shape = RoundedCornerShape(24.dp),
        color = colors.secondaryContainer,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = eyebrow,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSecondaryContainer,
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (live) colors.primaryContainer else colors.errorContainer,
                ) {
                    Text(
                        text = liveLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (live) colors.onPrimaryContainer else colors.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSecondaryContainer,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(shape = RoundedCornerShape(50), color = colors.onSecondaryContainer) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.secondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                Text(
                    text = experience,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSecondaryContainer,
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSecondaryContainer,
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.surfaceContainerHigh,
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(fromLabel, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                        Text(quoteKind, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    Text(
                        text = quoteValue,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ListingMetric(coverageLabel, coverageValue, coverageHint, Modifier.weight(1f))
                ListingMetric(hoursLabel, hoursValue, hoursHint, Modifier.weight(1f))
                ListingMetric(responseLabel, responseValue, responseHint, Modifier.weight(1f))
            }
            ChipRow(chips = tags)
            HorizontalDivider(color = colors.outlineVariant)
            AskITSecondaryButton(
                onClick = onEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_edit_listing"),
            ) {
                Text(editLabel)
            }
        }
    }
}

@Composable
private fun ListingMetric(label: String, value: String, hint: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                hint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
