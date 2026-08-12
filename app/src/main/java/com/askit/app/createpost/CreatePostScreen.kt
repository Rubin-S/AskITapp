package com.askit.app.createpost

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.app.explore.ExploreLocationSource
import com.askit.app.explore.ExploreSearchArea
import com.askit.app.explore.SearchAreaScreen
import com.askit.app.media.persistPhotoPickerReadAccess
import com.askit.designsystem.people.AskITAvatar
import com.askit.designsystem.R as DesignR
import com.askit.designsystem.posts.PostFeedAuthor
import com.askit.designsystem.posts.PostFeedContent
import com.askit.designsystem.posts.PostFeedItem
import com.askit.designsystem.posts.PostFeedMedia
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostRoute(
    viewModel: CreatePostViewModel,
    onBack: () -> Unit,
    onCompleteDraft: (PostDraft) -> Unit = {},
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    var showLocationPicker by rememberSaveable { mutableStateOf(false) }
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }

    fun requestBack() {
        if (state.screenMode == CreatePostScreenMode.PREVIEW) {
            viewModel.edit()
        } else if (viewModel.isDirty) {
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
        val location = state.location
        SearchAreaScreen(
            confirmedArea = ExploreSearchArea(
                placeId = location?.placeId,
                displayName = location?.publicAreaLabel
                    ?: stringResource(R.string.create_post_add_location),
                supportingText = null,
                latitude = location?.latitude,
                longitude = location?.longitude,
                radiusKm = 10,
                source = ExploreLocationSource.SAVED,
            ),
            onBack = { showLocationPicker = false },
            titleRes = R.string.create_post_location_title,
            showFilterControls = false,
            onApply = { area, _ ->
                if (area.isUsable) {
                    viewModel.setLocation(
                        PostPublicLocation(
                            publicAreaLabel = area.displayName,
                            placeId = area.placeId,
                            latitude = requireNotNull(area.latitude),
                            longitude = requireNotNull(area.longitude),
                        ),
                    )
                }
                showLocationPicker = false
            },
        )
    } else {
        CreatePostScreen(
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
            title = { Text(stringResource(R.string.create_post_discard_title)) },
            text = { Text(stringResource(R.string.create_post_discard_message)) },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.create_post_keep_editing))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onBack()
                    },
                ) {
                    Text(stringResource(R.string.create_post_discard))
                }
            },
        )
    }
}

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
private fun CreatePostScreen(
    viewModel: CreatePostViewModel,
    state: CreatePostFormState,
    validationErrors: Set<PostValidationField>,
    onBack: () -> Unit,
    onOpenLocationPicker: () -> Unit,
    onCompleteDraft: (PostDraft) -> Unit,
) {
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val contentResolver = LocalContext.current.contentResolver
    var pendingType by rememberSaveable { mutableStateOf<PostType?>(null) }
    var showClosingDatePicker by rememberSaveable { mutableStateOf(false) }

    val bodyBringIntoView = remember { BringIntoViewRequester() }
    val photoBringIntoView = remember { BringIntoViewRequester() }
    val carouselBringIntoView = remember { BringIntoViewRequester() }
    val beforeBringIntoView = remember { BringIntoViewRequester() }
    val afterBringIntoView = remember { BringIntoViewRequester() }
    val pollQuestionBringIntoView = remember { BringIntoViewRequester() }
    val pollOptionsBringIntoView = remember { BringIntoViewRequester() }
    val bodyFocusRequester = remember { FocusRequester() }
    val pollQuestionFocusRequester = remember { FocusRequester() }

    LaunchedEffect(state.previewAttempt) {
        if (state.screenMode == CreatePostScreenMode.EDITING && validationErrors.isNotEmpty()) {
            when (firstInvalidField(validationErrors)) {
                PostValidationField.TEXT_BODY -> {
                    bodyBringIntoView.bringIntoView()
                    bodyFocusRequester.requestFocus()
                }
                PostValidationField.PHOTO -> photoBringIntoView.bringIntoView()
                PostValidationField.CAROUSEL -> carouselBringIntoView.bringIntoView()
                PostValidationField.BEFORE_PHOTO -> beforeBringIntoView.bringIntoView()
                PostValidationField.AFTER_PHOTO -> afterBringIntoView.bringIntoView()
                PostValidationField.POLL_QUESTION -> {
                    pollQuestionBringIntoView.bringIntoView()
                    pollQuestionFocusRequester.requestFocus()
                }
                PostValidationField.POLL_OPTIONS -> pollOptionsBringIntoView.bringIntoView()
                PostValidationField.POLL_CLOSING -> Unit
            }
        }
    }

    val singlePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            persistPhotoPickerReadAccess(contentResolver, listOf(uri))
            viewModel.setPhoto(uri.toString())
        }
    }
    val carouselPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(MAX_CAROUSEL_ITEMS),
    ) { uris ->
        if (uris.isNotEmpty()) {
            persistPhotoPickerReadAccess(contentResolver, uris)
            viewModel.addCarouselMedia(uris.map(Uri::toString))
        }
    }
    var beforeAfterSlot by rememberSaveable { mutableStateOf<BeforeAfterSlot?>(null) }
    val beforeAfterPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            persistPhotoPickerReadAccess(contentResolver, listOf(uri))
            when (beforeAfterSlot) {
                BeforeAfterSlot.BEFORE -> viewModel.setBeforePhoto(uri.toString())
                BeforeAfterSlot.AFTER -> viewModel.setAfterPhoto(uri.toString())
                null -> Unit
            }
        }
        beforeAfterSlot = null
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.screenMode == CreatePostScreenMode.PREVIEW) {
                                R.string.create_post_preview_title
                            } else {
                            R.string.create_post_screen_title
                            },
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = if (state.screenMode == CreatePostScreenMode.PREVIEW) {
                            viewModel::edit
                        } else {
                            onBack
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.explore_back),
                        )
                    }
                },
                actions = {
                    if (state.screenMode == CreatePostScreenMode.EDITING) {
                        TextButton(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.preview()
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("create_post_preview_top"),
                        ) {
                            Text(stringResource(R.string.create_post_preview))
                        }
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .testTag("create_post_content"),
            contentPadding = PaddingValues(
                start = if (state.screenMode == CreatePostScreenMode.PREVIEW) 0.dp else 16.dp,
                top = padding.calculateTopPadding() + 16.dp,
                end = if (state.screenMode == CreatePostScreenMode.PREVIEW) 0.dp else 16.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                if (state.screenMode == CreatePostScreenMode.PREVIEW) {
                    CreatePostPreview(
                        state = state,
                        onEdit = viewModel::edit,
                        onComplete = {
                            viewModel.buildValidatedDraft()?.let(onCompleteDraft)
                        },
                    )
                } else {
                    CreatePostEditor(
                        viewModel = viewModel,
                        state = state,
                        validationErrors = validationErrors,
                        bodyBringIntoView = bodyBringIntoView,
                        photoBringIntoView = photoBringIntoView,
                        carouselBringIntoView = carouselBringIntoView,
                        beforeBringIntoView = beforeBringIntoView,
                        afterBringIntoView = afterBringIntoView,
                        pollQuestionBringIntoView = pollQuestionBringIntoView,
                        pollOptionsBringIntoView = pollOptionsBringIntoView,
                        bodyFocusRequester = bodyFocusRequester,
                        pollQuestionFocusRequester = pollQuestionFocusRequester,
                        onOpenLocationPicker = onOpenLocationPicker,
                        onPickSinglePhoto = {
                            singlePhotoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        onPickCarouselPhotos = {
                            carouselPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        onPickBeforePhoto = {
                            beforeAfterSlot = BeforeAfterSlot.BEFORE
                            beforeAfterPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        onPickAfterPhoto = {
                            beforeAfterSlot = BeforeAfterSlot.AFTER
                            beforeAfterPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        onOpenClosingDatePicker = { showClosingDatePicker = true },
                        onTypeChangeRequested = { type ->
                            if (viewModel.selectType(type)) {
                                pendingType = type
                            }
                        },
                    )
                }
            }
        }
    }

    pendingType?.let { type ->
        AlertDialog(
            onDismissRequest = { pendingType = null },
            title = { Text(stringResource(R.string.create_post_change_type_title)) },
            text = { Text(changeTypeMessage(type)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmTypeChange(type)
                        pendingType = null
                    },
                ) {
                    Text(stringResource(R.string.create_post_change_type))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingType = null }) {
                    Text(stringResource(R.string.create_post_keep_editing))
                }
            },
        )
    }

    if (showClosingDatePicker) {
        val poll = state.content as? PostContentDraft.Poll
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = poll?.closingAtMillis)
        DatePickerDialog(
            onDismissRequest = { showClosingDatePicker = false },
            confirmButton = {
                TextButton(
                    enabled = datePickerState.selectedDateMillis != null,
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            viewModel.setPollClosingRule(
                                PostPollClosingRule.CUSTOM_DATE,
                                selectedDate + DAY_MILLIS - 1,
                            )
                        }
                        showClosingDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.create_post_done))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClosingDatePicker = false }) {
                    Text(stringResource(R.string.create_post_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreatePostEditor(
    viewModel: CreatePostViewModel,
    state: CreatePostFormState,
    validationErrors: Set<PostValidationField>,
    bodyBringIntoView: BringIntoViewRequester,
    photoBringIntoView: BringIntoViewRequester,
    carouselBringIntoView: BringIntoViewRequester,
    beforeBringIntoView: BringIntoViewRequester,
    afterBringIntoView: BringIntoViewRequester,
    pollQuestionBringIntoView: BringIntoViewRequester,
    pollOptionsBringIntoView: BringIntoViewRequester,
    bodyFocusRequester: FocusRequester,
    pollQuestionFocusRequester: FocusRequester,
    onOpenLocationPicker: () -> Unit,
    onPickSinglePhoto: () -> Unit,
    onPickCarouselPhotos: () -> Unit,
    onPickBeforePhoto: () -> Unit,
    onPickAfterPhoto: () -> Unit,
    onOpenClosingDatePicker: () -> Unit,
    onTypeChangeRequested: (PostType) -> Unit,
) {
    val contentModifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 640.dp)

    Column(
        modifier = contentModifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CreatePostIdentity()
        PostTypeSelector(
            selectedType = state.content.type,
            onTypeSelected = onTypeChangeRequested,
        )
        when (val content = state.content) {
            is PostContentDraft.Text -> TextPostEditor(
                content = content,
                isError = PostValidationField.TEXT_BODY in validationErrors,
                bringIntoViewRequester = bodyBringIntoView,
                focusRequester = bodyFocusRequester,
                onBodyChange = viewModel::updateTextBody,
            )
            is PostContentDraft.Photo -> PhotoPostEditor(
                content = content,
                isError = PostValidationField.PHOTO in validationErrors,
                isDescriptionExpanded = PostDisclosure.PHOTO_DESCRIPTION in state.expandedDisclosures,
                bringIntoViewRequester = photoBringIntoView,
                onPickPhoto = onPickSinglePhoto,
                onRemovePhoto = viewModel::removePhoto,
                onReplacePhoto = onPickSinglePhoto,
                onCaptionChange = viewModel::updatePhotoCaption,
                onDescriptionChange = viewModel::updatePhotoImageDescription,
                onToggleDescription = { viewModel.toggleDisclosure(PostDisclosure.PHOTO_DESCRIPTION) },
            )
            is PostContentDraft.Carousel -> CarouselPostEditor(
                content = content,
                selectedIndex = state.selectedCarouselIndex,
                isError = PostValidationField.CAROUSEL in validationErrors,
                isDescriptionExpanded = PostDisclosure.CAROUSEL_DESCRIPTION in state.expandedDisclosures,
                bringIntoViewRequester = carouselBringIntoView,
                onPickPhotos = onPickCarouselPhotos,
                onSelectItem = viewModel::selectCarouselItem,
                onRemoveItem = viewModel::removeCarouselMedia,
                onMoveItem = viewModel::moveCarouselItem,
                onDescriptionChange = viewModel::updateCarouselItemDescription,
                onCaptionChange = viewModel::updateCarouselCaption,
                onToggleDescription = { viewModel.toggleDisclosure(PostDisclosure.CAROUSEL_DESCRIPTION) },
            )
            is PostContentDraft.BeforeAfter -> BeforeAfterPostEditor(
                content = content,
                isBeforeError = PostValidationField.BEFORE_PHOTO in validationErrors,
                isAfterError = PostValidationField.AFTER_PHOTO in validationErrors,
                beforeDescriptionExpanded = PostDisclosure.BEFORE_DESCRIPTION in state.expandedDisclosures,
                afterDescriptionExpanded = PostDisclosure.AFTER_DESCRIPTION in state.expandedDisclosures,
                beforeBringIntoViewRequester = beforeBringIntoView,
                afterBringIntoViewRequester = afterBringIntoView,
                onPickBefore = onPickBeforePhoto,
                onPickAfter = onPickAfterPhoto,
                onRemoveBefore = viewModel::removeBeforePhoto,
                onRemoveAfter = viewModel::removeAfterPhoto,
                onBeforeDescriptionChange = viewModel::updateBeforeImageDescription,
                onAfterDescriptionChange = viewModel::updateAfterImageDescription,
                onToggleBeforeDescription = { viewModel.toggleDisclosure(PostDisclosure.BEFORE_DESCRIPTION) },
                onToggleAfterDescription = { viewModel.toggleDisclosure(PostDisclosure.AFTER_DESCRIPTION) },
                onCaptionChange = viewModel::updateBeforeAfterCaption,
                onBeforeNoteChange = viewModel::updateBeforeNote,
                onAfterNoteChange = viewModel::updateAfterNote,
            )
            is PostContentDraft.Poll -> PollPostEditor(
                viewModel = viewModel,
                content = content,
                validationErrors = validationErrors,
                questionBringIntoViewRequester = pollQuestionBringIntoView,
                optionsBringIntoViewRequester = pollOptionsBringIntoView,
                questionFocusRequester = pollQuestionFocusRequester,
                onOpenClosingDatePicker = onOpenClosingDatePicker,
            )
        }
        PostLocationEditor(
            location = state.location,
            onAddOrChange = onOpenLocationPicker,
            onRemove = viewModel::removeLocation,
        )
    }
}

@Composable
private fun CreatePostIdentity() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            AskITAvatar(
                avatarUrl = null,
                avatarSize = 40.dp,
                fallbackIconSize = 26.dp,
            )
        }
        Column {
            Text(
                text = stringResource(R.string.create_post_you),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = stringResource(R.string.create_post_identity_helper),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PostTypeSelector(
    selectedType: PostType,
    onTypeSelected: (PostType) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.create_post_type),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.semantics { heading() },
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PostType.entries.forEach { type ->
                val selected = selectedType == type
                FilterChip(
                    selected = selected,
                    onClick = { onTypeSelected(type) },
                    label = { Text(postTypeLabel(type)) },
                    leadingIcon = if (selected) {
                        {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                            )
                        }
                    } else {
                        null
                    },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("create_post_type_${type.name.lowercase()}"),
                )
            }
        }
    }
}

