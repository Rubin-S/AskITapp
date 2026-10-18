package com.askit.app.demo

import com.askit.app.task.Task

/** Sample tasks for debug/preview builds only. Not compiled into release. */
internal fun sampleTasks(): List<Task> = listOf(
    Task(
        id = "task-1",
        title = "Fix kitchen sink leak and replace faucet",
        category = "Plumbing",
        categoryId = "plumbing",
        listedBudget = com.askit.app.posttask.PostTaskBudget.Fixed("120"),
        budget = "₹120",
        location = "Downtown · 2.4 km away",
        posterName = "Marcus Reed",
        posterId = "author-marcus",
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
        listedBudget = com.askit.app.posttask.PostTaskBudget.Fixed("80"),
        budget = "₹80",
        location = "Westside · 4.1 km away",
        posterName = "Chloe Bennett",
        posterId = "author-chloe",
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
        listedBudget = com.askit.app.posttask.PostTaskBudget.Fixed("250"),
        budget = "₹250",
        location = "North District · 5.8 km away",
        posterName = "David Miller",
        posterId = "author-david",
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
