package com.askit.app.inbox

import com.askit.app.R
import com.askit.app.session.MessagingAudience

/** The server must make the same decision independently before accepting real messages. */
enum class MessageAccess(val messageRes: Int? = null) {
    Allowed,
    ContactsOnly(R.string.messages_contacts_only),
    VerifiedOnly(R.string.messages_verified_only),
    Blocked(R.string.messages_privacy_blocked),
    Unavailable(R.string.messages_unavailable),
}

fun messagingAccess(
    senderId: String,
    recipientId: String,
    recipientPreference: String?,
    isRecipientContact: Boolean,
    senderVerified: Boolean,
): MessageAccess {
    if (senderId.isBlank() || recipientId.isBlank() || senderId == recipientId) return MessageAccess.Unavailable
    return when (MessagingAudience.fromStoredValue(recipientPreference)) {
        MessagingAudience.Everyone -> MessageAccess.Allowed
        MessagingAudience.ContactsOnly -> if (isRecipientContact) MessageAccess.Allowed else MessageAccess.ContactsOnly
        MessagingAudience.VerifiedOnly -> if (senderVerified) MessageAccess.Allowed else MessageAccess.VerifiedOnly
        null -> MessageAccess.Unavailable
    }
}
