package com.askit.app.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.designsystem.tasks.TaskPhotoMedia
import androidx.compose.ui.layout.ContentScale

@Composable
fun TaskPreferencesSummary(preferences: TaskProviderPreferences) {
    val age = when {
        preferences.minimumAge != null && preferences.maximumAge != null ->
            stringResource(R.string.task_preference_age_range, preferences.minimumAge, preferences.maximumAge)
        preferences.minimumAge != null -> stringResource(R.string.task_preference_age_minimum, preferences.minimumAge)
        preferences.maximumAge != null -> stringResource(R.string.task_preference_age_maximum, preferences.maximumAge)
        else -> stringResource(R.string.task_preference_none)
    }
    val languages = TaskLanguage.entries.filter { it in preferences.languages }
        .map { stringResource(it.labelRes) }.joinToString(", ")
        .ifEmpty { stringResource(R.string.task_preference_none) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.task_provider_preferences), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.task_preference_age_summary, age), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.task_preference_gender_summary, stringResource(preferences.gender.labelRes)), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.task_preference_language_summary, languages), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TaskPhotoGallery(photos: List<String>) {
    if (photos.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { photos.size })
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
            TaskPhotoMedia(
                photoModels = listOf(photos[page]),
                contentScale = ContentScale.Fit,
                showCount = false,
                description = stringResource(R.string.task_photo_position, page + 1, photos.size),
            )
        }
        Text(
            text = stringResource(R.string.task_photo_position, pagerState.currentPage + 1, photos.size),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.End),
        )
    }
}
