package com.askit.app.createpost

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PostType {
    TEXT,
    PHOTO,
    CAROUSEL,
    BEFORE_AFTER,
    POLL,
}

enum class CreatePostScreenMode {
    EDITING,
    PREVIEW,
}

enum class PostValidationField {
    TEXT_BODY,
    PHOTO,
    CAROUSEL,
    BEFORE_PHOTO,
    AFTER_PHOTO,
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

data class PostPublicLocation(
    val publicAreaLabel: String,
    val placeId: String?,
    val latitude: Double,
    val longitude: Double,
)

data class PostMediaDraft(
    val uri: String? = null,
    val imageDescription: String = "",
)

data class PostCarouselItem(
    val uri: String? = null,
    val imageDescription: String = "",
)

sealed interface PostContentDraft {
    val type: PostType

    data class Text(
        val body: String = "",
    ) : PostContentDraft {
        override val type: PostType = PostType.TEXT
    }

    data class Photo(
        val uri: String? = null,
        val caption: String = "",
        val imageDescription: String = "",
    ) : PostContentDraft {
        override val type: PostType = PostType.PHOTO
    }

    data class Carousel(
        val items: List<PostCarouselItem> = emptyList(),
        val caption: String = "",
    ) : PostContentDraft {
        override val type: PostType = PostType.CAROUSEL
    }

    data class BeforeAfter(
        val before: PostMediaDraft = PostMediaDraft(),
        val after: PostMediaDraft = PostMediaDraft(),
        val caption: String = "",
        val beforeNote: String = "",
        val afterNote: String = "",
    ) : PostContentDraft {
        override val type: PostType = PostType.BEFORE_AFTER
    }

