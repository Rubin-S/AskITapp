package com.askit.app.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.app.category.ASKIT_SERVICE_CATEGORIES
import com.askit.app.format.appLocale
import com.askit.app.home.model.FeedPost
import com.askit.app.home.model.PostMedia
import com.askit.app.listservice.labels
import com.askit.app.session.ServiceListing
import com.askit.app.task.Task
import com.askit.app.task.presentation
import com.askit.designsystem.profile.YourServiceCard
import com.askit.designsystem.tasks.TaskResultItem

/** Unknown authors cannot be assigned to a profile by display name. */
internal fun List<Task>.forProfile(accountId: String): List<Task> =
    if (accountId.isBlank()) emptyList() else filter { it.posterId == accountId }

/** The selected section. Owner buttons appear only when their callbacks are given. */
@Composable
fun ProfileSections(
    sectionId: String,
    state: ProfileUiState,
    posts: List<FeedPost>,
    tasks: List<Task>,
    onOpenPost: (String) -> Unit,
    onOpenTask: (String) -> Unit,
    modifier: Modifier = Modifier,
    onEditService: (() -> Unit)? = null,
    onCreateTask: (() -> Unit)? = null,
) {
    val owner = state.isOwnerView
    Box(modifier.fillMaxWidth()) {
        when (sectionId) {
            ProfileTab.ABOUT -> ServiceAbout(state.listing, onEditService.takeIf { owner })
            ProfileTab.SERVICES -> ServiceCard(state.listing, onEditService.takeIf { owner })
            ProfileTab.SHOWCASE, ProfileTab.POSTS -> PostsGrid(posts, onOpenPost)
            else -> ProfileTasks(tasks.forProfile(state.userId), onOpenTask, onCreateTask.takeIf { owner })
        }
    }
}

@Composable
private fun ProfileTasks(tasks: List<Task>, onOpenTask: (String) -> Unit, onCreateTask: (() -> Unit)?) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (onCreateTask != null) Button(onClick = onCreateTask) { Text(stringResource(R.string.profile_post_task)) }
        if (tasks.isEmpty()) EmptySection(R.string.profile_tasks_empty)
        tasks.forEach { task ->
            val labels = remember(task, context) { task.presentation(context) }
            TaskResultItem(
                title = task.title, category = labels.category, summary = task.description,
                budgetLabel = labels.budget, locationLabel = labels.location, timingLabel = labels.timing,
                posterName = task.posterName, postedLabel = labels.posted, photoModels = task.photos,
                scopeHighlights = labels.requirements, onClick = { onOpenTask(task.id) },
            )
        }
    }
}

@Composable
private fun ServiceAbout(listing: ServiceListing?, onEdit: (() -> Unit)?) {
    if (listing == null) return EmptySection(R.string.profile_services_empty)
    val labels = listing.labels(LocalContext.current)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(listing.title, style = MaterialTheme.typography.titleLarge)
        Text(listing.description, style = MaterialTheme.typography.bodyLarge)
        listOf(
            R.string.profile_service_category to listing.categoryLabel(),
            R.string.profile_service_experience to listing.experienceLabel(),
            R.string.profile_service_area to listOf(labels.responseHint, listing.coverage, labels.coverageHint)
                .filter(String::isNotBlank).joinToString(" · "),
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
        if (onEdit != null) Button(onClick = onEdit) { Text(stringResource(R.string.profile_edit_service)) }
    }
}

@Composable
private fun ServiceCard(listing: ServiceListing?, onEdit: (() -> Unit)?) {
    if (listing == null) return EmptySection(R.string.profile_services_empty, Modifier.testTag("profile_services_empty"))
    val labels = listing.labels(LocalContext.current)
    val locale = appLocale()
    YourServiceCard(
        eyebrow = stringResource(R.string.profile_service_offering).uppercase(locale),
        liveLabel = stringResource(if (listing.live) R.string.profile_service_active else R.string.profile_service_unavailable),
        live = listing.live,
        title = listing.title,
        category = listing.categoryLabel(),
        experience = listing.experienceLabel(),
        description = listing.description,
        fromLabel = stringResource(R.string.profile_from).uppercase(locale),
        quoteKind = stringResource(R.string.profile_visit_quote),
        quoteValue = labels.quote,
        coverageLabel = stringResource(R.string.profile_service_area).uppercase(locale),
        // Delivery modes say where the work happens, so they stand in for a missing service area.
        coverageValue = listing.coverage.ifBlank { labels.responseHint },
        coverageHint = labels.coverageHint,
        hoursLabel = stringResource(R.string.profile_hours).uppercase(locale),
        hoursValue = listing.hours,
        hoursHint = listing.hoursHint,
        responseLabel = stringResource(R.string.profile_response).uppercase(locale),
        responseValue = listing.response,
        responseHint = "",
        tags = listing.tags,
        editLabel = stringResource(R.string.profile_edit_service),
        onEdit = onEdit,
    )
}

/** The category in the current language when the listing has a known id; otherwise its saved label. */
@Composable
private fun ServiceListing.categoryLabel(): String =
    ASKIT_SERVICE_CATEGORIES.firstOrNull { it.id == categoryId }?.let { stringResource(it.labelRes) } ?: category

/** Whole years (Form B) read "5 years"; any older free text is shown as saved. */
@Composable
private fun ServiceListing.experienceLabel(): String =
    experience.trim().toIntOrNull()?.let { pluralStringResource(R.plurals.profile_experience_years, it, it) } ?: experience

@Composable
private fun EmptySection(text: Int, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(stringResource(text), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Ordered by the shared post store; pinned posts appear first for owners and visitors. */
@Composable
private fun PostsGrid(posts: List<FeedPost>, onOpen: (String) -> Unit) {
    if (posts.isEmpty()) return EmptySection(R.string.profile_posts_empty)
    val pinned = stringResource(R.string.post_pinned)
    val untitled = stringResource(R.string.post_detail_title)
    Column(Modifier.testTag("profile_posts"), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        posts.chunked(GRID_COLUMNS).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { post ->
                    PostTile(
                        post,
                        if (post.pinnedAtMillis !=
                            null
                        ) {
                            pinned
                        } else {
                            null
                        },
                        untitled,
                        onOpen,
                        Modifier.weight(1f),
                    )
                }
                repeat(GRID_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun PostTile(post: FeedPost, pinnedLabel: String?, untitled: String, onOpen: (String) -> Unit, modifier: Modifier = Modifier) {
    val title = post.content.ifBlank { post.poll?.question.orEmpty() }.ifBlank { untitled }
    Surface(
        onClick = { onOpen(post.id) },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = modifier.aspectRatio(3f / 4f).testTag("profile_post_${post.id}")
            .semantics { contentDescription = listOfNotNull(pinnedLabel, title).joinToString(". ") },
    ) {
        // The title stays underneath, so it shows while the image loads or if it fails.
        Box(contentAlignment = Alignment.Center) {
            Text(title, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, maxLines = 4, overflow = TextOverflow.Ellipsis)
            post.media.coverUrl()?.let { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
            if (pinnedLabel != null) {
                Text(
                    pinnedLabel,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
                        .background(Color.Black.copy(alpha = .8f), RoundedCornerShape(8.dp)).padding(6.dp),
                )
            }
        }
    }
}

private fun PostMedia?.coverUrl(): String? = when (this) {
    is PostMedia.SinglePhoto -> url
    is PostMedia.Carousel -> urls.firstOrNull()
    is PostMedia.BeforeAfter -> beforeUrl
    null -> null
}

private const val GRID_COLUMNS = 3
