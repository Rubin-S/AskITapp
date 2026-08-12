package com.askit.designsystem.tasks

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.askit.designsystem.R
import com.askit.designsystem.theme.AskITTheme

enum class TaskResultStatus {
    Open,
    Applied,
    Filled,
    Closed,
    Expired,
    Unavailable,
}

/**
 * The canonical public Task representation used by both Explore and Post Task Review.
 *
 * [photoModels] intentionally accepts Coil's existing model input seam. Production callers
 * currently pass URI/URL strings; tests can pass an in-memory bitmap without adding an app
 * dependency to the design system.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskResultItem(
    title: String,
    category: String,
    summary: String?,
    budgetLabel: String,
    locationLabel: String,
    timingLabel: String,
    posterName: String,
    postedLabel: String,
    modifier: Modifier = Modifier,
    status: TaskResultStatus? = null,
    photoModels: List<Any> = emptyList(),
    distanceLabel: String? = null,
    scopeHighlights: List<String> = emptyList(),
    onClick: (() -> Unit)? = null,
) {
    val displayTitle = title.trim()
    val displayCategory = category.trim()
    val visibleSummary = summary?.trim()?.takeIf(String::isNotEmpty)
    val visibleStatus = status
        ?.takeIf { it != TaskResultStatus.Open }
        ?.let { stringResource(taskStatusString(it)) }
    val locationWithDistance = listOfNotNull(
        locationLabel.trim().takeIf(String::isNotEmpty),
        distanceLabel?.trim()?.takeIf(String::isNotEmpty),
    ).joinToString(" · ")
    val taskMetadata = listOf(
        budgetLabel.trim(),
        locationWithDistance,
        timingLabel.trim(),
    )
    val posterMetadata = listOf(posterName.trim(), postedLabel.trim())
    val visibleHighlights = scopeHighlights
        .map(String::trim)
        .filter(String::isNotEmpty)
        .take(2)
    val openTaskLabel = stringResource(R.string.task_result_view_task, displayTitle)
    val itemModifier = modifier
        .fillMaxWidth()
        .heightIn(min = 48.dp)
        .semantics(mergeDescendants = true) {}
        .then(
            if (onClick == null) {
                Modifier
            } else {
                Modifier.clickable(
                    role = Role.Button,
                    onClickLabel = openTaskLabel,
                    onClick = onClick,
                )
            },
        )

    Card(
        modifier = itemModifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (photoModels.isNotEmpty()) {
                TaskPhotoMedia(photoModels = photoModels)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (displayCategory.isNotEmpty()) {
                    Text(
                        text = displayCategory,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (displayTitle.isNotEmpty()) {
                    Text(
                        text = displayTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (visibleSummary != null) {
                    Text(
                        text = visibleSummary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TaskMetadataFlow(values = taskMetadata)

                if (visibleHighlights.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        visibleHighlights.forEach { highlight ->
                            Text(
                                text = highlight,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }

                if (visibleStatus != null) {
                    Text(
                        text = visibleStatus,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                TaskMetadataFlow(values = posterMetadata)
            }
        }
    }
}

@Composable
private fun TaskPhotoMedia(photoModels: List<Any>) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(photoModels.first())
            .crossfade(false)
            .build(),
    )
    val photoCount = photoModels.size
    val imageUnavailable = painter.state.collectAsState().value is AsyncImagePainter.State.Error
    val mediaDescription = if (imageUnavailable) {
        stringResource(R.string.task_result_photo_unavailable)
    } else {
        pluralStringResource(R.plurals.task_result_photo_count, photoCount, photoCount)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 2f)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .semantics { contentDescription = mediaDescription },
        contentAlignment = Alignment.Center,
    ) {
        if (imageUnavailable) {
            Text(
                text = stringResource(R.string.task_result_photo_unavailable),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (photoCount > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.94f),
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clearAndSetSemantics {},
            ) {
                Text(
                    text = pluralStringResource(
                        R.plurals.task_result_photo_count,
                        photoCount,
                        photoCount,
                    ),
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun TaskMetadataFlow(
    values: List<String>,
    modifier: Modifier = Modifier,
) {
    val visibleValues = values.map(String::trim).filter(String::isNotEmpty)
    if (visibleValues.isEmpty()) return

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        visibleValues.forEach { value ->
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun taskStatusString(status: TaskResultStatus): Int = when (status) {
    TaskResultStatus.Open -> R.string.task_result_status_open
    TaskResultStatus.Applied -> R.string.task_result_status_applied
    TaskResultStatus.Filled -> R.string.task_result_status_filled
    TaskResultStatus.Closed -> R.string.task_result_status_closed
    TaskResultStatus.Expired -> R.string.task_result_status_expired
    TaskResultStatus.Unavailable -> R.string.task_result_status_unavailable
}

@Preview(showBackground = true, widthDp = 360)
@Preview(
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewTaskResultItems() {
    AskITTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TaskResultItem(
                title = "Repair laptop charging port",
                category = "Computer repair",
                summary = "Laptop only charges when the cable is held at an angle.",
                budgetLabel = "₹800–₹1,500",
                locationLabel = "Kallakurichi",
                timingLabel = "Needed Monday",
                posterName = "Arun P.",
                postedLabel = "Posted 2h ago",
                status = TaskResultStatus.Open,
                onClick = {},
            )
            TaskResultItem(
                title = "Translate a two-page document",
                category = "Translation",
                summary = "Translate the supplied English document into Tamil.",
                budgetLabel = "Quote required",
                locationLabel = "Remote",
                timingLabel = "Flexible",
                posterName = "Suresh K.",
                postedLabel = "Posted 40m ago",
                status = TaskResultStatus.Applied,
                onClick = {},
            )
        }
    }
}