    data class Poll(
        val question: String = "",
        val options: List<String> = listOf("", ""),
        val description: String = "",
        val closingRule: PostPollClosingRule = PostPollClosingRule.AFTER_24_HOURS,
        val closingAtMillis: Long? = null,
    ) : PostContentDraft {
        override val type: PostType = PostType.POLL
    }
}

data class PostDraft(
    val location: PostPublicLocation? = null,
    val content: PostContentDraft,
) {
    val type: PostType
        get() = content.type
}

data class CreatePostFormState(
    val content: PostContentDraft = PostContentDraft.Text(),
    val location: PostPublicLocation? = null,
    val expandedDisclosures: Set<PostDisclosure> = emptySet(),
    val selectedCarouselIndex: Int = 0,
    val hasAttemptedPreview: Boolean = false,
    val previewAttempt: Int = 0,
    val screenMode: CreatePostScreenMode = CreatePostScreenMode.EDITING,
)

class CreatePostViewModel(
    private val savedStateHandle: SavedStateHandle? = null,
) : ViewModel() {

    private val _formState = MutableStateFlow(restoreState())
    val formState: StateFlow<CreatePostFormState> = _formState.asStateFlow()

    fun startNewDraft() {
        setState(CreatePostFormState())
    }

    /** Returns true when the caller must confirm type-specific data loss. */
    fun selectType(type: PostType): Boolean {
        val current = _formState.value
        if (current.content.type == type) return false
        if (current.content.hasMeaningfulChanges) return true
        changeType(type)
        return false
    }

    fun confirmTypeChange(type: PostType) {
        if (_formState.value.content.type != type) changeType(type)
    }

    fun updateTextBody(value: String) = update { copy(content = PostContentDraft.Text(value)) }

    fun setPhoto(uri: String?) = update {
        val current = content as? PostContentDraft.Photo ?: return@update this
        copy(
            content = current.copy(
                uri = uri,
                imageDescription = if (uri == null || uri != current.uri) "" else current.imageDescription,
            ),
        )
    }

    fun removePhoto() = update {
        val current = content as? PostContentDraft.Photo ?: return@update this
        copy(content = current.copy(uri = null, imageDescription = ""))
    }

    fun updatePhotoCaption(value: String) = update {
        val current = content as? PostContentDraft.Photo ?: return@update this
        copy(content = current.copy(caption = value))
    }

    fun updatePhotoImageDescription(value: String) = update {
        val current = content as? PostContentDraft.Photo ?: return@update this
        copy(content = current.copy(imageDescription = value))
    }

    fun addCarouselMedia(uris: List<String>) {
        val current = _formState.value
        val content = current.content as? PostContentDraft.Carousel ?: return
        val added = uris.map(String::trim).filter(String::isNotEmpty)
        val items = (content.items + added.map { PostCarouselItem(uri = it) })
            .distinctBy(PostCarouselItem::uri)
            .take(MAX_CAROUSEL_ITEMS)
        setState(
            current.copy(
                content = content.copy(items = items),
                selectedCarouselIndex = items.lastIndex.coerceAtLeast(0),
            ),
        )
    }

    fun selectCarouselItem(index: Int) {
        val content = _formState.value.content as? PostContentDraft.Carousel ?: return
        if (index in content.items.indices) update { copy(selectedCarouselIndex = index) }
    }

    fun removeCarouselMedia(index: Int) {
        val current = _formState.value
        val content = current.content as? PostContentDraft.Carousel ?: return
        if (index !in content.items.indices) return
        val items = content.items.toMutableList().also { it.removeAt(index) }
        setState(
            current.copy(
                content = content.copy(items = items),
                selectedCarouselIndex = current.selectedCarouselIndex
                    .coerceAtMost(items.lastIndex.coerceAtLeast(0)),
            ),
        )
    }

    fun moveCarouselItem(index: Int, direction: Int) {
        val current = _formState.value
        val content = current.content as? PostContentDraft.Carousel ?: return
        val target = index + direction
        if (index !in content.items.indices || target !in content.items.indices) return
        val items = content.items.toMutableList()
        val moved = items.removeAt(index)
        items.add(target, moved)
        val selectedIndex = when (current.selectedCarouselIndex) {
            index -> target
            target -> index
            else -> current.selectedCarouselIndex
        }
        setState(current.copy(content = content.copy(items = items), selectedCarouselIndex = selectedIndex))
    }

    fun updateCarouselItemDescription(index: Int, value: String) {
        update {
            val current = content as? PostContentDraft.Carousel ?: return@update this
            if (index !in current.items.indices) return@update this
            copy(
                content = current.copy(
                    items = current.items.mapIndexed { itemIndex, item ->
                        if (itemIndex == index) item.copy(imageDescription = value) else item
                    },
                ),
            )
        }
    }

    fun updateCarouselCaption(value: String) = update {
        val current = content as? PostContentDraft.Carousel ?: return@update this
        copy(content = current.copy(caption = value))
    }

    fun setBeforePhoto(uri: String?) = update {
        val current = content as? PostContentDraft.BeforeAfter ?: return@update this
        copy(content = current.copy(before = PostMediaDraft(uri = uri)))
    }

    fun setAfterPhoto(uri: String?) = update {
        val current = content as? PostContentDraft.BeforeAfter ?: return@update this
        copy(content = current.copy(after = PostMediaDraft(uri = uri)))
    }

    fun removeBeforePhoto() = setBeforePhoto(null)

    fun removeAfterPhoto() = setAfterPhoto(null)

    fun updateBeforeImageDescription(value: String) = update {
        val current = content as? PostContentDraft.BeforeAfter ?: return@update this
        copy(content = current.copy(before = current.before.copy(imageDescription = value)))
    }

    fun updateAfterImageDescription(value: String) = update {
        val current = content as? PostContentDraft.BeforeAfter ?: return@update this
        copy(content = current.copy(after = current.after.copy(imageDescription = value)))
    }

    fun updateBeforeAfterCaption(value: String) = update {
        val current = content as? PostContentDraft.BeforeAfter ?: return@update this
        copy(content = current.copy(caption = value))
    }

    fun updateBeforeNote(value: String) = update {
        val current = content as? PostContentDraft.BeforeAfter ?: return@update this
        copy(content = current.copy(beforeNote = value))
    }

    fun updateAfterNote(value: String) = update {
        val current = content as? PostContentDraft.BeforeAfter ?: return@update this
        copy(content = current.copy(afterNote = value))
    }

    fun updatePollQuestion(value: String) = update {
        val current = content as? PostContentDraft.Poll ?: return@update this
        copy(content = current.copy(question = value))
    }

    fun updatePollOption(index: Int, value: String) = update {
        val current = content as? PostContentDraft.Poll ?: return@update this
        if (index !in current.options.indices) return@update this
        copy(content = current.copy(options = current.options.mapIndexed { optionIndex, option ->
            if (optionIndex == index) value else option
        }))
    }

    fun addPollOption() = update {
        val current = content as? PostContentDraft.Poll ?: return@update this
        if (current.options.size >= MAX_POLL_OPTIONS) return@update this
        copy(content = current.copy(options = current.options + ""))
    }

    fun removePollOption(index: Int) = update {
        val current = content as? PostContentDraft.Poll ?: return@update this
        if (current.options.size <= MIN_POLL_OPTIONS || index !in current.options.indices) {
            return@update this
        }
        copy(content = current.copy(options = current.options.toMutableList().also { it.removeAt(index) }))
    }

    fun updatePollDescription(value: String) = update {
        val current = content as? PostContentDraft.Poll ?: return@update this
        copy(content = current.copy(description = value))
    }

    fun setPollClosingRule(rule: PostPollClosingRule, closingAtMillis: Long? = null) = update {
        val current = content as? PostContentDraft.Poll ?: return@update this
        copy(
            content = current.copy(
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

    fun validationErrors(): Set<PostValidationField> =
        if (_formState.value.hasAttemptedPreview) validate(_formState.value) else emptySet()

    fun pollOptionError(index: Int): PollOptionError? {
        val poll = _formState.value.content as? PostContentDraft.Poll ?: return null
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
        get() = _formState.value.hasMeaningfulChanges

    private fun changeType(type: PostType) {
        setState(_formState.value.copy(content = emptyContent(type), selectedCarouselIndex = 0))
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
        clearContentKeys(handle)
        handle[CONTENT_TYPE_KEY] = state.content.type.name
        handle[LOCATION_LABEL_KEY] = state.location?.publicAreaLabel
        handle[LOCATION_PLACE_ID_KEY] = state.location?.placeId
        handle[LOCATION_LATITUDE_KEY] = state.location?.latitude
        handle[LOCATION_LONGITUDE_KEY] = state.location?.longitude
        handle[EXPANDED_KEY] = ArrayList(state.expandedDisclosures.map { it.name })
        handle[SELECTED_CAROUSEL_INDEX_KEY] = state.selectedCarouselIndex
        handle[ATTEMPTED_KEY] = state.hasAttemptedPreview
        handle[PREVIEW_ATTEMPT_KEY] = state.previewAttempt
        handle[SCREEN_MODE_KEY] = state.screenMode.name
        when (val content = state.content) {
            is PostContentDraft.Text -> handle[TEXT_BODY_KEY] = content.body
            is PostContentDraft.Photo -> {
                handle[PHOTO_URI_KEY] = content.uri
                handle[PHOTO_CAPTION_KEY] = content.caption
                handle[PHOTO_DESCRIPTION_KEY] = content.imageDescription
            }
            is PostContentDraft.Carousel -> {
                handle[CAROUSEL_URIS_KEY] = ArrayList(content.items.mapNotNull(PostCarouselItem::uri))
                handle[CAROUSEL_DESCRIPTIONS_KEY] = ArrayList(content.items.map { it.imageDescription })
                handle[CAROUSEL_CAPTION_KEY] = content.caption
            }
            is PostContentDraft.BeforeAfter -> {
                handle[BEFORE_URI_KEY] = content.before.uri
                handle[BEFORE_DESCRIPTION_KEY] = content.before.imageDescription
                handle[AFTER_URI_KEY] = content.after.uri
                handle[AFTER_DESCRIPTION_KEY] = content.after.imageDescription
                handle[BEFORE_AFTER_CAPTION_KEY] = content.caption
                handle[BEFORE_NOTE_KEY] = content.beforeNote
                handle[AFTER_NOTE_KEY] = content.afterNote
            }
            is PostContentDraft.Poll -> {
                handle[POLL_QUESTION_KEY] = content.question
                handle[POLL_OPTIONS_KEY] = ArrayList(content.options)
                handle[POLL_DESCRIPTION_KEY] = content.description
                handle[POLL_CLOSING_RULE_KEY] = content.closingRule.name
                handle[POLL_CLOSING_AT_KEY] = content.closingAtMillis
            }
        }
    }

    private fun restoreState(): CreatePostFormState {
        val handle = savedStateHandle ?: return CreatePostFormState()
        val type = handle.get<String>(CONTENT_TYPE_KEY).toEnumOrNull<PostType>() ?: PostType.TEXT
        val content = when (type) {
            PostType.TEXT -> PostContentDraft.Text(handle.get<String>(TEXT_BODY_KEY).orEmpty())
            PostType.PHOTO -> PostContentDraft.Photo(
                uri = handle.get<String>(PHOTO_URI_KEY),
                caption = handle.get<String>(PHOTO_CAPTION_KEY).orEmpty(),
                imageDescription = handle.get<String>(PHOTO_DESCRIPTION_KEY).orEmpty(),
            )
            PostType.CAROUSEL -> {
                val uris = readStringList(handle, CAROUSEL_URIS_KEY)
                val descriptions = readStringList(handle, CAROUSEL_DESCRIPTIONS_KEY)
                PostContentDraft.Carousel(
                    items = uris.mapIndexed { index, uri ->
                        PostCarouselItem(uri = uri, imageDescription = descriptions.getOrNull(index).orEmpty())
                    },
                    caption = handle.get<String>(CAROUSEL_CAPTION_KEY).orEmpty(),
                )
            }
            PostType.BEFORE_AFTER -> PostContentDraft.BeforeAfter(
                before = PostMediaDraft(
                    uri = handle.get<String>(BEFORE_URI_KEY),
                    imageDescription = handle.get<String>(BEFORE_DESCRIPTION_KEY).orEmpty(),
                ),
                after = PostMediaDraft(
                    uri = handle.get<String>(AFTER_URI_KEY),
                    imageDescription = handle.get<String>(AFTER_DESCRIPTION_KEY).orEmpty(),
                ),
                caption = handle.get<String>(BEFORE_AFTER_CAPTION_KEY).orEmpty(),
                beforeNote = handle.get<String>(BEFORE_NOTE_KEY).orEmpty(),
                afterNote = handle.get<String>(AFTER_NOTE_KEY).orEmpty(),
            )
            PostType.POLL -> PostContentDraft.Poll(
                question = handle.get<String>(POLL_QUESTION_KEY).orEmpty(),
                options = readStringList(handle, POLL_OPTIONS_KEY)
                    .ifEmpty { listOf("", "") }
                    .take(MAX_POLL_OPTIONS),
                description = handle.get<String>(POLL_DESCRIPTION_KEY).orEmpty(),
                closingRule = handle.get<String>(POLL_CLOSING_RULE_KEY)
                    .toEnumOrNull<PostPollClosingRule>()
                    ?: PostPollClosingRule.AFTER_24_HOURS,
                closingAtMillis = handle.get<Long>(POLL_CLOSING_AT_KEY),
            )
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
            content = content,
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

    private companion object {
        const val MAX_CAROUSEL_ITEMS = 10
        const val MIN_POLL_OPTIONS = 2
        const val MAX_POLL_OPTIONS = 6
        const val CONTENT_TYPE_KEY = "create_post_content_type"
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
        const val PHOTO_URI_KEY = "create_post_photo_uri"
        const val PHOTO_CAPTION_KEY = "create_post_photo_caption"
        const val PHOTO_DESCRIPTION_KEY = "create_post_photo_description"
        const val CAROUSEL_URIS_KEY = "create_post_carousel_uris"
        const val CAROUSEL_DESCRIPTIONS_KEY = "create_post_carousel_descriptions"
        const val CAROUSEL_CAPTION_KEY = "create_post_carousel_caption"
        const val BEFORE_URI_KEY = "create_post_before_uri"
        const val BEFORE_DESCRIPTION_KEY = "create_post_before_description"
        const val AFTER_URI_KEY = "create_post_after_uri"
        const val AFTER_DESCRIPTION_KEY = "create_post_after_description"
        const val BEFORE_AFTER_CAPTION_KEY = "create_post_before_after_caption"
        const val BEFORE_NOTE_KEY = "create_post_before_note"
        const val AFTER_NOTE_KEY = "create_post_after_note"
        const val POLL_QUESTION_KEY = "create_post_poll_question"
        const val POLL_OPTIONS_KEY = "create_post_poll_options"
        const val POLL_DESCRIPTION_KEY = "create_post_poll_description"
        const val POLL_CLOSING_RULE_KEY = "create_post_poll_closing_rule"
        const val POLL_CLOSING_AT_KEY = "create_post_poll_closing_at"

        fun clearContentKeys(handle: SavedStateHandle) {
            listOf(
                TEXT_BODY_KEY,
                PHOTO_URI_KEY,
                PHOTO_CAPTION_KEY,
                PHOTO_DESCRIPTION_KEY,
                CAROUSEL_URIS_KEY,
                CAROUSEL_DESCRIPTIONS_KEY,
                CAROUSEL_CAPTION_KEY,
                BEFORE_URI_KEY,
                BEFORE_DESCRIPTION_KEY,
                AFTER_URI_KEY,
                AFTER_DESCRIPTION_KEY,
                BEFORE_AFTER_CAPTION_KEY,
                BEFORE_NOTE_KEY,
                AFTER_NOTE_KEY,
                POLL_QUESTION_KEY,
                POLL_OPTIONS_KEY,
                POLL_DESCRIPTION_KEY,
                POLL_CLOSING_RULE_KEY,
                POLL_CLOSING_AT_KEY,
            ).forEach { key -> handle.remove<Any?>(key) }
        }

        fun readStringList(handle: SavedStateHandle, key: String): List<String> =
            handle.get<ArrayList<String>>(key)?.toList().orEmpty()
    }
}

private fun emptyContent(type: PostType): PostContentDraft = when (type) {
    PostType.TEXT -> PostContentDraft.Text()
    PostType.PHOTO -> PostContentDraft.Photo()
    PostType.CAROUSEL -> PostContentDraft.Carousel()
    PostType.BEFORE_AFTER -> PostContentDraft.BeforeAfter()
    PostType.POLL -> PostContentDraft.Poll()
}

private val PostContentDraft.hasMeaningfulChanges: Boolean
    get() = when (this) {
        is PostContentDraft.Text -> body.isNotBlank()
        is PostContentDraft.Photo -> uri != null || caption.isNotBlank() || imageDescription.isNotBlank()
        is PostContentDraft.Carousel -> items.isNotEmpty() || caption.isNotBlank() ||
            items.any { it.imageDescription.isNotBlank() }
        is PostContentDraft.BeforeAfter -> before.uri != null || after.uri != null ||
            caption.isNotBlank() || beforeNote.isNotBlank() || afterNote.isNotBlank() ||
            before.imageDescription.isNotBlank() || after.imageDescription.isNotBlank()
        is PostContentDraft.Poll -> question.isNotBlank() || options.any(String::isNotBlank) ||
            description.isNotBlank() || closingRule != PostPollClosingRule.AFTER_24_HOURS
    }

private val CreatePostFormState.hasMeaningfulChanges: Boolean
    get() = content.hasMeaningfulChanges || location != null

private fun validate(state: CreatePostFormState): Set<PostValidationField> = buildSet {
    when (val content = state.content) {
        is PostContentDraft.Text -> if (content.body.isBlank()) add(PostValidationField.TEXT_BODY)
        is PostContentDraft.Photo -> if (content.uri.isNullOrBlank()) add(PostValidationField.PHOTO)
        is PostContentDraft.Carousel -> if (
            content.items.size !in 2..10 || content.items.any { it.uri.isNullOrBlank() }
        ) {
            add(PostValidationField.CAROUSEL)
        }
        is PostContentDraft.BeforeAfter -> {
            if (content.before.uri.isNullOrBlank()) add(PostValidationField.BEFORE_PHOTO)
            if (content.after.uri.isNullOrBlank()) add(PostValidationField.AFTER_PHOTO)
        }
        is PostContentDraft.Poll -> {
            if (content.question.isBlank()) add(PostValidationField.POLL_QUESTION)
            if (content.options.size !in 2..6 || content.options.any(String::isBlank)) {
                add(PostValidationField.POLL_OPTIONS)
            } else if (content.options.map(String::trim).map(String::lowercase).toSet().size != content.options.size) {
                add(PostValidationField.POLL_OPTIONS)
            }
            if (
                content.closingRule == PostPollClosingRule.CUSTOM_DATE &&
                (content.closingAtMillis == null || content.closingAtMillis <= System.currentTimeMillis())
            ) {
                add(PostValidationField.POLL_CLOSING)
            }
        }
    }
}

private fun CreatePostFormState.toDraft(): PostDraft = PostDraft(
    location = location?.takeIf { it.publicAreaLabel.isNotBlank() },
    content = when (val value = content) {
        is PostContentDraft.Text -> value.copy(body = value.body.trim())
        is PostContentDraft.Photo -> value.copy(
            uri = requireNotNull(value.uri).trim(),
            caption = value.caption.trim(),
            imageDescription = value.imageDescription.trim(),
        )
        is PostContentDraft.Carousel -> value.copy(
            items = value.items.map { item ->
                item.copy(
                    uri = requireNotNull(item.uri).trim(),
                    imageDescription = item.imageDescription.trim(),
                )
            },
            caption = value.caption.trim(),
        )
        is PostContentDraft.BeforeAfter -> value.copy(
            before = value.before.copy(
                uri = requireNotNull(value.before.uri).trim(),
                imageDescription = value.before.imageDescription.trim(),
            ),
            after = value.after.copy(
                uri = requireNotNull(value.after.uri).trim(),
                imageDescription = value.after.imageDescription.trim(),
            ),
            caption = value.caption.trim(),
            beforeNote = value.beforeNote.trim(),
            afterNote = value.afterNote.trim(),
        )
        is PostContentDraft.Poll -> value.copy(
            question = value.question.trim(),
            options = value.options.map(String::trim),
            description = value.description.trim(),
        )
    },
)

private inline fun <reified T : Enum<T>> String?.toEnumOrNull(): T? =
    this?.let { value -> enumValues<T>().firstOrNull { it.name == value } }
