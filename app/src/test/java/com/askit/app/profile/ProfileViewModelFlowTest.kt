package com.askit.app.profile

import app.cash.turbine.test
import com.askit.app.R
import com.askit.app.auth.SignupDraft
import com.askit.app.auth.SignupResult
import com.askit.app.session.DefaultCarpentryListing
import com.askit.app.session.ProfileAvailability
import com.askit.app.session.SessionProfile
import com.askit.app.session.SessionProfileStore
import com.askit.app.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileViewModelFlowTest {
    @get:Rule val main = MainDispatcherRule()

    private val preview = ProfileCapabilities.forBuild(clientPreview = true)
    private val release = ProfileCapabilities.forBuild(clientPreview = false)

    private val draft = SignupDraft(
        fullName = "Arun Kumar", username = "arun_kumar", city = "Chennai", area = "", pincode = "600001",
        phoneNumber = "+919876543210", avatarUri = null, preferredLanguage = "English",
        email = "arun@example.com",
    )

    private class FailingRepository(store: SessionProfileStore) : ProfileRepository by store {
        override fun completeSignup(draft: SignupDraft): Unit = error("backend down")
        override fun updateWhoCanMessage(option: String): Unit = error("backend down")
    }

    private val listingDraft = com.askit.app.listservice.ListServiceDraft(
        categoryId = "carpenter",
        title = "Carpentry",
        description = "Repairs",
        deliveryModes = emptySet(),
        details = com.askit.app.listservice.ListServiceDetails("Weekdays", "5", null, null, null, null, null),
    )

    private fun vm(
        store: ProfileRepository,
        capabilities: ProfileCapabilities = preview,
        settings: DeviceSettings = InMemoryDeviceSettings(),
        cleaner: LocalDataCleaner = LocalDataCleaner {},
    ) = ProfileViewModel(store, capabilities, settings, cleaner, main.dispatcher)

    @Test fun blankProfileIsFailedUntilSignupCompletesThenReady() = runTest {
        val vm = vm(SessionProfileStore(SessionProfile(accountId = "u1")))
        vm.loadState.test {
            assertEquals(ProfileLoadState.Failed(R.string.profile_load_error), awaitItem())
            assertEquals(SignupResult.Saved, vm.completeSignup(draft) { true })
            assertEquals(ProfileLoadState.Ready, awaitItem())
        }
    }

    @Test fun invalidSignupNeverChangesTheLoadState() = runTest {
        val vm = vm(SessionProfileStore(SessionProfile(accountId = "u1")))
        vm.loadState.test {
            awaitItem()
            assertEquals(SignupResult.Invalid, vm.completeSignup(draft.copy(email = "bad")) { true })
            expectNoEvents()
        }
    }

    @Test fun failedWritesEmitOneErrorMessageForTheAppShell() = runTest {
        val store = SessionProfileStore(SessionProfile(accountId = "u1", displayName = "Arun"))
        val vm = vm(FailingRepository(store))
        vm.messages.test {
            assertEquals(SignupResult.Failed, vm.completeSignup(draft) { true })
            assertEquals(R.string.profile_action_error, awaitItem())
            vm.updateWhoCanMessage("Contacts only")
            assertEquals(R.string.profile_action_error, awaitItem())
            expectNoEvents()
        }
    }

    @Test fun messagesAreBufferedUntilSomeoneCollects() = runTest {
        // Settings and Edit used to lose these: the old SharedFlow dropped them with no collector.
        val vm = vm(FailingRepository(SessionProfileStore(SessionProfile(accountId = "u1", displayName = "Arun"))))
        vm.updateWhoCanMessage("Contacts only")
        vm.messages.test { assertEquals(R.string.profile_action_error, awaitItem()) }
    }

    @Test fun releaseRefusesEveryProfileWriteUpFrontAndChangesNothing() = runTest {
        val original =
            SessionProfile(accountId = "u1", displayName = "Arun", avatarUrl = "https://a/old.jpg", listing = DefaultCarpentryListing)
        val store = SessionProfileStore(original)
        val vm = vm(store, release)
        assertFalse(vm.canSignUp)
        assertEquals(SignupResult.Unavailable, vm.completeSignup(draft) { true })
        assertFalse(vm.saveIdentity(EditProfileFormState("Arun K", "arun", "Chennai", "", null, phoneNumber = "+919876543210")))
        assertFalse(vm.setBanner("content://banner"))
        assertFalse(vm.saveHighlight(ProfileHighlight("h", "u1", "Work")))
        assertFalse(vm.deleteHighlight("h"))
        assertFalse(vm.saveListing(DefaultCarpentryListing, listingDraft))
        vm.setAvatar("content://new")
        vm.updateWhoCanMessage("Contacts only")
        vm.updatePushNotifications(false)
        vm.updateJobAlerts(false)
        vm.updateAvailability(ProfileAvailability(available = false, fromMillis = 0, toMillis = 1))
        assertEquals(original, store.profile.value)
        vm.messages.test { expectNoEvents() }
    }

    @Test fun previewWritesGoThroughTheRepository() = runTest {
        val store = SessionProfileStore(SessionProfile(accountId = "u1", displayName = "Arun"))
        val vm = vm(store)
        vm.setAvatar("content://new")
        vm.updatePushNotifications(false)
        vm.updateWhoCanMessage("Contacts only")
        assertTrue(vm.setBanner("content://banner"))
        with(store.profile.value) {
            assertEquals("content://new", avatarUrl)
            assertFalse(pushNotificationsEnabled)
            assertTrue(jobAlertsEnabled)
            assertEquals("Contacts only", whoCanMessage)
            assertEquals("content://banner", bannerUrl)
        }
    }

    @Test fun locationIsADevicePreferenceThatTheNextAccountLoadCannotOverwrite() = runTest {
        val store = SessionProfileStore(SessionProfile(accountId = "u1", displayName = "Arun"))
        val vm = vm(store, release)
        vm.setLocationServicesEnabled(false)
        // A fresh /v1/me replaces the whole profile; the switch lives outside it.
        store.replaceAccount(SessionProfile(accountId = "u1", displayName = "Arun from server"))
        assertFalse(vm.locationServicesEnabled.value)
    }

    @Test fun languageFollowsTheDeviceInEveryBuild() = runTest {
        val store = SessionProfileStore(SessionProfile(accountId = "u1", displayName = "Arun"))
        vm(store, release).updateLanguage("Tamil")
        assertEquals("Tamil", store.profile.value.selectedLanguage)
    }

    @Test fun clearAppDataDeletesLocalDataForgetsTheAccountAndNeverPersistsABlankPersona() = runTest {
        val saved = mutableMapOf<String, SessionProfile>()
        val store = SessionProfileStore(SessionProfile(accountId = "preview-owner", displayName = "Meera")) { profile ->
            if (profile.accountId.isNotBlank()) saved[profile.accountId] = profile
            profile
        }
        var cleared = 0
        var done = false
        val vm = vm(store, cleaner = { cleared++ })
        vm.resetAppData { done = true }
        assertEquals(1, cleared)
        assertTrue(done)
        assertEquals(SessionProfile(), store.profile.value)
        // The stored persona is never overwritten by a blank copy, so re-entering it can't fail to load.
        assertNull(saved["preview-owner"])
        assertEquals(ProfileLoadState.Failed(R.string.profile_load_error), loadStateFor(store.profile.value))
    }

    @Test fun clearAppDataReportsAFailureAndDoesNotSignOutHalfway() = runTest {
        val store = SessionProfileStore(SessionProfile(accountId = "u1", displayName = "Arun"))
        var done = false
        val vm = vm(store, cleaner = { error("disk") })
        vm.messages.test {
            vm.resetAppData { done = true }
            assertEquals(R.string.profile_action_error, awaitItem())
        }
        assertFalse(done)
        assertEquals("Arun", store.profile.value.displayName)
    }
}
