package com.askit.app.posttask

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import com.askit.designsystem.dialogs.AskITAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.askit.app.R
import com.askit.app.explore.ExploreLocationSource
import com.askit.app.explore.ExploreSearchArea
import com.askit.app.explore.SearchAreaScreen
import com.askit.app.media.persistPhotoPickerReadAccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostTaskRoute(
    viewModel: PostTaskViewModel,
    onBack: () -> Unit,
    onCompleteDraft: (PostTaskDraft) -> Unit = {},
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    val submissionError by viewModel.submissionError.collectAsStateWithLifecycle()
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
            submissionError = submissionError,
        )
    }

    if (showDiscardDialog) {
        AskITAlertDialog(
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
                    Text(stringResource(R.string.post_task_discard), color = MaterialTheme.colorScheme.error)
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
    submissionError: Int?,
) {
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val contentResolver = LocalContext.current.contentResolver
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val targets = rememberPostTaskFieldTargets()

    LaunchedEffect(state.reviewAttempt) {
        if (state.screenMode == PostTaskScreenMode.FORM && validationErrors.isNotEmpty()) {
            // review() may have just expanded the section that owns the error; let it lay out
            // before bringing it into view.
            withFrameNanos { }
            firstInvalidPostTaskField(validationErrors)?.let { targets.reveal(it) }
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
                title = { Text(stringResource(if (viewModel.editingTaskId != null) R.string.task_edit_title else R.string.post_task_title)) },
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
                    submissionError?.let {
                        Text(stringResource(it), color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
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
                                    if (viewModel.editingTaskId != null) R.string.task_save_changes else R.string.post_task_complete_draft
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
                            targets = targets,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditUnavailableScreen(onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.task_edit_title)) }, navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.explore_back))
            }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text(stringResource(R.string.task_edit_unavailable))
            TextButton(onClick = onBack) { Text(stringResource(R.string.task_detail_go_back)) }
        }
    }
}
