package com.askit.app.inbox

data class ChatContact(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val body: String,
    val photoUri: String? = null,
    val fromLocalUser: Boolean,
    val sentAtMillis: Long,
)

data class Conversation(
    val id: String,
    val contact: ChatContact,
    val preview: String,
    val unreadCount: Int,
    val muted: Boolean = false,
    val updatedAtMillis: Long,
)

class InboxStore(
    seed: Boolean = true,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val conversations = linkedMapOf<String, Conversation>()
    private val messages = mutableMapOf<String, MutableList<ChatMessage>>()
    private var nextMessageId = 1

    val contacts: List<ChatContact> = if (seed) seededContacts() else emptyList()

    init {
        if (seed) {
            seededConversations().forEach { conversation ->
                conversations[conversation.id] = conversation
            }
            seededMessages().forEach { message ->
                messages.getOrPut(message.conversationId) { mutableListOf() }.add(message)
            }
        }
    }

    fun conversations(): List<Conversation> =
        conversations.values.sortedByDescending { it.updatedAtMillis }

    fun unreadCount(): Int = conversations.values.sumOf { it.unreadCount }

    fun messages(conversationId: String): List<ChatMessage> =
        messages[conversationId].orEmpty()

    fun conversation(id: String): Conversation? = conversations[id]

    fun markRead(conversationId: String) {
        val current = conversations[conversationId] ?: return
        conversations[conversationId] = current.copy(unreadCount = 0)
    }

    fun setUnread(conversationId: String, count: Int) {
        val current = conversations[conversationId] ?: return
        conversations[conversationId] = current.copy(unreadCount = count.coerceAtLeast(0))
    }

    fun setMuted(conversationId: String, muted: Boolean) {
        val current = conversations[conversationId] ?: return
        conversations[conversationId] = current.copy(muted = muted)
    }

    fun sendText(conversationId: String, body: String) {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) return
        appendMessage(conversationId, body = trimmed, photoUri = null)
    }

    fun sendPhoto(conversationId: String, photoUri: String) {
        appendMessage(conversationId, body = "", photoUri = photoUri)
    }

    fun startConversation(contact: ChatContact): String {
        conversations[contact.id]?.let { return it.id }
        val conversation = Conversation(
            id = contact.id,
            contact = contact,
            preview = "",
            unreadCount = 0,
            updatedAtMillis = clock(),
        )
        conversations[contact.id] = conversation
        messages[contact.id] = mutableListOf()
        return contact.id
    }

    fun searchContacts(query: String): List<ChatContact> {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) return contacts
        return contacts.filter { it.name.lowercase().contains(needle) }
    }

    private fun appendMessage(conversationId: String, body: String, photoUri: String?) {
        val conversation = conversations[conversationId] ?: return
        val message = ChatMessage(
            id = "msg-${nextMessageId++}",
            conversationId = conversationId,
            body = body,
            photoUri = photoUri,
            fromLocalUser = true,
            sentAtMillis = clock(),
        )
        messages.getOrPut(conversationId) { mutableListOf() }.add(message)
        val preview = photoUri?.let { "Photo" } ?: body
        conversations[conversationId] = conversation.copy(
            preview = preview,
            unreadCount = 0,
            updatedAtMillis = message.sentAtMillis,
        )
    }
}

internal fun seededContacts(): List<ChatContact> = listOf(
    ChatContact(id = "priya", name = "Priya"),
    ChatContact(id = "karthik", name = "Karthik"),
    ChatContact(id = "meena", name = "Meena"),
    ChatContact(id = "arun", name = "Arun Kumar"),
)

internal fun seededConversations(): List<Conversation> = listOf(
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

internal fun seededMessages(): List<ChatMessage> = listOf(
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
