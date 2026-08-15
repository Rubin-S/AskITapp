package com.askit.app.inbox

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.askit.app.jobs.Job
import com.askit.app.jobs.JobKind
import com.askit.app.jobs.JobParty
import com.askit.app.jobs.JobStatus
import com.askit.app.jobs.JobWorkMode
import com.askit.app.jobs.JobsStore
import com.askit.app.jobs.ui.JobDetail
import com.askit.app.jobs.ui.JobReview
import com.askit.app.jobs.ui.JobVerifyEnter
import com.askit.app.jobs.ui.JobsHub
import com.askit.app.session.SessionProfileStore
import com.askit.designsystem.theme.AskITTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
class MessagesScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun jobsHub_empty_light() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                JobsHub(jobs = emptyList(), viewAsOtherParty = false, onOpenJob = {})
            }
        }
        capture("jobs_hub_empty_light")
    }

    @Test
    fun jobsHub_pendingTwoRoles_light() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                JobsHub(
                    jobs = listOf(
                        pending(JobParty.Receiver, "Repair kitchen tap", "Priya", "Coimbatore"),
                        pending(JobParty.Applicant, "Laptop setup help", "Meena", ""),
                        Job(
                            id = "live-priya",
                            title = "AC service",
                            counterpartName = "Priya Nair",
                            kind = JobKind.TaskApplication,
                            localParty = JobParty.Receiver,
                            status = JobStatus.OnTheWay,
                            workMode = JobWorkMode.OnSite,
                            locationLabel = "Coimbatore",
                            otp = "1234",
                        ),
                    ),
                    viewAsOtherParty = false,
                    onOpenJob = {},
                )
            }
        }
        capture("jobs_hub_pending_two_roles_light")
    }

    @Test
    fun jobsHub_pendingTwoRoles_dark() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = true) {
                JobsHub(
                    jobs = listOf(
                        pending(JobParty.Receiver, "Repair kitchen tap", "Priya", "Coimbatore"),
                        pending(JobParty.Applicant, "Laptop setup help", "Meena", ""),
                        Job(
                            id = "live-priya",
                            title = "AC service",
                            counterpartName = "Priya Nair",
                            kind = JobKind.TaskApplication,
                            localParty = JobParty.Receiver,
                            status = JobStatus.OnTheWay,
                            workMode = JobWorkMode.OnSite,
                            locationLabel = "Coimbatore",
                            otp = "1234",
                        ),
                    ),
                    viewAsOtherParty = false,
                    onOpenJob = {},
                )
            }
        }
        capture("jobs_hub_pending_two_roles_dark")
    }

    @Test
    fun chatThread_composer_light() {
        val conversation = Conversation(
            id = "priya",
            contact = ChatContact("priya", "Priya"),
            preview = "Hello",
            unreadCount = 0,
            updatedAtMillis = 1L,
        )
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                ChatThread(
                    conversation = conversation,
                    messages = listOf(
                        ChatMessage("1", "priya", "Hi, is the tap still leaking?", fromLocalUser = false, sentAtMillis = 1L),
                    ),
                    viewAsOtherParty = false,
                    onBack = {},
                    onSendText = {},
                    onSendPhoto = {},
                    onMuteChanged = {},
                    onViewAsOtherParty = {},
                )
            }
        }
        capture("chat_thread_composer_light")
    }

    @Test
    fun chatsList_unread_light() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                ChatsPane(
                    conversations = listOf(
                        Conversation(
                            id = "priya",
                            contact = ChatContact("priya", "Priya"),
                            preview = "I can come tomorrow morning.",
                            unreadCount = 2,
                            updatedAtMillis = 60_000L,
                        ),
                        Conversation(
                            id = "karthik",
                            contact = ChatContact("karthik", "Karthik"),
                            preview = "Thanks for the details.",
                            unreadCount = 0,
                            updatedAtMillis = 1_000L,
                        ),
                    ),
                    onOpenChat = {},
                    nowMillis = 120_000L,
                )
            }
        }
        capture("chats_list_unread_light")
    }

    @Test
    fun jobOtp_enter_light() {
        val profile = SessionProfileStore()
        val store = JobsStore(profile, seedIncomingLeads = true)
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                JobVerifyEnter(jobId = "lead-incoming-task", store = store, onBack = {}, onVerified = {})
            }
        }
        capture("job_otp_enter_light")
    }

    @Test
    fun jobReview_light() {
        val profile = SessionProfileStore()
        val store = JobsStore(profile, seedIncomingLeads = true)
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                JobReview(jobId = "lead-incoming-task", store = store, onFinished = {})
            }
        }
        capture("job_review_light")
    }

    @Test
    @Config(qualifiers = "ta-rIN-w360dp-h800dp-normal-long-notround-any-xxxhdpi-keyshidden-nonav")
    fun messagesHub_tamil() {
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                MessagesRoute(
                    conversations = emptyList(),
                    jobs = emptyList(),
                    viewAsOtherParty = false,
                    onCompose = {},
                    onOpenChat = {},
                    onOpenJob = {},
                )
            }
        }
        capture("messages_hub_tamil")
    }

    @Test
    fun jobDetail_receiverPending_light() {
        val profile = SessionProfileStore()
        val store = JobsStore(profile, seedIncomingLeads = true)
        composeTestRule.setContent {
            AskITTheme(darkTheme = false) {
                JobDetail(
                    job = store.job("lead-incoming-task")!!,
                    store = store,
                    viewAsOtherParty = false,
                    clock = { 0L },
                    onBack = {},
                    onShareCode = {},
                    onEnterCode = {},
                    onReview = {},
                )
            }
        }
        capture("job_detail_receiver_pending_light")
    }

    @Test
    fun jobDetail_receiverPending_dark() {
        val profile = SessionProfileStore()
        val store = JobsStore(profile, seedIncomingLeads = true)
        composeTestRule.setContent {
            AskITTheme(darkTheme = true) {
                JobDetail(
                    job = store.job("lead-incoming-task")!!,
                    store = store,
                    viewAsOtherParty = false,
                    clock = { 0L },
                    onBack = {},
                    onShareCode = {},
                    onEnterCode = {},
                    onReview = {},
                )
            }
        }
        capture("job_detail_receiver_pending_dark")
    }

    private fun pending(
        party: JobParty,
        title: String,
        name: String,
        location: String,
    ) = Job(
        id = "$party-$title",
        title = title,
        counterpartName = name,
        kind = JobKind.TaskApplication,
        localParty = party,
        status = JobStatus.Applied,
        workMode = if (location.isBlank()) JobWorkMode.Remote else JobWorkMode.OnSite,
        locationLabel = location,
        otp = "1234",
    )

    private fun capture(fileName: String) {
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/$fileName.png",
        )
    }
}
