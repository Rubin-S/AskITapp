package com.askit.app.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.designsystem.R as DsR
import com.askit.designsystem.chat.MessageBubble
import com.askit.designsystem.people.AskITAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatThread(
    conversation: Conversation,
    messages: List<ChatMessage>,
    onBack: () -> Unit,
    onSendText: (String) -> Unit,
    onSendPhoto: (String) -> Unit,
    onMuteChanged: (Boolean) -> Unit,
    onBlock: () -> Unit = {},
    onReport: () -> Unit = {},
    viewAsOtherParty: Boolean = false,
    onViewAsOtherParty: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var draft by rememberSaveable { mutableStateOf("") }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var viewAsOther by rememberSaveable { mutableStateOf(viewAsOtherParty) }
    val snackbarHostState = remember { SnackbarHostState() }
    val reportedMessage = stringResource(R.string.messages_reported)
    LaunchedEffect(conversation.reported) {
        if (conversation.reported) snackbarHostState.showSnackbar(reportedMessage)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AskITAvatar(
                            avatarUrl = conversation.contact.avatarUrl,
                            avatarSize = 32.dp,
                            fallbackIconSize = 20.dp,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                        )
                        Column {
                            Text(
                                text = conversation.contact.name,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(R.string.messages_online),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            painter = painterResource(DsR.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.messages_back),
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chat_overflow"),
                    ) {
                        Icon(
                            painter = painterResource(DsR.drawable.ic_more),
                            contentDescription = stringResource(R.string.messages_more),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.messages_profile)) },
                            onClick = { menuExpanded = false },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.messages_mute)) },
                            trailingIcon = {
                                Switch(
                                    checked = conversation.muted,
                                    onCheckedChange = onMuteChanged,
                                )
                            },
                            onClick = { onMuteChanged(!conversation.muted) },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.messages_block)) },
                            onClick = {
                                menuExpanded = false
                                onBlock()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.messages_report)) },
                            onClick = {
                                menuExpanded = false
                                onReport()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.messages_view_as_other)) },
                            onClick = {
                                menuExpanded = false
                                viewAsOther = !viewAsOther
                                onViewAsOtherParty()
                            },
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (conversation.blocked) {
                Text(
                    text = stringResource(R.string.messages_blocked),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("chat_blocked"),
                )
            } else {
                ChatComposer(
                    value = draft,
                    onValueChange = { draft = it },
                    onSend = {
                        onSendText(draft)
                        draft = ""
                    },
                    onPhotoPicked = onSendPhoto,
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .testTag("chat_messages"),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val ordered = messages.asReversed()
            itemsIndexed(ordered, key = { _, item -> item.id }) { index, message ->
                val previous = ordered.getOrNull(index + 1)
                val sameAuthor = previous?.fromLocalUser == message.fromLocalUser
                val fromLocal = if (viewAsOther) !message.fromLocalUser else message.fromLocalUser
                MessageBubble(
                    body = message.body,
                    photoModel = message.photoUri,
                    fromLocalUser = fromLocal,
                    showAvatar = !fromLocal && !sameAuthor,
                    avatarUrl = conversation.contact.avatarUrl,
                    modifier = Modifier.padding(bottom = if (sameAuthor) 4.dp else 0.dp),
                )
            }
        }
    }
}
