package com.askit.app.profile

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
    val language: String = "English",
    val discloseApproximateLocation: Boolean = false,
) {
    val isValid: Boolean
        get() = accountDraft().validation(photoReadable = true).isValid && bio.length <= BioMaxLength && title.length <= 100

    fun accountDraft() = com.askit.app.auth.SignupDraft(displayName, username, city, pincode, phoneNumber, avatarUrl, language, email = email)

    fun isDirty(original: EditProfileFormState): Boolean =
        displayName != original.displayName ||
            username != original.username ||
            city != original.city ||
            bio != original.bio ||
            avatarUrl != original.avatarUrl || title != original.title || email != original.email ||
            pincode != original.pincode || phoneNumber != original.phoneNumber || language != original.language || discloseApproximateLocation != original.discloseApproximateLocation
}

const val BioMaxLength = 200

fun isUsernameValid(username: String): Boolean =
    username.matches(Regex("^[A-Za-z0-9._]+$"))
