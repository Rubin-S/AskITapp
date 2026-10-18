package com.askit.app.auth

/** Shared Form A rules; verification of phone/email ownership belongs to the server. */
internal fun validAccountEmail(value: String?): Boolean = value != null && value.length <= 254 &&
    value.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))

internal fun validAccountPostcode(value: String): Boolean = value.matches(Regex("[1-9][0-9]{5}"))

internal fun validAccountPhone(value: String): Boolean =
    PhoneNumberValidator.validate(value, if (value.startsWith('+')) null else "IN") is PhoneValidation.Valid
