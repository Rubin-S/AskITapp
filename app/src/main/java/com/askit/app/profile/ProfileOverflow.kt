package com.askit.app.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.askit.app.R
import com.askit.app.auth.validAccountEmail
import com.askit.app.auth.validAccountPhone
import com.askit.app.session.ServiceListing
import com.askit.designsystem.profile.ProfileMenuItem
import com.askit.designsystem.profile.ProfileTopBar

/**
 * Owners get a Settings gear plus Copy/Share of their own link. Visitors get About, Copy, Share and,
 * when [onModerate] is given (signed in, social supported, not blocked yet), Block and Report.
 * A blocked profile offers Unblock ([onUnblock]) in place of Block.
 */
@Composable
internal fun ProfileOverflowTopBar(
    profile: ProfileUiState,
    viewerId: String?,
    publicUrl: String?,
    onBack: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null,
    onModerate: ((Boolean) -> Boolean)? = null,
    onUnblock: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var about by rememberSaveable(profile.userId) { mutableStateOf(false) }
    var moderation by rememberSaveable(profile.userId) { mutableStateOf<Boolean?>(null) }
    var failed by rememberSaveable(profile.userId, moderation) { mutableStateOf(false) }
    val owner = profile.isOwnerView
    val canModerate = onModerate != null && canMutateProfileVisitor(viewerId, profile.userId, profile.isPublicPreview)
    val share = stringResource(R.string.profile_share)
    val copyLabel = stringResource(R.string.profile_copy_url)
    val linkItems = listOf(
        ProfileMenuItem("copy", copyLabel, { copyProfileLink(context, publicUrl, copyLabel) }),
        ProfileMenuItem("share", share, { shareProfileLink(context, publicUrl, share) }),
    )
    val menu = when {
        profile.userId.isBlank() -> emptyList()

        owner -> linkItems

        else -> listOf(ProfileMenuItem("about", stringResource(R.string.profile_about_account), { about = true })) + linkItems +
            listOf(
                if (onUnblock != null) {
                    ProfileMenuItem("unblock", stringResource(R.string.profile_unblock), onUnblock)
                } else {
                    ProfileMenuItem("block", stringResource(R.string.messages_block), { moderation = false }, canModerate)
                },
                ProfileMenuItem("report", stringResource(R.string.messages_report), { moderation = true }, canModerate),
            )
    }
    ProfileTopBar(
        profile.displayUsername,
        stringResource(R.string.messages_more),
        menu,
        onBack = onBack,
        backContentDescription = stringResource(R.string.explore_back),
        onSettingsClick = onSettings.takeIf { owner },
        settingsContentDescription = stringResource(R.string.settings_title),
    )
    if (about) {
        AlertDialog(
            onDismissRequest = { about = false },
            title = { Text(stringResource(R.string.profile_about_account)) },
            text = {
                Column {
                    Text(profile.displayName)
                    Text(profile.displayUsername)
                    if (profile.joinedYear.isNotBlank()) Text(stringResource(R.string.profile_joined_year, profile.joinedYear))
                }
            },
            confirmButton = { TextButton(onClick = { about = false }) { Text(stringResource(R.string.explore_back)) } },
        )
    }
    moderation?.let { report ->
        val action = stringResource(if (report) R.string.messages_report else R.string.messages_block)
        AlertDialog(
            onDismissRequest = { moderation = null },
            title = { Text(action) },
            text = {
                Text(
                    when {
                        failed -> stringResource(R.string.profile_action_error)
                        report -> stringResource(R.string.profile_report_explainer, profile.displayName)
                        else -> stringResource(R.string.profile_block_explainer, profile.displayName)
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = { if (onModerate?.invoke(report) == true) moderation = null else failed = true }) { Text(action) }
            },
            dismissButton = { TextButton(onClick = { moderation = null }) { Text(stringResource(R.string.profile_cancel)) } },
        )
    }
}

private fun copyProfileLink(context: Context, url: String?, label: String) {
    if (url == null) return linkUnavailable(context)
    context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText(label, url))
    Toast.makeText(context, R.string.profile_url_copied, Toast.LENGTH_SHORT).show()
}

private fun shareProfileLink(context: Context, url: String?, title: String) {
    if (url == null) return linkUnavailable(context)
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, url)
    runCatching { context.startActivity(Intent.createChooser(send, title)) }.onFailure { linkUnavailable(context) }
}

private fun linkUnavailable(context: Context) = Toast.makeText(context, R.string.profile_link_unavailable, Toast.LENGTH_LONG).show()

/** Shown only when the listing publishes a valid phone or email (see [hasPublishedContact]). */
@Composable
internal fun ProfileContactDialog(listing: ServiceListing?, readOnly: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    fun open(scheme: String, address: String) {
        val action = if (scheme == "tel") Intent.ACTION_DIAL else Intent.ACTION_SENDTO
        runCatching { context.startActivity(Intent(action, Uri.fromParts(scheme, address, null))) }
            .onFailure { Toast.makeText(context, R.string.external_link_unavailable, Toast.LENGTH_LONG).show() }
    }
    val phone = listing?.phoneNumber?.takeIf(::validAccountPhone)
    val email = listing?.email?.takeIf(::validAccountEmail)
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.profile_contact)) }, text = {
        Column {
            if (phone == null && email == null) Text(stringResource(R.string.profile_contact_unavailable))
            phone?.let { TextButton(onClick = { open("tel", it) }, enabled = !readOnly) { Text(it) } }
            email?.let { TextButton(onClick = { open("mailto", it) }, enabled = !readOnly) { Text(it) } }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.explore_back)) } })
}
