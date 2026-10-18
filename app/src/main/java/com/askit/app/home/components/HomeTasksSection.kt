package com.askit.app.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.askit.app.task.presentation
import com.askit.designsystem.tasks.TaskResultItem
import com.askit.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.app.home.model.FeedItem
import com.askit.app.home.model.TaskPreview

@Composable
fun HomeTasksSection(
    section: FeedItem.TaskSection,
    modifier: Modifier = Modifier,
    onTaskClick: (TaskPreview) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.home_tasks_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(
                items = section.tasks,
                key = { it.id },
            ) { task ->
                HomeTaskCard(
                    task = task,
                    onClick = { onTaskClick(task) },
                )
            }
        }
    }
}

@Composable
private fun HomeTaskCard(
    task: TaskPreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val display = task.sourceTask?.presentation(LocalContext.current)
    TaskResultItem(
        title = task.title,
        category = display?.category ?: task.category,
        summary = task.sourceTask?.description,
        budgetLabel = display?.budget ?: task.budgetLabel,
        locationLabel = display?.location ?: task.locationLabel,
        timingLabel = display?.timing ?: task.urgencyLabel.orEmpty(),
        posterName = task.posterName,
        postedLabel = display?.posted.orEmpty(),
        photoModels = task.sourceTask?.photos.orEmpty(),
        scopeHighlights = display?.requirements.orEmpty(),
        modifier = modifier.width(280.dp),
        onClick = onClick,
    )
}
