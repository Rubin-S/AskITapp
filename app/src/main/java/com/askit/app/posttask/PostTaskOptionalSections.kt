package com.askit.app.posttask

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import com.askit.designsystem.inputs.AskITTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.relocation.bringIntoViewRequester
import coil3.compose.AsyncImage
import com.askit.app.R

@Composable
internal fun PostTaskBudgetSection(
    viewModel: PostTaskViewModel,
    state: PostTaskFormState,
    validationErrors: Set<PostTaskValidationField>,
    targets: PostTaskFieldTargets,
) {
    Spacer(Modifier.height(12.dp))
    OptionalSectionRow(
        titleRes = R.string.post_task_add_budget,
        expanded = PostTaskOptionalSection.BUDGET in state.expandedOptionalSections,
        onClick = { viewModel.toggleOptionalSection(PostTaskOptionalSection.BUDGET) },
        testTag = "post_task_budget_disclosure",
    )
    if (PostTaskOptionalSection.BUDGET in state.expandedOptionalSections) {
        Column(Modifier.selectableGroup()) {
            PostTaskChoiceRow(
                modifier = Modifier.testTag("post_task_budget_quotes"),
                label = stringResource(R.string.post_task_request_quotes),
                selected = state.budgetMode == PostTaskBudgetMode.REQUEST_QUOTES,
                onClick = { viewModel.selectBudgetMode(PostTaskBudgetMode.REQUEST_QUOTES) },
            )
            PostTaskChoiceRow(
                modifier = Modifier.testTag("post_task_budget_fixed"),
                label = stringResource(R.string.post_task_fixed_amount),
                selected = state.budgetMode == PostTaskBudgetMode.FIXED,
                onClick = { viewModel.selectBudgetMode(PostTaskBudgetMode.FIXED) },
            )
            PostTaskChoiceRow(
                modifier = Modifier.testTag("post_task_budget_range"),
                label = stringResource(R.string.post_task_budget_range),
                selected = state.budgetMode == PostTaskBudgetMode.RANGE,
                onClick = { viewModel.selectBudgetMode(PostTaskBudgetMode.RANGE) },
            )
        }
        when (state.budgetMode) {
            PostTaskBudgetMode.FIXED -> {
                AskITTextField(
                    value = state.fixedBudget,
                    onValueChange = viewModel::updateFixedBudget,
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(targets.fixedBudget)
                        .focusRequester(targets.fixedBudgetFocus)
                        .testTag("post_task_fixed_budget"),
                    label = { Text(stringResource(R.string.post_task_amount)) },
                    supportingText = {
                        if (PostTaskValidationField.FIXED_BUDGET in validationErrors) {
                            Text(stringResource(R.string.post_task_error_fixed_budget))
                        }
                    },
                    isError = PostTaskValidationField.FIXED_BUDGET in validationErrors,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                    ),
                )
            }
            PostTaskBudgetMode.RANGE -> {
                Column(Modifier.bringIntoViewRequester(targets.rangeBudget)) {
                    AskITTextField(
                        value = state.minimumBudget,
                        onValueChange = viewModel::updateMinimumBudget,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(targets.minimumBudgetFocus)
                            .testTag("post_task_minimum_budget"),
                        label = { Text(stringResource(R.string.post_task_minimum_amount)) },
                        isError = PostTaskValidationField.BUDGET_RANGE in validationErrors,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next,
                        ),
                    )
                    AskITTextField(
                        value = state.maximumBudget,
                        onValueChange = viewModel::updateMaximumBudget,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .testTag("post_task_maximum_budget"),
                        label = { Text(stringResource(R.string.post_task_maximum_amount)) },
                        supportingText = {
                            if (PostTaskValidationField.BUDGET_RANGE in validationErrors) {
                                Text(stringResource(R.string.post_task_error_budget_range))
                            }
                        },
                        isError = PostTaskValidationField.BUDGET_RANGE in validationErrors,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done,
                        ),
                    )
                }
            }
            PostTaskBudgetMode.REQUEST_QUOTES, null -> Unit
        }
    }
}

@Composable
internal fun PostTaskPreferencesSection(
    viewModel: PostTaskViewModel,
    state: PostTaskFormState,
    validationErrors: Set<PostTaskValidationField>,
    targets: PostTaskFieldTargets,
) {
    OptionalSectionRow(
        titleRes = R.string.task_provider_preferences,
        expanded = PostTaskOptionalSection.PREFERENCES in state.expandedOptionalSections,
        onClick = { viewModel.toggleOptionalSection(PostTaskOptionalSection.PREFERENCES) },
        testTag = "post_task_preferences_disclosure",
    )
    if (PostTaskOptionalSection.PREFERENCES in state.expandedOptionalSections) {
        PostTaskPreferences(
            viewModel = viewModel,
            state = state,
            ageError = PostTaskValidationField.AGE_PREFERENCE in validationErrors,
            modifier = Modifier.bringIntoViewRequester(targets.preferences),
        )
    }
}

