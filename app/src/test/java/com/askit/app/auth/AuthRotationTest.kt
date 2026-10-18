package com.askit.app.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRotationTest {
    @Test fun configurationChangesKeepInFlightOtpRequests() {
        // Rotation / dark mode / locale switch: the view model survives, so must the request.
        assertFalse(shouldCancelAuthRequestOnDispose(isChangingConfigurations = true))
    }

    @Test fun leavingTheScreenStillCancels() {
        assertTrue(shouldCancelAuthRequestOnDispose(isChangingConfigurations = false))
    }
}
