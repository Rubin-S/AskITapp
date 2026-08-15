package com.askit.app.inbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.designsystem.R as DsR
import com.askit.designsystem.people.AskITAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMessageScreen(
    contacts: List<ChatContact>,
    onBack: () -> Unit,
    onSelectContact: (ChatContact) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val matches = contacts.filter {
        query.isBlank() || it.name.contains(query, ignoreCase = true)
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.messages_compose)) },
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
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_message_to"),
                label = { Text(stringResource(R.string.messages_to_hint)) },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(matches, key = { it.id }) { contact ->
                    ListItem(
                        headlineContent = {
                            Text(contact.name, style = MaterialTheme.typography.titleMedium)
                        },
                        leadingContent = {
                            AskITAvatar(
                                avatarUrl = contact.avatarUrl,
                                avatarSize = 40.dp,
                                fallbackIconSize = 24.dp,
                            )
                        },
                        modifier = Modifier
                            .testTag("new_message_contact_${contact.id}")
                            .clickable(role = Role.Button) { onSelectContact(contact) },
                    )
                }
            }
        }
    }
}
