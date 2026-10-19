package com.askit.app.profile

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.askit.app.navigation.AppDestination
import com.askit.app.navigation.AskITNavigationState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileReturnNavigationTest {
    private val home = NavBackStack<NavKey>(AppDestination.Home)
    private val explore = NavBackStack<NavKey>(AppDestination.Explore)
    private val profile = NavBackStack<NavKey>(AppDestination.Profile)
    private val route = mutableStateOf<NavKey>(AppDestination.Home)
    private val navigation = AskITNavigationState(
        route,
        mapOf(AppDestination.Home to home, AppDestination.Explore to explore, AppDestination.Profile to profile),
    )

    @Test fun ownProfileNavigationClosesVisitorEntriesInTheProfileStack() {
        profile.add(AppDestination.UserProfile("other"))
        route.value = AppDestination.Profile
        navigation.navigateToRoot(AppDestination.Profile)
        assertEquals(listOf(AppDestination.Profile), profile.toList())
        assertTrue(navigation.isAtRoot)
    }

    @Test fun openingYourOwnIdFromHomeGoesToTheProfileTabWithoutTouchingHome() {
        navigation.openProfile("me", currentUserId = "me")
        assertEquals(AppDestination.Profile, navigation.topLevelRoute)
        assertEquals(listOf(AppDestination.Home), home.toList())
    }

    @Test fun anotherPersonIsPushedOnTheCurrentTab() {
        navigation.openProfile("other", currentUserId = "me")
        assertEquals(listOf(AppDestination.Home, AppDestination.UserProfile("other")), home.toList())
    }

    @Test fun aSelfEntryPushedEarlierIsRemovedSoHomeNeverBouncesBackToProfile() {
        // A stale UserProfile(self), e.g. pushed before sign-in resolved, must not trap the Home tab.
        home.add(AppDestination.UserProfile("me"))
        explore.add(AppDestination.UserProfile("me"))
        navigation.openProfile("me", currentUserId = "me")
        assertEquals(listOf(AppDestination.Home), home.toList())
        assertEquals(listOf(AppDestination.Explore), explore.toList())
        assertTrue(navigation.goBack())
        assertEquals(AppDestination.Home, navigation.topLevelRoute)
        assertTrue(navigation.isAtRoot)
    }

    @Test fun aBlankIdIsNeverTreatedAsTheOwner() {
        navigation.openProfile("", currentUserId = "")
        assertEquals(AppDestination.UserProfile(""), home.last())
    }
}
