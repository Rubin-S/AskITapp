package com.askit.app.inbox

import android.text.format.DateUtils
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.askit.designsystem.R
import com.askit.designsystem.chat.ConversationRow
import com.askit.designsystem.empty.AskITEmptyState

@Composable
fun ChatsPane(
    conversations: List<Conversation>,
    onOpenChat: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    nowMillis: Long = System.currentTimeMillis(),
) {
    if (conversations.isEmpty()) {
        AskITEmptyState(
            iconRes = R.drawable.ic_inbox_outlined,
            title = stringResource(R.string.empty_inbox_title),
            supporting = stringResource(R.string.empty_inbox_supporting),
            modifier = modifier.testTag("chats_empty"),
        )
        return
    }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("chats_list"),
        contentPadding = contentPadding,
    ) {
        itemsIndexed(conversations, key = { _, conversation -> conversation.id }) { index, conversation ->
            ConversationRow(
                name = conversation.contact.name,
                preview = conversation.preview,
                unreadCount = conversation.unreadCount,
                timeLabel = relativeTimeLabel(conversation.updatedAtMillis, nowMillis),
                avatarUrl = conversation.contact.avatarUrl,
                onClick = { onOpenChat(conversation.id) },
                showDivider = index < conversations.lastIndex,
                modifier = Modifier.testTag("chat_row_${conversation.id}"),
            )
        }
    }
}

private fun relativeTimeLabel(updatedAtMillis: Long, nowMillis: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        updatedAtMillis,
        nowMillis,
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()
}
