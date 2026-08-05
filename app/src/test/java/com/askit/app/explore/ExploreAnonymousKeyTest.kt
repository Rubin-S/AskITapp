package com.askit.app.explore

import com.askit.designsystem.tasks.TaskResultStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ExploreAnonymousKeyTest {

    @Test
    fun personKeysFollowRenderedContentWhenAnEarlierRowMoves() {
        val first = person(name = "First professional")
        val second = person(name = "Second professional")
        val unrelated = person(name = "Unrelated professional")

        val before = buildExplorePersonRenderRows(listOf(first, second))
        val after = buildExplorePersonRenderRows(listOf(unrelated, first, second))

        assertEquals(before[0].uiKey, after[1].uiKey)
        assertEquals(before[1].uiKey, after[2].uiKey)
        assertEquals(2, before.map(ExplorePersonRenderRow::uiKey).toSet().size)
    }

    @Test
    fun identicalAnonymousPeopleGetDistinctOccurrenceKeys() {
        val duplicate = person(name = "Same professional")

        val rows = buildExplorePersonRenderRows(listOf(duplicate, duplicate))

        assertNotEquals(rows[0].uiKey, rows[1].uiKey)
    }

    @Test
    fun taskKeysFollowRenderedContentWhenAnEarlierRowMoves() {
        val first = task(title = "First task")
        val second = task(title = "Second task")
        val unrelated = task(title = "Unrelated task")

        val before = buildExploreTaskRenderRows(listOf(first, second))
        val after = buildExploreTaskRenderRows(listOf(unrelated, first, second))

        assertEquals(before[0].uiKey, after[1].uiKey)
        assertEquals(before[1].uiKey, after[2].uiKey)
    }

    @Test
    fun realIdsRemainTheCanonicalInteractionKeys() {
        val personRow = buildExplorePersonRenderRows(
            listOf(person(id = "  person-7  ", name = "Named professional")),
        ).single()
        val taskRow = buildExploreTaskRenderRows(
            listOf(task(id = "  task-9  ", title = "Named task")),
        ).single()

        assertEquals("person-7", personRow.stableId)
        assertEquals("person-7", personRow.uiKey)
        assertEquals("task-9", taskRow.stableId)
        assertEquals("task-9", taskRow.uiKey)
    }

    private fun person(
        id: String = "",
        name: String,
    ) = ExplorePersonResult(
        id = id,
        name = name,
        avatarUrl = null,
        primaryService = "Electrician",
        additionalServices = listOf("Wiring"),
        rating = 4.5,
        reviewCount = 8,
        locationLabel = "Kallakurichi",
        priceLabel = "Quote required",
        statusLabel = "Available today",
        matchReasons = setOf(PersonMatchReason.Service),
    )

    private fun task(
        id: String = "",
        title: String,
    ) = ExploreTaskResult(
        id = id,
        title = title,
        category = "Electrical work",
        summary = "Repair work near Kallakurichi",
        budgetLabel = "Quote required",
        locationLabel = "Kallakurichi",
        timingLabel = "Needed Monday",
        posterName = "Poster",
        postedLabel = "Posted today",
        status = TaskResultStatus.Open,
    )
}
