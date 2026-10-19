package com.askit.app.profile

import com.askit.app.auth.SignupDraft
import com.askit.app.auth.SignupResult
import com.askit.app.session.SessionProfile
import com.askit.app.session.SessionProfileStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SignupAvailabilityTest {
    private val draft = SignupDraft(
        fullName = "Arun Kumar", username = "arun_kumar", city = "Chennai", area = "", pincode = "600001",
        phoneNumber = "+919876543210", avatarUri = "content://media/picker/1", preferredLanguage = "English",
        email = "arun@example.com",
    )

    @Test fun releaseSignupSaysUnavailableAndSavesNothing() = runBlocking {
        val store = SessionProfileStore(SessionProfile(accountId = "u1"))
        val viewModel = ProfileViewModel(store, ProfileCapabilities.forBuild(clientPreview = false))
        assertFalse(viewModel.canSignUp)
        assertEquals(SignupResult.Unavailable, viewModel.completeSignup(draft) { true })
        assertEquals("", store.profile.value.displayName)
    }

    @Test fun previewSignupSavesAValidDraftAndBecomesReady() = runBlocking {
        val store = SessionProfileStore(SessionProfile(accountId = "u1"))
        val viewModel = ProfileViewModel(store, ProfileCapabilities.forBuild(clientPreview = true))
        assertEquals(SignupResult.Saved, viewModel.completeSignup(draft) { true })
        assertEquals("Arun Kumar", store.profile.value.displayName)
        assertEquals(ProfileLoadState.Ready, loadStateFor(store.profile.value))
    }

    @Test fun unreadablePhotoIsInvalidNotSaved() = runBlocking {
        val store = SessionProfileStore(SessionProfile(accountId = "u1"))
        val viewModel = ProfileViewModel(store, ProfileCapabilities.forBuild(clientPreview = true))
        assertEquals(SignupResult.Invalid, viewModel.completeSignup(draft) { false })
        assertTrue(store.profile.value.displayName.isEmpty())
    }
}
