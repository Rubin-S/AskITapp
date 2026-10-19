package com.askit.app.format

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale

class DateTimeFormattingTest {
    private val ist = ZoneId.of("Asia/Kolkata")
    private val newYork = ZoneId.of("America/New_York")
    private val english = Locale.ENGLISH
    private val tamil = Locale.forLanguageTag("ta-IN")

    private fun utcMillis(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0) =
        LocalDateTime.of(year, month, day, hour, minute).toInstant(ZoneOffset.UTC).toEpochMilli()

    @Test fun pickerDatesRoundTripAndIgnoreTheDeviceZone() {
        val picked = utcMillis(2026, 10, 25) // what a Material date picker returns for 25 Oct
        assertEquals(LocalDate.of(2026, 10, 25), pickerDate(picked))
        assertEquals(picked, LocalDate.of(2026, 10, 25).toPickerMillis())
        // The old availability formatter used the device zone: in New York this showed 24 Oct.
        assertEquals(LocalDate.of(2026, 10, 24), localDateOf(picked, newYork))
        assertEquals(formatCalendarDate(LocalDate.of(2026, 10, 25), english), formatPickerDate(picked, english))
        assertTrue(formatPickerDate(picked, english).contains("25"))
    }

    @Test fun instantsAreShownOnTheDayTheyHappenWhereTheUserIs() {
        // 23:59:59 UTC on 25 Oct is already 05:29 on 26 Oct in IST.
        val lateUtc = utcMillis(2026, 10, 25, 23, 59) + 59_999
        assertEquals(LocalDate.of(2026, 10, 26), localDateOf(lateUtc, ist))
        assertEquals(formatCalendarDate(LocalDate.of(2026, 10, 26), english), formatInstantDate(lateUtc, english, ist))
        assertEquals(formatCalendarDate(LocalDate.of(2026, 10, 25), english), formatInstantDate(lateUtc, english, ZoneOffset.UTC))

        // 18:29 UTC is 23:59 IST (same day); 18:30 UTC is midnight IST (next day).
        val beforeIstMidnight = utcMillis(2026, 10, 25, 18, 29)
        val atIstMidnight = utcMillis(2026, 10, 25, 18, 30)
        assertEquals(LocalDate.of(2026, 10, 25), localDateOf(beforeIstMidnight, ist))
        assertEquals(LocalDate.of(2026, 10, 26), localDateOf(atIstMidnight, ist))
        assertNotEquals(formatInstantDate(beforeIstMidnight, english, ist), formatInstantDate(atIstMidnight, english, ist))
    }

    @Test fun pollClosingOnAPickedDayEndsAtThatDaysMidnightInTheUsersZone() {
        val picked = pickerDate(utcMillis(2026, 10, 25))
        val closesAt = endOfDayMillis(picked, ist)
        assertEquals(utcMillis(2026, 10, 25, 18, 30) - 1, closesAt)
        // Shown back as the day the user picked, and the picker reopens on it.
        assertEquals(formatCalendarDate(picked, english), formatInstantDate(closesAt, english, ist))
        assertEquals(utcMillis(2026, 10, 25), localDateOf(closesAt, ist).toPickerMillis())
        // The old "picked UTC midnight + 1 day - 1 ms" closed at 05:29 IST the next day and read as 26 Oct.
        val legacy = utcMillis(2026, 10, 25) + 24 * 60 * 60 * 1000L - 1
        assertEquals(LocalDate.of(2026, 10, 26), localDateOf(legacy, ist))
    }

    @Test fun formattingFollowsTheGivenLocale() {
        val day = LocalDate.of(2026, 10, 25)
        val englishText = formatCalendarDate(day, english)
        val tamilText = formatCalendarDate(day, tamil)
        assertTrue(englishText, englishText.contains("Oct"))
        assertNotEquals(englishText, tamilText)
        assertTrue(tamilText, tamilText.contains("2026"))
        val millis = utcMillis(2026, 10, 25, 6, 0)
        assertNotEquals(formatInstantDateTime(millis, english, ist), formatInstantDateTime(millis, tamil, ist))
    }
}
