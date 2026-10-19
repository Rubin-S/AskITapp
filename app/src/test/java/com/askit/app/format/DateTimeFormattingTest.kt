package com.askit.app.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale

/**
 * Instants are shown on the day they happen where the user is (IST is UTC+05:30, so the day
 * changes at 18:30 UTC); picked calendar days are never shifted by a zone.
 */
class DateTimeFormattingTest {
    private val ist = ZoneId.of("Asia/Kolkata")
    private val newYork = ZoneId.of("America/New_York")

    private fun utcMillis(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0) =
        LocalDateTime.of(year, month, day, hour, minute).toInstant(ZoneOffset.UTC).toEpochMilli()

    @Test fun anInstantFallsOnTheIstDayAcrossTheMidnightBoundary() {
        val lastIstMinute = utcMillis(2026, 10, 25, 18, 29)
        val istMidnight = utcMillis(2026, 10, 25, 18, 30)
        assertEquals(LocalDate.of(2026, 10, 25), localDateOf(lastIstMinute, ist))
        assertEquals(LocalDate.of(2026, 10, 26), localDateOf(istMidnight, ist))
        assertEquals(
            formatCalendarDate(LocalDate.of(2026, 10, 26), Locale.ENGLISH),
            formatInstantDate(istMidnight, Locale.ENGLISH, ist),
        )
    }

    @Test fun aPickedDayIsTheSameDayInEveryZone() {
        val picked = LocalDate.of(2026, 10, 25).toPickerMillis()
        assertEquals(utcMillis(2026, 10, 25), picked)
        assertEquals(LocalDate.of(2026, 10, 25), pickerDate(picked))
        // Reading the picker value in the device zone (the old bug) shows the day before in New York.
        assertEquals(LocalDate.of(2026, 10, 24), localDateOf(picked, newYork))
        assertEquals(formatCalendarDate(LocalDate.of(2026, 10, 25), Locale.ENGLISH), formatPickerDate(picked, Locale.ENGLISH))
    }
}
