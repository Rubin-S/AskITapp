package com.askit.designsystem.tasks

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    status: TaskResultStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusText = stringResource(taskStatusString(status))
    val categoryStatus = joinMetadata(category, statusText)
    val taskMetadata = joinMetadata(budgetLabel, locationLabel, timingLabel)
    val posterMetadata = joinMetadata(posterName, postedLabel)
    val visibleSummary = summary?.takeIf(String::isNotBlank)
    val openTaskLabel = stringResource(R.string.task_result_view_task, title)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = openTaskLabel,
                onClick = onClick,
            )
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = categoryStatus,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (visibleSummary != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = visibleSummary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (taskMetadata.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = taskMetadata,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (posterMetadata.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = posterMetadata,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun joinMetadata(vararg values: String): String =
    values
        .filter(String::isNotBlank)
        .joinToString(" · ")

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
        Column(modifier = Modifier.fillMaxWidth()) {
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
            HorizontalDivider()
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
            HorizontalDivider()
            TaskResultItem(
                title = "Repair washing machine",
                category = "Appliance repair",
                summary = "Machine stops during the spin cycle.",
                budgetLabel = "₹700–₹1,000",
                locationLabel = "Kallakurichi",
                timingLabel = "Needed yesterday",
                posterName = "Lakshmi R.",
                postedLabel = "Posted yesterday",
                status = TaskResultStatus.Filled,
                onClick = {},
            )
            HorizontalDivider()
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                TaskResultItem(
                    title = "A very long task title that should remain readable at narrow widths",
                    category = "A long category label for a task with detailed scope",
                    summary = "A long task summary that can grow to two lines without clipping important context.",
                    budgetLabel = "Quote required",
                    locationLabel = "A long locality label for a narrow screen",
                    timingLabel = "Needed next Monday afternoon",
                    posterName = "A privacy-safe poster display name",
                    postedLabel = "Posted yesterday",
                    status = TaskResultStatus.Closed,
                    onClick = {},
                )
            }
        }
    }
}
