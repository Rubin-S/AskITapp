package com.askit.app.task

import com.askit.app.home.data.FakeHomeRepository
import com.askit.app.home.model.FeedItem
import com.askit.app.posttask.PostTaskBudget
import com.askit.app.posttask.PostTaskDraft
import com.askit.app.posttask.PostTaskLocation
import com.askit.app.posttask.PostTaskTimingMode
import com.askit.app.posttask.PostTaskWorkMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskUnificationTest {

    @Test
    fun defaultSeededTasks_areAvailableInRepository() = runTest {
        val repository = InMemoryTaskRepository()
        val initialTasks = repository.tasks.value

        assertTrue(initialTasks.isNotEmpty())
        assertEquals(3, initialTasks.size)

        val task1 = repository.getTask("task-1")
        assertNotNull(task1)
        assertEquals("Fix kitchen sink leak and replace faucet", task1?.title)

        val task2 = repository.getTask("task-2")
        assertNotNull(task2)
        assertEquals("Assemble 3-piece modular shelving unit", task2?.title)

        val task3 = repository.getTask("task-3")
        assertNotNull(task3)
        assertEquals("Install EV Level 2 Home Charger in Garage", task3?.title)

        val nonExistentTask = repository.getTask("non-existent-task-id")
        assertNull(nonExistentTask)
    }

    @Test
    fun createTask_addsToRepository_andUpdatesFlow() = runTest {
        val repository = InMemoryTaskRepository()

        val draft = PostTaskDraft(
            categoryId = "plumbing",
            title = "Emergency Pipe Leak Repair",
            details = "Main supply line has a crack near shutoff valve.",
            workMode = PostTaskWorkMode.AT_MY_LOCATION,
            location = PostTaskLocation(
                publicAreaLabel = "Downtown Seattle",
                placeId = "place-1",
                latitude = 47.6062,
                longitude = -122.3321,
                privateAddressOrLandmark = "123 Main St",
            ),
            timingMode = PostTaskTimingMode.ASAP,
            selectedDateMillis = null,
            budget = PostTaskBudget.Fixed("180"),
            photos = listOf("https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=800"),
            itemDetails = null,
            accessInstructions = null,
        )

        val createdTask = repository.createTask(draft)

        assertEquals("Emergency Pipe Leak Repair", createdTask.title)
        assertEquals("Plumbing", createdTask.category)
        assertEquals("$180", createdTask.budget)
        assertEquals("Downtown Seattle", createdTask.location)
        assertEquals("Urgent · ASAP", createdTask.urgency)
        assertEquals("Main supply line has a crack near shutoff valve.", createdTask.description)

        val currentTasks = repository.tasks.value
        assertEquals(createdTask, currentTasks.first())
        assertEquals(4, currentTasks.size)

        val retrievedCreatedTask = repository.getTask(createdTask.id)
        assertEquals(createdTask, retrievedCreatedTask)
    }

    @Test
    fun taskMappers_mapToHomeAndExploreModelsCorrectly() {
        val task = Task(
            id = "task-unified-1",
            title = "Replace Bathroom Vanity & Mirror",
            category = "Carpentry",
            budget = "$250",
            location = "Capitol Hill",
            posterName = "Sarah Jenkins",
            posterAvatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=300",
            urgency = "This week",
            postedTime = "2h ago",
            description = "Remove existing 36-inch vanity and install new double-sink vanity with plumbing hookups.",
        )

        val homePreview = task.toTaskPreview()
        assertEquals(task.id, homePreview.id)
        assertEquals(task.title, homePreview.title)
        assertEquals(task.category, homePreview.category)
        assertEquals(task.budget, homePreview.budgetLabel)
        assertEquals(task.location, homePreview.locationLabel)
        assertEquals(task.posterName, homePreview.posterName)
        assertEquals(task.urgency, homePreview.urgencyLabel)

        val exploreResult = task.toExploreTaskResult()
        assertEquals(task.id, exploreResult.id)
        assertEquals(task.title, exploreResult.title)
        assertEquals(task.category, exploreResult.category)
        assertEquals(task.budget, exploreResult.budgetLabel)
        assertEquals(task.location, exploreResult.locationLabel)
        assertEquals(task.posterName, exploreResult.posterName)
    }

    @Test
    fun fakeHomeRepository_dynamicallyObservesTaskRepository() = runTest {
        val taskRepository = InMemoryTaskRepository()
        val homeRepository = FakeHomeRepository(taskRepository = taskRepository)

        val page1Initial = homeRepository.getFeed(1).first()
        val initialTaskSection = page1Initial.filterIsInstance<FeedItem.TaskSection>().firstOrNull()
        assertNotNull(initialTaskSection)
        assertEquals(3, initialTaskSection?.tasks?.size)

        val draft = PostTaskDraft(
            categoryId = "electrical",
            title = "Install EV Level 2 Charger",
            details = "Need dedicated 240V 50A breaker installed in garage.",
            workMode = PostTaskWorkMode.AT_MY_LOCATION,
            location = PostTaskLocation(
                publicAreaLabel = "Bellevue",
                placeId = "place-2",
                latitude = 47.6101,
                longitude = -122.2015,
                privateAddressOrLandmark = "456 Oak St",
            ),
            timingMode = PostTaskTimingMode.FLEXIBLE,
            selectedDateMillis = null,
            budget = PostTaskBudget.Fixed("350"),
            photos = emptyList(),
            itemDetails = null,
            accessInstructions = null,
        )

        taskRepository.createTask(draft)

        val page1Updated = homeRepository.getFeed(1).first()
        val updatedTaskSection = page1Updated.filterIsInstance<FeedItem.TaskSection>().firstOrNull()
        assertNotNull(updatedTaskSection)
        assertEquals(4, updatedTaskSection?.tasks?.size)
        assertEquals("Install EV Level 2 Charger", updatedTaskSection?.tasks?.first()?.title)
    }
}