@Composable
private fun TextPostEditor(
    content: PostContentDraft.Text,
    isError: Boolean,
    bringIntoViewRequester: BringIntoViewRequester,
    focusRequester: FocusRequester,
    onBodyChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = content.body,
        onValueChange = onBodyChange,
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .focusRequester(focusRequester)
            .testTag("create_post_text_body"),
        label = { Text(stringResource(R.string.create_post_whats_on_your_mind)) },
        supportingText = {
            if (isError) Text(stringResource(R.string.create_post_error_text))
        },
        isError = isError,
        minLines = 6,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
    )
}

@Composable
private fun PhotoPostEditor(
    content: PostContentDraft.Photo,
    isError: Boolean,
    isDescriptionExpanded: Boolean,
    bringIntoViewRequester: BringIntoViewRequester,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onReplacePhoto: () -> Unit,
    onCaptionChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onToggleDescription: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (content.uri.isNullOrBlank()) {
            EmptyMediaPrompt(
                title = R.string.create_post_photo_empty_title,
                buttonLabel = R.string.create_post_add_photo,
                onClick = onPickPhoto,
                testTag = "create_post_add_photo",
            )
            if (isError) PostError(R.string.create_post_error_photo)
        } else {
            ComposerImage(
                uri = content.uri,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp, max = 480.dp),
                contentDescription = content.imageDescription.ifBlank {
                    stringResource(R.string.create_post_photo_content_description)
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    onClick = onReplacePhoto,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("create_post_replace_photo"),
                ) {
                    Text(stringResource(R.string.create_post_replace))
                }
                TextButton(
                    onClick = onRemovePhoto,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("create_post_remove_photo"),
                ) {
                    Text(stringResource(R.string.create_post_remove))
                }
            }
        }
        OutlinedTextField(
            value = content.caption,
            onValueChange = onCaptionChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_post_photo_caption"),
            label = { Text(stringResource(R.string.create_post_caption_optional)) },
            minLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
        )
        DisclosureButton(
            expanded = isDescriptionExpanded,
            collapsedLabel = R.string.create_post_add_image_description,
            expandedLabel = R.string.create_post_hide_image_description,
            onClick = onToggleDescription,
            testTag = "create_post_photo_description_toggle",
        )
        if (isDescriptionExpanded) {
            OutlinedTextField(
                value = content.imageDescription,
                onValueChange = onDescriptionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_post_photo_description"),
                label = { Text(stringResource(R.string.create_post_image_description_optional)) },
                supportingText = {
                    Text(stringResource(R.string.create_post_image_description_helper))
                },
                minLines = 3,
            )
        }
    }
}

