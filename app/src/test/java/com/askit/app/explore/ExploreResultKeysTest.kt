package com.askit.app.explore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ExploreResultKeysTest {
    @Test fun duplicatePeopleAreMergedKeepingEveryServiceAndReason() {
        val merged = distinctPeopleById(
            listOf(
                person("1", additionalServices = listOf("Wiring"), matchReasons = setOf(PersonMatchReason.Identity)),
                person(" 1 ", additionalServices = listOf("Lights"), matchReasons = setOf(PersonMatchReason.Service)),
                person("2"),
            ),
        )
        assertEquals(2, merged.size)
        assertEquals(listOf("Wiring", "Lights"), merged[0].additionalServices)
        assertEquals(setOf(PersonMatchReason.Identity, PersonMatchReason.Service), merged[0].matchReasons)
    }

    @Test fun peopleWithoutAnIdAreNeverMerged() {
        assertEquals(2, distinctPeopleById(listOf(person(""), person(""))).size)
    }

    @Test fun anonymousRowsGetUniqueKeysAndIdentifiedRowsUseTheirId() {
        val rows = buildExplorePersonRenderRows(listOf(person(""), person(""), person(" 7 ")))
        assertNotEquals(rows[0].uiKey, rows[1].uiKey)
        assertEquals("7", rows[2].uiKey)
        assertEquals(3, rows.map { it.uiKey }.toSet().size)
    }

    @Test fun taskRowsAreDedupedById() {
        val tasks = distinctTasksById(listOf(task("a"), task("a "), task(""), task("")))
        assertEquals(3, tasks.size)
        val rows = buildExploreTaskRenderRows(tasks)
        assertEquals(3, rows.map { it.uiKey }.toSet().size)
    }

    @Test fun requestKeyChangesWithTheSearchRadius() {
        assertNotEquals(exploreRequestKey("plumber", area(10)), exploreRequestKey("plumber", area(25)))
        assertEquals(exploreRequestKey("plumber", area()), exploreRequestKey("plumber", area()))
    }
}
