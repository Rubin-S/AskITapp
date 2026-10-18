package com.askit.app.createpost

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CreatePostScreenMode {
    EDITING,
    PREVIEW,
}

enum class PostMediaLayout {
    GALLERY,
    BEFORE_AFTER,
}

enum class PostValidationField {
    CONTENT,
    BEFORE_PHOTO,
    AFTER_PHOTO,
    PHOTO_RATIO,
    POLL_QUESTION,
    POLL_OPTIONS,
    POLL_CLOSING,
}

enum class PostDisclosure {
    PHOTO_DESCRIPTION,
    CAROUSEL_DESCRIPTION,
    BEFORE_DESCRIPTION,
    AFTER_DESCRIPTION,
}

enum class PostPollClosingRule {
    AFTER_24_HOURS,
    CUSTOM_DATE,
}

enum class PollOptionError {
    EMPTY,
    DUPLICATE,
}

data class PostPublicLocation(val publicAreaLabel: String, val placeId: String?, val latitude: Double, val longitude: Double)

data class PostMediaDraft(val uri: String? = null, val imageDescription: String = "", val width: Int = 0, val height: Int = 0)

data class PostPollDraft(
    val question: String = "",
    val options: List<String> = listOf("", ""),
    val closingRule: PostPollClosingRule = PostPollClosingRule.AFTER_24_HOURS,
    val closingAtMillis: Long? = null,
)

data class PostDraft(
    val photos: List<PostMediaDraft> = emptyList(),
    val mediaLayout: PostMediaLayout = PostMediaLayout.GALLERY,
    val body: String = "",
    val poll: PostPollDraft? = null,
    val location: PostPublicLocation? = null,
)

data class CreatePostFormState(
    val photos: List<PostMediaDraft> = emptyList(),
    val mediaLayout: PostMediaLayout = PostMediaLayout.GALLERY,
    val body: String = "",
    val poll: PostPollDraft? = null,
    val location: PostPublicLocation? = null,
    val expandedDisclosures: Set<PostDisclosure> = emptySet(),
    val selectedCarouselIndex: Int = 0,
    val hasAttemptedPreview: Boolean = false,
    val previewAttempt: Int = 0,
    val screenMode: CreatePostScreenMode = CreatePostScreenMode.EDITING,
)

class CreatePostViewModel(private val savedStateHandle: SavedStateHandle? = null) : ViewModel() {

    private val _formState = MutableStateFlow(restoreState())
    val formState: StateFlow<CreatePostFormState> = _formState.asStateFlow()
    private var accountId: String? = savedStateHandle?.get("create_post_account")
    var editingPostId: String? = savedStateHandle?.get("create_post_edit_id")
        private set
    private var editBaseline: PostDraft? = null
    private var ownedCrops = savedStateHandle?.let { readStringList(it, "post_owned_crops").toSet() }.orEmpty()

    private fun setOwnedCrops(uris: Set<String>) {
        ownedCrops = uris
        savedStateHandle?.set("post_owned_crops", ArrayList(uris))
    }

    private fun discardOwnedCrops() {
        ownedCrops.forEach { deletePendingCrop(PostMediaDraft(uri = it)) }
        setOwnedCrops(emptySet())
    }

    // Transfer only the published files to the local post repository; discard unused replacements.
    fun retainPublishedCrops() {
        val published = _formState.value.photos.mapNotNull { it.uri }.toSet()
        (ownedCrops - published).forEach { deletePendingCrop(PostMediaDraft(uri = it)) }
        setOwnedCrops(emptySet())
    }
    private val _cropSession = MutableStateFlow(restoreCropSession())
    internal val cropSession: StateFlow<PostCropSession?> = _cropSession.asStateFlow()
    private val _cropError = MutableStateFlow<Int?>(null)
    internal val cropError: StateFlow<Int?> = _cropError.asStateFlow()

