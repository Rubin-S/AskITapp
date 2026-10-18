package com.askit.app.profile

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.askit.app.navigation.AppDestination
import com.askit.app.navigation.AskITNavigationState
import org.junit.Assert.*
import org.junit.Test

class ProfileReturnNavigationTest {
    @Test fun ownProfileNavigationClosesVisitorEntriesInTheProfileStack() {
        val stack = NavBackStack<NavKey>(AppDestination.Profile, AppDestination.UserProfile("other"))
        val route = mutableStateOf<NavKey>(AppDestination.Profile)
        val navigation = AskITNavigationState(route, mapOf(AppDestination.Profile to stack))
        navigation.navigateToRoot(AppDestination.Profile)
        assertEquals(listOf(AppDestination.Profile), stack.toList())
        assertTrue(navigation.isAtRoot)
    }
}
