package com.askit.app.explore

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

internal data class ExploreListPosition(val index: Int, val offset: Int)

internal data class ExploreListPositionState(val index: MutableIntState, val offset: MutableIntState)

/**
 * Keeps one LazyColumn while remembering the last viewport for each logical result scope.
 * Only primitive viewport values and request signatures are saveable; result rows remain
 * caller-owned and are never placed in saved instance state.
 */
internal class ExploreScrollState(
    private val positions: Map<ExploreResultScope, ExploreListPositionState>,
    private val requestKey: MutableState<String?>,
    private val filterKeys: Map<ExploreResultScope, MutableState<String?>>,
    private val sortKeys: Map<ExploreResultScope, MutableState<String?>>,
) {
    fun position(scope: ExploreResultScope): ExploreListPosition {
        val state = positions.getValue(scope)
        return ExploreListPosition(
            index = state.index.intValue,
            offset = state.offset.intValue,
        )
    }

    fun record(scope: ExploreResultScope, index: Int, offset: Int) {
        val state = positions.getValue(scope)
        state.index.intValue = index
        state.offset.intValue = offset
    }

    fun reset(scope: ExploreResultScope) {
        val state = positions.getValue(scope)
        state.index.intValue = 0
        state.offset.intValue = 0
    }

    fun resetAll() {
        ExploreResultScope.entries.forEach(::reset)
    }

    fun synchronize(
        currentRequestKey: String,
        currentFilterKeys: Map<ExploreResultScope, String>,
        currentSortKeys: Map<ExploreResultScope, String>,
    ) {
        val requestChanged = requestKey.value != currentRequestKey
        if (requestChanged) {
            resetAll()
        } else {
            ExploreResultScope.entries.forEach { scope ->
                if (filterKeys.getValue(scope).value != currentFilterKeys.getValue(scope)) {
                    reset(scope)
                }
                if (sortKeys.getValue(scope).value != currentSortKeys.getValue(scope)) {
                    reset(scope)
                }
            }
        }

        requestKey.value = currentRequestKey
        ExploreResultScope.entries.forEach { scope ->
            filterKeys.getValue(scope).value = currentFilterKeys.getValue(scope)
            sortKeys.getValue(scope).value = currentSortKeys.getValue(scope)
        }
    }
}

@Composable
internal fun rememberExploreScrollState(): ExploreScrollState {
    val allIndex = rememberSaveable { mutableIntStateOf(0) }
    val allOffset = rememberSaveable { mutableIntStateOf(0) }
    val peopleIndex = rememberSaveable { mutableIntStateOf(0) }
    val peopleOffset = rememberSaveable { mutableIntStateOf(0) }
    val servicesIndex = rememberSaveable { mutableIntStateOf(0) }
    val servicesOffset = rememberSaveable { mutableIntStateOf(0) }
    val tasksIndex = rememberSaveable { mutableIntStateOf(0) }
    val tasksOffset = rememberSaveable { mutableIntStateOf(0) }

    val requestKey = rememberSaveable { mutableStateOf<String?>(null) }
    val allFilterKey = rememberSaveable { mutableStateOf<String?>(null) }
    val peopleFilterKey = rememberSaveable { mutableStateOf<String?>(null) }
    val servicesFilterKey = rememberSaveable { mutableStateOf<String?>(null) }
    val tasksFilterKey = rememberSaveable { mutableStateOf<String?>(null) }
    val allSortKey = rememberSaveable { mutableStateOf<String?>(null) }
    val peopleSortKey = rememberSaveable { mutableStateOf<String?>(null) }
    val servicesSortKey = rememberSaveable { mutableStateOf<String?>(null) }
    val tasksSortKey = rememberSaveable { mutableStateOf<String?>(null) }

    return remember {
        ExploreScrollState(
            positions = mapOf(
                ExploreResultScope.All to ExploreListPositionState(allIndex, allOffset),
                ExploreResultScope.People to ExploreListPositionState(peopleIndex, peopleOffset),
                ExploreResultScope.Services to ExploreListPositionState(
                    servicesIndex,
                    servicesOffset,
                ),
                ExploreResultScope.Tasks to ExploreListPositionState(tasksIndex, tasksOffset),
            ),
            requestKey = requestKey,
            filterKeys = mapOf(
                ExploreResultScope.All to allFilterKey,
                ExploreResultScope.People to peopleFilterKey,
                ExploreResultScope.Services to servicesFilterKey,
                ExploreResultScope.Tasks to tasksFilterKey,
            ),
            sortKeys = mapOf(
                ExploreResultScope.All to allSortKey,
                ExploreResultScope.People to peopleSortKey,
                ExploreResultScope.Services to servicesSortKey,
                ExploreResultScope.Tasks to tasksSortKey,
            ),
        )
    }
}
