package com.askit.app.posttask

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.category.ASKIT_SERVICE_CATEGORIES
import com.askit.app.forms.FormFieldError
import com.askit.app.forms.FormRadioRow
import com.askit.app.task.formatTaskDate
import com.askit.app.task.labelRes
import com.askit.designsystem.inputs.AskITTextField

@Composable
internal fun PostTaskForm(
    modifier: Modifier = Modifier,
    viewModel: PostTaskViewModel,
    state: PostTaskFormState,
    validationErrors: Set<PostTaskValidationField>,
    targets: PostTaskFieldTargets,
    onOpenLocationPicker: () -> Unit,
    onOpenDatePicker: () -> Unit,
    onPickPhotos: () -> Unit,
    onRemovePhoto: (String) -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateDetails: (String) -> Unit,
) {
    val contentModifier = Modifier
        .then(modifier)
        .fillMaxWidth()
        .widthIn(max = 640.dp)

    Column(
        modifier = contentModifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.post_task_intro),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        PostTaskWhatSection(
            state = state,
            validationErrors = validationErrors,
            targets = targets,
            onSelectCategory = viewModel::selectCategory,
            onUpdateCustomCategory = viewModel::updateCustomCategory,
        )
        PostTaskTitleAndDetailsFields(
            state = state,
            validationErrors = validationErrors,
            targets = targets,
            onUpdateTitle = onUpdateTitle,
            onUpdateDetails = onUpdateDetails,
        )
        PostTaskWhereSection(
            state = state,
            validationErrors = validationErrors,
            targets = targets,
            onSelectWorkMode = viewModel::selectWorkMode,
            onOpenLocationPicker = onOpenLocationPicker,
            onUpdatePrivateAddress = viewModel::updatePrivateAddressOrLandmark,
        )
        PostTaskWhenSection(
            state = state,
            validationErrors = validationErrors,
            targets = targets,
            onSelectTiming = viewModel::selectTiming,
            onOpenDatePicker = onOpenDatePicker,
        )
        PostTaskBudgetSection(viewModel, state, validationErrors, targets)
        PostTaskPreferencesSection(viewModel, state, validationErrors, targets)
        PostTaskPhotosSection(
            state = state,
            onToggle = { viewModel.toggleOptionalSection(PostTaskOptionalSection.PHOTOS) },
            onPickPhotos = onPickPhotos,
            onRemovePhoto = onRemovePhoto,
        )
        PostTaskItemDetailsSection(viewModel, state)
        PostTaskAccessSection(
            state = state,
            onToggle = { viewModel.toggleOptionalSection(PostTaskOptionalSection.ACCESS_INSTRUCTIONS) },
            onUpdateAccessInstructions = viewModel::updateAccessInstructions,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostTaskWhatSection(
    state: PostTaskFormState,
    validationErrors: Set<PostTaskValidationField>,
    targets: PostTaskFieldTargets,
    onSelectCategory: (String) -> Unit,
    onUpdateCustomCategory: (String) -> Unit,
) {
    Spacer(Modifier.height(12.dp))
    PostTaskSectionHeading(R.string.post_task_what_you_need)

    var categoryMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val selectedCategoryLabel = when {
        state.categoryId == POST_TASK_OTHER_CATEGORY_ID ->
            stringResource(R.string.post_task_other_category)

        else ->
            ASKIT_SERVICE_CATEGORIES
                .firstOrNull { it.id == state.categoryId }
                ?.let { category -> stringResource(category.labelRes) }
                ?: stringResource(R.string.post_task_select_category)
    }
    ExposedDropdownMenuBox(
        expanded = categoryMenuExpanded,
        onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded },
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(targets.category)
            .testTag("post_task_category_options"),
    ) {
        AskITTextField(
            value = selectedCategoryLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.post_task_category)) },
            supportingText = {
                if (
                    PostTaskValidationField.CATEGORY in validationErrors &&
                    state.categoryId != POST_TASK_OTHER_CATEGORY_ID
                ) {
                    Text(stringResource(R.string.post_task_error_category))
                } else {
                    Text(stringResource(R.string.post_task_category_helper))
                }
            },
            isError = PostTaskValidationField.CATEGORY in validationErrors &&
                state.categoryId != POST_TASK_OTHER_CATEGORY_ID,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .testTag("post_task_category_dropdown"),
        )
        ExposedDropdownMenu(
            expanded = categoryMenuExpanded,
            onDismissRequest = { categoryMenuExpanded = false },
            modifier = Modifier.testTag("post_task_category_menu"),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            ASKIT_SERVICE_CATEGORIES.forEach { category ->
                DropdownMenuItem(
                    text = { Text(stringResource(category.labelRes)) },
                    onClick = {
                        onSelectCategory(category.id)
                        categoryMenuExpanded = false
                    },
                    modifier = Modifier.testTag("post_task_category_${category.id}"),
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.post_task_other_category)) },
                onClick = {
                    onSelectCategory(POST_TASK_OTHER_CATEGORY_ID)
                    categoryMenuExpanded = false
                },
                modifier = Modifier.testTag("post_task_category_other"),
            )
        }
    }
    if (state.categoryId == POST_TASK_OTHER_CATEGORY_ID) {
        AskITTextField(
            value = state.customCategory,
            onValueChange = onUpdateCustomCategory,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("post_task_custom_category"),
            label = { Text(stringResource(R.string.post_task_other_category_name)) },
            supportingText = {
                if (PostTaskValidationField.CATEGORY in validationErrors) {
                    Text(stringResource(R.string.post_task_error_other_category))
                } else {
                    Text(stringResource(R.string.post_task_other_category_helper))
                }
            },
            isError = PostTaskValidationField.CATEGORY in validationErrors,
            singleLine = true,
        )
    }
}

