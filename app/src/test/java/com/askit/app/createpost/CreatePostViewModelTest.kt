package com.askit.app.createpost

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatePostViewModelTest {

    @Test
    fun emptyDraft_failsPreview_withContentError() {
        val viewModel = CreatePostViewModel(SavedStateHandle())

        assertFalse(viewModel.isDirty)
        assertFalse(viewModel.preview())
        assertEquals(setOf(PostValidationField.CONTENT), viewModel.validationErrors())
    }

    @Test
    fun textOnly_preservesParagraphs_andBuildsOnePayload() {
        val viewModel = CreatePostViewModel(SavedStateHandle())
        viewModel.updateTextBody("First paragraph.\n\nSecond paragraph.")

        assertTrue(viewModel.preview())
        assertEquals(
            PostDraft(body = "First paragraph.\n\nSecond paragraph."),
            viewModel.buildValidatedDraft(),
        )
    }

    @Test
    fun savedState_restoresPublicLocationAndScreenMode() {
        val handle = SavedStateHandle()
        val viewModel = CreatePostViewModel(handle)
        val location = PostPublicLocation("Karaikal", "place-1", 10.0, 79.0)
        viewModel.setLocation(location)
        viewModel.updateTextBody("A public-area update")

        assertTrue(viewModel.preview())

        val restored = CreatePostViewModel(handle)
        assertEquals(location, restored.formState.value.location)
        assertEquals(CreatePostScreenMode.PREVIEW, restored.formState.value.screenMode)
        assertEquals("A public-area update", restored.formState.value.body)
    }

    @Test
    fun onePhoto_isValid_andReplaceClearsDescription() {
        val viewModel = CreatePostViewModel(SavedStateHandle())
        viewModel.addPhotos(listOf("content://photo/one"))
        viewModel.updatePhotoDescription(0, "A blue toolbox")

        viewModel.setPhotoAt(0, "content://photo/two")
        assertEquals("", viewModel.formState.value.photos[0].imageDescription)
        assertEquals("content://photo/two", viewModel.formState.value.photos[0].uri)
        assertTrue(viewModel.preview())
        assertEquals(1, viewModel.buildValidatedDraft()?.photos?.size)
    }

    @Test
    fun photos_enforceLimit_reorderWithDescriptions_andRestore() {
        val handle = SavedStateHandle()
        val viewModel = CreatePostViewModel(handle)
        viewModel.addPhotos((1..12).map { "content://photo/$it" })
        viewModel.updatePhotoDescription(0, "First image")
        viewModel.movePhoto(0, 1)

        val photos = viewModel.formState.value.photos
        assertEquals(10, photos.size)
        assertEquals("content://photo/2", photos[0].uri)
        assertEquals("First image", photos[1].imageDescription)
        assertTrue(viewModel.preview())

        val restored = CreatePostViewModel(handle)
        assertEquals(photos, restored.formState.value.photos)
        assertEquals(PostMediaLayout.GALLERY, restored.formState.value.mediaLayout)
    }

    @Test
    fun beforeAfter_keepsExtraPhotos_whenToggledOnAndOff() {
        val viewModel = CreatePostViewModel(SavedStateHandle())
        viewModel.addPhotos(listOf("content://a", "content://b", "content://c"))
        viewModel.setMediaLayout(PostMediaLayout.BEFORE_AFTER)
        assertEquals(3, viewModel.formState.value.photos.size)
        viewModel.setMediaLayout(PostMediaLayout.GALLERY)
        assertEquals(
            listOf("content://a", "content://b", "content://c"),
            viewModel.formState.value.photos.map { it.uri },
        )
    }

    @Test
    fun beforeAfter_requiresBothSlots_andRestores() {
        val handle = SavedStateHandle()
        val viewModel = CreatePostViewModel(handle)
        viewModel.setMediaLayout(PostMediaLayout.BEFORE_AFTER)
        viewModel.setPhotoAt(0, "content://before")
        viewModel.updatePhotoDescription(0, "Before state")

        assertFalse(viewModel.preview())
        assertTrue(PostValidationField.AFTER_PHOTO in viewModel.validationErrors())

        viewModel.setPhotoAt(1, "content://after")
        assertTrue(viewModel.preview())

        val restored = CreatePostViewModel(handle)
        assertEquals(PostMediaLayout.BEFORE_AFTER, restored.formState.value.mediaLayout)
        assertEquals("content://before", restored.formState.value.photos[0].uri)
        assertEquals("Before state", restored.formState.value.photos[0].imageDescription)
        assertEquals("content://after", restored.formState.value.photos[1].uri)
    }

    @Test
    fun pollOnly_enforcesTwoToSixOptions_andRejectsDuplicates() {
        val viewModel = CreatePostViewModel(SavedStateHandle())
        viewModel.addPoll()
        viewModel.updatePollQuestion("Which finish do you prefer?")
        viewModel.updatePollOption(0, "Yes")
        viewModel.updatePollOption(1, " yes ")

        assertEquals(PollOptionError.DUPLICATE, viewModel.pollOptionError(0))
        assertFalse(viewModel.preview())
        assertTrue(PostValidationField.POLL_OPTIONS in viewModel.validationErrors())

        viewModel.updatePollOption(1, "No")
        repeat(10) { viewModel.addPollOption() }
        assertEquals(6, viewModel.formState.value.poll?.options?.size)
        repeat(6) { viewModel.removePollOption(0) }
        assertEquals(2, viewModel.formState.value.poll?.options?.size)
    }

    @Test
    fun poll_defaultCloseIsValid_andCustomPastDateIsRejected() {
        val viewModel = CreatePostViewModel(SavedStateHandle())
        viewModel.addPoll()
        viewModel.updatePollQuestion("Which day works?")
        viewModel.updatePollOption(0, "Saturday")
        viewModel.updatePollOption(1, "Sunday")

        assertTrue(viewModel.preview())
        viewModel.edit()
        viewModel.setPollClosingRule(PostPollClosingRule.CUSTOM_DATE, System.currentTimeMillis() - 1)
        assertFalse(viewModel.preview())
        assertTrue(PostValidationField.POLL_CLOSING in viewModel.validationErrors())
    }

    @Test
    fun photoTextAndPoll_completeTogether() {
        val viewModel = CreatePostViewModel(SavedStateHandle())
        viewModel.addPhotos(listOf("content://photo/one"))
        viewModel.updateTextBody("The finished repair.")
        viewModel.addPoll()
        viewModel.updatePollQuestion("Which finish looks better?")
        viewModel.updatePollOption(0, "Matte")
        viewModel.updatePollOption(1, "Wood")

        assertTrue(viewModel.preview())
        val draft = viewModel.buildValidatedDraft()
        assertEquals(1, draft?.photos?.size)
        assertEquals("The finished repair.", draft?.body)
        assertEquals("Which finish looks better?", draft?.poll?.question)
    }

    @Test
    fun previewEditAndCompletion_preserveStateAndEmitValidatedDraft() {
        val viewModel = CreatePostViewModel(SavedStateHandle())
        viewModel.updateTextBody("A short update")
        assertTrue(viewModel.preview())
        viewModel.edit()
        viewModel.updateTextBody("An edited update")
        assertTrue(viewModel.preview())
        val completed = viewModel.buildValidatedDraft()

        assertEquals(CreatePostScreenMode.PREVIEW, viewModel.formState.value.screenMode)
        assertEquals("An edited update", completed?.body)
    }

    @Test
    fun removingPoll_clearsPollValidation() {
        val viewModel = CreatePostViewModel(SavedStateHandle())
        viewModel.addPoll()
        assertFalse(viewModel.preview())
        assertTrue(PostValidationField.POLL_QUESTION in viewModel.validationErrors())

        viewModel.removePoll()
        viewModel.updateTextBody("Just text")
        assertTrue(viewModel.preview())
        assertNull(viewModel.buildValidatedDraft()?.poll)
    }
}
