package com.askit.app.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The stored values are an API contract; these tests stop a rename from silently breaking it. */
class AccountSettingsTest {
    @Test fun languageStoredValuesAreUnchanged() {
        assertEquals(setOf("English", "Tamil"), AppLanguage.storedValues)
        assertEquals("ta", AppLanguage.fromStoredValue("Tamil").languageTag)
        assertEquals("en", AppLanguage.fromStoredValue("English").languageTag)
    }

    @Test fun unknownOrDifferentlyCasedLanguagesFallBackToEnglishAsBefore() {
        assertEquals(AppLanguage.English, AppLanguage.fromStoredValue("tamil"))
        assertEquals(AppLanguage.English, AppLanguage.fromStoredValue(null))
        assertEquals(AppLanguage.Tamil, AppLanguage.fromLanguageTag("ta"))
        assertEquals(AppLanguage.English, AppLanguage.fromLanguageTag("hi"))
    }

    @Test fun messagingAudienceStoredValuesAreUnchanged() {
        assertEquals(setOf("Everyone", "Contacts only", "Verified users only"), MessagingAudience.storedValues)
        assertEquals(MessagingAudience.ContactsOnly, MessagingAudience.fromStoredValue("Contacts only"))
        assertNull(MessagingAudience.fromStoredValue("everyone"))
    }

    @Test fun profileDefaultsUseTheStoredValues() {
        assertEquals("English", SessionProfile().selectedLanguage)
        assertEquals("Everyone", SessionProfile().whoCanMessage)
    }
}
