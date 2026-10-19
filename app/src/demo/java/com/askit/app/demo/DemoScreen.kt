package com.askit.app.demo

import com.askit.app.navigation.AppDestination

/** One row of the demo screen menu. */
internal data class DemoScreen(val label: String, val destination: AppDestination, val needsSignIn: Boolean = true)

private const val DEMO_PHONE = "+919876543210"

/**
 * Every screen of the app, with seeded ids from the fixtures so each one opens with content.
 * Screens that only make sense for an owner's own item (edit task/post) are reached from the item.
 */
internal fun demoScreens(): List<DemoScreen> = listOf(
    DemoScreen("Welcome", AppDestination.Entry, needsSignIn = false),
    DemoScreen("Sign in: phone number", AppDestination.AuthPhone, needsSignIn = false),
    DemoScreen("Sign in: OTP screen (layout; verify via phone flow)", AppDestination.AuthOtp(DEMO_PHONE), needsSignIn = false),
    DemoScreen("Sign up: profile form", AppDestination.FormA(DEMO_PHONE), needsSignIn = false),
    DemoScreen("Home feed", AppDestination.Home),
    DemoScreen("Explore (people, services, tasks)", AppDestination.Explore),
    DemoScreen("Explore: choose location", AppDestination.ExploreLocationDestination),
    DemoScreen("Explore: search area", AppDestination.SearchAreaDestination),
    DemoScreen("Inbox", AppDestination.Inbox),
    DemoScreen("Inbox: new message", AppDestination.NewMessage),
    DemoScreen("Chat thread (Priya)", AppDestination.ChatThread("priya")),
    DemoScreen("My profile", AppDestination.Profile),
    DemoScreen("Edit profile", AppDestination.EditProfile),
    DemoScreen("Settings", AppDestination.ProfileSettings),
    DemoScreen("Public profile (Rahul)", AppDestination.UserProfile("member-1")),
    DemoScreen("Post a task", AppDestination.PostTask),
    DemoScreen("Task detail", AppDestination.TaskDetail("task-1")),
    DemoScreen("List a service", AppDestination.ListService),
    DemoScreen("Service detail", AppDestination.ServiceDetail("srv-1")),
    DemoScreen("Create post", AppDestination.CreatePost),
    DemoScreen("Post detail", AppDestination.PostDetail("post-1")),
    DemoScreen("Create story", AppDestination.Story),
    DemoScreen("Story viewer", AppDestination.StoryViewer("story-1")),
    DemoScreen("Creator dashboard", AppDestination.CreatorDashboard),
    DemoScreen("Provider dashboard", AppDestination.ProviderDashboard),
    DemoScreen("Job detail", AppDestination.JobDetail("preview-verify")),
    DemoScreen("Job: enter start code (1234)", AppDestination.JobVerifyEnter("preview-verify")),
    DemoScreen("Job: share start code", AppDestination.JobVerifyShare("preview-review")),
    DemoScreen("Job: review", AppDestination.JobReview("preview-review")),
)
