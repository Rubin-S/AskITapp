package com.askit.app.profile

import com.askit.app.auth.AccountFormRules
import com.askit.app.auth.SignupDraft
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Edit profile follows the account contract: city is required; username, email and PIN code may
 * be empty but must be valid when given. Signup (Form A) still requires username, email and PIN.
 */
class EditProfileValidationTest {
    private val valid = EditProfileFormState(
        displayName = "Asha",
        username = "",
        city = "Karaikal",
        bio = "",
        avatarUrl = null,
        phoneNumber = "+919876543210",
    )

    @Test fun cityAndNameAreRequiredWhileUsernameEmailAndPinAreOptional() {
        assertTrue(valid.isValid)
        assertFalse(valid.copy(city = "  ").isValid)
        assertFalse(valid.copy(displayName = "").isValid)
        assertFalse(valid.copy(phoneNumber = "12345").isValid)
    }

    @Test fun optionalFieldsMustBeValidWhenGiven() {
        assertTrue(valid.copy(username = "asha_k", email = "asha@example.com", pincode = "609609").isValid)
        assertFalse(valid.copy(username = "asha k").isValid)
        assertFalse(valid.copy(email = "asha@").isValid)
        assertFalse(valid.copy(pincode = "012345").isValid)
        assertFalse(valid.copy(bio = "x".repeat(BIO_MAX_LENGTH + 1)).isValid)
    }

    @Test fun signupStillRequiresUsernameEmailAndPin() {
        val draft = SignupDraft("Asha", "asha", "", "609609", "+919876543210", null, "English", email = "asha@example.com")
        assertTrue(draft.validation(photoReadable = true).isValid)
        assertFalse(draft.copy(email = null).validation(photoReadable = true).isValid)
        assertFalse(draft.copy(username = "").validation(photoReadable = true).isValid)
        assertFalse(draft.copy(pincode = "").validation(photoReadable = true).isValid)
        assertTrue(draft.copy(email = null, city = "Karaikal").validation(true, AccountFormRules.Edit).isValid)
    }
}
