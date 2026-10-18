package com.askit.app.posttask

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import com.askit.designsystem.inputs.AskITTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.task.TaskGenderPreference
import com.askit.app.task.TaskLanguage
import com.askit.app.task.labelRes

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PostTaskPreferences(
    viewModel: PostTaskViewModel,
    state: PostTaskFormState,
    ageError: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.task_preference_age_hint), style = MaterialTheme.typography.bodySmall)
        AskITTextField(
            value = state.minimumAge,
            onValueChange = viewModel::updateMinimumAge,
            label = { Text(stringResource(R.string.task_preference_minimum_age)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = ageError,
            modifier = Modifier.fillMaxWidth().testTag("post_task_minimum_age"),
        )
        AskITTextField(
            value = state.maximumAge,
            onValueChange = viewModel::updateMaximumAge,
            label = { Text(stringResource(R.string.task_preference_maximum_age)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = ageError,
            modifier = Modifier.fillMaxWidth().testTag("post_task_maximum_age"),
        )
        if (ageError) Text(stringResource(R.string.task_preference_age_error), color = MaterialTheme.colorScheme.error)
        Text(stringResource(R.string.task_preference_gender), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskGenderPreference.entries.forEach { gender ->
                FilterChip(
                    selected = state.genderPreference == gender,
                    onClick = { viewModel.selectGender(gender) },
                    label = { Text(stringResource(gender.labelRes)) },
                    modifier = Modifier.testTag("post_task_gender_${gender.name}"),
                )
            }
        }
        Text(stringResource(R.string.task_preference_languages), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.preferredLanguages.isEmpty(),
                onClick = viewModel::clearLanguages,
                label = { Text(stringResource(R.string.task_preference_none)) },
            )
            TaskLanguage.entries.forEach { language ->
                FilterChip(
                    selected = language in state.preferredLanguages,
                    onClick = { viewModel.toggleLanguage(language) },
                    label = { Text(stringResource(language.labelRes)) },
                    modifier = Modifier.testTag("post_task_language_${language.name}"),
                )
            }
        }
    }
}