@Composable
private fun CarouselPostEditor(
    content: PostContentDraft.Carousel,
    selectedIndex: Int,
    isError: Boolean,
    isDescriptionExpanded: Boolean,
    bringIntoViewRequester: BringIntoViewRequester,
    onPickPhotos: () -> Unit,
    onSelectItem: (Int) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onMoveItem: (Int, Int) -> Unit,
    onDescriptionChange: (Int, String) -> Unit,
    onCaptionChange: (String) -> Unit,
    onToggleDescription: () -> Unit,
) {
    val selected = selectedIndex.coerceIn(0, content.items.lastIndex.coerceAtLeast(0))
    val selectedItem = content.items.getOrNull(selected)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (content.items.isEmpty()) {
            EmptyMediaPrompt(
                title = R.string.create_post_carousel_empty_title,
                buttonLabel = R.string.create_post_add_photos,
                onClick = onPickPhotos,
                testTag = "create_post_add_carousel_photos",
            )
        } else {
            Text(
                text = pluralStringResource(
                    R.plurals.create_post_carousel_count,
                    content.items.size,
                    content.items.size,
                ),
                style = MaterialTheme.typography.titleSmall,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("create_post_carousel_thumbnails"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                content.items.forEachIndexed { index, item ->
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(MaterialTheme.shapes.small)
                            .border(
                                BorderStroke(
                                    width = if (index == selected) 2.dp else 1.dp,
                                    color = if (index == selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                ),
                                MaterialTheme.shapes.small,
                            )
                            .clickable(
                                role = Role.Button,
                                onClickLabel = stringResource(
                                    R.string.create_post_select_image,
                                    index + 1,
                                ),
                                onClick = { onSelectItem(index) },
                            )
                            .testTag("create_post_carousel_thumbnail_$index"),
                    ) {
                        ComposerImage(
                            uri = item.uri,
                            contentDescription = stringResource(
                                R.string.create_post_image_description_number,
                                index + 1,
                            ),
                            modifier = Modifier.fillMaxSize(),
                        )
                        Text(
                            text = (index + 1).toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .background(
                                    MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.9f),
                                    MaterialTheme.shapes.small,
                                )
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                        )
                    }
                }
            }
            selectedItem?.let { item ->
                Text(
                    text = stringResource(
                        R.string.create_post_image_position,
                        selected + 1,
                        content.items.size,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ComposerImage(
                    uri = item.uri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp, max = 480.dp),
                    contentDescription = stringResource(
                        R.string.create_post_image_description_number,
                        selected + 1,
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TextButton(
                        onClick = { onMoveItem(selected, -1) },
                        enabled = selected > 0,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("create_post_carousel_move_earlier"),
                    ) {
                        Text(stringResource(R.string.create_post_move_earlier))
                    }
                    TextButton(
                        onClick = { onMoveItem(selected, 1) },
                        enabled = selected < content.items.lastIndex,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("create_post_carousel_move_later"),
                    ) {
                        Text(stringResource(R.string.create_post_move_later))
                    }
                    TextButton(
                        onClick = { onRemoveItem(selected) },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("create_post_carousel_remove"),
                    ) {
                        Text(stringResource(R.string.create_post_remove))
                    }
                }
                DisclosureButton(
                    expanded = isDescriptionExpanded,
                    collapsedLabel = R.string.create_post_add_image_description,
                    expandedLabel = R.string.create_post_hide_image_description,
                    onClick = onToggleDescription,
                    testTag = "create_post_carousel_description_toggle",
                )
                if (isDescriptionExpanded) {
                    OutlinedTextField(
                        value = item.imageDescription,
                        onValueChange = { onDescriptionChange(selected, it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_post_carousel_description"),
                        label = {
                            Text(
                                stringResource(
                                    R.string.create_post_image_description_number,
                                    selected + 1,
                                ),
                            )
                        },
                        minLines = 3,
                    )
                }
            }
        }
        if (content.items.size < MAX_CAROUSEL_ITEMS) {
            OutlinedButton(
                onClick = onPickPhotos,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("create_post_add_more_carousel_photos"),
            ) {
                Text(stringResource(R.string.create_post_add_photos))
            }
        }
        if (isError) PostError(R.string.create_post_error_carousel)
        OutlinedTextField(
            value = content.caption,
            onValueChange = onCaptionChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_post_carousel_caption"),
            label = { Text(stringResource(R.string.create_post_caption_optional)) },
            minLines = 3,
        )
    }
}

@Composable
private fun BeforeAfterPostEditor(
    content: PostContentDraft.BeforeAfter,
    isBeforeError: Boolean,
    isAfterError: Boolean,
    beforeDescriptionExpanded: Boolean,
    afterDescriptionExpanded: Boolean,
    beforeBringIntoViewRequester: BringIntoViewRequester,
    afterBringIntoViewRequester: BringIntoViewRequester,
    onPickBefore: () -> Unit,
    onPickAfter: () -> Unit,
    onRemoveBefore: () -> Unit,
    onRemoveAfter: () -> Unit,
    onBeforeDescriptionChange: (String) -> Unit,
    onAfterDescriptionChange: (String) -> Unit,
    onToggleBeforeDescription: () -> Unit,
    onToggleAfterDescription: () -> Unit,
    onCaptionChange: (String) -> Unit,
    onBeforeNoteChange: (String) -> Unit,
    onAfterNoteChange: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val sideBySide = maxWidth >= 480.dp
        if (sideBySide) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BeforeAfterSlotEditor(
                    label = R.string.create_post_before,
                    content = content.before,
                    isError = isBeforeError,
                    isDescriptionExpanded = beforeDescriptionExpanded,
                    bringIntoViewRequester = beforeBringIntoViewRequester,
                    onPick = onPickBefore,
                    onRemove = onRemoveBefore,
                    onDescriptionChange = onBeforeDescriptionChange,
                    onToggleDescription = onToggleBeforeDescription,
                    modifier = Modifier.weight(1f),
                    testTag = "create_post_before_slot",
                )
                BeforeAfterSlotEditor(
                    label = R.string.create_post_after,
                    content = content.after,
                    isError = isAfterError,
                    isDescriptionExpanded = afterDescriptionExpanded,
                    bringIntoViewRequester = afterBringIntoViewRequester,
                    onPick = onPickAfter,
                    onRemove = onRemoveAfter,
                    onDescriptionChange = onAfterDescriptionChange,
                    onToggleDescription = onToggleAfterDescription,
                    modifier = Modifier.weight(1f),
                    testTag = "create_post_after_slot",
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                BeforeAfterSlotEditor(
                    label = R.string.create_post_before,
                    content = content.before,
                    isError = isBeforeError,
                    isDescriptionExpanded = beforeDescriptionExpanded,
                    bringIntoViewRequester = beforeBringIntoViewRequester,
                    onPick = onPickBefore,
                    onRemove = onRemoveBefore,
                    onDescriptionChange = onBeforeDescriptionChange,
                    onToggleDescription = onToggleBeforeDescription,
                    testTag = "create_post_before_slot",
                )
                BeforeAfterSlotEditor(
                    label = R.string.create_post_after,
                    content = content.after,
                    isError = isAfterError,
                    isDescriptionExpanded = afterDescriptionExpanded,
                    bringIntoViewRequester = afterBringIntoViewRequester,
                    onPick = onPickAfter,
                    onRemove = onRemoveAfter,
                    onDescriptionChange = onAfterDescriptionChange,
                    onToggleDescription = onToggleAfterDescription,
                    testTag = "create_post_after_slot",
                )
            }
        }
    }
    OutlinedTextField(
        value = content.caption,
        onValueChange = onCaptionChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("create_post_before_after_caption"),
        label = { Text(stringResource(R.string.create_post_caption_optional)) },
        minLines = 3,
    )
    OutlinedTextField(
        value = content.beforeNote,
        onValueChange = onBeforeNoteChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("create_post_before_note"),
        label = { Text(stringResource(R.string.create_post_before_note_optional)) },
        minLines = 2,
    )
    OutlinedTextField(
        value = content.afterNote,
        onValueChange = onAfterNoteChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("create_post_after_note"),
        label = { Text(stringResource(R.string.create_post_after_note_optional)) },
        minLines = 2,
    )
}

@Composable
private fun BeforeAfterSlotEditor(
    @StringRes label: Int,
    content: PostMediaDraft,
    isError: Boolean,
    isDescriptionExpanded: Boolean,
    bringIntoViewRequester: BringIntoViewRequester,
    onPick: () -> Unit,
    onRemove: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onToggleDescription: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String,
) {
    Column(
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .testTag(testTag),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.semantics { heading() },
        )
        if (content.uri.isNullOrBlank()) {
            OutlinedButton(
                onClick = onPick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.create_post_add_photo))
            }
            if (isError) {
                val errorMessage = stringResource(
                    if (label == R.string.create_post_before) {
                        R.string.create_post_error_before
                    } else {
                        R.string.create_post_error_after
                    },
                )
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.semantics { error(errorMessage) },
                )
            }
        } else {
            ComposerImage(
                uri = content.uri,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp, max = 480.dp),
                contentDescription = content.imageDescription.ifBlank {
                    stringResource(
                        if (label == R.string.create_post_before) {
                            R.string.create_post_before_content_description
                        } else {
                            R.string.create_post_after_content_description
                        },
                    )
                },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onPick,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(stringResource(R.string.create_post_replace))
                }
                TextButton(
                    onClick = onRemove,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(stringResource(R.string.create_post_remove))
                }
            }
        }
        DisclosureButton(
            expanded = isDescriptionExpanded,
            collapsedLabel = R.string.create_post_add_image_description,
            expandedLabel = R.string.create_post_hide_image_description,
            onClick = onToggleDescription,
            testTag = "${testTag}_description_toggle",
        )
        if (isDescriptionExpanded) {
            OutlinedTextField(
                value = content.imageDescription,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.create_post_image_description_optional)) },
                minLines = 3,
            )
        }
    }
}

