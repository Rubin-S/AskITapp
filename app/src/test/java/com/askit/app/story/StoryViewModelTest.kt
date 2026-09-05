package com.askit.app.story

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryViewModelTest {

    @Test
    fun initialState_isCleanCaptureScreen() {
        val viewModel = StoryViewModel(SavedStateHandle())

        assertFalse(viewModel.isDirty)
        assertEquals(StoryScreenMode.Capture, viewModel.formState.value.screenMode)
        assertEquals(null, viewModel.buildValidatedDraft())
    }

    @Test
    fun photoCapture_movesToEditor_andBuildsDraft() {
        val viewModel = StoryViewModel(SavedStateHandle())
        viewModel.onMediaCaptured("content://photo", StoryMediaType.Photo)
        viewModel.updateCaption("Hello neighbors")
        viewModel.setAudience(StoryAudience.CloseCircle)

        val draft = viewModel.buildValidatedDraft()
        assertEquals(StoryMediaType.Photo, draft?.mediaType)
        assertEquals("content://photo", draft?.mediaUri)
        assertEquals("Hello neighbors", draft?.caption)
        assertEquals(StoryAudience.CloseCircle, draft?.audience)
    }

    @Test
    fun createText_addsTextLayer_andMarksDirty() {
        val viewModel = StoryViewModel(SavedStateHandle())
        viewModel.openCreateText()
        viewModel.updateCreateText("AskIT update")
        viewModel.confirmCreateText()

        assertTrue(viewModel.isDirty)
        assertEquals(StoryScreenMode.Editor, viewModel.formState.value.screenMode)
        assertEquals(1, viewModel.formState.value.layers.size)
        val textLayer = viewModel.formState.value.layers.first() as StoryLayer.Text
        assertEquals("AskIT update", textLayer.text)
    }

    @Test
    fun stickers_layers_audience_emitValidatedDraft() {
        val viewModel = StoryViewModel(SavedStateHandle())
        viewModel.onMediaCaptured("content://photo", StoryMediaType.Photo)
        viewModel.addSticker(StoryStickerKind.Hashtag, "#askit", "askit")
        viewModel.addTextLayer("Overlay")
        viewModel.setAudience(StoryAudience.Message)

        val draft = viewModel.buildValidatedDraft()
        assertEquals(2, draft?.layers?.size)
        assertEquals(StoryAudience.Message, draft?.audience)
    }

    @Test
    fun longVideo_trimEnd_defaultsToMaxDuration() {
        val viewModel = StoryViewModel(SavedStateHandle())
        viewModel.onMediaCaptured("content://video", StoryMediaType.Video, 30_000L)

        assertEquals(STORY_MAX_DURATION_MS, viewModel.formState.value.trimEndMs)
        assertTrue(viewModel.needsTrimmer())
        viewModel.updateTrimRange(2_000L, 12_000L)
        assertEquals(2_000L, viewModel.formState.value.trimStartMs)
        assertEquals(12_000L, viewModel.formState.value.trimEndMs)
    }

    @Test
    fun openShareSheet_stillBuildsValidatedDraft() {
        val viewModel = StoryViewModel(SavedStateHandle())
        viewModel.onMediaCaptured("content://photo", StoryMediaType.Photo)
        viewModel.openShareSheet()

        assertEquals("content://photo", viewModel.buildValidatedDraft()?.mediaUri)
    }

    @Test
    fun savedStateHandle_restoresLayersAfterProcessDeath() {
        val handle = SavedStateHandle()
        val viewModel = StoryViewModel(handle)
        viewModel.onMediaCaptured("content://photo", StoryMediaType.Photo)
        viewModel.addTextLayer("Restored")
        viewModel.updateCaption("Caption")

        val restored = StoryViewModel(handle)
        assertEquals(StoryScreenMode.Editor, restored.formState.value.screenMode)
        assertEquals("Caption", restored.formState.value.caption)
        assertEquals("Restored", (restored.formState.value.layers.first() as StoryLayer.Text).text)
    }

    @Test
    fun addTextLayer_updateText_persistsOnLayer() {
        val viewModel = StoryViewModel(SavedStateHandle())
        viewModel.onMediaCaptured("content://photo", StoryMediaType.Photo)
        viewModel.addTextLayer()
        val id = viewModel.formState.value.layers.first().id
        viewModel.updateTextLayer(id, text = "Hello neighbors")

        val layer = viewModel.formState.value.layers.first() as StoryLayer.Text
        assertEquals("Hello neighbors", layer.text)
        assertEquals(id, viewModel.formState.value.editingTextLayerId)
    }

    @Test
    fun startReshareDraft_initializesEditorWithPostCardAndDarkBackground() {
        val viewModel = StoryViewModel(SavedStateHandle())
        val samplePost = com.askit.app.home.data.getFeedPostById("post-1")

        viewModel.startReshareDraft(samplePost)

        val state = viewModel.formState.value
        assertEquals(StoryScreenMode.Editor, state.screenMode)
        assertEquals(StoryMediaType.SolidBackground, state.mediaType)
        assertEquals(samplePost, state.sharedPost)
        assertEquals(StoryReshareCardStyle.FullCard, state.reshareCardStyle)
        assertTrue(viewModel.isDirty)

        val draft = viewModel.buildValidatedDraft()
        assertEquals(samplePost, draft?.sharedPost)
        assertEquals(StoryReshareCardStyle.FullCard, draft?.reshareCardStyle)
    }

    @Test
    fun toggleReshareCardStyle_switchesBetweenFullAndMinimal() {
        val viewModel = StoryViewModel(SavedStateHandle())
        val samplePost = com.askit.app.home.data.getFeedPostById("post-1")

        viewModel.startReshareDraft(samplePost)
        assertEquals(StoryReshareCardStyle.FullCard, viewModel.formState.value.reshareCardStyle)

        viewModel.toggleReshareCardStyle()
        assertEquals(StoryReshareCardStyle.MinimalCard, viewModel.formState.value.reshareCardStyle)

        viewModel.toggleReshareCardStyle()
        assertEquals(StoryReshareCardStyle.FullCard, viewModel.formState.value.reshareCardStyle)
    }
}