    internal fun beginPhotoCrop(sources: List<String>, targetIndex: Int? = null) {
        if (sources.isEmpty() || _cropSession.value != null) return
        val photos = _formState.value.photos
        if (targetIndex != null && targetIndex !in 0 until MAX_POST_PHOTOS) return
        val remaining = photos.filterIndexed { index, photo -> index != targetIndex && !photo.uri.isNullOrBlank() }
        if (remaining.size + sources.size > MAX_POST_PHOTOS) {
            _cropError.value = com.askit.app.R.string.post_crop_too_many
            return
        }
        _cropError.value = null
        setCropSession(
            PostCropSession(
                sources = sources,
                targetIndex = targetIndex,
                ratio = remaining.firstNotNullOfOrNull { it.photoRatio },
            ),
        )
    }

    internal fun cancelPhotoCrop() {
        _cropSession.value?.completed?.forEach(::deletePendingCrop)
        setCropSession(null)
    }

    internal fun acceptPhotoCrop(source: String, photo: PostMediaDraft) {
        val session = _cropSession.value
        val ratio = photo.photoRatio
        if (session == null || session.source != source || ratio == null || (session.ratio != null && ratio != session.ratio)) {
            deletePendingCrop(photo)
            return
        }
        val next = session.copy(completed = session.completed + photo, ratio = ratio)
        if (next.completed.size < next.sources.size) {
            setCropSession(next)
            return
        }
        setOwnedCrops(ownedCrops + next.completed.mapNotNull { it.uri })
        update {
            val result = if (next.targetIndex == null) {
                photos.filter { !it.uri.isNullOrBlank() } + next.completed
            } else {
                photos.toMutableList().apply {
                    while (size <= next.targetIndex) add(PostMediaDraft())
                    this[next.targetIndex] = photo
                }
            }
            copy(
                photos = result,
                mediaLayout = if (next.targetIndex == null) PostMediaLayout.GALLERY else mediaLayout,
                selectedCarouselIndex = next.targetIndex ?: result.lastIndex.coerceAtLeast(0),
            )
        }
        setCropSession(null)
    }

    private fun setCropSession(session: PostCropSession?) {
        _cropSession.value = session
        savedStateHandle?.apply {
            set("post_crop_sources", session?.sources?.let { ArrayList(it) })
            set("post_crop_target", session?.targetIndex)
            set("post_crop_ratio", session?.ratio?.name)
            set("post_crop_outputs", session?.completed?.map { it.uri.orEmpty() }?.let { ArrayList(it) })
            set("post_crop_widths", session?.completed?.map { it.width }?.let { ArrayList(it) })
            set("post_crop_heights", session?.completed?.map { it.height }?.let { ArrayList(it) })
        }
    }

    private fun restoreCropSession(): PostCropSession? {
        val handle = savedStateHandle ?: return null
        val sources = readStringList(handle, "post_crop_sources").takeIf { it.isNotEmpty() } ?: return null
        val outputs = readStringList(handle, "post_crop_outputs")
        val widths = handle.get<ArrayList<Int>>("post_crop_widths").orEmpty()
        val heights = handle.get<ArrayList<Int>>("post_crop_heights").orEmpty()
        if (outputs.size >= sources.size) return null
        return PostCropSession(
            sources,
            outputs.mapIndexed { index, uri ->
                PostMediaDraft(uri = uri, width = widths.getOrElse(index) { 0 }, height = heights.getOrElse(index) { 0 })
            },
            handle.get<Int>("post_crop_target"),
            handle.get<String>("post_crop_ratio").toEnumOrNull<PostPhotoRatio>(),
        )
    }

    fun activateAccount(id: String?) {
        val next = id?.takeIf(String::isNotBlank)
        if (next != accountId) {
            startNewDraft()
            accountId = next
        }
        savedStateHandle?.set("create_post_account", next)
    }

    fun beginEdit(postId: String, draft: PostDraft) {
        if (editingPostId == postId) {
            if (editBaseline == null) editBaseline = draft
            return
        }
        cancelPhotoCrop()
        discardOwnedCrops()
        editingPostId = postId
        savedStateHandle?.set("create_post_edit_id", postId)
        editBaseline = draft
        setState(
            CreatePostFormState(
                photos = draft.photos,
                mediaLayout = draft.mediaLayout,
                body = draft.body,
                poll = draft.poll,
                location = draft.location,
            ),
        )
    }

