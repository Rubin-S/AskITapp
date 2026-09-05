package com.askit.app.createpost

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.askit.app.R
import com.askit.app.createpost.components.BeforeAfterSlot
import com.askit.app.createpost.components.CreatePostAddOnsTray
import com.askit.app.createpost.components.CreatePostCanvas
import com.askit.app.createpost.components.CreatePostIdentityHeader
import com.askit.app.createpost.components.CreatePostMediaCard
import com.askit.app.createpost.components.CreatePostPollSection
import com.askit.app.createpost.components.CreatePostTopBar
import com.askit.app.createpost.components.pollClosingSummary
import com.askit.app.explore.ExploreLocationSource
import com.askit.app.explore.ExploreSearchArea
import com.askit.app.explore.SearchAreaScreen
import com.askit.app.media.persistPhotoPickerReadAccess
import com.askit.designsystem.posts.PostFeedAuthor
import com.askit.designsystem.posts.PostFeedContent
import com.askit.designsystem.posts.PostFeedItem
import com.askit.designsystem.posts.PostFeedMedia
import com.askit.designsystem.posts.PostFeedMediaContent
import com.askit.designsystem.posts.PostFeedPoll

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

@OptIn(ExperimentalMaterial3Api::class)
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
    var showClosingDatePicker by rememberSaveable { mutableStateOf(false) }
    var beforeAfterSlot by rememberSaveable { mutableStateOf<BeforeAfterSlot?>(null) }

    val mediaBringIntoView = remember { BringIntoViewRequester() }
    val bodyBringIntoView = remember { BringIntoViewRequester() }
    val pollQuestionBringIntoView = remember { BringIntoViewRequester() }
    val pollOptionsBringIntoView = remember { BringIntoViewRequester() }
    val bodyFocusRequester = remember { FocusRequester() }
    val pollQuestionFocusRequester = remember { FocusRequester() }

    LaunchedEffect(state.previewAttempt) {
        if (state.screenMode == CreatePostScreenMode.EDITING && validationErrors.isNotEmpty()) {
            when (firstInvalidField(validationErrors)) {
                PostValidationField.CONTENT -> {
                    bodyBringIntoView.bringIntoView()
                    bodyFocusRequester.requestFocus()
                }
                PostValidationField.BEFORE_PHOTO,
                PostValidationField.AFTER_PHOTO,
                -> mediaBringIntoView.bringIntoView()
                PostValidationField.POLL_QUESTION -> {
                    pollQuestionBringIntoView.bringIntoView()
                    pollQuestionFocusRequester.requestFocus()
                }
                PostValidationField.POLL_OPTIONS -> pollOptionsBringIntoView.bringIntoView()
                PostValidationField.POLL_CLOSING -> Unit
            }
        }
    }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(MAX_POST_PHOTOS),
    ) { uris ->
        if (uris.isNotEmpty()) {
            persistPhotoPickerReadAccess(contentResolver, uris)
            viewModel.addPhotos(uris.map(Uri::toString))
        }
    }
    val slotPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            persistPhotoPickerReadAccess(contentResolver, listOf(uri))
            when (beforeAfterSlot) {
                BeforeAfterSlot.BEFORE -> viewModel.setPhotoAt(0, uri.toString())
                BeforeAfterSlot.AFTER -> viewModel.setPhotoAt(1, uri.toString())
                BeforeAfterSlot.REPLACE -> {
                    val index = state.selectedCarouselIndex
                    viewModel.setPhotoAt(index, uri.toString())
                }
                null -> viewModel.addPhotos(listOf(uri.toString()))
            }
        }
        beforeAfterSlot = null
    }

    val isPostValid = state.body.isNotBlank() || state.photos.any { !it.uri.isNullOrBlank() } || state.poll != null

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CreatePostTopBar(
                screenMode = state.screenMode,
                isPostValid = isPostValid,
                onBack = onBack,
                onEdit = viewModel::edit,
                onPreview = {
                    keyboardController?.hide()
                    viewModel.preview()
                },
                onPost = {
                    keyboardController?.hide()
                    if (viewModel.preview()) {
                        viewModel.buildValidatedDraft()?.let(onCompleteDraft)
                    }
                },
            )
        },
        bottomBar = {
            if (state.screenMode == CreatePostScreenMode.PREVIEW) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.navigationBarsPadding()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Button(
                            onClick = { viewModel.buildValidatedDraft()?.let(onCompleteDraft) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .heightIn(min = 52.dp)
                                .testTag("create_post_complete"),
                        ) {
                            Text(stringResource(R.string.create_post_complete_draft))
                        }
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
                .testTag("create_post_content"),
            contentPadding = PaddingValues(
                horizontal = if (state.screenMode == CreatePostScreenMode.PREVIEW) 0.dp else 16.dp,
                vertical = 12.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                if (state.screenMode == CreatePostScreenMode.PREVIEW) {
                    CreatePostPreview(state = state, onEdit = viewModel::edit)
                } else {
                    CreatePostEditor(
                        viewModel = viewModel,
                        state = state,
                        validationErrors = validationErrors,
                        bodyBringIntoView = bodyBringIntoView,
                        pollQuestionBringIntoView = pollQuestionBringIntoView,
                        pollOptionsBringIntoView = pollOptionsBringIntoView,
                        bodyFocusRequester = bodyFocusRequester,
                        pollQuestionFocusRequester = pollQuestionFocusRequester,
                        onOpenLocationPicker = onOpenLocationPicker,
                        onPickGallery = {
                            viewModel.setMediaLayout(PostMediaLayout.GALLERY)
                            galleryPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        onPickSlot = { slot ->
                            beforeAfterSlot = slot
                            slotPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        onOpenClosingDatePicker = { showClosingDatePicker = true },
                    )
                }
            }
        }
    }

    if (showClosingDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.poll?.closingAtMillis,
        )
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

@Composable
private fun CreatePostEditor(
    viewModel: CreatePostViewModel,
    state: CreatePostFormState,
    validationErrors: Set<PostValidationField>,
    bodyBringIntoView: BringIntoViewRequester,
    pollQuestionBringIntoView: BringIntoViewRequester,
    pollOptionsBringIntoView: BringIntoViewRequester,
    bodyFocusRequester: FocusRequester,
    pollQuestionFocusRequester: FocusRequester,
    onOpenLocationPicker: () -> Unit,
    onPickGallery: () -> Unit,
    onPickSlot: (BeforeAfterSlot) -> Unit,
    onOpenClosingDatePicker: () -> Unit,
) {
    val isBeforeAfter = state.mediaLayout == PostMediaLayout.BEFORE_AFTER

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 1. Author Row
        CreatePostIdentityHeader()

        // 2. Visual Media Card (Media First!)
        CreatePostMediaCard(
            photos = state.photos,
            mediaLayout = state.mediaLayout,
            selectedCarouselIndex = state.selectedCarouselIndex,
            expandedDisclosures = state.expandedDisclosures,
            validationErrors = validationErrors,
            onToggleMediaLayout = viewModel::setMediaLayout,
            onPickGallery = onPickGallery,
            onPickSlot = onPickSlot,
            onSelectCarouselIndex = viewModel::selectPhoto,
            onRemovePhoto = viewModel::removePhotoAt,
            onClearSlot = { index -> viewModel.setPhotoAt(index, null) },
            onMovePhoto = viewModel::movePhoto,
            onDescriptionChange = viewModel::updatePhotoDescription,
            onToggleDisclosure = viewModel::toggleDisclosure,
        )

        // 3. Caption Canvas (Positioned directly below Media!)
        CreatePostCanvas(
            text = state.body,
            onTextChange = viewModel::updateTextBody,
            validationErrors = validationErrors,
            bringIntoViewRequester = bodyBringIntoView,
            focusRequester = bodyFocusRequester,
            placeholderText = if (isBeforeAfter) {
                "Describe what work was done, materials used, turnaround time..."
            } else if (state.photos.isNotEmpty()) {
                "Write a caption for your work photos..."
            } else {
                stringResource(R.string.create_post_whats_on_your_mind)
            },
        )

        // 4. Poll Section (if added)
        if (state.poll != null) {
            CreatePostPollSection(
                viewModel = viewModel,
                content = state.poll,
                validationErrors = validationErrors,
                questionBringIntoViewRequester = pollQuestionBringIntoView,
                optionsBringIntoViewRequester = pollOptionsBringIntoView,
                questionFocusRequester = pollQuestionFocusRequester,
                onOpenClosingDatePicker = onOpenClosingDatePicker,
            )
        }

        // 5. Add to Post Tray (Poll trigger + Location tagger)
        CreatePostAddOnsTray(
            hasPoll = state.poll != null,
            location = state.location,
            onAddPoll = viewModel::addPoll,
            onAddOrChangeLocation = onOpenLocationPicker,
            onRemoveLocation = viewModel::removeLocation,
        )
    }
}

@Composable
private fun CreatePostPreview(
    state: CreatePostFormState,
    onEdit: () -> Unit,
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
            content = state.toFeedContent(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_post_preview_feed_item"),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
        }
    }
}

