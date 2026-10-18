package com.askit.app.session

import androidx.annotation.StringRes
import com.askit.app.R

/**
 * App languages. [storedValue] is what the profile and the account API carry
 * (docs/profile-backend-contract.md), so it must not change; [labelRes] is what the UI shows.
 */
enum class AppLanguage(
    val storedValue: String,
    val languageTag: String,
    /** Settings label, localised ("Tamil (தமிழ்)" in English, "தமிழ்" in Tamil). */
    @StringRes val labelRes: Int,
    /** The language's own name, the same in every locale. */
    @StringRes val endonymRes: Int,
) {
    English("English", "en", R.string.settings_language_english, R.string.language_endonym_english),
    Tamil("Tamil", "ta", R.string.settings_language_tamil, R.string.language_endonym_tamil),
    ;

    companion object {
        val storedValues: Set<String> = entries.mapTo(linkedSetOf(), AppLanguage::storedValue)

        /** Exact match on the stored value; anything else is English, as before. */
        fun fromStoredValue(value: String?): AppLanguage = entries.firstOrNull { it.storedValue == value } ?: English

        /** The language of a locale tag such as "ta" or "en-IN". */
        fun fromLanguageTag(languageTag: String): AppLanguage = if (languageTag == Tamil.languageTag) Tamil else English
    }
}

/** Who may start a conversation with the account. [storedValue] is the account API value. */
enum class MessagingAudience(val storedValue: String, @StringRes val labelRes: Int) {
    Everyone("Everyone", R.string.settings_message_everyone),
    ContactsOnly("Contacts only", R.string.settings_message_contacts),
    VerifiedOnly("Verified users only", R.string.settings_message_verified),
    ;

    companion object {
        val storedValues: Set<String> = entries.mapTo(linkedSetOf(), MessagingAudience::storedValue)

        fun fromStoredValue(value: String?): MessagingAudience? = entries.firstOrNull { it.storedValue == value }
    }
}

/**
 * The account's active role. [storedValue] is the persisted and wire value (unchanged English
 * keys); [labelRes] is what the UI shows.
 */
enum class AccountRole(val storedValue: String, @StringRes val labelRes: Int) {
    Seeker("Seeker", R.string.account_role_seeker),
    Provider("Provider", R.string.account_role_provider),
    Both("Both", R.string.account_role_both),
    ;

    companion object {
        val storedValues: Set<String> = entries.mapTo(linkedSetOf(), AccountRole::storedValue)

        fun fromStoredValue(value: String?): AccountRole? = entries.firstOrNull { it.storedValue == value }
    }
}
