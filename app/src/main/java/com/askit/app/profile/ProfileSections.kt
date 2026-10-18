package com.askit.app.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.home.model.FeedPost
import com.askit.app.listservice.ListServiceDeliveryMode
import com.askit.app.listservice.labels
import com.askit.app.session.ServiceListing
import com.askit.app.task.Task
import com.askit.app.task.presentation
import com.askit.designsystem.profile.YourServiceCard
import com.askit.designsystem.tasks.TaskResultItem

/** Unknown authors cannot be assigned to a profile by display name. */
internal fun List<Task>.forProfile(accountId: String): List<Task> =
    if (accountId.isBlank()) emptyList() else filter { it.posterId == accountId }

@Composable
fun ProfileSections(
    sectionId: String,
    state: ProfileUiState,
    posts: List<FeedPost>,
    tasks: List<Task>,
    onOpenPost: (String) -> Unit,
    onOpenTask: (String) -> Unit,
    onEditService: () -> Unit = {},
    onCreateTask: () -> Unit = {},
) {
    val canManage = state.isOwner && !state.isPublicPreview
    when (sectionId) {
        "about" -> ProfileServiceAbout(state.listing, canManage, onEditService)

        "services" -> ServicesPane(state.listing, canManage, onEditService)

        "showcase", "posts" -> ProfilePostsPane(posts, onOpenPost)

        "tasks" -> {
            val context = LocalContext.current
            val ownedTasks = tasks.forProfile(state.userId)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (canManage) Button(onClick = onCreateTask) { Text(stringResource(R.string.profile_post_task)) }
                if (ownedTasks.isEmpty()) Text(stringResource(R.string.profile_tasks_empty), Modifier.padding(24.dp))
                ownedTasks.forEach { task ->
                    val labels = task.presentation(context)
                    TaskResultItem(
                        title = task.title, category = labels.category, summary = task.description,
                        budgetLabel = labels.budget, locationLabel = labels.location, timingLabel = labels.timing,
                        posterName = task.posterName, postedLabel = labels.posted, photoModels = task.photos,
                        scopeHighlights = labels.requirements, onClick = { onOpenTask(task.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileServiceAbout(listing: ServiceListing?, canManage: Boolean, onEdit: () -> Unit) {
    if (listing == null) {
        Text(stringResource(R.string.profile_services_empty), Modifier.padding(24.dp))
        return
    }
    val coverageHint = listing.labels(LocalContext.current).coverageHint
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(listing.title, style = MaterialTheme.typography.titleLarge)
        Text(listing.description, style = MaterialTheme.typography.bodyLarge)
        listOf(
            R.string.profile_service_category to listing.category,
            R.string.profile_service_experience to listing.experience,
            R.string.profile_service_area to (
                listing.deliveryModes.map { mode ->
                    stringResource(
                        when (mode) {
                            ListServiceDeliveryMode.REMOTE -> R.string.list_service_remote
                            ListServiceDeliveryMode.AT_CUSTOMER_LOCATION -> R.string.list_service_at_customer_location
                            ListServiceDeliveryMode.AT_PROVIDER_LOCATION -> R.string.list_service_at_provider_location
                        },
                    )
                } + listOf(listing.coverage, coverageHint)
                ).filter(String::isNotBlank).joinToString(" · "),
            R.string.profile_hours to listing.hours,
            R.string.profile_contact_phone to listing.phoneNumber,
            R.string.profile_contact_email to listing.email,
            R.string.profile_service_certificate to listing.certificate.orEmpty(),
            R.string.profile_service_materials to listing.materials.orEmpty(),
        ).filter { it.second.isNotBlank() }.forEach { (label, value) ->
            Column {
                Text(stringResource(label), style = MaterialTheme.typography.labelLarge)
                Text(value, style = MaterialTheme.typography.bodyLarge)
            }
        }
        if (canManage) Button(onClick = onEdit) { Text(stringResource(R.string.profile_edit_service)) }
    }
}

@Composable
fun ServicesPane(listing: ServiceListing?, isOwner: Boolean, onEditListing: () -> Unit, modifier: Modifier = Modifier) {
    if (listing != null) {
        val labels = listing.labels(LocalContext.current)
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            YourServiceCard(
                eyebrow = stringResource(R.string.profile_service_offering).uppercase(),
                liveLabel = stringResource(if (listing.live) R.string.profile_service_active else R.string.profile_service_unavailable),
                live = listing.live,
                title = listing.title,
                category = listing.category,
                experience = listing.experience,
                description = listing.description,
                fromLabel = stringResource(R.string.profile_from).uppercase(),
                quoteKind = stringResource(R.string.profile_visit_quote),
                quoteValue = labels.quote,
                coverageLabel = stringResource(R.string.profile_service_area).uppercase(),
                coverageValue = listing.coverage.ifBlank { labels.responseHint },
                coverageHint = labels.coverageHint,
                hoursLabel = stringResource(R.string.profile_hours).uppercase(),
                hoursValue = listing.hours,
                hoursHint = listing.hoursHint,
                responseLabel = stringResource(R.string.profile_response).uppercase(),
                responseValue = listing.response,
                responseHint = labels.responseHint,
                tags = listing.tags,
                editLabel = if (isOwner) stringResource(R.string.profile_edit_service) else "",
                onEdit = onEditListing,
            )
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp)
                .testTag("profile_services_empty"),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.profile_services_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