@Composable
private fun PollPostEditor(
    viewModel: CreatePostViewModel,
    content: PostContentDraft.Poll,
    validationErrors: Set<PostValidationField>,
    questionBringIntoViewRequester: BringIntoViewRequester,
    optionsBringIntoViewRequester: BringIntoViewRequester,
    questionFocusRequester: FocusRequester,
    onOpenClosingDatePicker: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = content.question,
            onValueChange = viewModel::updatePollQuestion,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(questionBringIntoViewRequester)
                .focusRequester(questionFocusRequester)
                .testTag("create_post_poll_question"),
            label = { Text(stringResource(R.string.create_post_question)) },
            supportingText = {
                if (PostValidationField.POLL_QUESTION in validationErrors) {
                    Text(stringResource(R.string.create_post_error_poll_question))
                }
            },
            isError = PostValidationField.POLL_QUESTION in validationErrors,
            minLines = 2,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(optionsBringIntoViewRequester)
                .testTag("create_post_poll_options"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content.options.forEachIndexed { index, option ->
                val optionError = viewModel.pollOptionError(index)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    OutlinedTextField(
                        value = option,
                        onValueChange = { viewModel.updatePollOption(index, it) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("create_post_poll_option_$index"),
                        label = {
                            Text(stringResource(R.string.create_post_poll_option, index + 1))
                        },
                        supportingText = {
                            when (optionError) {
                                PollOptionError.EMPTY -> Text(stringResource(R.string.create_post_error_poll_answer))
                                PollOptionError.DUPLICATE -> Text(stringResource(R.string.create_post_error_poll_duplicate))
                                null -> Unit
                            }
                        },
                        isError = optionError != null &&
                            PostValidationField.POLL_OPTIONS in validationErrors,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Text,
                        ),
                    )
                    if (content.options.size > 2) {
                        IconButton(
                            onClick = { viewModel.removePollOption(index) },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("create_post_remove_poll_option_$index"),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_clear),
                                contentDescription = stringResource(
                                    R.string.create_post_remove_poll_option,
                                    index + 1,
                                ),
                            )
                        }
                    }
                }
            }
        }
        if (PostValidationField.POLL_OPTIONS in validationErrors &&
            content.options.size < 2
        ) {
            PostError(R.string.create_post_error_poll_options)
        }
        if (content.options.size < MAX_POLL_OPTIONS) {
            TextButton(
                onClick = viewModel::addPollOption,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("create_post_add_poll_option"),
            ) {
                Text(stringResource(R.string.create_post_add_option))
            }
        }
        OutlinedTextField(
            value = content.description,
            onValueChange = viewModel::updatePollDescription,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_post_poll_description"),
            label = { Text(stringResource(R.string.create_post_description_optional)) },
            minLines = 3,
        )
        Text(
            text = pollClosingSummary(content),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(
            onClick = onOpenClosingDatePicker,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag("create_post_change_closing_time"),
        ) {
            Text(stringResource(R.string.create_post_change_closing_time))
        }
        if (PostValidationField.POLL_CLOSING in validationErrors) {
            PostError(R.string.create_post_error_poll_closing)
        }
    }
}