    fun startNewDraft() {
        cancelPhotoCrop()
        discardOwnedCrops()
        _cropError.value = null
        editingPostId = null
        editBaseline = null
        savedStateHandle?.set<String?>("create_post_edit_id", null)
        setState(CreatePostFormState())
    }

    fun updateTextBody(value: String) = update { copy(body = value) }

    fun setPhotoAt(index: Int, uri: String?) = update {
        // Filled media enters through the crop workflow; clearing a slot preserves its position.
        if (uri != null || index !in photos.indices) return@update this
        copy(photos = photos.mapIndexed { i, photo -> if (i == index) PostMediaDraft() else photo })
    }

    fun removePhotoAt(index: Int) = update {
        if (index !in photos.indices) return@update this
        val next = photos.toMutableList().also { it.removeAt(index) }
        copy(
            photos = next,
            selectedCarouselIndex = selectedCarouselIndex.coerceAtMost(next.lastIndex.coerceAtLeast(0)),
        )
    }

    fun selectPhoto(index: Int) = update {
        if (index in photos.indices) copy(selectedCarouselIndex = index) else this
    }

    fun movePhoto(index: Int, direction: Int) = update {
        val target = index + direction
        if (index !in photos.indices || target !in photos.indices) return@update this
        val next = photos.toMutableList()
        val moved = next.removeAt(index)
        next.add(target, moved)
        val selected = when (selectedCarouselIndex) {
            index -> target
            target -> index
            else -> selectedCarouselIndex
        }
        copy(photos = next, selectedCarouselIndex = selected)
    }

    fun updatePhotoDescription(index: Int, value: String) = update {
        if (index !in photos.indices) return@update this
        copy(
            photos = photos.mapIndexed { photoIndex, photo ->
                if (photoIndex == index) photo.copy(imageDescription = value) else photo
            },
        )
    }

    fun setMediaLayout(layout: PostMediaLayout) = update {
        if (layout == mediaLayout) return@update this
        val nextPhotos = when (layout) {
            PostMediaLayout.GALLERY -> photos.filter { !it.uri.isNullOrBlank() }

            PostMediaLayout.BEFORE_AFTER -> {
                val kept = photos.filter { !it.uri.isNullOrBlank() }
                if (kept.size >= 2) kept.take(2) else kept + List(2 - kept.size) { PostMediaDraft() }
            }
        }
        copy(
            mediaLayout = layout,
            photos = nextPhotos,
            selectedCarouselIndex = 0,
        )
    }

    fun addPoll() = update { if (poll == null) copy(poll = PostPollDraft()) else this }

    fun removePoll() = update { copy(poll = null) }

    fun updatePollQuestion(value: String) = update {
        val current = poll ?: return@update this
        copy(poll = current.copy(question = value))
    }

    fun updatePollOption(index: Int, value: String) = update {
        val current = poll ?: return@update this
        if (index !in current.options.indices) return@update this
        copy(
            poll = current.copy(
                options = current.options.mapIndexed { optionIndex, option ->
                    if (optionIndex == index) value else option
                },
            ),
        )
    }

    fun addPollOption() = update {
        val current = poll ?: return@update this
        if (current.options.size >= MAX_POLL_OPTIONS) return@update this
        copy(poll = current.copy(options = current.options + ""))
    }

    fun removePollOption(index: Int) = update {
        val current = poll ?: return@update this
        if (current.options.size <= MIN_POLL_OPTIONS || index !in current.options.indices) {
            return@update this
        }
        copy(
            poll = current.copy(
                options = current.options.toMutableList().also { it.removeAt(index) },
            ),
        )
    }

    fun setPollClosingRule(rule: PostPollClosingRule, closingAtMillis: Long? = null) = update {
        val current = poll ?: return@update this
        copy(
            poll = current.copy(
                closingRule = rule,
                closingAtMillis = closingAtMillis.takeIf { rule == PostPollClosingRule.CUSTOM_DATE },
            ),
        )
    }

