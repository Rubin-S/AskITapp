package com.askit.app.home.components

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.askit.app.R
import com.askit.app.home.model.FeedItem
import com.askit.app.home.model.TaskPreview
import com.askit.app.task.presentation
import com.askit.designsystem.components.AskITCarouselSection
import com.askit.designsystem.tasks.TaskResultItem

@Composable
fun HomeTasksSection(section: FeedItem.TaskSection, modifier: Modifier = Modifier, onTaskClick: (TaskPreview) -> Unit = {}) {
    AskITCarouselSection(
        title = stringResource(R.string.home_tasks_title),
        items = section.tasks,
        key = { it.id },
        modifier = modifier,
    ) { task, itemModifier ->
        HomeTaskCard(task = task, onClick = { onTaskClick(task) }, modifier = itemModifier)
    }
}

@Composable
private fun HomeTaskCard(task: TaskPreview, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
        modifier = modifier,
        onClick = onClick,
    )
}