@Composable
private fun CreatePostFormState.toFeedContent(): PostFeedContent {
    val filled = photos.filter { !it.uri.isNullOrBlank() }
    val media = when {
        mediaLayout == PostMediaLayout.BEFORE_AFTER && filled.size >= 2 ->
            PostFeedMediaContent.BeforeAfter(
                before = filled[0].toFeedMedia(),
                after = filled[1].toFeedMedia(),
            )
        filled.size >= 2 -> PostFeedMediaContent.Carousel(
            items = filled.map { it.toFeedMedia() },
        )
        filled.size == 1 -> PostFeedMediaContent.Photo(filled[0].toFeedMedia())
        else -> null
    }
    val pollContent = poll?.let { draft ->
        PostFeedPoll(
            question = draft.question.trim(),
            options = draft.options.map(String::trim),
            closingSummary = pollClosingSummary(draft),
        )
    }
    return PostFeedContent(
        media = media,
        body = body.trim().takeIf(String::isNotEmpty),
        poll = pollContent,
    )
}

private fun PostMediaDraft.toFeedMedia(): PostFeedMedia = PostFeedMedia(
    model = uri.orEmpty(),
    contentDescription = imageDescription.trim().takeIf(String::isNotEmpty),
)

private fun firstInvalidField(errors: Set<PostValidationField>): PostValidationField = listOf(
    PostValidationField.CONTENT,
    PostValidationField.BEFORE_PHOTO,
    PostValidationField.AFTER_PHOTO,
    PostValidationField.POLL_QUESTION,
    PostValidationField.POLL_OPTIONS,
    PostValidationField.POLL_CLOSING,
).first { it in errors }

private const val MAX_POST_PHOTOS = CreatePostViewModel.MAX_POST_PHOTOS
private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