@Composable
private fun PostLocationEditor(
    location: PostPublicLocation?,
    onAddOrChange: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (location == null) {
            TextButton(
                onClick = onAddOrChange,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("create_post_add_location"),
            ) {
                Text(stringResource(R.string.create_post_add_location))
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_location_on),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = location.publicAreaLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = onAddOrChange,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(stringResource(R.string.create_post_change_location))
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("create_post_remove_location"),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_clear),
                        contentDescription = stringResource(R.string.create_post_remove_location),
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.create_post_location_privacy),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("create_post_location_privacy"),
        )
    }
}

@Composable
private fun CreatePostPreview(
    state: CreatePostFormState,
    onEdit: () -> Unit,
    onComplete: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PostFeedItem(
            author = PostFeedAuthor(
                displayName = stringResource(R.string.create_post_you),
            ),
            locationLabel = state.location?.publicAreaLabel,
            content = state.content.toFeedContent(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_post_preview_feed_item"),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("create_post_edit"),
            ) {
                Text(stringResource(R.string.create_post_edit))
            }
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("create_post_complete"),
            ) {
                Text(stringResource(R.string.create_post_complete_draft))
            }
        }
    }
}

@Composable
private fun EmptyMediaPrompt(
    @StringRes title: Int,
    @StringRes buttonLabel: Int,
    onClick: () -> Unit,
    testTag: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_add),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onClick,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag(testTag),
        ) {
            Text(stringResource(buttonLabel))
        }
    }
}

