package com.askit.app.posttask

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.app.category.ASKIT_SERVICE_CATEGORIES
import com.askit.app.explore.ExploreLocationSource
import com.askit.app.explore.ExploreSearchArea
import com.askit.app.explore.SearchAreaScreen
import com.askit.app.media.persistPhotoPickerReadAccess
import com.askit.designsystem.R as DsR
import com.askit.designsystem.tasks.TaskResultItem
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostTaskRoute(
    viewModel: PostTaskViewModel,
    onBack: () -> Unit,
    onCompleteDraft: (PostTaskDraft) -> Unit = {},
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    var showLocationPicker by rememberSaveable { mutableStateOf(false) }
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }

    fun requestBack() {
        if (viewModel.isDirty) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    BackHandler {
        if (showDiscardDialog) {
            showDiscardDialog = false
        } else if (showLocationPicker) {
            showLocationPicker = false
        } else {
            requestBack()
        }
    }

    if (showLocationPicker) {
        val confirmedArea = ExploreSearchArea(
            placeId = state.placeId,
            displayName = state.publicAreaLabel.ifBlank {
                stringResource(R.string.post_task_location_not_selected)
            },
            supportingText = null,
            latitude = state.latitude,
            longitude = state.longitude,
            radiusKm = 10,
            source = ExploreLocationSource.SAVED,
        )
        SearchAreaScreen(
            confirmedArea = confirmedArea,
            onBack = { showLocationPicker = false },
            titleRes = R.string.post_task_choose_area_title,
            showFilterControls = false,
            onApply = { area, _ ->
                if (area.isUsable) {
                    viewModel.selectLocation(
                        placeId = area.placeId,
                        publicAreaLabel = area.displayName,
                        latitude = requireNotNull(area.latitude),
                        longitude = requireNotNull(area.longitude),
                    )
                }
                showLocationPicker = false
            },
        )
    } else {
        PostTaskScreen(
            viewModel = viewModel,
            state = state,
            validationErrors = viewModel.validationErrors(),
            onBack = ::requestBack,
            onOpenLocationPicker = { showLocationPicker = true },
            onCompleteDraft = onCompleteDraft,
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.post_task_discard_title)) },
            text = { Text(stringResource(R.string.post_task_discard_message)) },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.post_task_keep_editing))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onBack()
                    },
                ) {
                    Text(stringResource(R.string.post_task_discard))
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PostTaskScreen(
    viewModel: PostTaskViewModel,
    state: PostTaskFormState,
    validationErrors: Set<PostTaskValidationField>,
    onBack: () -> Unit,
    onOpenLocationPicker: () -> Unit,
    onCompleteDraft: (PostTaskDraft) -> Unit,
) {
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val contentResolver = LocalContext.current.contentResolver
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val categoryBringIntoView = remember { BringIntoViewRequester() }
    val titleBringIntoView = remember { BringIntoViewRequester() }
    val detailsBringIntoView = remember { BringIntoViewRequester() }
    val workModeBringIntoView = remember { BringIntoViewRequester() }
    val locationBringIntoView = remember { BringIntoViewRequester() }
    val timingBringIntoView = remember { BringIntoViewRequester() }
    val dateBringIntoView = remember { BringIntoViewRequester() }
    val fixedBudgetBringIntoView = remember { BringIntoViewRequester() }
    val rangeBudgetBringIntoView = remember { BringIntoViewRequester() }
    val titleFocusRequester = remember { FocusRequester() }
    val detailsFocusRequester = remember { FocusRequester() }
    val fixedBudgetFocusRequester = remember { FocusRequester() }
    val minimumBudgetFocusRequester = remember { FocusRequester() }

    LaunchedEffect(state.reviewAttempt) {
        if (state.screenMode == PostTaskScreenMode.FORM && validationErrors.isNotEmpty()) {
            when (firstInvalidField(validationErrors)) {
                PostTaskValidationField.CATEGORY -> categoryBringIntoView.bringIntoView()
                PostTaskValidationField.TITLE -> {
                    titleBringIntoView.bringIntoView()
                    titleFocusRequester.requestFocus()
                }
                PostTaskValidationField.DETAILS -> {
                    detailsBringIntoView.bringIntoView()
                    detailsFocusRequester.requestFocus()
                }
                PostTaskValidationField.WORK_MODE -> workModeBringIntoView.bringIntoView()
                PostTaskValidationField.LOCATION -> locationBringIntoView.bringIntoView()
                PostTaskValidationField.TIMING -> timingBringIntoView.bringIntoView()
                PostTaskValidationField.DATE -> dateBringIntoView.bringIntoView()
                PostTaskValidationField.FIXED_BUDGET -> {
                    fixedBudgetBringIntoView.bringIntoView()
                    fixedBudgetFocusRequester.requestFocus()
                }
                PostTaskValidationField.BUDGET_RANGE -> {
                    rangeBudgetBringIntoView.bringIntoView()
                    minimumBudgetFocusRequester.requestFocus()
                }
            }
        }
    }

    LaunchedEffect(state.screenMode) {
        if (state.screenMode == PostTaskScreenMode.REVIEW) {
            listState.scrollToItem(0)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
    ) { uris ->
        if (uris.isNotEmpty()) {
                persistPhotoPickerReadAccess(contentResolver, uris)
            viewModel.setPhotoUris(viewModel.formState.value.photoUris + uris.map(Uri::toString))
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.post_task_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.explore_back),
                        )
                    }
                },
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.navigationBarsPadding()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Button(
                        onClick = {
                            if (state.screenMode == PostTaskScreenMode.REVIEW) {
                                viewModel.buildValidatedDraft()?.let(onCompleteDraft)
                            } else {
                                keyboardController?.hide()
                                viewModel.review()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .heightIn(min = 52.dp)
                            .testTag(
                                if (state.screenMode == PostTaskScreenMode.REVIEW) {
                                    "post_task_complete"
                                } else {
                                    "post_task_review"
                                },
                            ),
                    ) {
                        Text(
                            stringResource(
                                if (state.screenMode == PostTaskScreenMode.REVIEW) {
                                    R.string.post_task_complete_draft
                                } else {
                                    R.string.post_task_review_task
                                },
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    if (state.screenMode == PostTaskScreenMode.REVIEW) {
                        PostTaskReview(
                            state = state,
                            onEdit = viewModel::edit,
                        )
                    } else {
                        PostTaskForm(
                            viewModel = viewModel,
                            state = state,
                            validationErrors = validationErrors,
                            categoryBringIntoView = categoryBringIntoView,
                            titleBringIntoView = titleBringIntoView,
                            detailsBringIntoView = detailsBringIntoView,
                            workModeBringIntoView = workModeBringIntoView,
                            locationBringIntoView = locationBringIntoView,
                            timingBringIntoView = timingBringIntoView,
                            dateBringIntoView = dateBringIntoView,
                            fixedBudgetBringIntoView = fixedBudgetBringIntoView,
                            rangeBudgetBringIntoView = rangeBudgetBringIntoView,
                            titleFocusRequester = titleFocusRequester,
                            detailsFocusRequester = detailsFocusRequester,
                            fixedBudgetFocusRequester = fixedBudgetFocusRequester,
                            minimumBudgetFocusRequester = minimumBudgetFocusRequester,
                            onOpenLocationPicker = onOpenLocationPicker,
                            onOpenDatePicker = { showDatePicker = true },
                            onPickPhotos = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                            onRemovePhoto = viewModel::removePhoto,
                            onUpdateTitle = viewModel::updateTitle,
                            onUpdateDetails = viewModel::updateDetails,
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDateMillis,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    enabled = datePickerState.selectedDateMillis != null,
                    onClick = {
                        viewModel.selectDate(datePickerState.selectedDateMillis)
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.post_task_done))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.post_task_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
private fun PostTaskForm(
    modifier: Modifier = Modifier,
    viewModel: PostTaskViewModel,
    state: PostTaskFormState,
    validationErrors: Set<PostTaskValidationField>,
    categoryBringIntoView: BringIntoViewRequester,
    titleBringIntoView: BringIntoViewRequester,
    detailsBringIntoView: BringIntoViewRequester,
    workModeBringIntoView: BringIntoViewRequester,
    locationBringIntoView: BringIntoViewRequester,
    timingBringIntoView: BringIntoViewRequester,
    dateBringIntoView: BringIntoViewRequester,
    fixedBudgetBringIntoView: BringIntoViewRequester,
    rangeBudgetBringIntoView: BringIntoViewRequester,
    titleFocusRequester: FocusRequester,
    detailsFocusRequester: FocusRequester,
    fixedBudgetFocusRequester: FocusRequester,
    minimumBudgetFocusRequester: FocusRequester,
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

        Spacer(Modifier.height(12.dp))
        PostTaskSectionHeading(R.string.post_task_what_you_need)

        var categoryMenuExpanded by rememberSaveable { mutableStateOf(false) }
        val selectedCategoryLabel = when {
            state.categoryId == POST_TASK_OTHER_CATEGORY_ID ->
                stringResource(R.string.post_task_other_category)
            else -> ASKIT_SERVICE_CATEGORIES
                .firstOrNull { it.id == state.categoryId }
                ?.let { category -> stringResource(category.labelRes) }
                ?: stringResource(R.string.post_task_select_category)
        }
        ExposedDropdownMenuBox(
            expanded = categoryMenuExpanded,
            onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded },
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(categoryBringIntoView)
                .testTag("post_task_category_options"),
        ) {
            OutlinedTextField(
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
                            viewModel.selectCategory(category.id)
                            categoryMenuExpanded = false
                        },
                        modifier = Modifier.testTag("post_task_category_${category.id}"),
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.post_task_other_category)) },
                    onClick = {
                        viewModel.selectCategory(POST_TASK_OTHER_CATEGORY_ID)
                        categoryMenuExpanded = false
                    },
                    modifier = Modifier.testTag("post_task_category_other"),
                )
            }
        }
        if (state.categoryId == POST_TASK_OTHER_CATEGORY_ID) {
            OutlinedTextField(
                value = state.customCategory,
                onValueChange = viewModel::updateCustomCategory,
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

        OutlinedTextField(
            value = state.title,
            onValueChange = onUpdateTitle,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(titleBringIntoView)
                .focusRequester(titleFocusRequester)
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

        OutlinedTextField(
            value = state.details,
            onValueChange = onUpdateDetails,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(detailsBringIntoView)
                .focusRequester(detailsFocusRequester)
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

        Spacer(Modifier.height(12.dp))
        PostTaskSectionHeading(R.string.post_task_where)
        Column(
            modifier = Modifier
                .bringIntoViewRequester(workModeBringIntoView)
                .selectableGroup()
                .testTag("post_task_work_mode_options"),
        ) {
            PostTaskChoiceRow(
                modifier = Modifier.testTag("post_task_work_mode_at_my_location"),
                label = stringResource(R.string.post_task_at_my_location),
                selected = state.workMode == PostTaskWorkMode.AT_MY_LOCATION,
                onClick = { viewModel.selectWorkMode(PostTaskWorkMode.AT_MY_LOCATION) },
            )
            PostTaskChoiceRow(
                modifier = Modifier.testTag("post_task_work_mode_provider"),
                label = stringResource(R.string.post_task_at_provider_location),
                selected = state.workMode == PostTaskWorkMode.AT_PROVIDER_LOCATION,
                onClick = { viewModel.selectWorkMode(PostTaskWorkMode.AT_PROVIDER_LOCATION) },
            )
            PostTaskChoiceRow(
                modifier = Modifier.testTag("post_task_work_mode_remote"),
                label = stringResource(R.string.post_task_remote),
                selected = state.workMode == PostTaskWorkMode.REMOTE,
                onClick = { viewModel.selectWorkMode(PostTaskWorkMode.REMOTE) },
            )
        }
        if (PostTaskValidationField.WORK_MODE in validationErrors) {
            PostTaskError(R.string.post_task_error_work_mode)
        }

        if (state.workMode == PostTaskWorkMode.AT_MY_LOCATION) {
            Column(modifier = Modifier.bringIntoViewRequester(locationBringIntoView)) {
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
                    PostTaskError(R.string.post_task_error_location)
                }
                Text(
                    text = stringResource(R.string.post_task_location_privacy),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .testTag("post_task_location_privacy"),
                )
                OutlinedTextField(
                    value = state.privateAddressOrLandmark,
                    onValueChange = viewModel::updatePrivateAddressOrLandmark,
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

        Spacer(Modifier.height(12.dp))
        PostTaskSectionHeading(R.string.post_task_when)
        Column(
            modifier = Modifier
                .bringIntoViewRequester(timingBringIntoView)
                .selectableGroup()
                .testTag("post_task_timing_options"),
        ) {
            PostTaskChoiceRow(
                modifier = Modifier.testTag("post_task_timing_asap"),
                label = stringResource(R.string.post_task_asap),
                selected = state.timingMode == PostTaskTimingMode.ASAP,
                onClick = { viewModel.selectTiming(PostTaskTimingMode.ASAP) },
            )
            PostTaskChoiceRow(
                modifier = Modifier.testTag("post_task_timing_date"),
                label = stringResource(R.string.post_task_on_date),
                selected = state.timingMode == PostTaskTimingMode.DATE,
                onClick = { viewModel.selectTiming(PostTaskTimingMode.DATE) },
            )
            PostTaskChoiceRow(
                modifier = Modifier.testTag("post_task_timing_flexible"),
                label = stringResource(R.string.post_task_flexible),
                selected = state.timingMode == PostTaskTimingMode.FLEXIBLE,
                onClick = { viewModel.selectTiming(PostTaskTimingMode.FLEXIBLE) },
            )
        }
        if (PostTaskValidationField.TIMING in validationErrors) {
            PostTaskError(R.string.post_task_error_timing)
        }
        if (state.timingMode == PostTaskTimingMode.DATE) {
            OutlinedButton(
                onClick = onOpenDatePicker,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .bringIntoViewRequester(dateBringIntoView)
                    .testTag("post_task_date_picker"),
            ) {
                Text(
                    text = state.selectedDateMillis?.let(::formatDate)
                        ?.let { date -> stringResource(R.string.post_task_selected_date, date) }
                        ?: stringResource(R.string.post_task_select_date),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (PostTaskValidationField.DATE in validationErrors) {
                PostTaskError(R.string.post_task_error_date)
            }
        }

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
                    OutlinedTextField(
                        value = state.fixedBudget,
                        onValueChange = viewModel::updateFixedBudget,
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(fixedBudgetBringIntoView)
                            .focusRequester(fixedBudgetFocusRequester)
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
                    Column(Modifier.bringIntoViewRequester(rangeBudgetBringIntoView)) {
                        OutlinedTextField(
                            value = state.minimumBudget,
                            onValueChange = viewModel::updateMinimumBudget,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(minimumBudgetFocusRequester)
                                .testTag("post_task_minimum_budget"),
                            label = { Text(stringResource(R.string.post_task_minimum_amount)) },
                            isError = PostTaskValidationField.BUDGET_RANGE in validationErrors,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next,
                            ),
                        )
                        OutlinedTextField(
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

        OptionalSectionRow(
            titleRes = R.string.post_task_add_photos,
            expanded = PostTaskOptionalSection.PHOTOS in state.expandedOptionalSections,
            onClick = { viewModel.toggleOptionalSection(PostTaskOptionalSection.PHOTOS) },
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

        OptionalSectionRow(
            titleRes = R.string.post_task_add_item_details,
            expanded = PostTaskOptionalSection.ITEM_DETAILS in state.expandedOptionalSections,
            onClick = { viewModel.toggleOptionalSection(PostTaskOptionalSection.ITEM_DETAILS) },
            testTag = "post_task_item_details_disclosure",
        )
        if (PostTaskOptionalSection.ITEM_DETAILS in state.expandedOptionalSections) {
            OutlinedTextField(
                value = state.quantityOrMeasurements,
                onValueChange = viewModel::updateQuantityOrMeasurements,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_task_quantity"),
                label = { Text(stringResource(R.string.post_task_quantity_measurements)) },
                minLines = 2,
            )
            OutlinedTextField(
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

        if (state.workMode == PostTaskWorkMode.AT_MY_LOCATION) {
            OptionalSectionRow(
                titleRes = R.string.post_task_add_access_instructions,
                expanded = PostTaskOptionalSection.ACCESS_INSTRUCTIONS in state.expandedOptionalSections,
                onClick = {
                    viewModel.toggleOptionalSection(PostTaskOptionalSection.ACCESS_INSTRUCTIONS)
                },
                testTag = "post_task_access_disclosure",
            )
            if (PostTaskOptionalSection.ACCESS_INSTRUCTIONS in state.expandedOptionalSections) {
                OutlinedTextField(
                    value = state.accessInstructions,
                    onValueChange = viewModel::updateAccessInstructions,
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

@Composable
private fun PostTaskReview(
    modifier: Modifier = Modifier,
    state: PostTaskFormState,
    onEdit: () -> Unit,
) {
    val category = ASKIT_SERVICE_CATEGORIES.firstOrNull { it.id == state.categoryId }
    val categoryLabel = when {
        state.categoryId == POST_TASK_OTHER_CATEGORY_ID -> state.customCategory.trim()
        category != null -> stringResource(category.labelRes)
        else -> state.categoryId.orEmpty()
    }
    val workModeLabel = when (state.workMode) {
        PostTaskWorkMode.AT_MY_LOCATION -> stringResource(R.string.post_task_at_my_location)
        PostTaskWorkMode.AT_PROVIDER_LOCATION -> stringResource(R.string.post_task_at_provider_location)
        PostTaskWorkMode.REMOTE -> stringResource(R.string.post_task_remote)
        null -> ""
    }
    val timingLabel = when (state.timingMode) {
        PostTaskTimingMode.ASAP -> stringResource(R.string.post_task_asap)
        PostTaskTimingMode.DATE -> if (state.selectedDateMillis != null) {
            formatDate(state.selectedDateMillis)
        } else {
            stringResource(R.string.post_task_on_date)
        }
        PostTaskTimingMode.FLEXIBLE -> stringResource(R.string.post_task_flexible)
        null -> ""
    }
    val privateAddress = state.privateAddressOrLandmark.trim()
    val accessInstructions = state.accessInstructions.trim()
    val previewLocationLabel = when (state.workMode) {
        PostTaskWorkMode.AT_MY_LOCATION -> state.publicAreaLabel.trim().ifEmpty { "Local Area" }
        PostTaskWorkMode.AT_PROVIDER_LOCATION ->
            stringResource(R.string.post_task_at_provider_location)
        PostTaskWorkMode.REMOTE -> stringResource(R.string.post_task_remote)
        null -> "Local Area"
    }
    val previewTimingLabel = when (state.timingMode) {
        PostTaskTimingMode.ASAP -> stringResource(R.string.post_task_asap)
        PostTaskTimingMode.DATE -> state.selectedDateMillis?.let(::formatDate).orEmpty()
        PostTaskTimingMode.FLEXIBLE -> stringResource(R.string.post_task_flexible)
        null -> ""
    }
    val previewBudgetLabel = when (state.budgetMode) {
        PostTaskBudgetMode.REQUEST_QUOTES -> stringResource(R.string.post_task_request_quotes)
        PostTaskBudgetMode.FIXED -> state.fixedBudget.trim().takeIf(String::isNotEmpty)?.let {
            stringResource(R.string.post_task_budget_fixed_value, it)
        }.orEmpty().ifEmpty { "Request Quotes" }
        PostTaskBudgetMode.RANGE -> {
            val minimum = state.minimumBudget.trim()
            val maximum = state.maximumBudget.trim()
            if (minimum.isEmpty() || maximum.isEmpty()) {
                "Request Quotes"
            } else {
                stringResource(R.string.post_task_budget_range_display, minimum, maximum)
            }
        }
        null -> "Request Quotes"
    }
    val scopeHighlights = listOfNotNull(
        listOf(state.quantityOrMeasurements.trim(), state.brandOrModel.trim())
            .filter(String::isNotEmpty)
            .joinToString(" · ")
            .takeIf(String::isNotEmpty),
        state.materialsPolicy?.let { policy -> stringResource(policy.labelRes) },
    ).take(2)

    val requirementsList = buildList {
        if (workModeLabel.isNotBlank()) {
            add("Work mode: $workModeLabel")
        }
        if (state.quantityOrMeasurements.isNotBlank()) {
            add("Quantity / Measurements: ${state.quantityOrMeasurements.trim()}")
        }
        if (state.brandOrModel.isNotBlank()) {
            add("Brand / Model: ${state.brandOrModel.trim()}")
        }
        state.materialsPolicy?.let { policy ->
            add("Materials: ${stringResource(policy.labelRes)}")
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Review header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.post_task_review_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.post_task_review_helper),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Top Hero: Feed Card Preview (How it appears in Explore & Home)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    painter = painterResource(DsR.drawable.ic_visibility),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Feed Card Preview",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            TaskResultItem(
                title = state.title.trim(),
                category = categoryLabel,
                summary = state.details.trim().takeIf(String::isNotEmpty),
                budgetLabel = previewBudgetLabel,
                locationLabel = previewLocationLabel,
                timingLabel = previewTimingLabel,
                posterName = "You (Task Creator)",
                postedLabel = "Review Draft",
                status = null,
                photoModels = state.photoUris,
                scopeHighlights = scopeHighlights,
                modifier = Modifier.testTag("post_task_provider_preview_card"),
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(vertical = 4.dp),
        )

        // Full Task Details Header
        Text(
            text = "Full Task Details",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Category & timing badge row (matching TaskDetailScreen)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = categoryLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }

            Text(
                text = timingLabel.ifEmpty { "Flexible" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Title (matching TaskDetailScreen)
        Text(
            text = state.title.trim(),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Estimated Budget & Timing Card (matching TaskDetailScreen)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Estimated Budget",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = previewBudgetLabel,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Timeline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = timingLabel.ifEmpty { "Flexible" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (state.timingMode == PostTaskTimingMode.ASAP) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                }
            }
        }

        // Requester & Location row (matching TaskDetailScreen)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Y",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "You (Task Creator)",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = previewLocationLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Description (matching TaskDetailScreen)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = state.details.trim(),
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Key Requirements (matching TaskDetailScreen)
        if (requirementsList.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Key Requirements",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                requirementsList.forEach { req ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = req,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Photos (matching TaskDetailScreen)
        if (state.photoUris.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Photos (${state.photoUris.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                PhotoList(uris = state.photoUris, onRemovePhoto = {}, allowRemove = false)
            }
        }

        // Dedicated Private Information Card (with lock icon & reassurance)
        if (state.workMode == PostTaskWorkMode.AT_MY_LOCATION &&
            (privateAddress.isNotBlank() || accessInstructions.isNotBlank())
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            painter = painterResource(DsR.drawable.ic_lock),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "Private Information",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Text(
                        text = "🔒 Visible only to your assigned provider after you accept their offer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (privateAddress.isNotBlank()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        PostTaskSummaryRow(R.string.post_task_address_optional, privateAddress)
                    }

                    if (accessInstructions.isNotBlank()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        PostTaskSummaryBlock(R.string.post_task_access_instructions, accessInstructions)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Edit button
        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .testTag("post_task_edit"),
        ) {
            Text(stringResource(R.string.post_task_edit))
        }
    }
}

private val PostTaskMaterialsPolicy.labelRes: Int
    get() = when (this) {
        PostTaskMaterialsPolicy.REQUESTER_HAS_MATERIALS -> R.string.post_task_materials_requester
        PostTaskMaterialsPolicy.PROVIDER_BRINGS_MATERIALS -> R.string.post_task_materials_provider
        PostTaskMaterialsPolicy.DECIDE_AFTER_INSPECTION -> R.string.post_task_materials_decide_later
    }

@Composable
private fun PostTaskSectionHeading(@StringRes titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun PostTaskSummaryRow(@StringRes labelRes: Int, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun PostTaskSummaryBlock(@StringRes labelRes: Int, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun PostTaskChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.secondary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun OptionalSectionRow(
    @StringRes titleRes: Int,
    expanded: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    val stateText = stringResource(
        if (expanded) R.string.post_task_collapse else R.string.post_task_expand,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .testTag(testTag)
            .semantics {
                stateDescription = stateText
            }
            .selectable(
                selected = expanded,
                onClick = onClick,
                role = Role.Button,
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stateText,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun PostTaskError(@StringRes messageRes: Int) {
    val message = stringResource(messageRes)
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.semantics { error(message) },
    )
}

private fun firstInvalidField(errors: Set<PostTaskValidationField>): PostTaskValidationField =
    listOf(
        PostTaskValidationField.CATEGORY,
        PostTaskValidationField.TITLE,
        PostTaskValidationField.DETAILS,
        PostTaskValidationField.WORK_MODE,
        PostTaskValidationField.LOCATION,
        PostTaskValidationField.TIMING,
        PostTaskValidationField.DATE,
        PostTaskValidationField.FIXED_BUDGET,
        PostTaskValidationField.BUDGET_RANGE,
    ).first { it in errors }

private fun formatDate(millis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(Date(millis))
