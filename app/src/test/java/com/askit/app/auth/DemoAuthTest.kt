package com.askit.app.auth

import com.askit.app.R
import com.askit.app.di.AppConfig
import com.askit.app.session.SessionProfile
import com.askit.app.session.SessionProfileStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/** The demo build's fully local sign-in: Welcome first, "Explore demo", any valid number + OTP 123456. */
class DemoAuthTest {
    private fun demoAuth(store: SessionProfileStore = SessionProfileStore()) = AuthViewModel(
        clientPreview = true,
        profileStore = store,
        clock = { 0L },
        previewProfileLoader = { id -> SessionProfile(accountId = id, displayName = "Demo owner") },
        autoSignInPreviewOwner = false,
    )

    @Test fun demoBuildWaitsOnTheWelcomeScreenSignedOut() {
        val auth = demoAuth()
        assertNull(auth.session)
        assertNull(auth.state.value.destination)
    }

    @Test fun exploreDemoSignsInAsTheSeededOwner() {
        val store = SessionProfileStore()
        val auth = demoAuth(store)
        auth.enterDemo()
        assertEquals(PREVIEW_OWNER_ID, auth.session?.userId)
        assertEquals(PREVIEW_OWNER_ID, store.profile.value.accountId)
        assertEquals(AuthDestination.Home, auth.state.value.destination)
    }

    @Test fun anyValidNumberGetsALocalCodeAndOnlyTheTestOtpVerifies() {
        val auth = demoAuth()
        auth.requestCode("+919876543210")
        assertEquals(AuthDestination.Code, auth.state.value.destination)
        auth.verifyCode("000000")
        assertEquals(R.string.auth_invalid_code, auth.state.value.errorRes)
        assertNull(auth.session)
        auth.verifyCode(PREVIEW_OTP)
        assertEquals(AuthDestination.Signup, auth.state.value.destination)
        assertFalse(auth.session!!.profileComplete)
    }

    @Test fun signOutReturnsToWelcomeAndExploreDemoWorksAgain() {
        val auth = demoAuth()
        auth.enterDemo()
        auth.signOut()
        assertNull(auth.session)
        assertEquals(AuthDestination.Entry, auth.state.value.destination)
        auth.enterDemo()
        assertEquals(PREVIEW_OWNER_ID, auth.session?.userId)
    }

    @Test fun clientPreviewStillSignsStraightInAndDemoIsOffByDefault() {
        val auth = AuthViewModel(
            clientPreview = true,
            clock = { 0L },
            previewProfileLoader = { id -> SessionProfile(accountId = id) },
        )
        assertEquals(PREVIEW_OWNER_ID, auth.session?.userId)
        assertFalse(AppConfig(backendBaseUrl = null, clientPreview = true).demoBuild)
    }

    @Test fun enterDemoDoesNothingOutsideLocalBuilds() {
        val auth = AuthViewModel(clientPreview = false, clock = { 0L })
        auth.enterDemo()
        assertNull(auth.session)
    }
}
