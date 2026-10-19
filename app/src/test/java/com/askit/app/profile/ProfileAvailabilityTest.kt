package com.askit.app.profile

import com.askit.app.format.toPickerMillis
import com.askit.app.session.AvailabilityStatus
import com.askit.app.session.ProfileAvailability
import com.askit.app.session.ServiceListing
import com.askit.app.session.SessionProfile
import com.askit.app.session.statusOn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** A provider's "away" window is computed from the dates on each read, inclusive of both days. */
class ProfileAvailabilityTest {
    private val from = LocalDate.of(2026, 10, 20)
    private val to = LocalDate.of(2026, 10, 25)
    private val away = ProfileAvailability(available = false, fromMillis = from.toPickerMillis(), toMillis = to.toPickerMillis())

    @Test fun theWindowIsScheduledBeforeAwayDuringAndOverAfter() {
        assertEquals(AvailabilityStatus.Scheduled(from), away.statusOn(from.minusDays(1)))
        assertEquals(AvailabilityStatus.Away(to), away.statusOn(from))
        assertEquals(AvailabilityStatus.Away(to), away.statusOn(to))
        assertEquals(AvailabilityStatus.Available, away.statusOn(to.plusDays(1)))
    }

    @Test fun thePublishedListingIsPausedOnlyInsideTheWindow() {
        val profile = SessionProfile(accountId = "pro", displayName = "Asha", listing = listing(), availability = away)
        assertTrue(profile.toUiState(today = from.minusDays(1)).listing!!.live)
        assertFalse(profile.toUiState(today = from).listing!!.live)
        assertFalse(profile.toPublicProfile(today = to).listing!!.live)
        assertTrue(profile.toPublicProfile(today = to.plusDays(1)).listing!!.live)
    }

    private fun listing() = ServiceListing(
        title = "Carpentry",
        category = "carpentry",
        description = "Doors and windows",
        coverage = "Karaikal",
        hours = "9-5",
        hoursHint = "",
        response = "1h",
        tags = emptyList(),
        experience = "5",
    )
}
