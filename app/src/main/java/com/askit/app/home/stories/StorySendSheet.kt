package com.askit.app.home.stories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.inbox.ChatContact
import com.askit.designsystem.people.AskITAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorySendSheet(storyId: String, contacts: List<ChatContact>, onDismiss: () -> Unit, onSend: (ChatContact) -> Boolean) {
    var sent by rememberSaveable(storyId) { mutableStateOf(emptyList<String>()) }
    var failed by rememberSaveable(storyId) { mutableStateOf(false) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        contentWindowInsets = { WindowInsets.safeDrawing },
    ) {
        Text(
            stringResource(R.string.story_send_heading),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        Text(
            stringResource(R.string.story_send_local),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        if (failed) {
            Text(
                stringResource(R.string.story_send_failed),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
        LazyColumn(Modifier.fillMaxWidth().weight(1f, fill = false)) {
            if (contacts.isEmpty()) {
                item {
                    Text(stringResource(R.string.story_send_empty), Modifier.padding(24.dp))
                }
            }
            items(contacts, key = { it.id }) { contact ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AskITAvatar(avatarUrl = contact.avatarUrl, avatarSize = 44.dp, fallbackIconSize = 24.dp)
                    Text(contact.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Button(onClick = {
                        if (contact.id !in sent) {
                            if (onSend(contact)) {
                                sent = sent + contact.id
                                failed = false
                            } else {
                                failed = true
                            }
                        }
                    }, enabled = contact.id !in sent, modifier = Modifier.testTag("story_send_${contact.id}")) {
                        Text(stringResource(if (contact.id in sent) R.string.story_send_sent else R.string.story_send_action))
                    }
                }
            }
        }
        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End).padding(16.dp)) {
            Text(stringResource(R.string.story_send_done))
        }
    }
}
