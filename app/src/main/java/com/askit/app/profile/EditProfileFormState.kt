package com.askit.app.profile

import com.askit.app.auth.AccountFormRules
import com.askit.app.auth.SignupDraft
import com.askit.app.session.AppLanguage

@kotlinx.serialization.Serializable
data class EditProfileFormState(
    val displayName: String,
    val username: String,
    val city: String,
    val bio: String,
    val avatarUrl: String?,
    val title: String = "",
    val email: String = "",
    val pincode: String = "",
    val phoneNumber: String = "",
    val language: String = AppLanguage.English.storedValue,
    val discloseApproximateLocation: Boolean = false,
) {
    /** Checked against the account contract (city required; username, email and PIN optional). */
    val isValid: Boolean
        get() = accountDraft().validation(photoReadable = true, rules = AccountFormRules.Edit).isValid &&
            bio.length <= BIO_MAX_LENGTH && title.length <= TITLE_MAX_LENGTH

    fun accountDraft() = SignupDraft(displayName, username, city, pincode, phoneNumber, avatarUrl, language, email = email)
}

const val BIO_MAX_LENGTH = 200
const val TITLE_MAX_LENGTH = 100

/** Kept in the profile package: `/v1/me` validation (a detekt-baselined condition) names it here. */
fun isUsernameValid(username: String): Boolean = username.matches(Regex("^[A-Za-z0-9._]+$"))
