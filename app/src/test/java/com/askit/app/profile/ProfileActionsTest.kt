package com.askit.app.profile

import com.askit.app.config.AppLinks
import com.askit.app.config.profileUrl
import org.junit.Assert.*
import org.junit.Test

class ProfileActionsTest {
    @Test fun actionSetsMatchAllFourContexts() {
        assertEquals(listOf(ProfileActionId.EDIT, ProfileActionId.VIEW_AS, ProfileActionId.PROVIDER_DASHBOARD), profileActionIds(true, true))
        assertEquals(listOf(ProfileActionId.FOLLOW, ProfileActionId.CONTACT, ProfileActionId.MESSAGE), profileActionIds(true, false))
        assertEquals(listOf(ProfileActionId.EDIT, ProfileActionId.CREATOR_DASHBOARD, ProfileActionId.POST_TASK), profileActionIds(false, true))
        assertEquals(listOf(ProfileActionId.FOLLOW, ProfileActionId.MESSAGE), profileActionIds(false, false))
    }
    @Test fun publicPreviewSelfAndSignedOutViewsCannotMutate() {
        assertFalse(canMutateProfileVisitor("owner", "owner", false))
        assertFalse(canMutateProfileVisitor("visitor", "owner", true))
        assertFalse(canMutateProfileVisitor(null, "owner", false))
        assertTrue(canMutateProfileVisitor("visitor", "owner", false))
    }
    @Test fun publicLinksRequireRealHttpsConfiguration() {
        assertNull(AppLinks().profileUrl("owner"))
        assertNull(AppLinks(publicProfileBaseUrl = "http://example.com/u").profileUrl("owner"))
        assertNull(AppLinks(publicProfileBaseUrl = "https://user:secret@example.com/u").profileUrl("owner"))
        assertNull(AppLinks(publicProfileBaseUrl = "https://example.com/u").profileUrl(""))
        assertEquals("https://example.com/u/account%2Fone", AppLinks(publicProfileBaseUrl = "https://example.com/u").profileUrl("account/one"))
    }
}
