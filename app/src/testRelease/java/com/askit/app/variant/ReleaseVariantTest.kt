package com.askit.app.variant

import com.askit.app.BuildConfig
import com.askit.app.creatordashboard.CreatorTimeRange
import com.askit.app.home.data.HomeFeedFixtures
import com.askit.app.inbox.InboxSeed
import com.askit.app.session.SessionProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Release guard: runs only in `testReleaseUnitTest`. It fails if demo/preview fixtures become
 * reachable from the release variant (for example if `src/demo/java` is added to release, or a
 * fixture is moved back into `src/main`).
 */
class ReleaseVariantTest {
    @Test fun releaseIsNeverAClientPreview() {
        assertFalse(BuildConfig.CLIENT_PREVIEW)
    }

    @Test fun releaseVariantSuppliesNoDemoContent() {
        assertFalse(AppVariant.hasDemoContent)
        assertTrue(AppVariant.seededTasks().isEmpty())
        assertTrue(AppVariant.homeStories().isEmpty())
        assertEquals(HomeFeedFixtures.Empty, AppVariant.homeFeed())
        assertTrue(AppVariant.explorePeople().isEmpty())
        assertEquals(InboxSeed.Empty, AppVariant.inboxSeed())
        assertTrue(AppVariant.incomingLeads().isEmpty())
        assertNull(AppVariant.serviceDetail("srv-1"))
        assertNull(AppVariant.publicProfile("preview-owner"))
        assertNull(AppVariant.previewProfile("preview-owner"))
        assertNull(AppVariant.providerDashboardSample(SessionProfile()))
        CreatorTimeRange.entries.forEach { assertNull(AppVariant.creatorDashboardSample(it, isServiceProvider = true)) }
    }

    @Test fun fixtureClassesAreNotOnTheReleaseClasspath() {
        val demoOnly = listOf(
            "com.askit.app.demo.HomeFixtures",
            "com.askit.app.demo.TaskFixturesKt",
            "com.askit.app.demo.ExploreFixturesKt",
            "com.askit.app.demo.InboxFixturesKt",
            "com.askit.app.demo.JobFixturesKt",
            "com.askit.app.demo.NearbyProfessionalsKt",
            "com.askit.app.demo.ServiceFixturesKt",
            "com.askit.app.demo.ProviderDashboardFixturesKt",
            "com.askit.app.demo.CreatorDashboardFixtures",
            "com.askit.app.preview.ClientPreviewKt",
            "com.askit.app.preview.PreviewProfileStorage",
            "com.askit.app.preview.PreviewPublicProfilesKt",
        )
        val reachable = demoOnly.filter { name ->
            runCatching { Class.forName(name, false, javaClass.classLoader) }.isSuccess
        }
        if (reachable.isNotEmpty()) fail("Demo fixtures reachable from release: $reachable")
    }
}
