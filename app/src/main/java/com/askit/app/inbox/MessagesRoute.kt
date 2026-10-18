package com.askit.app.inbox

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.jobs.Job
import com.askit.app.jobs.ui.JobsHub
import com.askit.designsystem.navigation.AskITSectionTabs
import com.askit.designsystem.navigation.SectionTab
import com.askit.designsystem.R as DsR

enum class MessagesPane {
    Chats,
    Jobs,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesRoute(
    conversations: List<Conversation>,
    jobs: List<Job>,
    onCompose: () -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenJob: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialPane: MessagesPane = MessagesPane.Chats,
    onAcceptJob: (String) -> Unit = {},
    onDeclineJob: (String) -> Unit = {},
    onCancelJob: (String) -> Unit = {},
    viewAsOtherJobIds: Set<String> = emptySet(),
) {
    var selectedId by rememberSaveable(initialPane) { mutableStateOf(initialPane.name) }
    val panes = MessagesPane.entries
    val selectedPane = panes.firstOrNull { it.name == selectedId } ?: initialPane
    val listPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(stringResource(R.string.messages_title)) },
                actions = {
                    IconButton(
                        onClick = onCompose,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("messages_compose"),
                    ) {
                        Icon(
                            painter = painterResource(DsR.drawable.ic_edit),
                            contentDescription = stringResource(R.string.messages_compose),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
            AskITSectionTabs(
                tabs = panes.map { pane ->
                    SectionTab(
                        pane.name,
                        stringResource(if (pane == MessagesPane.Chats) R.string.messages_tab_chats else R.string.messages_tab_jobs),
                    )
                },
                selectedId = selectedPane.name,
                onSelect = { selectedId = it },
                modifier = Modifier.testTag("messages_segments"),
                tabTagPrefix = "messages_tab_",
            )
            when (selectedPane) {
                MessagesPane.Chats -> ChatsPane(
                    conversations = conversations,
                    onOpenChat = onOpenChat,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = listPadding,
                )

                MessagesPane.Jobs -> JobsHub(
                    jobs = jobs,
                    onOpenJob = onOpenJob,
                    viewAsOtherJobIds = viewAsOtherJobIds,
                    onAccept = onAcceptJob,
                    onDecline = onDeclineJob,
                    onCancel = onCancelJob,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = listPadding,
                )
            }
        }
    }
}
