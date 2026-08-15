package com.askit.app.profile

data class EditProfileFormState(
    val displayName: String,
    val username: String,
    val city: String,
    val bio: String,
    val avatarUrl: String?,
    val usernameTouched: Boolean = false,
    val displayNameTouched: Boolean = false,
) {
    val usernameError: Boolean
        get() = usernameTouched && !isUsernameValid(username)

    val displayNameError: Boolean
        get() = displayNameTouched && displayName.isBlank()

    val isValid: Boolean
        get() = displayName.isNotBlank() && isUsernameValid(username) && bio.length <= BioMaxLength

    fun isDirty(original: EditProfileFormState): Boolean =
        displayName != original.displayName ||
            username != original.username ||
            city != original.city ||
            bio != original.bio ||
            avatarUrl != original.avatarUrl
}

const val BioMaxLength = 200

fun isUsernameValid(username: String): Boolean =
    username.matches(Regex("^[A-Za-z0-9._]+$"))
