package com.askit.app.variant

import com.askit.app.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Release guard (runs only in testReleaseUnitTest). A client-preview release would accept any
 * phone number with OTP 123456, and demo fixtures would show fake people as real ones.
 */
class ReleaseVariantTest {
    @Test fun releaseIsNeitherAClientPreviewNorADemo() {
        assertFalse(BuildConfig.CLIENT_PREVIEW)
        assertFalse(BuildConfig.DEMO_BUILD)
        assertFalse(AppVariant.hasDemoContent)
        assertTrue(AppVariant.explorePeople().isEmpty())
        assertTrue(AppVariant.incomingLeads().isEmpty())
    }

    @Test fun demoFixtureClassesAreNotOnTheReleaseClasspath() {
        val demoOnly = listOf(
            "com.askit.app.demo.HomeFixtures",
            "com.askit.app.demo.ExploreFixturesKt",
            "com.askit.app.demo.InboxFixturesKt",
            "com.askit.app.demo.JobFixturesKt",
            "com.askit.app.preview.ClientPreviewKt",
            "com.askit.app.preview.PreviewProfileStorage",
            "com.askit.app.preview.PreviewPublicProfilesKt",
        )
        val reachable = demoOnly.filter { runCatching { Class.forName(it, false, javaClass.classLoader) }.isSuccess }
        assertEquals(emptyList<String>(), reachable)
    }
}