    fun toggleDisclosure(disclosure: PostDisclosure) = update {
        copy(
            expandedDisclosures = if (disclosure in expandedDisclosures) {
                expandedDisclosures - disclosure
            } else {
                expandedDisclosures + disclosure
            },
        )
    }

    fun setLocation(location: PostPublicLocation) = update { copy(location = location) }

    fun removeLocation() = update { copy(location = null) }

    fun preview(): Boolean {
        val attemptedState = _formState.value.copy(
            hasAttemptedPreview = true,
            previewAttempt = _formState.value.previewAttempt + 1,
        )
        setState(attemptedState)
        if (validate(attemptedState).isNotEmpty()) return false
        setState(attemptedState.copy(screenMode = CreatePostScreenMode.PREVIEW))
        return true
    }

    fun edit() = update { copy(screenMode = CreatePostScreenMode.EDITING) }

    fun validationErrors(): Set<PostValidationField> = if (_formState.value.hasAttemptedPreview) validate(_formState.value) else emptySet()

    fun pollOptionError(index: Int): PollOptionError? {
        val poll = _formState.value.poll ?: return null
        if (index !in poll.options.indices) return null
        val option = poll.options[index].trim()
        if (option.isEmpty()) return PollOptionError.EMPTY
        val duplicate = poll.options
            .map(String::trim)
            .withIndex()
            .any { (otherIndex, other) -> otherIndex != index && other.equals(option, ignoreCase = true) }
        return PollOptionError.DUPLICATE.takeIf { duplicate }
    }

    fun buildValidatedDraft(): PostDraft? {
        val state = _formState.value
        if (validate(state).isNotEmpty()) return null
        return state.toDraft()
    }

    val isDirty: Boolean
        get() = if (editingPostId != null &&
            editBaseline != null
        ) {
            _formState.value.toDraft() != editBaseline
        } else {
            _formState.value.hasMeaningfulChanges
        }

    private fun update(transform: CreatePostFormState.() -> CreatePostFormState) {
        setState(_formState.value.transform())
    }

    private fun setState(next: CreatePostFormState) {
        _formState.value = next
        persist(next)
    }

    private fun persist(state: CreatePostFormState) {
        val handle = savedStateHandle ?: return
        handle[MEDIA_LAYOUT_KEY] = state.mediaLayout.name
        handle[PHOTO_URIS_KEY] = ArrayList(state.photos.map { it.uri.orEmpty() })
        handle["create_post_photo_widths"] = ArrayList(state.photos.map { it.width })
        handle["create_post_photo_heights"] = ArrayList(state.photos.map { it.height })
        handle[PHOTO_DESCRIPTIONS_KEY] = ArrayList(state.photos.map(PostMediaDraft::imageDescription))
        handle[TEXT_BODY_KEY] = state.body
        handle[HAS_POLL_KEY] = state.poll != null
        handle[POLL_QUESTION_KEY] = state.poll?.question
        handle[POLL_OPTIONS_KEY] = state.poll?.let { ArrayList(it.options) }
        handle[POLL_CLOSING_RULE_KEY] = state.poll?.closingRule?.name
        handle[POLL_CLOSING_AT_KEY] = state.poll?.closingAtMillis
        handle[LOCATION_LABEL_KEY] = state.location?.publicAreaLabel
        handle[LOCATION_PLACE_ID_KEY] = state.location?.placeId
        handle[LOCATION_LATITUDE_KEY] = state.location?.latitude
        handle[LOCATION_LONGITUDE_KEY] = state.location?.longitude
        handle[EXPANDED_KEY] = ArrayList(state.expandedDisclosures.map { it.name })
        handle[SELECTED_CAROUSEL_INDEX_KEY] = state.selectedCarouselIndex
        handle[ATTEMPTED_KEY] = state.hasAttemptedPreview
        handle[PREVIEW_ATTEMPT_KEY] = state.previewAttempt
        handle[SCREEN_MODE_KEY] = state.screenMode.name
    }

