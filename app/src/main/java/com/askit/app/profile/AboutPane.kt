package com.askit.app.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
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
import com.askit.app.R
import com.askit.app.session.SessionProfile
import com.askit.designsystem.profile.ChipRow
import com.askit.designsystem.profile.MetricSpec
import com.askit.designsystem.profile.MetricTrio
import com.askit.designsystem.profile.ProfileSectionCard

@Composable
fun AboutPane(
    profile: SessionProfile,
    experiences: List<ProfileExperience>,
    completedCount: Int,
    onEditAbout: () -> Unit,
    onEditLookingFor: () -> Unit,
    onEditSkills: () -> Unit,
    onAddLicense: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("profile_about_pane"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProfileSectionCard(
            title = stringResource(R.string.profile_about),
            actionLabel = stringResource(R.string.profile_edit_section),
            onAction = onEditAbout,
        ) {
            Text(
                text = profile.about,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ProfileSectionCard(
            title = stringResource(R.string.profile_looking_for),
            actionLabel = stringResource(R.string.profile_edit_section),
            onAction = onEditLookingFor,
        ) {
            Text(
                text = stringResource(R.string.profile_looking_for_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ChipRow(
                chips = profile.lookingFor,
                addLabel = stringResource(R.string.profile_add_chip),
                onAdd = onEditLookingFor,
            )
        }
        ProfileSectionCard(title = stringResource(R.string.profile_reliability)) {
            MetricTrio(
                first = MetricSpec(completedCount.toString(), stringResource(R.string.profile_metric_jobs_done)),
                second = MetricSpec(profile.reviews.size.toString(), stringResource(R.string.profile_metric_reviews)),
                third = MetricSpec(
                    if (profile.reviews.isEmpty()) {
                        "—"
                    } else {
                        String.format("%.1f", profile.reviews.map { it.rating }.average())
                    },
                    stringResource(R.string.profile_metric_rating),
                ),
            )
        }
        if (profile.hasListedService) {
            ProfileSectionCard(
                title = stringResource(R.string.profile_skills),
                actionLabel = stringResource(R.string.profile_edit_section),
                onAction = onEditSkills,
            ) {
                ChipRow(chips = profile.skills.ifEmpty { profile.listing?.tags.orEmpty() })
            }
            if (experiences.isNotEmpty()) {
                ProfileSectionCard(title = stringResource(R.string.profile_experience)) {
                    experiences.forEachIndexed { index, item ->
                        ExperienceRow(item)
                        if (index != experiences.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
            ProfileSectionCard(title = stringResource(R.string.profile_languages)) {
                profile.languages.forEachIndexed { index, (language, level) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(language, color = MaterialTheme.colorScheme.onSurface)
                        Text(level, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (index != profile.languages.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
            ProfileSectionCard(
                title = stringResource(R.string.profile_licenses),
                actionLabel = stringResource(R.string.profile_licenses_add),
                onAction = onAddLicense,
            ) {
                if (profile.licenses.isEmpty()) {
                    Text(
                        text = stringResource(R.string.profile_licenses_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("profile_licenses_empty"),
                    )
                } else {
                    profile.licenses.forEachIndexed { index, license ->
                        Text(license, color = MaterialTheme.colorScheme.onSurface)
                        if (index != profile.licenses.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExperienceRow(item: ProfileExperience) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(item.iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Column {
            Text(item.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(item.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
