package com.askit.app.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.askit.app.R
import com.askit.app.auth.validAccountEmail
import com.askit.app.auth.validAccountPhone
import com.askit.app.session.ServiceListing
import com.askit.designsystem.profile.ProfileMenuItem
import com.askit.designsystem.profile.ProfileTopBar

@Composable
internal fun ProfileOverflowTopBar(
    profile: ProfileUiState,
    viewerId: String?,
    publicUrl: String?,
    onBack: (() -> Unit)? = null,
    onSettings: () -> Unit = {},
    onModerate: (Boolean) -> Boolean = { false },
) {
    val context = LocalContext.current
    var about by remember(profile.userId) { mutableStateOf(false) }
    var moderation by remember(profile.userId) { mutableStateOf<Boolean?>(null) }
    var failed by remember { mutableStateOf(false) }
    val owner = profile.isOwner && !profile.isPublicPreview
    val canMutate = canMutateProfileVisitor(viewerId, profile.userId, profile.isPublicPreview)
    val unavailable = stringResource(R.string.profile_link_unavailable)
    val copied = stringResource(R.string.profile_url_copied)
    val share = stringResource(R.string.profile_share)
    val menus = listOf(
        ProfileMenuItem("about", stringResource(R.string.profile_about_account), { about = true }),
        ProfileMenuItem("copy", stringResource(R.string.profile_copy_url), {
            if (publicUrl == null) {
                Toast.makeText(context, unavailable, Toast.LENGTH_LONG).show()
            } else {
                context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Profile URL", publicUrl))
                Toast.makeText(context, copied, Toast.LENGTH_SHORT).show()
            }
        }),
        ProfileMenuItem("share", share, {
            if (publicUrl == null) {
                Toast.makeText(context, unavailable, Toast.LENGTH_LONG).show()
            } else {
                runCatching {
                    context.startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, publicUrl)
                            },
                            share,
                        ),
                    )
                }
                    .onFailure { Toast.makeText(context, unavailable, Toast.LENGTH_LONG).show() }
            }
        }),
        ProfileMenuItem("block", stringResource(R.string.messages_block), { moderation = false }, canMutate),
        ProfileMenuItem("report", stringResource(R.string.messages_report), { moderation = true }, canMutate),
    )
    ProfileTopBar(
        profile.displayUsername,
        stringResource(R.string.messages_more),
        if (owner) emptyList() else menus,
        onBack = onBack,
        backContentDescription = stringResource(R.string.explore_back),
        onSettingsClick = onSettings.takeIf { owner },
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
        AlertDialog(
            onDismissRequest = {
                moderation = null
            },
            title = { Text(stringResource(if (report) R.string.messages_report else R.string.messages_block)) },
            text = { Text(if (failed) stringResource(R.string.profile_action_unavailable) else profile.displayName) },
            confirmButton = {
                TextButton(onClick = {
                    if (onModerate(report)) {
                        moderation = null
                        failed = false
                    } else {
                        failed = true
                    }
                }) {
                    Text(stringResource(if (report) R.string.messages_report else R.string.messages_block))
                }
            },
            dismissButton = { TextButton(onClick = { moderation = null }) { Text(stringResource(R.string.profile_cancel)) } },
        )
    }
}

@Composable
internal fun ProfileContactDialog(listing: ServiceListing?, readOnly: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val unavailable = stringResource(R.string.profile_action_unavailable)
    fun open(scheme: String, address: String) {
        runCatching {
            context.startActivity(
                Intent(
                    if (scheme ==
                        "tel"
                    ) {
                        Intent.ACTION_DIAL
                    } else {
                        Intent.ACTION_SENDTO
                    },
                    Uri.fromParts(scheme, address, null),
                ),
            )
        }
            .onFailure { Toast.makeText(context, unavailable, Toast.LENGTH_LONG).show() }
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