    private fun restoreState(): CreatePostFormState {
        val handle = savedStateHandle ?: return CreatePostFormState()
        val uris = readStringList(handle, PHOTO_URIS_KEY)
        val descriptions = readStringList(handle, PHOTO_DESCRIPTIONS_KEY)
        val widths = handle.get<ArrayList<Int>>("create_post_photo_widths").orEmpty()
        val heights = handle.get<ArrayList<Int>>("create_post_photo_heights").orEmpty()
        val photos = uris.mapIndexed { index, uri ->
            PostMediaDraft(
                uri = uri.takeIf(String::isNotBlank),
                imageDescription = descriptions.getOrNull(index).orEmpty(),
                width = widths.getOrElse(index) { 0 },
                height = heights.getOrElse(index) { 0 },
            )
        }
        val poll = if (handle.get<Boolean>(HAS_POLL_KEY) == true) {
            PostPollDraft(
                question = handle.get<String>(POLL_QUESTION_KEY).orEmpty(),
                options = readStringList(handle, POLL_OPTIONS_KEY)
                    .ifEmpty { listOf("", "") }
                    .take(MAX_POLL_OPTIONS),
                closingRule = handle.get<String>(POLL_CLOSING_RULE_KEY)
                    .toEnumOrNull<PostPollClosingRule>()
                    ?: PostPollClosingRule.AFTER_24_HOURS,
                closingAtMillis = handle.get<Long>(POLL_CLOSING_AT_KEY),
            )
        } else {
            null
        }
        val location = handle.get<String>(LOCATION_LABEL_KEY)?.takeIf(String::isNotBlank)?.let {
            PostPublicLocation(
                publicAreaLabel = it,
                placeId = handle.get<String>(LOCATION_PLACE_ID_KEY),
                latitude = handle.get<Double>(LOCATION_LATITUDE_KEY) ?: 0.0,
                longitude = handle.get<Double>(LOCATION_LONGITUDE_KEY) ?: 0.0,
            )
        }
        return CreatePostFormState(
            photos = photos,
            mediaLayout = handle.get<String>(MEDIA_LAYOUT_KEY)
                .toEnumOrNull<PostMediaLayout>()
                ?: PostMediaLayout.GALLERY,
            body = handle.get<String>(TEXT_BODY_KEY).orEmpty(),
            poll = poll,
            location = location,
            expandedDisclosures = readStringList(handle, EXPANDED_KEY)
                .mapNotNull { it.toEnumOrNull<PostDisclosure>() }
                .toSet(),
            selectedCarouselIndex = handle.get<Int>(SELECTED_CAROUSEL_INDEX_KEY) ?: 0,
            hasAttemptedPreview = handle.get<Boolean>(ATTEMPTED_KEY) ?: false,
            previewAttempt = handle.get<Int>(PREVIEW_ATTEMPT_KEY) ?: 0,
            screenMode = handle.get<String>(SCREEN_MODE_KEY)
                .toEnumOrNull<CreatePostScreenMode>()
                ?: CreatePostScreenMode.EDITING,
        )
    }

    internal companion object {
        const val MAX_POST_PHOTOS = 10
        const val MIN_POLL_OPTIONS = 2
        const val MAX_POLL_OPTIONS = 6
        const val MEDIA_LAYOUT_KEY = "create_post_media_layout"
        const val PHOTO_URIS_KEY = "create_post_photo_uris"
        const val PHOTO_DESCRIPTIONS_KEY = "create_post_photo_descriptions"
        const val LOCATION_LABEL_KEY = "create_post_location_label"
        const val LOCATION_PLACE_ID_KEY = "create_post_location_place_id"
        const val LOCATION_LATITUDE_KEY = "create_post_location_latitude"
        const val LOCATION_LONGITUDE_KEY = "create_post_location_longitude"
        const val EXPANDED_KEY = "create_post_expanded_disclosures"
        const val SELECTED_CAROUSEL_INDEX_KEY = "create_post_selected_carousel_index"
        const val ATTEMPTED_KEY = "create_post_attempted_preview"
        const val PREVIEW_ATTEMPT_KEY = "create_post_preview_attempt"
        const val SCREEN_MODE_KEY = "create_post_screen_mode"
        const val TEXT_BODY_KEY = "create_post_text_body"
        const val HAS_POLL_KEY = "create_post_has_poll"
        const val POLL_QUESTION_KEY = "create_post_poll_question"
        const val POLL_OPTIONS_KEY = "create_post_poll_options"
        const val POLL_CLOSING_RULE_KEY = "create_post_poll_closing_rule"
        const val POLL_CLOSING_AT_KEY = "create_post_poll_closing_at"

        fun readStringList(handle: SavedStateHandle, key: String): List<String> = handle.get<ArrayList<String>>(key)?.toList().orEmpty()
    }
}

