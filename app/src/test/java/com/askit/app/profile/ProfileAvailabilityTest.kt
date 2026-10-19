package com.askit.app.profile

import com.askit.app.format.toPickerMillis
import com.askit.app.session.AvailabilityStatus
import com.askit.app.session.DefaultCarpentryListing
import com.askit.app.session.ProfileAvailability
import com.askit.app.session.SessionProfile
import com.askit.app.session.SessionProfileStore
import com.askit.app.session.isAvailableOn
import com.askit.app.session.statusOn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProfileAvailabilityTest {
    private val today = LocalDate.of(2026, 10, 19)
    private fun away(from: LocalDate, to: LocalDate) =
        ProfileAvailability(available = false, fromMillis = from.toPickerMillis(), toMillis = to.toPickerMillis())

    @Test fun aFutureStartOnlySchedulesThePause() {
        val window = away(today.plusDays(3), today.plusDays(5))
        assertEquals(AvailabilityStatus.Scheduled(today.plusDays(3)), window.statusOn(today))
        assertTrue(window.isAvailableOn(today))
    }

    @Test fun theWindowIsInclusiveAndEndsByItselfTheDayAfterTo() {
        val window = away(today, today.plusDays(2))
        assertEquals(AvailabilityStatus.Away(today.plusDays(2)), window.statusOn(today))
        assertEquals(AvailabilityStatus.Away(today.plusDays(2)), window.statusOn(today.plusDays(2)))
        assertEquals(AvailabilityStatus.Available, window.statusOn(today.plusDays(3)))
    }

    @Test fun availableIgnoresAnyStoredDates() {
        assertEquals(AvailabilityStatus.Available, ProfileAvailability().statusOn(today))
        assertEquals(AvailabilityStatus.Available, away(today, today).copy(available = true).statusOn(today))
    }

    @Test fun bothDirectionsSaveAndTheListingStatusIsComputedAtReadTime() {
        val store = SessionProfileStore(SessionProfile(accountId = "p", listing = DefaultCarpentryListing.copy(live = false)))
        store.updateAvailability(away(today.plusDays(1), today.plusDays(4)))
        // Saving re-publishes; the window alone decides what people see on a given day.
        assertTrue(store.profile.value.listing!!.live)
        assertTrue(store.profile.value.toUiState(today = today).listing!!.live)
        assertFalse(store.profile.value.toUiState(today = today.plusDays(2)).listing!!.live)
        assertFalse(store.profile.value.toPublicProfile(today.plusDays(2)).listing!!.live)
        assertTrue(store.profile.value.toUiState(today = today.plusDays(5)).listing!!.live)
        store.updateAvailability(ProfileAvailability())
        assertTrue(store.profile.value.toUiState(today = today.plusDays(2)).listing!!.live)
    }

    @Test fun awayNeedsBothDays() {
        val store = SessionProfileStore(SessionProfile(accountId = "p"))
        assertThrows(IllegalArgumentException::class.java) {
            store.updateAvailability(ProfileAvailability(available = false, fromMillis = today.toPickerMillis()))
        }
    }
}
