package com.askit.app.format

import android.text.format.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The strings themselves come from the platform (DateUtils), which plain JVM tests cannot run and
 * Robolectric cannot load on this project's arm64 hosts; this pins the one thing AskIT decides.
 */
class RelativeTimeLabelTest {
    @Test fun abbreviatedOnlyAddsTheAbbreviatedRelativeFlag() {
        assertEquals(DateUtils.FORMAT_ABBREV_MONTH, relativeTimeFlags(abbreviated = false))
        assertEquals(DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_ABBREV_RELATIVE, relativeTimeFlags(abbreviated = true))
    }
}
