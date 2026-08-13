package com.askit.app.createpost

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.askit.designsystem.posts.BeforeAfterSlider
import com.askit.designsystem.posts.PostFeedAuthor
import com.askit.designsystem.posts.PostFeedContent
import com.askit.designsystem.posts.PostFeedItem
import com.askit.designsystem.posts.PostFeedMedia
import com.askit.designsystem.posts.PostFeedMediaContent
import com.askit.designsystem.posts.PostFeedPoll
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
                .imePadding()
                .testTag("create_post_content"),
            contentPadding = PaddingValues(
                horizontal = if (state.screenMode == CreatePostScreenMode.PREVIEW) 0.dp else 16.dp,
                vertical = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                if (state.screenMode == CreatePostScreenMode.PREVIEW) {
                    CreatePostPreview(state = state, onEdit = viewModel::edit)
                } else {
                    CreatePostEditor(
                        viewModel = viewModel,
                        state = state,
                        validationErrors = validationErrors,
                        mediaBringIntoView = mediaBringIntoView,
                        bodyBringIntoView = bodyBringIntoView,
                        pollQuestionBringIntoView = pollQuestionBringIntoView,
                        pollOptionsBringIntoView = pollOptionsBringIntoView,
                        bodyFocusRequester = bodyFocusRequester,
                        pollQuestionFocusRequester = pollQuestionFocusRequester,
                        onOpenLocationPicker = onOpenLocationPicker,
                        onPickGallery = {
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
    mediaBringIntoView: BringIntoViewRequester,
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CreatePostIdentity()
        PostLocationEditor(
            location = state.location,
            onAddOrChange = onOpenLocationPicker,
            onRemove = viewModel::removeLocation,
        )
        MediaSection(
            state = state,
            validationErrors = validationErrors,
            bringIntoViewRequester = mediaBringIntoView,
            onPickGallery = onPickGallery,
            onPickSlot = onPickSlot,
            onToggleLayout = {
                viewModel.setMediaLayout(
                    if (state.mediaLayout == PostMediaLayout.BEFORE_AFTER) {
                        PostMediaLayout.GALLERY
                    } else {
                        PostMediaLayout.BEFORE_AFTER
                    },
                )
            },
            onSelect = viewModel::selectPhoto,
            onRemove = viewModel::removePhotoAt,
            onClearSlot = { index -> viewModel.setPhotoAt(index, null) },
            onMove = viewModel::movePhoto,
            onDescriptionChange = viewModel::updatePhotoDescription,
            onToggleDisclosure = viewModel::toggleDisclosure,
        )
        OutlinedTextField(
            value = state.body,
            onValueChange = viewModel::updateTextBody,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(bodyBringIntoView)
                .focusRequester(bodyFocusRequester)
                .testTag("create_post_text_body"),
            label = { Text(stringResource(R.string.create_post_whats_on_your_mind)) },
            supportingText = {
                if (PostValidationField.CONTENT in validationErrors) {
                    Text(stringResource(R.string.create_post_error_content))
                }
            },
            isError = PostValidationField.CONTENT in validationErrors,
            minLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
        )
        if (state.poll == null) {
            TextButton(
                onClick = viewModel::addPoll,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("create_post_add_poll"),
            ) {
                Text(stringResource(R.string.create_post_add_poll))
            }
        } else {
            PollPostEditor(
                viewModel = viewModel,
                content = state.poll,
                validationErrors = validationErrors,
                questionBringIntoViewRequester = pollQuestionBringIntoView,
                optionsBringIntoViewRequester = pollOptionsBringIntoView,
                questionFocusRequester = pollQuestionFocusRequester,
                onOpenClosingDatePicker = onOpenClosingDatePicker,
            )
            TextButton(
                onClick = viewModel::removePoll,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("create_post_remove_poll"),
            ) {
                Text(stringResource(R.string.create_post_remove_poll))
            }
        }
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

@Composable
private fun MediaSection(
    state: CreatePostFormState,
    validationErrors: Set<PostValidationField>,
    bringIntoViewRequester: BringIntoViewRequester,
    onPickGallery: () -> Unit,
    onPickSlot: (BeforeAfterSlot) -> Unit,
    onToggleLayout: () -> Unit,
    onSelect: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onClearSlot: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onDescriptionChange: (Int, String) -> Unit,
    onToggleDisclosure: (PostDisclosure) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FilterChip(
            selected = state.mediaLayout == PostMediaLayout.BEFORE_AFTER,
            onClick = onToggleLayout,
            label = { Text(stringResource(R.string.create_post_before_after)) },
            modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag("create_post_before_after_chip"),
        )
        if (state.mediaLayout == PostMediaLayout.BEFORE_AFTER) {
            BeforeAfterEditor(
                photos = state.photos,
                validationErrors = validationErrors,
                beforeDescriptionExpanded = PostDisclosure.BEFORE_DESCRIPTION in state.expandedDisclosures,
                afterDescriptionExpanded = PostDisclosure.AFTER_DESCRIPTION in state.expandedDisclosures,
                onPickSlot = onPickSlot,
                onRemove = onClearSlot,
                onDescriptionChange = onDescriptionChange,
                onToggleBeforeDescription = { onToggleDisclosure(PostDisclosure.BEFORE_DESCRIPTION) },
                onToggleAfterDescription = { onToggleDisclosure(PostDisclosure.AFTER_DESCRIPTION) },
            )
        } else {
            GalleryEditor(
                photos = state.photos,
                selectedIndex = state.selectedCarouselIndex,
                isDescriptionExpanded = PostDisclosure.CAROUSEL_DESCRIPTION in state.expandedDisclosures ||
                    PostDisclosure.PHOTO_DESCRIPTION in state.expandedDisclosures,
                onPickGallery = onPickGallery,
                onPickReplace = { onPickSlot(BeforeAfterSlot.REPLACE) },
                onSelect = onSelect,
                onRemove = onRemove,
                onMove = onMove,
                onDescriptionChange = onDescriptionChange,
                onToggleDescription = {
                    onToggleDisclosure(
                        if (state.photos.size > 1) {
                            PostDisclosure.CAROUSEL_DESCRIPTION
                        } else {
                            PostDisclosure.PHOTO_DESCRIPTION
                        },
                    )
                },
            )
        }
    }
}

@Composable
private fun GalleryEditor(
    photos: List<PostMediaDraft>,
    selectedIndex: Int,
    isDescriptionExpanded: Boolean,
    onPickGallery: () -> Unit,
    onPickReplace: () -> Unit,
    onSelect: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onDescriptionChange: (Int, String) -> Unit,
    onToggleDescription: () -> Unit,
) {
    if (photos.isEmpty()) {
        EmptyMediaPrompt(
            title = R.string.create_post_photos_empty_title,
            buttonLabel = R.string.create_post_add_photos,
            onClick = onPickGallery,
            testTag = "create_post_add_photos",
        )
        return
    }
    val selected = selectedIndex.coerceIn(0, photos.lastIndex)
    val selectedItem = photos[selected]
    if (photos.size == 1) {
        ComposerImage(
            uri = selectedItem.uri,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp, max = 480.dp),
            contentDescription = selectedItem.imageDescription.ifBlank {
                stringResource(R.string.create_post_photo_content_description)
            },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = onPickReplace,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("create_post_replace_photo"),
            ) {
                Text(stringResource(R.string.create_post_replace))
            }
            TextButton(
                onClick = { onRemove(0) },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("create_post_remove_photo"),
            ) {
                Text(stringResource(R.string.create_post_remove))
            }
        }
    } else {
        Text(
            text = pluralStringResource(
                R.plurals.create_post_carousel_count,
                photos.size,
                photos.size,
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
            photos.forEachIndexed { index, item ->
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
                            onClickLabel = stringResource(R.string.create_post_select_image, index + 1),
                            onClick = { onSelect(index) },
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
                }
            }
        }
        ComposerImage(
            uri = selectedItem.uri,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp, max = 480.dp),
            contentDescription = stringResource(
                R.string.create_post_image_description_number,
                selected + 1,
            ),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(
                onClick = { onMove(selected, -1) },
                enabled = selected > 0,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("create_post_carousel_move_earlier"),
            ) {
                Text(stringResource(R.string.create_post_move_earlier))
            }
            TextButton(
                onClick = { onMove(selected, 1) },
                enabled = selected < photos.lastIndex,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("create_post_carousel_move_later"),
            ) {
                Text(stringResource(R.string.create_post_move_later))
            }
            TextButton(
                onClick = { onRemove(selected) },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("create_post_carousel_remove"),
            ) {
                Text(stringResource(R.string.create_post_remove))
            }
        }
    }
    if (photos.size < MAX_POST_PHOTOS) {
        OutlinedButton(
            onClick = onPickGallery,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("create_post_add_more_carousel_photos"),
        ) {
            Text(stringResource(R.string.create_post_add_photos))
        }
    }
    DisclosureButton(
        expanded = isDescriptionExpanded,
        collapsedLabel = R.string.create_post_add_image_description,
        expandedLabel = R.string.create_post_hide_image_description,
        onClick = onToggleDescription,
        testTag = if (photos.size > 1) {
            "create_post_carousel_description_toggle"
        } else {
            "create_post_photo_description_toggle"
        },
    )
    if (isDescriptionExpanded) {
        OutlinedTextField(
            value = selectedItem.imageDescription,
            onValueChange = { onDescriptionChange(selected, it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(
                    if (photos.size > 1) "create_post_carousel_description" else "create_post_photo_description",
                ),
            label = { Text(stringResource(R.string.create_post_image_description_optional)) },
            supportingText = { Text(stringResource(R.string.create_post_image_description_helper)) },
            minLines = 3,
        )
    }
}

@Composable
private fun BeforeAfterEditor(
    photos: List<PostMediaDraft>,
    validationErrors: Set<PostValidationField>,
    beforeDescriptionExpanded: Boolean,
    afterDescriptionExpanded: Boolean,
    onPickSlot: (BeforeAfterSlot) -> Unit,
    onRemove: (Int) -> Unit,
    onDescriptionChange: (Int, String) -> Unit,
    onToggleBeforeDescription: () -> Unit,
    onToggleAfterDescription: () -> Unit,
) {
    val before = photos.getOrNull(0) ?: PostMediaDraft()
    val after = photos.getOrNull(1) ?: PostMediaDraft()
    if (!before.uri.isNullOrBlank() && !after.uri.isNullOrBlank()) {
        BeforeAfterSlider(
            before = PostFeedMedia(
                model = before.uri,
                contentDescription = before.imageDescription.trim().takeIf(String::isNotEmpty),
            ),
            after = PostFeedMedia(
                model = after.uri,
                contentDescription = after.imageDescription.trim().takeIf(String::isNotEmpty),
            ),
        )
        BeforeAfterSlotEditor(
            label = R.string.create_post_before,
            content = before,
            isError = false,
            isDescriptionExpanded = beforeDescriptionExpanded,
            onPick = { onPickSlot(BeforeAfterSlot.BEFORE) },
            onRemove = { onRemove(0) },
            onDescriptionChange = { onDescriptionChange(0, it) },
            onToggleDescription = onToggleBeforeDescription,
            showImage = false,
            testTag = "create_post_before_slot",
        )
        BeforeAfterSlotEditor(
            label = R.string.create_post_after,
            content = after,
            isError = false,
            isDescriptionExpanded = afterDescriptionExpanded,
            onPick = { onPickSlot(BeforeAfterSlot.AFTER) },
            onRemove = { onRemove(1) },
            onDescriptionChange = { onDescriptionChange(1, it) },
            onToggleDescription = onToggleAfterDescription,
            showImage = false,
            testTag = "create_post_after_slot",
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BeforeAfterSlotEditor(
            label = R.string.create_post_before,
            content = before,
            isError = PostValidationField.BEFORE_PHOTO in validationErrors,
            isDescriptionExpanded = beforeDescriptionExpanded,
            onPick = { onPickSlot(BeforeAfterSlot.BEFORE) },
            onRemove = { onRemove(0) },
            onDescriptionChange = { onDescriptionChange(0, it) },
            onToggleDescription = onToggleBeforeDescription,
            testTag = "create_post_before_slot",
        )
        BeforeAfterSlotEditor(
            label = R.string.create_post_after,
            content = after,
            isError = PostValidationField.AFTER_PHOTO in validationErrors,
            isDescriptionExpanded = afterDescriptionExpanded,
            onPick = { onPickSlot(BeforeAfterSlot.AFTER) },
            onRemove = { onRemove(1) },
            onDescriptionChange = { onDescriptionChange(1, it) },
            onToggleDescription = onToggleAfterDescription,
            testTag = "create_post_after_slot",
        )
    }
}

@Composable
private fun BeforeAfterSlotEditor(
    @StringRes label: Int,
    content: PostMediaDraft,
    isError: Boolean,
    isDescriptionExpanded: Boolean,
    onPick: () -> Unit,
    onRemove: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onToggleDescription: () -> Unit,
    modifier: Modifier = Modifier,
    showImage: Boolean = true,
    testTag: String,
) {
    Column(
        modifier = modifier.testTag(testTag),
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
                PostError(
                    if (label == R.string.create_post_before) {
                        R.string.create_post_error_before
                    } else {
                        R.string.create_post_error_after
                    },
                )
            }
        } else {
            if (showImage) {
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
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onPick, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.create_post_replace))
                }
                TextButton(onClick = onRemove, modifier = Modifier.heightIn(min = 48.dp)) {
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
    content: PostPollDraft,
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
                        label = { Text(stringResource(R.string.create_post_poll_option, index + 1)) },
                        supportingText = {
                            when (optionError) {
                                PollOptionError.EMPTY -> Text(stringResource(R.string.create_post_error_poll_answer))
                                PollOptionError.DUPLICATE -> Text(stringResource(R.string.create_post_error_poll_duplicate))
                                null -> Unit
                            }
                        },
                        isError = optionError != null && PostValidationField.POLL_OPTIONS in validationErrors,
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
private fun pollClosingSummary(content: PostPollDraft): String = when (content.closingRule) {
    PostPollClosingRule.AFTER_24_HOURS -> stringResource(R.string.create_post_closes_24_hours)
    PostPollClosingRule.CUSTOM_DATE -> content.closingAtMillis?.let { millis ->
        stringResource(R.string.create_post_closes_on, formatDate(millis))
    } ?: stringResource(R.string.create_post_closes_24_hours)
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

private fun formatDate(millis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(Date(millis))

private enum class BeforeAfterSlot {
    BEFORE,
    AFTER,
    REPLACE,
}

private const val MAX_POST_PHOTOS = CreatePostViewModel.MAX_POST_PHOTOS
private const val MAX_POLL_OPTIONS = CreatePostViewModel.MAX_POLL_OPTIONS
private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