private val CreatePostFormState.hasMeaningfulChanges: Boolean
    get() = photos.any { !it.uri.isNullOrBlank() || it.imageDescription.isNotBlank() } ||
        body.isNotBlank() ||
        poll.hasMeaningfulChanges ||
        location != null

private val PostPollDraft?.hasMeaningfulChanges: Boolean
    get() {
        val poll = this ?: return false
        return poll.question.isNotBlank() ||
            poll.options.any(String::isNotBlank) ||
            poll.closingRule != PostPollClosingRule.AFTER_24_HOURS
    }

private fun validate(state: CreatePostFormState): Set<PostValidationField> = buildSet {
    val hasMedia = state.filledPhotos().isNotEmpty()
    val ratios = state.filledPhotos().map { it.photoRatio }
    if (ratios.any { it == null } || ratios.distinct().size > 1) add(PostValidationField.PHOTO_RATIO)
    val hasBody = state.body.isNotBlank()
    val hasPoll = state.poll != null
    if (!hasMedia && !hasBody && !hasPoll) add(PostValidationField.CONTENT)
    if (state.mediaLayout == PostMediaLayout.BEFORE_AFTER) {
        if (state.photos.getOrNull(0)?.uri.isNullOrBlank()) add(PostValidationField.BEFORE_PHOTO)
        if (state.photos.getOrNull(1)?.uri.isNullOrBlank()) add(PostValidationField.AFTER_PHOTO)
    }
    val poll = state.poll
    if (poll != null) {
        if (poll.question.isBlank()) add(PostValidationField.POLL_QUESTION)
        if (poll.options.size !in MIN_POLL_OPTIONS..MAX_POLL_OPTIONS || poll.options.any(String::isBlank)) {
            add(PostValidationField.POLL_OPTIONS)
        } else if (poll.options.map { it.trim().lowercase() }.toSet().size != poll.options.size) {
            add(PostValidationField.POLL_OPTIONS)
        }
        if (
            poll.closingRule == PostPollClosingRule.CUSTOM_DATE &&
            (poll.closingAtMillis == null || poll.closingAtMillis <= System.currentTimeMillis())
        ) {
            add(PostValidationField.POLL_CLOSING)
        }
    }
}

private fun CreatePostFormState.toDraft(): PostDraft = PostDraft(
    photos = filledPhotos().map { photo ->
        photo.copy(
            uri = requireNotNull(photo.uri).trim(),
            imageDescription = photo.imageDescription.trim(),
        )
    },
    mediaLayout = mediaLayout,
    body = body.trim(),
    poll = poll?.copy(
        question = poll.question.trim(),
        options = poll.options.map(String::trim),
    ),
    location = location?.takeIf { it.publicAreaLabel.isNotBlank() },
)

private fun CreatePostFormState.filledPhotos(): List<PostMediaDraft> = photos.filter { !it.uri.isNullOrBlank() }

internal fun PostDraft.isValidPostDraft(): Boolean =
    photos.size <= CreatePostViewModel.MAX_POST_PHOTOS && photos.all { !it.uri.isNullOrBlank() } &&
        validate(CreatePostFormState(photos = photos, mediaLayout = mediaLayout, body = body, poll = poll, location = location)).isEmpty()

private const val MIN_POLL_OPTIONS = CreatePostViewModel.MIN_POLL_OPTIONS
private const val MAX_POLL_OPTIONS = CreatePostViewModel.MAX_POLL_OPTIONS

private inline fun <reified T : Enum<T>> String?.toEnumOrNull(): T? =
    this?.let { value -> enumValues<T>().firstOrNull { it.name == value } }
