package com.askit.app.createpost

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatePostViewModelTest {

    @Test
    fun initialDraft_isTextClean_andPreviewReportsBody() {
        val viewModel = CreatePostViewModel(SavedStateHandle())

        assertEquals(PostType.TEXT, viewModel.formState.value.content.type)
        assertFalse(viewModel.isDirty)
        assertFalse(viewModel.preview())
        assertEquals(setOf(PostValidationField.TEXT_BODY), viewModel.validationErrors())
    }

    @Test
    fun textDraft_preservesParagraphs_andBuildsOnePayload() {
        val viewModel = CreatePostViewModel(SavedStateHandle())
        viewModel.updateTextBody("First paragraph.\n\nSecond paragraph.")

        assertTrue(viewModel.preview())
        assertEquals(
            PostDraft(
                location = null,
                content = PostContentDraft.Text("First paragraph.\n\nSecond paragraph."),
            ),
            viewModel.buildValidatedDraft(),
        )
    }

    @Test
    fun emptyTypeSwitch_isImmediate_andPreservesLocation() {
        val viewModel = CreatePostViewModel(SavedStateHandle())
        val location = PostPublicLocation("Karaikal", "place-1", 10.0, 79.0)
        viewModel.setLocation(location)

        assertFalse(viewModel.selectType(PostType.PHOTO))
        assertEquals(PostType.PHOTO, viewModel.formState.value.content.type)
        assertEquals(location, viewModel.formState.value.location)
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
        assertEquals(
            "A public-area update",
            (restored.formState.value.content as PostContentDraft.Text).body,
        )
    }

    @Test
    fun dirtyTypeSwitch_requiresConfirmation_andClearsOnlyActivePayload() {
        val viewModel = CreatePostViewModel(SavedStateHandle())
        viewModel.updateTextBody("An authored update")

        assertTrue(viewModel.selectType(PostType.POLL))
        assertEquals(PostType.TEXT, viewModel.formState.value.content.type)

        viewModel.confirmTypeChange(PostType.POLL)
        assertEquals(PostContentDraft.Poll(), viewModel.formState.value.content)
    }

    @Test
    fun photo_replaceClearsDescription_removeRetainsCaption() {
        val viewModel = CreatePostViewModel(SavedStateHandle())
        viewModel.selectType(PostType.PHOTO)
        viewModel.setPhoto("content://photo/one")
        viewModel.updatePhotoCaption("A useful caption")
        viewModel.updatePhotoImageDescription("A blue toolbox")

        viewModel.setPhoto("content://photo/two")
        assertEquals("A useful caption", (viewModel.formState.value.content as PostContentDraft.Photo).caption)
        assertEquals("", (viewModel.formState.value.content as PostContentDraft.Photo).imageDescription)

        viewModel.removePhoto()
        val content = viewModel.formState.value.content as PostContentDraft.Photo
        assertNull(content.uri)
        assertEquals("A useful caption", content.caption)
        assertEquals("", content.imageDescription)
    }

    @Test
    fun carousel_enforcesLimit_reordersItemsWithDescriptions_andRestores() {
        val handle = SavedStateHandle()
        val viewModel = CreatePostViewModel(handle)
        viewModel.selectType(PostType.CAROUSEL)
        viewModel.addCarouselMedia((1..12).map { "content://photo/$it" })
        viewModel.updateCarouselItemDescription(0, "First image")
        viewModel.moveCarouselItem(0, 1)

        val content = viewModel.formState.value.content as PostContentDraft.Carousel
        assertEquals(10, content.items.size)
        assertEquals("content://photo/2", content.items[0].uri)
        assertEquals("First image", content.items[1].imageDescription)
        assertTrue(viewModel.preview())

        val restored = CreatePostViewModel(handle)
        val restoredContent = restored.formState.value.content as PostContentDraft.Carousel
        assertEquals(content.items, restoredContent.items)
        assertEquals(PostType.CAROUSEL, restoredContent.type)
    }

    @Test
    fun carousel_requiresTwoImages() {
        val viewModel = CreatePostViewModel(SavedStateHandle())
        viewModel.selectType(PostType.CAROUSEL)
        viewModel.addCarouselMedia(listOf("content://photo/one"))

        assertFalse(viewModel.preview())
        assertEquals(setOf(PostValidationField.CAROUSEL), viewModel.validationErrors())
    }

    @Test
    fun beforeAfter_restoresFixedSlots_andValidatesBoth() {
        val handle = SavedStateHandle()
        val viewModel = CreatePostViewModel(handle)
        viewModel.selectType(PostType.BEFORE_AFTER)
        viewModel.setBeforePhoto("content://before")
        viewModel.updateBeforeImageDescription("Before state")
        viewModel.setAfterPhoto("content://after")
        viewModel.updateAfterNote("After note")

        assertTrue(viewModel.preview())
        val restored = CreatePostViewModel(handle)
        val content = restored.formState.value.content as PostContentDraft.BeforeAfter
        assertEquals("content://before", content.before.uri)
        assertEquals("Before state", content.before.imageDescription)
        assertEquals("content://after", content.after.uri)
        assertEquals("After note", content.afterNote)
    }

    @Test
    fun poll_enforcesTwoToSixOptions_andRejectsDuplicates() {
        val viewModel = CreatePostViewModel(SavedStateHandle())
        viewModel.selectType(PostType.POLL)
        viewModel.updatePollQuestion("Which finish do you prefer?")
        viewModel.updatePollOption(0, "Yes")
        viewModel.updatePollOption(1, " yes ")

        assertEquals(PollOptionError.DUPLICATE, viewModel.pollOptionError(0))
        assertFalse(viewModel.preview())
        assertTrue(PostValidationField.POLL_OPTIONS in viewModel.validationErrors())

        viewModel.updatePollOption(1, "No")
        repeat(10) { viewModel.addPollOption() }
        assertEquals(6, (viewModel.formState.value.content as PostContentDraft.Poll).options.size)
        repeat(6) { viewModel.removePollOption(0) }
        assertEquals(2, (viewModel.formState.value.content as PostContentDraft.Poll).options.size)
    }

    @Test
    fun poll_defaultCloseIsValid_andCustomPastDateIsRejected() {
        val viewModel = CreatePostViewModel(SavedStateHandle())
        viewModel.selectType(PostType.POLL)
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
    fun previewEditAndCompletion_preserveStateAndEmitValidatedDraft() {
        var completed: PostDraft? = null
        val viewModel = CreatePostViewModel(SavedStateHandle())
        viewModel.updateTextBody("A short update")
        assertTrue(viewModel.preview())
        viewModel.edit()
        viewModel.updateTextBody("An edited update")
        assertTrue(viewModel.preview())
        completed = viewModel.buildValidatedDraft()

        assertEquals(CreatePostScreenMode.PREVIEW, viewModel.formState.value.screenMode)
        assertEquals("An edited update", (completed?.content as PostContentDraft.Text).body)
    }
}