@Composable
internal fun PostTaskPhotosSection(
    state: PostTaskFormState,
    onToggle: () -> Unit,
    onPickPhotos: () -> Unit,
    onRemovePhoto: (String) -> Unit,
) {
    OptionalSectionRow(
        titleRes = R.string.post_task_add_photos,
        expanded = PostTaskOptionalSection.PHOTOS in state.expandedOptionalSections,
        onClick = onToggle,
        testTag = "post_task_photos_disclosure",
    )
    if (PostTaskOptionalSection.PHOTOS in state.expandedOptionalSections) {
        Text(
            text = stringResource(R.string.post_task_photos_helper),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(
            onClick = onPickPhotos,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("post_task_pick_photos"),
        ) {
            Text(
                stringResource(
                    if (state.photoUris.isEmpty()) {
                        R.string.post_task_choose_photos
                    } else {
                        R.string.post_task_add_more_photos
                    },
                ),
            )
        }
        PhotoList(
            uris = state.photoUris,
            onRemovePhoto = onRemovePhoto,
        )
    }
}

@Composable
internal fun PostTaskItemDetailsSection(
    viewModel: PostTaskViewModel,
    state: PostTaskFormState,
) {
    OptionalSectionRow(
        titleRes = R.string.post_task_add_item_details,
        expanded = PostTaskOptionalSection.ITEM_DETAILS in state.expandedOptionalSections,
        onClick = { viewModel.toggleOptionalSection(PostTaskOptionalSection.ITEM_DETAILS) },
        testTag = "post_task_item_details_disclosure",
    )
    if (PostTaskOptionalSection.ITEM_DETAILS in state.expandedOptionalSections) {
        AskITTextField(
            value = state.quantityOrMeasurements,
            onValueChange = viewModel::updateQuantityOrMeasurements,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("post_task_quantity"),
            label = { Text(stringResource(R.string.post_task_quantity_measurements)) },
            minLines = 2,
        )
        AskITTextField(
            value = state.brandOrModel,
            onValueChange = viewModel::updateBrandOrModel,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("post_task_brand_model"),
            label = { Text(stringResource(R.string.post_task_brand_model)) },
            singleLine = true,
        )
        Text(
            text = stringResource(R.string.post_task_materials),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 4.dp),
        )
        Column(Modifier.selectableGroup()) {
            PostTaskChoiceRow(
                label = stringResource(R.string.post_task_materials_requester),
                selected = state.materialsPolicy == PostTaskMaterialsPolicy.REQUESTER_HAS_MATERIALS,
                onClick = {
                    viewModel.selectMaterialsPolicy(
                        PostTaskMaterialsPolicy.REQUESTER_HAS_MATERIALS,
                    )
                },
            )
            PostTaskChoiceRow(
                label = stringResource(R.string.post_task_materials_provider),
                selected = state.materialsPolicy == PostTaskMaterialsPolicy.PROVIDER_BRINGS_MATERIALS,
                onClick = {
                    viewModel.selectMaterialsPolicy(
                        PostTaskMaterialsPolicy.PROVIDER_BRINGS_MATERIALS,
                    )
                },
            )
            PostTaskChoiceRow(
                label = stringResource(R.string.post_task_materials_decide_later),
                selected = state.materialsPolicy == PostTaskMaterialsPolicy.DECIDE_AFTER_INSPECTION,
                onClick = {
                    viewModel.selectMaterialsPolicy(
                        PostTaskMaterialsPolicy.DECIDE_AFTER_INSPECTION,
                    )
                },
            )
        }
    }
}

@Composable
internal fun PostTaskAccessSection(
    state: PostTaskFormState,
    onToggle: () -> Unit,
    onUpdateAccessInstructions: (String) -> Unit,
) {
    if (state.workMode == PostTaskWorkMode.AT_MY_LOCATION) {
        OptionalSectionRow(
            titleRes = R.string.post_task_add_access_instructions,
            expanded = PostTaskOptionalSection.ACCESS_INSTRUCTIONS in state.expandedOptionalSections,
            onClick = onToggle,
            testTag = "post_task_access_disclosure",
        )
        if (PostTaskOptionalSection.ACCESS_INSTRUCTIONS in state.expandedOptionalSections) {
            AskITTextField(
                value = state.accessInstructions,
                onValueChange = onUpdateAccessInstructions,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_task_access_instructions"),
                label = { Text(stringResource(R.string.post_task_access_instructions)) },
                supportingText = { Text(stringResource(R.string.post_task_access_helper)) },
                minLines = 3,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhotoList(
    uris: List<String>,
    onRemovePhoto: (String) -> Unit,
    allowRemove: Boolean = true,
) {
    if (uris.isEmpty()) return
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        uris.forEachIndexed { index, uri ->
            val description = stringResource(R.string.post_task_photo_description, index + 1)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(min = 88.dp, max = 96.dp),
            ) {
                Box(modifier = Modifier.size(88.dp)) {
                    AsyncImage(
                        model = uri,
                        contentDescription = description,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                    if (allowRemove) {
                        IconButton(
                            onClick = { onRemovePhoto(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    shape = CircleShape,
                                ),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_clear),
                                contentDescription = stringResource(
                                    R.string.post_task_remove_photo,
                                    index + 1,
                                ),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}
