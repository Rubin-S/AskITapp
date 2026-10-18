package com.askit.app.profile

import app.cash.turbine.test
import com.askit.app.R
import com.askit.app.auth.SignupDraft
import com.askit.app.auth.SignupResult
import com.askit.app.session.SessionProfile
import com.askit.app.session.SessionProfileStore
import com.askit.app.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileViewModelFlowTest {
    @get:Rule val main = MainDispatcherRule()

    private val draft = SignupDraft(
        fullName = "Arun Kumar", username = "arun_kumar", city = "Chennai", area = "", pincode = "600001",
        phoneNumber = "+919876543210", avatarUri = null, preferredLanguage = "English",
        email = "arun@example.com",
    )

    private class FailingRepository(store: SessionProfileStore) : ProfileRepository by LocalProfileRepository(store) {
        override fun completeSignup(draft: SignupDraft): Unit = error("backend down")
        override fun updateLanguage(lang: String): Unit = error("backend down")
    }

    @Test fun blankProfileIsFailedUntilSignupCompletesThenReady() = runTest {
        val store = SessionProfileStore(SessionProfile(accountId = "u1"))
        val vm = ProfileViewModel(LocalProfileRepository(store), signupAvailable = true)
        vm.loadState.test {
            assertEquals(ProfileLoadState.Failed(R.string.profile_load_error), awaitItem())
            assertEquals(SignupResult.Saved, vm.completeSignup(draft) { true })
            assertEquals(ProfileLoadState.Ready, awaitItem())
        }
    }

    @Test fun invalidSignupNeverChangesTheLoadState() = runTest {
        val store = SessionProfileStore(SessionProfile(accountId = "u1"))
        val vm = ProfileViewModel(LocalProfileRepository(store), signupAvailable = true)
        vm.loadState.test {
            awaitItem()
            assertEquals(SignupResult.Invalid, vm.completeSignup(draft.copy(email = "bad")) { true })
            expectNoEvents()
        }
    }

    @Test fun failedSignupEmitsOneErrorMessageAndStaysFailed() = runTest {
        val store = SessionProfileStore(SessionProfile(accountId = "u1"))
        val vm = ProfileViewModel(FailingRepository(store), signupAvailable = true)
        vm.messages.test {
            assertEquals(SignupResult.Failed, vm.completeSignup(draft) { true })
            assertEquals(R.string.profile_action_error, awaitItem())
            expectNoEvents()
        }
        assertTrue(store.profile.value.displayName.isEmpty())
    }

    @Test fun settingsFailuresAndResetsReportThroughMessages() = runTest {
        val store = SessionProfileStore(SessionProfile(accountId = "u1", displayName = "Arun"))
        val vm = ProfileViewModel(FailingRepository(store), signupAvailable = true)
        vm.messages.test {
            assertTrue(vm.updateLanguage("Tamil").isFailure)
            assertEquals(R.string.profile_action_error, awaitItem())
            assertTrue(vm.resetAppData().isSuccess)
            assertEquals(R.string.settings_data_cleared, awaitItem())
        }
        assertEquals("", store.profile.value.displayName)
    }

    @Test fun avatarChangesOutsideThePreviewAreRefusedWithAMessage() = runTest {
        // Unit tests run against debug/release BuildConfig, where CLIENT_PREVIEW is false.
        val store = SessionProfileStore(SessionProfile(accountId = "u1", displayName = "Arun", avatarUrl = "old"))
        val vm = ProfileViewModel(LocalProfileRepository(store), signupAvailable = false)
        vm.messages.test {
            vm.setAvatar("u1", "content://new")
            assertEquals(R.string.profile_action_error, awaitItem())
        }
        assertEquals("old", store.profile.value.avatarUrl)
    }
}