@Composable
private fun ComposerImage(
    uri: String?,
    modifier: Modifier = Modifier,
    contentDescription: String,
) {
    AsyncImage(
        model = uri,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    )
}

@Composable
private fun DisclosureButton(
    expanded: Boolean,
    @StringRes collapsedLabel: Int,
    @StringRes expandedLabel: Int,
    onClick: () -> Unit,
    testTag: String,
) {
    val labelRes = if (expanded) expandedLabel else collapsedLabel
    val label = stringResource(labelRes)
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .testTag(testTag)
            .semantics { stateDescription = label },
    ) {
        Text(label)
    }
}

@Composable
private fun PostError(@StringRes message: Int) {
    val text = stringResource(message)
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.semantics { error(text) },
    )
}

@Composable
private fun postTypeLabel(type: PostType): String = stringResource(
    when (type) {
        PostType.TEXT -> R.string.create_post_type_text
        PostType.PHOTO -> R.string.create_post_type_photo
        PostType.CAROUSEL -> R.string.create_post_type_carousel
        PostType.BEFORE_AFTER -> R.string.create_post_type_before_after
        PostType.POLL -> R.string.create_post_type_poll
    },
)

@Composable
private fun changeTypeMessage(type: PostType): String = stringResource(
    when (type) {
        PostType.TEXT -> R.string.create_post_change_to_text_message
        PostType.PHOTO -> R.string.create_post_change_to_photo_message
        PostType.CAROUSEL -> R.string.create_post_change_to_carousel_message
        PostType.BEFORE_AFTER -> R.string.create_post_change_to_before_after_message
        PostType.POLL -> R.string.create_post_change_to_poll_message
    },
)

