package com.askit.app.demo

import com.askit.app.inbox.ChatContact
import com.askit.app.inbox.ChatMessage
import com.askit.app.inbox.Conversation
import com.askit.app.inbox.InboxSeed

/** Sample contacts and chats for debug/preview builds. Not compiled into release. */
internal fun sampleInboxSeed(): InboxSeed = InboxSeed(seededContacts(), seededConversations(), seededMessages())

private fun seededContacts(): List<ChatContact> = listOf(
    ChatContact(id = "priya", name = "Priya"),
    ChatContact(id = "karthik", name = "Karthik"),
    ChatContact(id = "meena", name = "Meena"),
    ChatContact(id = "arun", name = "Arun Kumar"),
)

private fun seededConversations(): List<Conversation> = listOf(
    Conversation(
        id = "priya",
        contact = ChatContact(id = "priya", name = "Priya"),
        preview = "I can come tomorrow morning.",
        unreadCount = 0,
        updatedAtMillis = 2_000L,
    ),
    Conversation(
        id = "karthik",
        contact = ChatContact(id = "karthik", name = "Karthik"),
        preview = "Thanks for the details.",
        unreadCount = 0,
        updatedAtMillis = 1_000L,
    ),
)

private fun seededMessages(): List<ChatMessage> = listOf(
    ChatMessage(
        id = "m1",
        conversationId = "priya",
        body = "Hi, is the tap still leaking?",
        fromLocalUser = false,
        sentAtMillis = 1_000L,
    ),
    ChatMessage(
        id = "m2",
        conversationId = "priya",
        body = "I can come tomorrow morning.",
        fromLocalUser = false,
        sentAtMillis = 2_000L,
    ),
    ChatMessage(
        id = "m3",
        conversationId = "karthik",
        body = "Thanks for the details.",
        fromLocalUser = false,
        sentAtMillis = 1_000L,
    ),
)
