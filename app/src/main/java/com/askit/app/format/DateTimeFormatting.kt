package com.askit.app.format

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/*
 * The one place AskIT turns times into text. Two kinds of value, never mixed:
 *
 * - Instants (createdAt, updatedAt, a poll's closing moment) are shown in the device's zone,
 *   because "when did this happen" is relative to where the reader is.
 * - Calendar dates (a task's requested day, availability from/to) are a day, not a moment. They
 *   are never shifted by a time zone. Material date pickers hand these over as midnight UTC
 *   millis, so they are read back in UTC ([pickerDate]) and formatted as a [LocalDate].
 *
 * Every formatter takes the locale explicitly (English or Tamil, from the app's configuration)
 * so a per-app language choice is honoured even where Locale.getDefault() lags behind.
 */

/** The app's current UI locale (follows the per-app language setting). */
@Composable
@ReadOnlyComposable
fun appLocale(): Locale = LocalConfiguration.current.locales[0]

/** Medium date of an instant in [zone], e.g. "25 Oct 2026" / "25 அக்., 2026". */
fun formatInstantDate(epochMillis: Long, locale: Locale, zone: ZoneId = ZoneId.systemDefault()): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).withZone(zone).format(Instant.ofEpochMilli(epochMillis))

/** Medium date and short time of an instant in [zone]. */
fun formatInstantDateTime(epochMillis: Long, locale: Locale, zone: ZoneId = ZoneId.systemDefault()): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(locale)
        .withZone(zone)
        .format(Instant.ofEpochMilli(epochMillis))

/** Medium date of a calendar day. No time zone is involved. */
fun formatCalendarDate(date: LocalDate, locale: Locale): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(date)

/** The calendar day a Material date-picker value (midnight UTC millis) stands for. */
fun pickerDate(pickerMillis: Long): LocalDate = Instant.ofEpochMilli(pickerMillis).atZone(ZoneOffset.UTC).toLocalDate()

/** The Material date-picker value (midnight UTC millis) for a calendar day. */
fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

/** Formats a stored date-picker value as the calendar day the user picked. */
fun formatPickerDate(pickerMillis: Long, locale: Locale): String = formatCalendarDate(pickerDate(pickerMillis), locale)

/** The day an instant falls on in [zone]. */
fun localDateOf(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()

/** The last millisecond of [date] in [zone]: "closes on this day" means until the day ends where the user is. */
fun endOfDayMillis(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long =
    date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

/**
 * "5 minutes ago" ([abbreviated] = false) or "5 mins ago" (true) at minute resolution; times in the
 * future read as "in 5 minutes", and anything older than a week falls back to a short date
 * ("3 Oct"). Uses the platform's localized relative-time strings, so Tamil comes from the system.
 */
fun relativeTimeLabel(timeMillis: Long, nowMillis: Long, abbreviated: Boolean = false): String = DateUtils.getRelativeTimeSpanString(
    timeMillis,
    nowMillis,
    DateUtils.MINUTE_IN_MILLIS,
    relativeTimeFlags(abbreviated),
).toString()

/** Both forms share the date fallback; only the relative part ("minutes" vs "mins") differs. */
internal fun relativeTimeFlags(abbreviated: Boolean): Int =
    DateUtils.FORMAT_ABBREV_MONTH or (if (abbreviated) DateUtils.FORMAT_ABBREV_RELATIVE else 0)