@Composable
private fun PostTaskTitleAndDetailsFields(
    state: PostTaskFormState,
    validationErrors: Set<PostTaskValidationField>,
    targets: PostTaskFieldTargets,
    onUpdateTitle: (String) -> Unit,
    onUpdateDetails: (String) -> Unit,
) {
    AskITTextField(
        value = state.title,
        onValueChange = onUpdateTitle,
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(targets.title)
            .focusRequester(targets.titleFocus)
            .testTag("post_task_title"),
        label = { Text(stringResource(R.string.post_task_task_title)) },
        supportingText = {
            if (PostTaskValidationField.TITLE in validationErrors) {
                Text(stringResource(R.string.post_task_error_title))
            } else {
                Text(stringResource(R.string.post_task_title_example))
            }
        },
        isError = PostTaskValidationField.TITLE in validationErrors,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )

    AskITTextField(
        value = state.details,
        onValueChange = onUpdateDetails,
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(targets.details)
            .focusRequester(targets.detailsFocus)
            .testTag("post_task_details"),
        label = { Text(stringResource(R.string.post_task_details)) },
        supportingText = {
            if (PostTaskValidationField.DETAILS in validationErrors) {
                Text(stringResource(R.string.post_task_error_details))
            } else {
                Text(stringResource(R.string.post_task_details_helper))
            }
        },
        isError = PostTaskValidationField.DETAILS in validationErrors,
        minLines = 4,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
    )
}

