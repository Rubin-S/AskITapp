package com.askit.app.auth

import com.askit.app.profile.EditProfileFormState
import org.junit.Assert.*
import org.junit.Test

class AccountFormTest {
    private fun draft() = SignupDraft("Arun Kumar", "arun", "", "600001", "+919876543210", null, "English", email = "arun@example.com")

    @Test fun photoAndCityAreOptionalButAnUnreadableProvidedPhotoIsRejected() {
        assertTrue(draft().validation(false).isValid)
        assertFalse(draft().copy(avatarUri = "content://unreadable").validation(false).isValid)
    }
    @Test fun emailPostcodePhoneAndLanguageAreRequired() {
        for (invalid in listOf(draft().copy(email = null), draft().copy(email = "bad"),
            draft().copy(pincode = ""), draft().copy(pincode = "000000"),
            draft().copy(phoneNumber = "12"), draft().copy(preferredLanguage = ""))) {
            assertFalse(invalid.toString(), invalid.validation(true).isValid)
        }
    }
    @Test fun editingUsesTheSameAccountValidationAndTracksEveryField() {
        val original = EditProfileFormState("Arun Kumar", "arun", "", "", null,
            email = "arun@example.com", pincode = "600001", phoneNumber = "+919876543210", language = "English")
        assertTrue(original.isValid)
        assertFalse(original.copy(email = "").isValid)
        assertFalse(original.copy(pincode = "12").isValid)
        assertTrue(original.copy(title = "Homeowner").isDirty(original))
        assertTrue(original.copy(language = "Tamil").isDirty(original))
    }
    @Test fun previewSignupCannotOverwriteThePreviouslySignedInAccount() {
        val saved = mutableMapOf<String, com.askit.app.session.SessionProfile>()
        val store = com.askit.app.session.SessionProfileStore(persist = { saved[it.accountId] = it; it })
        // Preview personas live in the demo source set, so supply the signed-in owner explicitly.
        val auth = AuthViewModel(clientPreview = true, profileStore = store, clock = { 0L },
            previewProfileLoader = { id -> com.askit.app.session.SessionProfile(accountId = id, displayName = "Preview owner") })
        assertEquals("preview-owner", store.profile.value.accountId)
        val owner = store.profile.value
        auth.signOut()
        auth.requestCode("+919876543210")
        auth.verifyCode("123456")
        store.completeSignup(SignupDraft("New person", "new.person", "", "641001", "+919876543210", null, "English", email = "new@example.com"))
        assertEquals(auth.session!!.userId, store.profile.value.accountId)
        assertEquals(owner, saved[owner.accountId])
    }
}
