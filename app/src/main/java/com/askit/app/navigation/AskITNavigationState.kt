package com.askit.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer

internal class AskITNavigationState(
    private val topLevelRouteState: MutableState<NavKey>,
    private val backStacks: Map<AppDestination, NavBackStack<NavKey>>,
) {
    val topLevelRoute: AppDestination
        get() = topLevelRouteState.value as AppDestination

    fun navigate(route: AppDestination) {
        topLevelRouteState.value = route
    }

    fun push(route: AppDestination) {
        val currentStack = backStacks.getValue(topLevelRoute)
        if (currentStack.lastOrNull() != route) currentStack.add(route)
    }

    fun navigateAndPush(top: AppDestination, route: AppDestination) {
        topLevelRouteState.value = top
        val stack = backStacks.getValue(top)
        if (stack.lastOrNull() != route) stack.add(route)
    }

    fun pop(): Boolean {
        val currentStack = backStacks.getValue(topLevelRoute)
        if (currentStack.size <= 1) return false
        currentStack.removeLastOrNull()
        return true
    }

    fun clearToHome() {
        topLevelRouteState.value = AppDestination.Home
        val stack = backStacks.getValue(AppDestination.Home)
        while (stack.size > 1) {
            stack.removeLastOrNull()
        }
    }

    val isAtRoot: Boolean
        get() = backStacks.getValue(topLevelRoute).size == 1

    fun goBack(): Boolean {
        val currentStack = backStacks.getValue(topLevelRoute)
        if (currentStack.lastOrNull() == AppDestination.Entry) {
            return false
        }
        if (currentStack.size > 1) {
            currentStack.removeLastOrNull()
            return true
        }
        if (topLevelRoute != AppDestination.Home) {
            topLevelRouteState.value = AppDestination.Home
            return true
        }
        return false
    }

    @Composable
    fun toEntries(entryProvider: (NavKey) -> NavEntry<NavKey>): List<NavEntry<NavKey>> {
        val decoratedEntries = backStacks.mapValues { (_, stack) ->
            rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator<NavKey>()),
                entryProvider = entryProvider,
            )
        }
        val routesInUse = if (topLevelRoute == AppDestination.Home) {
            listOf(AppDestination.Home)
        } else {
            listOf(AppDestination.Home, topLevelRoute)
        }
        return routesInUse.flatMap { decoratedEntries.getValue(it) }
    }
}

@Composable
internal fun rememberAskITNavigationState(
    initialRoute: AppDestination = AppDestination.Home,
): AskITNavigationState {
    val topLevelRoute = rememberSerializable(
        serializer = MutableStateSerializer(NavKeySerializer()),
    ) {
        mutableStateOf<NavKey>(AppDestination.Home)
    }
    val backStacks = TOP_LEVEL_ROUTES.associateWith { route ->
        if (route == AppDestination.Home && initialRoute == AppDestination.Entry) {
            rememberNavBackStack(AppDestination.Home, AppDestination.Entry)
        } else {
            rememberNavBackStack(route)
        }
    }
    return remember {
        AskITNavigationState(
            topLevelRouteState = topLevelRoute,
            backStacks = backStacks,
        )
    }
}
