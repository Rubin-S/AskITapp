package com.askit.app.auth

import android.content.ContentResolver
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import com.askit.app.profile.isUsernameValid

data class SignupDraft(
    val fullName: String,
    val username: String,
    val city: String,
    val pincode: String,
    val phoneNumber: String,
    val avatarUri: String?,
    val preferredLanguage: String,
    val area: String = "",
    val email: String? = null,
) {
    fun validation(photoReadable: Boolean) = SignupValidation(
        photoValid = avatarUri.isNullOrBlank() || photoReadable,
        nameValid = fullName.isNotBlank(),
        usernameValid = isUsernameValid(username),
        postcodeValid = validAccountPostcode(pincode),
        phoneValid = validAccountPhone(phoneNumber),
        languageValid = preferredLanguage in setOf("English", "Tamil"),
        emailValid = validAccountEmail(email),
    )

    fun normalized() = copy(
        fullName = fullName.trim(),
        username = username.trim(),
        city = city.trim(),
        area = area.trim(),
        pincode = pincode.trim(),
        phoneNumber = (PhoneNumberValidator.validate(phoneNumber, if (phoneNumber.startsWith('+')) null else "IN") as? PhoneValidation.Valid)?.e164 ?: phoneNumber.trim(),
        email = email?.trim()?.takeIf(String::isNotBlank),
    )
}

data class SignupValidation(
    val photoValid: Boolean,
    val nameValid: Boolean,
    val usernameValid: Boolean,
    val postcodeValid: Boolean,
    val phoneValid: Boolean,
    val languageValid: Boolean,
    val emailValid: Boolean,
) {
    val isValid: Boolean
        get() = photoValid && nameValid && usernameValid && postcodeValid && phoneValid && languageValid && emailValid
}

/** Decodes a small preview to check readability without allocating the full photo. Call off main. */
internal fun isSignupPhotoReadable(contentResolver: ContentResolver, avatarUri: String?): Boolean {
    if (avatarUri.isNullOrBlank()) return false
    return runCatching {
        val uri = avatarUri.toUri()
        if (uri.scheme != ContentResolver.SCHEME_CONTENT) return false
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
            Unit // Bounds-only decoding intentionally returns no bitmap.
        } ?: return false
        if (options.outWidth <= 0 || options.outHeight <= 0) return false
        options.inJustDecodeBounds = false
        options.inSampleSize = maxOf(options.outWidth / 128, options.outHeight / 128, 1)
        val preview = contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        } ?: return false
        preview.recycle()
        true
    }.getOrDefault(false)
}
