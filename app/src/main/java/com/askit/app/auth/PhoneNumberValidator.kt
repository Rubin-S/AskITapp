package com.askit.app.auth

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType

internal sealed interface PhoneValidation {
    data class Valid(val e164: String) : PhoneValidation
    data object InvalidNumber : PhoneValidation
    data object CountryMismatch : PhoneValidation
}

internal object PhoneNumberValidator {
    private val phoneUtil by lazy { PhoneNumberUtil.getInstance() }
    private val supportedRegions = defaultCountryCodes.map { it.isoCode }.toSet()

    /** A null country accepts only international input from a supported region. */
    fun validate(input: String, countryIso: String? = null): PhoneValidation {
        if (countryIso != null && countryIso !in supportedRegions) return PhoneValidation.InvalidNumber
        val compact = normalize(input) ?: return PhoneValidation.InvalidNumber
        val international = compact.startsWith('+')
        if (!international && countryIso == null) return PhoneValidation.InvalidNumber
        if (!compact.matches(Regex("\\+?[0-9]+"))) return PhoneValidation.InvalidNumber

        val number = try {
            phoneUtil.parse(compact, countryIso ?: "ZZ")
        } catch (_: NumberParseException) {
            return PhoneValidation.InvalidNumber
        }
        val inferredRegion = phoneUtil.getRegionCodeForNumber(number)
        if (countryIso != null && international &&
            (
                number.countryCode != phoneUtil.getCountryCodeForRegion(countryIso) ||
                    (inferredRegion != null && inferredRegion != countryIso && phoneUtil.isValidNumber(number))
                )
        ) {
            return PhoneValidation.CountryMismatch
        }
        val region = countryIso ?: inferredRegion
        if (region !in supportedRegions) return PhoneValidation.InvalidNumber

        // Do not let the parser remove extra national digits or a trunk prefix for India.
        if (region == "IN") {
            val nationalDigits = if (international) compact.removePrefix("+91") else compact
            if (nationalDigits.length != 10) return PhoneValidation.InvalidNumber
        }
        if (!phoneUtil.isValidNumberForRegion(number, region)) return PhoneValidation.InvalidNumber
        if (phoneUtil.getNumberType(number) !in setOf(PhoneNumberType.MOBILE, PhoneNumberType.FIXED_LINE_OR_MOBILE)) {
            return PhoneValidation.InvalidNumber
        }
        val e164 = phoneUtil.format(number, PhoneNumberFormat.E164)
        // Explicit international input must not lose a digit through national-prefix repair.
        if (international && compact != e164) return PhoneValidation.InvalidNumber
        if (!e164.matches(Regex("\\+[1-9][0-9]{6,14}"))) return PhoneValidation.InvalidNumber
        return PhoneValidation.Valid(e164)
    }

    /** Strip formatting only, never unknown characters, extensions, or excess digits. */
    private fun normalize(input: String): String? {
        val normalized = StringBuilder()
        var parenthesesOpen = false
        for (codePoint in input.codePoints().toArray()) {
            when {
                Character.getType(codePoint) == Character.DECIMAL_DIGIT_NUMBER.toInt() ->
                    normalized.append(Character.digit(codePoint, 10))

                Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint) || codePoint == '-'.code -> Unit

                codePoint == '+'.code -> normalized.append('+')

                codePoint == '('.code -> {
                    if (parenthesesOpen) return null
                    parenthesesOpen = true
                }

                codePoint == ')'.code -> {
                    if (!parenthesesOpen) return null
                    parenthesesOpen = false
                }

                else -> return null
            }
        }
        return if (parenthesesOpen) null else normalized.toString()
    }
}
