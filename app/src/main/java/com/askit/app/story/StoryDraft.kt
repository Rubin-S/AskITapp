package com.askit.app.story

import com.askit.app.home.model.FeedPost
import kotlinx.serialization.Serializable

const val STORY_MAX_DURATION_MS = 15_000L

@Serializable
enum class StoryScreenMode {
    Capture,
    CreateText,
    Editor,
}

@Serializable
enum class StoryMediaType {
    Photo,
    Video,
    SolidBackground,
}

@Serializable
enum class StoryAudience {
    Everyone,
    CloseCircle,
    Message,
}

@Serializable
enum class StoryTextAlignment {
    Start,
    Center,
    End,
}

@Serializable
enum class StoryStickerKind {
    Location,
    Mention,
    Hashtag,
    Poll,
    Question,
    Link,
    Photo,
    Emoji,
    PostReshare,
}

enum class StoryReshareCardStyle {
    FullCard,
    MinimalCard,
}

enum class StoryEditorTool {
    Text,
    Stickers,
    Background,
    Mention,
    Save,
    Draw,
    More,
}

@Serializable
data class StoryTransform(
    val panX: Float = 0f,
    val panY: Float = 0f,
    val zoom: Float = 1f,
)

@Serializable
sealed class StoryLayer {
    abstract val id: String
    abstract val zIndex: Int
    abstract val offsetX: Float
    abstract val offsetY: Float
    abstract val rotation: Float
    abstract val scale: Float

    @Serializable
    data class Text(
        override val id: String,
        override val zIndex: Int,
        override val offsetX: Float = 0f,
        override val offsetY: Float = 0f,
        override val rotation: Float = 0f,
        override val scale: Float = 1f,
        val text: String = "",
        val fontFamily: String = "Default",
        val textSize: Float = 24f,
        val colorArgb: Long = 0xFFFFFFFF,
        val hasBackground: Boolean = false,
        val backgroundArgb: Long = 0xFF000000,
        val alignment: StoryTextAlignment = StoryTextAlignment.Center,
    ) : StoryLayer()

    @Serializable
    data class Sticker(
        override val id: String,
        override val zIndex: Int,
        override val offsetX: Float = 0f,
        override val offsetY: Float = 0f,
        override val rotation: Float = 0f,
        override val scale: Float = 1f,
        val kind: StoryStickerKind,
        val label: String,
        val payload: String = "",
    ) : StoryLayer()

    @Serializable
    data class Draw(
        override val id: String,
        override val zIndex: Int,
        override val offsetX: Float = 0f,
        override val offsetY: Float = 0f,
        override val rotation: Float = 0f,
        override val scale: Float = 1f,
        val strokeColorArgb: Long = 0xFFFFFFFF,
        val strokeWidth: Float = 4f,
        val points: List<Pair<Float, Float>> = emptyList(),
    ) : StoryLayer()
}

@Serializable
data class StoryPollSticker(
    val question: String,
    val options: List<String>,
)

data class StoryDraft(
    val mediaUri: String? = null,
    val mediaType: StoryMediaType? = null,
    val solidBackgroundArgb: Long? = null,
    val durationMs: Long? = null,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long? = null,
    val transform: StoryTransform = StoryTransform(),
    val caption: String = "",
    val audience: StoryAudience = StoryAudience.Everyone,
    val layers: List<StoryLayer> = emptyList(),
    val sharedPost: FeedPost? = null,
    val reshareCardStyle: StoryReshareCardStyle = StoryReshareCardStyle.FullCard,
)

data class StoryFormState(
    val screenMode: StoryScreenMode = StoryScreenMode.Capture,
    val mediaUri: String? = null,
    val mediaType: StoryMediaType? = null,
    val solidBackgroundIndex: Int = 0,
    val durationMs: Long? = null,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long? = null,
    val transform: StoryTransform = StoryTransform(),
    val createTextDraft: String = "",
    val layers: List<StoryLayer> = emptyList(),
    val selectedLayerId: String? = null,
    val caption: String = "",
    val audience: StoryAudience = StoryAudience.Everyone,
    val activeTool: StoryEditorTool? = null,
    val showStickerTray: Boolean = false,
    val showShareSheet: Boolean = false,
    val flashEnabled: Boolean = false,
    val useFrontCamera: Boolean = false,
    val galleryThumbUri: String? = null,
    val editingTextLayerId: String? = null,
    val drawStrokeColorArgb: Long = 0xFFFFFFFF,
    val currentDrawLayerId: String? = null,
    val sharedPost: FeedPost? = null,
    val reshareCardStyle: StoryReshareCardStyle = StoryReshareCardStyle.FullCard,
) {
    val hasMeaningfulChanges: Boolean
        get() {
            if (sharedPost != null) return true
            if (mediaUri != null || mediaType == StoryMediaType.SolidBackground) return true
            if (createTextDraft.isNotBlank()) return true
            if (layers.isNotEmpty()) return true
            if (caption.isNotBlank()) return true
            if (trimStartMs > 0L || trimEndMs != null) return true
            if (transform.panX != 0f || transform.panY != 0f || transform.zoom != 1f) return true
            return false
        }
}
