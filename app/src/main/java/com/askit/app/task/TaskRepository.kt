package com.askit.app.task

import com.askit.app.posttask.PostTaskBudget
import com.askit.app.posttask.PostTaskDraft
import com.askit.app.posttask.PostTaskTimingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface TaskRepository {
    val tasks: StateFlow<List<Task>>
    fun getTask(id: String): Task?
    fun createTask(draft: PostTaskDraft): Task
}

class InMemoryTaskRepository(
    initialTasks: List<Task> = defaultSeededTasks(),
) : TaskRepository {

    private val _tasks = MutableStateFlow(initialTasks)
    override val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private var nextId = 100

    override fun getTask(id: String): Task? {
        return _tasks.value.firstOrNull { it.id == id }
    }

    override fun createTask(draft: PostTaskDraft): Task {
        val taskId = "task-${nextId++}"
        val budgetString = when (val b = draft.budget) {
            is PostTaskBudget.Fixed -> "$${b.amount}"
            is PostTaskBudget.Range -> "$${b.minimum} - $${b.maximum}"
            PostTaskBudget.RequestQuotes, null -> "Request Quotes"
        }
        val urgencyString = when (draft.timingMode) {
            PostTaskTimingMode.ASAP -> "Urgent · ASAP"
            PostTaskTimingMode.DATE -> "Scheduled"
            PostTaskTimingMode.FLEXIBLE -> "Flexible timing"
        }
        val locationString = draft.location?.publicAreaLabel?.ifBlank { "Local Area" } ?: "Local Area"
        val categoryString = draft.customCategory?.takeIf { it.isNotBlank() }
            ?: draft.categoryId.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        val newTask = Task(
            id = taskId,
            title = draft.title,
            category = categoryString,
            categoryId = draft.categoryId,
            budget = budgetString,
            location = locationString,
            posterName = "Meera Raman",
            urgency = urgencyString,
            postedTime = "Just now",
            description = draft.details,
            requirements = buildList {
                draft.itemDetails?.brandOrModel?.takeIf { it.isNotBlank() }?.let { add("Brand / Model: $it") }
                draft.itemDetails?.quantityOrMeasurements?.takeIf { it.isNotBlank() }?.let { add("Specifications: $it") }
                draft.accessInstructions?.takeIf { it.isNotBlank() }?.let { add("Access: $it") }
            },
            photos = draft.photos,
            status = "Open",
            createdAtMillis = System.currentTimeMillis(),
        )

        _tasks.update { current -> listOf(newTask) + current }
        return newTask
    }
}

fun defaultSeededTasks(): List<Task> = listOf(
    Task(
        id = "task-1",
        title = "Fix kitchen sink leak and replace faucet",
        category = "Plumbing",
        categoryId = "plumbing",
        budget = "$120",
        location = "Downtown · 2.4 km away",
        posterName = "Marcus Reed",
        urgency = "Urgent · Today",
        postedTime = "Posted 2 hours ago",
        description = "Looking for an experienced plumber to inspect and fix a steady leak originating from the cold water supply pipe under the kitchen sink. Also need a new pull-down stainless steel faucet installed (unit already purchased on site).",
        requirements = listOf(
            "Bring standard plumbing hand tools and Teflon seal tape",
            "Inspect supply shutoff valves for corrosion",
            "Test hot & cold water pressure after faucet installation",
            "Clean up work area upon completion",
        ),
    ),
    Task(
        id = "task-2",
        title = "Assemble 3-piece modular shelving unit",
        category = "Carpentry",
        categoryId = "carpentry",
        budget = "$80",
        location = "Westside · 4.1 km away",
        posterName = "Chloe Bennett",
        urgency = "Flexible timing",
        postedTime = "Posted 4 hours ago",
        description = "Need assistance assembling three modular Scandinavian birch shelving units. Flat-pack boxes are placed in the living room on the ground floor with all hardware and manuals included.",
        requirements = listOf(
            "Power drill / screwdriver set recommended",
            "Secure top anti-tip wall brackets for earthquake safety",
            "Hardware and assembly manual provided",
        ),
    ),
    Task(
        id = "task-3",
        title = "Install EV Level 2 Home Charger in Garage",
        category = "Electrical",
        categoryId = "electrical",
        budget = "$250",
        location = "North District · 5.8 km away",
        posterName = "David Miller",
        urgency = "This Week",
        postedTime = "Posted yesterday",
        description = "Looking for a licensed electrician to install a 240V 50A NEMA 14-50 dedicated circuit and mount a Wallbox Pulsar Plus EV charger in attached garage. Main breaker panel is located 12 feet away.",
        requirements = listOf(
            "Licensed & insured electrical certification",
            "Supply 6 AWG copper wire and 50A double-pole breaker",
            "Test voltage and verify ground continuity",
        ),
    ),
)