@Composable
private fun pollClosingSummary(content: PostContentDraft.Poll): String = when (content.closingRule) {
    PostPollClosingRule.AFTER_24_HOURS -> stringResource(R.string.create_post_closes_24_hours)
    PostPollClosingRule.CUSTOM_DATE -> content.closingAtMillis?.let { millis ->
        stringResource(R.string.create_post_closes_on, formatDate(millis))
    } ?: stringResource(R.string.create_post_closes_24_hours)
}

@Composable
private fun PostContentDraft.toFeedContent(): PostFeedContent = when (this) {
    is PostContentDraft.Text -> PostFeedContent.Text(body = body.trim())
    is PostContentDraft.Photo -> PostFeedContent.Photo(
        image = PostFeedMedia(
            model = uri.orEmpty(),
            contentDescription = imageDescription.trim().takeIf(String::isNotEmpty),
        ),
        caption = caption.trim().takeIf(String::isNotEmpty),
    )
    is PostContentDraft.Carousel -> PostFeedContent.Carousel(
        items = items.mapNotNull { item ->
            item.uri?.takeIf(String::isNotEmpty)?.let { uri ->
                PostFeedMedia(
                    model = uri,
                    contentDescription = item.imageDescription.trim().takeIf(String::isNotEmpty),
                )
            }
        },
        caption = caption.trim().takeIf(String::isNotEmpty),
    )
    is PostContentDraft.BeforeAfter -> PostFeedContent.BeforeAfter(
        before = PostFeedMedia(
            model = before.uri.orEmpty(),
            contentDescription = before.imageDescription.trim().takeIf(String::isNotEmpty),
        ),
        after = PostFeedMedia(
            model = after.uri.orEmpty(),
            contentDescription = after.imageDescription.trim().takeIf(String::isNotEmpty),
        ),
        caption = caption.trim().takeIf(String::isNotEmpty),
        beforeNote = beforeNote.trim().takeIf(String::isNotEmpty),
        afterNote = afterNote.trim().takeIf(String::isNotEmpty),
    )
    is PostContentDraft.Poll -> PostFeedContent.Poll(
        question = question.trim(),
        options = options.map(String::trim),
        description = description.trim().takeIf(String::isNotEmpty),
        closingSummary = pollClosingSummary(this),
    )
}

private fun firstInvalidField(errors: Set<PostValidationField>): PostValidationField = listOf(
    PostValidationField.TEXT_BODY,
    PostValidationField.PHOTO,
    PostValidationField.CAROUSEL,
    PostValidationField.BEFORE_PHOTO,
    PostValidationField.AFTER_PHOTO,
    PostValidationField.POLL_QUESTION,
    PostValidationField.POLL_OPTIONS,
    PostValidationField.POLL_CLOSING,
).first { it in errors }

private fun formatDate(millis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(Date(millis))

private enum class BeforeAfterSlot {
    BEFORE,
    AFTER,
}

private const val MAX_CAROUSEL_ITEMS = 10
private const val MAX_POLL_OPTIONS = 6
private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