@Composable
private fun PostTaskWhereSection(
    state: PostTaskFormState,
    validationErrors: Set<PostTaskValidationField>,
    targets: PostTaskFieldTargets,
    onSelectWorkMode: (PostTaskWorkMode) -> Unit,
    onOpenLocationPicker: () -> Unit,
    onUpdatePrivateAddress: (String) -> Unit,
) {
    Spacer(Modifier.height(12.dp))
    PostTaskSectionHeading(R.string.post_task_where)
    Column(
        modifier = Modifier
            .bringIntoViewRequester(targets.workMode)
            .selectableGroup()
            .testTag("post_task_work_mode_options"),
    ) {
        FormRadioRow(
            modifier = Modifier.testTag("post_task_work_mode_at_my_location"),
            label = stringResource(R.string.post_task_at_my_location),
            selected = state.workMode == PostTaskWorkMode.AT_MY_LOCATION,
            onClick = { onSelectWorkMode(PostTaskWorkMode.AT_MY_LOCATION) },
        )
        FormRadioRow(
            modifier = Modifier.testTag("post_task_work_mode_provider"),
            label = stringResource(R.string.post_task_at_provider_location),
            selected = state.workMode == PostTaskWorkMode.AT_PROVIDER_LOCATION,
            onClick = { onSelectWorkMode(PostTaskWorkMode.AT_PROVIDER_LOCATION) },
        )
        FormRadioRow(
            modifier = Modifier.testTag("post_task_work_mode_remote"),
            label = stringResource(R.string.post_task_remote),
            selected = state.workMode == PostTaskWorkMode.REMOTE,
            onClick = { onSelectWorkMode(PostTaskWorkMode.REMOTE) },
        )
    }
    if (PostTaskValidationField.WORK_MODE in validationErrors) {
        FormFieldError(R.string.post_task_error_work_mode)
    }

    if (state.workMode == PostTaskWorkMode.AT_MY_LOCATION) {
        Column(modifier = Modifier.bringIntoViewRequester(targets.location)) {
            OutlinedButton(
                onClick = onOpenLocationPicker,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .testTag("post_task_choose_area"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = state.publicAreaLabel.ifBlank {
                            stringResource(R.string.post_task_choose_area)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = if (state.publicAreaLabel.isBlank()) {
                            stringResource(R.string.post_task_location_selection_helper)
                        } else {
                            stringResource(R.string.post_task_change_area)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (PostTaskValidationField.LOCATION in validationErrors) {
                FormFieldError(R.string.post_task_error_location)
            }
            Text(
                text = stringResource(R.string.post_task_location_privacy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("post_task_location_privacy"),
            )
            AskITTextField(
                value = state.privateAddressOrLandmark,
                onValueChange = onUpdatePrivateAddress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .testTag("post_task_private_address"),
                label = { Text(stringResource(R.string.post_task_address_optional)) },
                minLines = 2,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
        }
    }
}

@Composable
private fun PostTaskWhenSection(
    state: PostTaskFormState,
    validationErrors: Set<PostTaskValidationField>,
    targets: PostTaskFieldTargets,
    onSelectTiming: (PostTaskTimingMode) -> Unit,
    onOpenDatePicker: () -> Unit,
) {
    Spacer(Modifier.height(12.dp))
    PostTaskSectionHeading(R.string.post_task_when)
    Column(
        modifier = Modifier
            .bringIntoViewRequester(targets.timing)
            .selectableGroup()
            .testTag("post_task_timing_options"),
    ) {
        FormRadioRow(
            modifier = Modifier.testTag("post_task_timing_asap"),
            label = stringResource(R.string.post_task_asap),
            selected = state.timingMode == PostTaskTimingMode.ASAP,
            onClick = { onSelectTiming(PostTaskTimingMode.ASAP) },
        )
        FormRadioRow(
            modifier = Modifier.testTag("post_task_timing_date"),
            label = stringResource(R.string.post_task_on_date),
            selected = state.timingMode == PostTaskTimingMode.DATE,
            onClick = { onSelectTiming(PostTaskTimingMode.DATE) },
        )
        FormRadioRow(
            modifier = Modifier.testTag("post_task_timing_flexible"),
            label = stringResource(R.string.post_task_flexible),
            selected = state.timingMode == PostTaskTimingMode.FLEXIBLE,
            onClick = { onSelectTiming(PostTaskTimingMode.FLEXIBLE) },
        )
    }
    if (PostTaskValidationField.TIMING in validationErrors) {
        FormFieldError(R.string.post_task_error_timing)
    }
    if (state.timingMode == PostTaskTimingMode.DATE) {
        OutlinedButton(
            onClick = onOpenDatePicker,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .bringIntoViewRequester(targets.date)
                .testTag("post_task_date_picker"),
        ) {
            Text(
                text = state.selectedDateMillis?.let { formatTaskDate(it) }
                    ?.let { date -> stringResource(R.string.post_task_selected_date, date) }
                    ?: stringResource(R.string.post_task_select_date),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (PostTaskValidationField.DATE in validationErrors) {
            FormFieldError(R.string.post_task_error_date)
        }
    }
}
