package com.askit.app.story

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.askit.app.home.model.FeedPost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StoryViewModel(
    private val savedStateHandle: SavedStateHandle? = null,
) : ViewModel() {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _formState = MutableStateFlow(restoreState())
    val formState: StateFlow<StoryFormState> = _formState.asStateFlow()

    val isDirty: Boolean
        get() = _formState.value.hasMeaningfulChanges

    fun startNewDraft() {
        setState(StoryFormState())
    }

    fun startReshareDraft(post: FeedPost) {
        setState(
            _formState.value.copy(
                screenMode = StoryScreenMode.Editor,
                mediaType = StoryMediaType.SolidBackground,
                mediaUri = null,
                durationMs = null,
                trimStartMs = 0L,
                trimEndMs = null,
                layers = emptyList(),
                selectedLayerId = null,
                createTextDraft = "",
                sharedPost = post,
                reshareCardStyle = StoryReshareCardStyle.FullCard,
                solidBackgroundIndex = 4, // Deep dark gradient background
            ),
        )
    }

    fun toggleReshareCardStyle() = update {
        val next = if (reshareCardStyle == StoryReshareCardStyle.FullCard) {
            StoryReshareCardStyle.MinimalCard
        } else {
            StoryReshareCardStyle.FullCard
        }
        copy(reshareCardStyle = next)
    }

    fun setScreenMode(mode: StoryScreenMode) = update { copy(screenMode = mode) }

    fun toggleFlash() = update { copy(flashEnabled = !flashEnabled) }

    fun toggleCameraFacing() = update { copy(useFrontCamera = !useFrontCamera) }

    fun setGalleryThumb(uri: String?) = update { copy(galleryThumbUri = uri) }

    fun onMediaCaptured(uri: String, type: StoryMediaType, durationMs: Long? = null) {
        val trimEnd = when {
            type == StoryMediaType.Video && durationMs != null && durationMs > STORY_MAX_DURATION_MS ->
                STORY_MAX_DURATION_MS
            type == StoryMediaType.Video && durationMs != null -> durationMs
            else -> null
        }
        setState(
            _formState.value.copy(
                mediaUri = uri,
                mediaType = type,
                durationMs = durationMs,
                trimStartMs = 0L,
                trimEndMs = trimEnd,
                screenMode = StoryScreenMode.Editor,
                layers = emptyList(),
                selectedLayerId = null,
                createTextDraft = "",
            ),
        )
    }

    fun onGalleryMediaSelected(uri: String, type: StoryMediaType, durationMs: Long? = null) {
        setGalleryThumb(uri)
        onMediaCaptured(uri, type, durationMs)
    }

    fun updateCreateText(value: String) = update { copy(createTextDraft = value) }

    fun cycleSolidBackground() = update {
        copy(solidBackgroundIndex = (solidBackgroundIndex + 1) % STORY_SOLID_BACKGROUNDS.size)
    }

    fun confirmCreateText() {
        val text = _formState.value.createTextDraft.trim()
        if (text.isEmpty()) return
        val layer = StoryLayer.Text(
            id = newLayerId(),
            zIndex = nextZIndex(),
            text = text,
        )
        setState(
            _formState.value.copy(
                mediaType = StoryMediaType.SolidBackground,
                mediaUri = null,
                durationMs = null,
                trimStartMs = 0L,
                trimEndMs = null,
                layers = listOf(layer),
                selectedLayerId = layer.id,
                screenMode = StoryScreenMode.Editor,
                createTextDraft = "",
            ),
        )
    }

    fun openCreateText() = update {
        copy(
            screenMode = StoryScreenMode.CreateText,
            createTextDraft = "",
            mediaUri = null,
            mediaType = null,
            layers = emptyList(),
        )
    }

    fun backToCapture() = update {
        copy(
            screenMode = StoryScreenMode.Capture,
            activeTool = null,
            showStickerTray = false,
            showShareSheet = false,
            editingTextLayerId = null,
        )
    }

    fun updateCaption(value: String) = update { copy(caption = value) }

    fun setAudience(audience: StoryAudience) = update { copy(audience = audience) }

    fun setActiveTool(tool: StoryEditorTool?) = update {
        copy(
            activeTool = tool,
            showStickerTray = tool == StoryEditorTool.Stickers,
            editingTextLayerId = if (tool == StoryEditorTool.Text) editingTextLayerId else null,
        )
    }

    fun startTextEditing(id: String) = update {
        copy(
            editingTextLayerId = id,
            selectedLayerId = id,
            activeTool = StoryEditorTool.Text,
        )
    }

    fun openStickerTray() = update { copy(showStickerTray = true, activeTool = StoryEditorTool.Stickers) }

    fun closeStickerTray() = update { copy(showStickerTray = false, activeTool = null) }

    fun openShareSheet() = update { copy(showShareSheet = true) }

    fun closeShareSheet() = update { copy(showShareSheet = false) }

    fun updateTrimStart(value: Long) = update {
        val end = trimEndMs ?: durationMs ?: STORY_MAX_DURATION_MS
        copy(trimStartMs = value.coerceIn(0L, (end - 1_000L).coerceAtLeast(0L)))
    }

    fun updateTrimEnd(value: Long) = update {
        val max = durationMs ?: STORY_MAX_DURATION_MS
        copy(trimEndMs = value.coerceIn(trimStartMs + 1_000L, max))
    }

    fun updateTrimRange(startMs: Long, endMs: Long) {
        val max = _formState.value.durationMs ?: STORY_MAX_DURATION_MS
        val start = startMs.coerceIn(0L, (max - 1_000L).coerceAtLeast(0L))
        val end = endMs.coerceIn(start + 1_000L, max)
        update { copy(trimStartMs = start, trimEndMs = end) }
    }

    fun updateTransform(panX: Float, panY: Float, zoom: Float) = update {
        copy(transform = StoryTransform(panX, panY, zoom.coerceIn(0.5f, 3f)))
    }

    fun selectLayer(id: String?) = update { copy(selectedLayerId = id) }

    fun updateLayerTransform(
        id: String,
        offsetX: Float,
        offsetY: Float,
        rotation: Float,
        scale: Float,
    ) = updateLayers { layer ->
        if (layer.id != id) layer
        else when (layer) {
            is StoryLayer.Text -> layer.copy(
                offsetX = offsetX,
                offsetY = offsetY,
                rotation = rotation,
                scale = scale,
            )
            is StoryLayer.Sticker -> layer.copy(
                offsetX = offsetX,
                offsetY = offsetY,
                rotation = rotation,
                scale = scale,
            )
            is StoryLayer.Draw -> layer.copy(
                offsetX = offsetX,
                offsetY = offsetY,
                rotation = rotation,
                scale = scale,
            )
        }
    }

    fun addTextLayer(text: String = "") {
        val layer = StoryLayer.Text(
            id = newLayerId(),
            zIndex = nextZIndex(),
            text = text,
        )
        update {
            copy(
                layers = layers + layer,
                selectedLayerId = layer.id,
                editingTextLayerId = layer.id,
                activeTool = StoryEditorTool.Text,
            )
        }
    }

    fun updateTextLayer(
        id: String,
        text: String? = null,
        fontFamily: String? = null,
        textSize: Float? = null,
        colorArgb: Long? = null,
        hasBackground: Boolean? = null,
        backgroundArgb: Long? = null,
        alignment: StoryTextAlignment? = null,
    ) = updateLayers { layer ->
        if (layer.id != id || layer !is StoryLayer.Text) layer
        else layer.copy(
            text = text ?: layer.text,
            fontFamily = fontFamily ?: layer.fontFamily,
            textSize = textSize ?: layer.textSize,
            colorArgb = colorArgb ?: layer.colorArgb,
            hasBackground = hasBackground ?: layer.hasBackground,
            backgroundArgb = backgroundArgb ?: layer.backgroundArgb,
            alignment = alignment ?: layer.alignment,
        )
    }

    fun finishTextEditing() = update { copy(editingTextLayerId = null, activeTool = null) }

    fun addSticker(kind: StoryStickerKind, label: String, payload: String = "") {
        val layer = StoryLayer.Sticker(
            id = newLayerId(),
            zIndex = nextZIndex(),
            kind = kind,
            label = label,
            payload = payload,
        )
        update {
            copy(
                layers = layers + layer,
                selectedLayerId = layer.id,
                showStickerTray = false,
                activeTool = null,
            )
        }
    }

    fun addDrawPoint(x: Float, y: Float) {
        val state = _formState.value
        val existingId = state.currentDrawLayerId
        if (existingId != null) {
            updateLayers { layer ->
                if (layer.id == existingId && layer is StoryLayer.Draw) {
                    layer.copy(points = layer.points + (x to y))
                } else {
                    layer
                }
            }
        } else {
            val layer = StoryLayer.Draw(
                id = newLayerId(),
                zIndex = nextZIndex(),
                strokeColorArgb = state.drawStrokeColorArgb,
                points = listOf(x to y),
            )
            update {
                copy(
                    layers = layers + layer,
                    currentDrawLayerId = layer.id,
                    selectedLayerId = layer.id,
                )
            }
        }
    }

    fun finishDrawStroke() = update { copy(currentDrawLayerId = null) }

    fun setDrawColor(argb: Long) = update { copy(drawStrokeColorArgb = argb) }

    fun markDraftSaved() {
        // Bookmark action keeps the user on the editor with the current draft intact.
    }

    fun solidBackgroundArgb(): Long =
        STORY_SOLID_BACKGROUNDS[_formState.value.solidBackgroundIndex]

    fun needsTrimmer(): Boolean {
        val state = _formState.value
        return state.mediaType == StoryMediaType.Video &&
            (state.durationMs ?: 0L) > STORY_MAX_DURATION_MS
    }

    fun buildValidatedDraft(): StoryDraft? {
        val state = _formState.value
        if (state.screenMode != StoryScreenMode.Editor && state.screenMode != StoryScreenMode.CreateText) {
            return null
        }
        val hasMedia = state.mediaUri != null ||
            state.mediaType == StoryMediaType.SolidBackground ||
            state.sharedPost != null
        if (!hasMedia) return null
        return StoryDraft(
            mediaUri = state.mediaUri,
            mediaType = state.mediaType,
            solidBackgroundArgb = if (state.mediaType == StoryMediaType.SolidBackground) {
                solidBackgroundArgb()
            } else {
                null
            },
            durationMs = state.durationMs,
            trimStartMs = state.trimStartMs,
            trimEndMs = state.trimEndMs,
            transform = state.transform,
            caption = state.caption.trim(),
            audience = state.audience,
            layers = state.layers.sortedBy(StoryLayer::zIndex),
            sharedPost = state.sharedPost,
            reshareCardStyle = state.reshareCardStyle,
        )
    }

    private fun update(transform: StoryFormState.() -> StoryFormState) {
        setState(_formState.value.transform())
    }

    private fun updateLayers(transform: (StoryLayer) -> StoryLayer) {
        update { copy(layers = layers.map(transform)) }
    }

    private fun setState(next: StoryFormState) {
        _formState.value = next
        persist(next)
    }

    private fun nextZIndex(): Int = (_formState.value.layers.maxOfOrNull { it.zIndex } ?: 0) + 1

    private fun newLayerId(): String = "layer-${System.nanoTime()}"

    private fun persist(state: StoryFormState) {
        val handle = savedStateHandle ?: return
        handle[SCREEN_MODE_KEY] = state.screenMode.name
        handle[MEDIA_URI_KEY] = state.mediaUri
        handle[MEDIA_TYPE_KEY] = state.mediaType?.name
        handle[SOLID_BG_INDEX_KEY] = state.solidBackgroundIndex
        handle[DURATION_KEY] = state.durationMs
        handle[TRIM_START_KEY] = state.trimStartMs
        handle[TRIM_END_KEY] = state.trimEndMs
        handle[TRANSFORM_KEY] = json.encodeToString(state.transform)
        handle[CREATE_TEXT_KEY] = state.createTextDraft
        handle[LAYERS_KEY] = json.encodeToString(state.layers)
        handle[SELECTED_LAYER_KEY] = state.selectedLayerId
        handle[CAPTION_KEY] = state.caption
        handle[AUDIENCE_KEY] = state.audience.name
        handle[FLASH_KEY] = state.flashEnabled
        handle[FRONT_CAMERA_KEY] = state.useFrontCamera
        handle[GALLERY_THUMB_KEY] = state.galleryThumbUri
        handle[DRAW_COLOR_KEY] = state.drawStrokeColorArgb
    }

    private fun restoreState(): StoryFormState {
        val handle = savedStateHandle ?: return StoryFormState()
        val layersJson = handle.get<String>(LAYERS_KEY)
        val layers = layersJson?.let {
            runCatching { json.decodeFromString<List<StoryLayer>>(it) }.getOrDefault(emptyList())
        }.orEmpty()
        val transformJson = handle.get<String>(TRANSFORM_KEY)
        val transform = transformJson?.let {
            runCatching { json.decodeFromString<StoryTransform>(it) }.getOrNull()
        } ?: StoryTransform()
        return StoryFormState(
            screenMode = handle.get<String>(SCREEN_MODE_KEY)
                .toEnumOrNull<StoryScreenMode>() ?: StoryScreenMode.Capture,
            mediaUri = handle.get<String>(MEDIA_URI_KEY),
            mediaType = handle.get<String>(MEDIA_TYPE_KEY).toEnumOrNull<StoryMediaType>(),
            solidBackgroundIndex = handle.get<Int>(SOLID_BG_INDEX_KEY) ?: 0,
            durationMs = handle.get<Long>(DURATION_KEY),
            trimStartMs = handle.get<Long>(TRIM_START_KEY) ?: 0L,
            trimEndMs = handle.get<Long>(TRIM_END_KEY),
            transform = transform,
            createTextDraft = handle.get<String>(CREATE_TEXT_KEY).orEmpty(),
            layers = layers,
            selectedLayerId = handle.get<String>(SELECTED_LAYER_KEY),
            caption = handle.get<String>(CAPTION_KEY).orEmpty(),
            audience = handle.get<String>(AUDIENCE_KEY)
                .toEnumOrNull<StoryAudience>() ?: StoryAudience.Everyone,
            flashEnabled = handle.get<Boolean>(FLASH_KEY) ?: false,
            useFrontCamera = handle.get<Boolean>(FRONT_CAMERA_KEY) ?: false,
            galleryThumbUri = handle.get<String>(GALLERY_THUMB_KEY),
            drawStrokeColorArgb = handle.get<Long>(DRAW_COLOR_KEY) ?: 0xFFFFFFFF,
        )
    }

    private inline fun <reified T : Enum<T>> String?.toEnumOrNull(): T? =
        this?.let { runCatching { enumValueOf<T>(it) }.getOrNull() }

    companion object {
        private const val SCREEN_MODE_KEY = "story_screen_mode"
        private const val MEDIA_URI_KEY = "story_media_uri"
        private const val MEDIA_TYPE_KEY = "story_media_type"
        private const val SOLID_BG_INDEX_KEY = "story_solid_bg_index"
        private const val DURATION_KEY = "story_duration_ms"
        private const val TRIM_START_KEY = "story_trim_start_ms"
        private const val TRIM_END_KEY = "story_trim_end_ms"
        private const val TRANSFORM_KEY = "story_transform"
        private const val CREATE_TEXT_KEY = "story_create_text"
        private const val LAYERS_KEY = "story_layers"
        private const val SELECTED_LAYER_KEY = "story_selected_layer"
        private const val CAPTION_KEY = "story_caption"
        private const val AUDIENCE_KEY = "story_audience"
        private const val FLASH_KEY = "story_flash"
        private const val FRONT_CAMERA_KEY = "story_front_camera"
        private const val GALLERY_THUMB_KEY = "story_gallery_thumb"
        private const val DRAW_COLOR_KEY = "story_draw_color"
    }
}

val STORY_SOLID_BACKGROUNDS = listOf(
    0xFF000000,
    0xFF3D7100,
    0xFF7CE605,
    0xFFFFFFFF,
    0xFF212121,
)
