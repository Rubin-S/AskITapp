package com.askit.app.demo

import android.app.ActivityManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.askit.app.BuildConfig
import com.askit.app.R
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

/** Small "DEMO" chip over every screen of the demo build; tapping it opens the screen menu. */
@Composable
internal fun DemoBadge(signedIn: Boolean, onOpen: (AppDestination) -> Unit, onExploreDemo: () -> Unit) {
    if (!BuildConfig.DEMO_BUILD) return
    var open by rememberSaveable { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().wrapContentSize(Alignment.TopCenter)) {
        Surface(
            modifier = Modifier.padding(top = 2.dp)
                .clickable(role = Role.Button, onClickLabel = stringResource(R.string.demo_menu_title)) { open = true }
                .testTag("demo_badge"),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f),
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ) {
            Text(
                stringResource(R.string.demo_badge),
                Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
    if (open) {
        DemoMenuDialog(
            signedIn = signedIn,
            onOpen = {
                open = false
                onOpen(it)
            },
            onExploreDemo = {
                open = false
                onExploreDemo()
            },
            onDismiss = { open = false },
        )
    }
}

/** Settings rows of the demo build: open the screen menu, or wipe everything back to a fresh install. */
@Composable
internal fun DemoSettings(onOpen: (AppDestination) -> Unit) {
    if (!BuildConfig.DEMO_BUILD) return
    var menu by rememberSaveable { mutableStateOf(false) }
    var reset by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth().testTag("demo_settings")) {
        Text(stringResource(R.string.demo_settings_title), style = MaterialTheme.typography.titleSmall)
        Text(stringResource(R.string.demo_settings_body), style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = { menu = true }, Modifier.testTag("demo_open_menu")) { Text(stringResource(R.string.demo_menu_title)) }
        TextButton(onClick = { reset = true }, Modifier.testTag("demo_reset")) { Text(stringResource(R.string.demo_reset)) }
    }
    if (menu) {
        DemoMenuDialog(
            signedIn = true,
            onOpen = {
                menu = false
                onOpen(it)
            },
            onExploreDemo = { menu = false },
            onDismiss = { menu = false },
        )
    }
    if (reset) {
        AlertDialog(
            onDismissRequest = { reset = false },
            title = { Text(stringResource(R.string.demo_reset)) },
            text = { Text(stringResource(R.string.demo_reset_body)) },
            confirmButton = {
                TextButton(onClick = { resetDemoData(context) }, Modifier.testTag("demo_reset_confirm")) {
                    Text(stringResource(R.string.demo_reset_confirm))
                }
            },
            dismissButton = { TextButton(onClick = { reset = false }) { Text(stringResource(R.string.demo_cancel)) } },
        )
    }
}

@Composable
private fun DemoMenuDialog(signedIn: Boolean, onOpen: (AppDestination) -> Unit, onExploreDemo: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("demo_menu"),
        title = { Text(stringResource(R.string.demo_menu_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (!signedIn) {
                    Text(stringResource(R.string.demo_menu_signed_out), style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = onExploreDemo, Modifier.testTag("demo_menu_explore")) {
                        Text(stringResource(R.string.entry_explore_demo))
                    }
                }
                demoScreens().filter { signedIn || !it.needsSignIn }.forEach { screen ->
                    TextButton(
                        onClick = { onOpen(screen.destination) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
                    ) { Text(screen.label, Modifier.fillMaxWidth()) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.demo_close)) } },
    )
}

/** Clears every file, preference and in-memory edit of this demo app, like a fresh install. Closes the app. */
private fun resetDemoData(context: Context) {
    context.getSystemService(ActivityManager::class.java).clearApplicationUserData()
}
