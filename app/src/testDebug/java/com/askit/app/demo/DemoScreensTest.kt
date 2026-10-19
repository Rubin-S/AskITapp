package com.askit.app.demo

import com.askit.app.navigation.AppDestination
import com.askit.app.navigation.TOP_LEVEL_ROUTES
import com.askit.app.variant.AppVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoScreensTest {
    private val screens = demoScreens()

    @Test fun menuListsEachScreenOnceIncludingWelcomeAndEveryTab() {
        val destinations = screens.map { it.destination }
        assertEquals(destinations.size, destinations.toSet().size)
        assertTrue(destinations.containsAll(TOP_LEVEL_ROUTES))
        assertTrue(AppDestination.Entry in destinations)
    }

    @Test fun onlyAuthScreensAreOfferedSignedOut() {
        val signedOut = screens.filterNot { it.needsSignIn }.map { it.destination::class }
        assertEquals(
            setOf(AppDestination.Entry::class, AppDestination.AuthPhone::class, AppDestination.AuthOtp::class, AppDestination.FormA::class),
            signedOut.toSet(),
        )
    }

    @Test fun parameterisedScreensPointAtSeededFixtures() {
        screens.map { it.destination }.forEach { destination ->
            when (destination) {
                is AppDestination.TaskDetail -> assertTrue(sampleTasks().any { it.id == destination.taskId })
                is AppDestination.ServiceDetail -> assertNotNull(sampleServiceDetail(destination.serviceId))
                is AppDestination.UserProfile -> assertNotNull(AppVariant.publicProfile(destination.userId))
                is AppDestination.StoryViewer -> assertTrue(HomeFixtures.stories().any { it.id == destination.startStoryId })
                is AppDestination.PostDetail -> assertTrue(HomeFixtures.feed().posts.any { it.id == destination.postId })
                is AppDestination.ChatThread -> assertTrue(sampleInboxSeed().conversations.any { it.id == destination.conversationId })
                is AppDestination.JobDetail -> assertTrue(clientPreviewJobs().any { it.id == destination.jobId })
                is AppDestination.JobVerifyEnter -> assertTrue(clientPreviewJobs().any { it.id == destination.jobId })
                is AppDestination.JobVerifyShare -> assertTrue(clientPreviewJobs().any { it.id == destination.jobId })
                is AppDestination.JobReview -> assertTrue(clientPreviewJobs().any { it.id == destination.jobId })
                else -> Unit
            }
        }
    }
}
