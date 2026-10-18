package com.askit.app.home.posts

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.askit.app.R
import com.askit.app.home.model.FeedPost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostActionsSheet(postId: String, store: PostStore, currentAccountId: () -> String?, onDismiss: () -> Unit, onEdit: (String) -> Unit) {
    val posts by store.posts.collectAsStateWithLifecycle()
    val account by store.account.collectAsStateWithLifecycle()
    val post = posts[postId]?.takeIf { !it.archived || store.isOwner(it, account.accountId) }
    val context = LocalContext.current
    var phase by rememberSaveable(postId, account.accountId) { mutableStateOf("options") }
    var reasonName by rememberSaveable(postId, account.accountId) { mutableStateOf<String?>(null) }
    var error by rememberSaveable(postId, account.accountId) { mutableStateOf<Int?>(null) }
    val signedIn = store.isCurrentAccount(account.accountId)
    val owner = post != null && store.isOwner(post, account.accountId)
    fun finish(result: PostResult, report: Boolean = false) {
        when (result) {
            is PostResult.Failure -> error = result.messageRes

            is PostResult.Success -> {
                onDismiss()
                Toast.makeText(context, if (report) R.string.post_report_saved else R.string.post_action_done, Toast.LENGTH_LONG).show()
            }
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        contentWindowInsets = { WindowInsets.safeDrawing },
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(
                    when (phase) {
                        "report" -> R.string.post_report_title
                        "archive" -> R.string.post_archive
                        else -> R.string.post_actions_title
                    },
                ),
                style = MaterialTheme.typography.titleLarge,
            )
            if (post == null) {
                Text(stringResource(R.string.post_unavailable))
            } else {
                Text(post.authorName, style = MaterialTheme.typography.bodyMedium)
                if (!signedIn) Text(stringResource(R.string.post_action_sign_in))
                error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
                when (phase) {
                    "report" -> {
                        Text(stringResource(R.string.post_report_notice))
                        Column(Modifier.selectableGroup()) {
                            PostReportReason.entries.forEach { reason ->
                                Row(
                                    Modifier.fillMaxWidth().selectable(
                                        reasonName == reason.name,
                                        enabled = signedIn,
                                        role = Role.RadioButton,
                                        onClick = {
                                            reasonName = reason.name
                                            error = null
                                        },
                                    ).padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(reasonName == reason.name, onClick = null, enabled = signedIn)
                                    Text(stringResource(reason.labelRes), Modifier.padding(start = 10.dp))
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = onDismiss) { Text(stringResource(R.string.story_cancel)) }
                            TextButton(
                                enabled = signedIn && reasonName != null && postId !in account.reports,
                                onClick = {
                                    PostReportReason.entries.firstOrNull {
                                        it.name == reasonName
                                    }?.let { finish(store.report(postId, currentAccountId(), it), report = true) }
                                },
                            ) {
                                Text(stringResource(R.string.post_report_confirm))
                            }
                        }
                    }

                    "archive" -> {
                        Text(stringResource(R.string.post_archive_message))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = onDismiss) { Text(stringResource(R.string.story_cancel)) }
                            TextButton(onClick = {
                                finish(store.setArchived(postId, currentAccountId(), true))
                            }) { Text(stringResource(R.string.post_archive)) }
                        }
                    }

                    else -> if (owner) {
                        PostMenuAction(R.string.post_edit_title) {
                            if (store.editableDraft(postId, currentAccountId()) == null) {
                                error = R.string.post_edit_unavailable
                            } else {
                                onDismiss()
                                onEdit(postId)
                            }
                        }
                        PostMenuAction(
                            if (postId in
                                account.saved
                            ) {
                                R.string.post_unsave
                            } else {
                                R.string.post_save
                            },
                        ) { finish(store.toggleSaved(postId, currentAccountId())) }
                        PostMenuAction(if (post.archived) R.string.post_unarchive else R.string.post_archive) {
                            if (post.archived) finish(store.setArchived(postId, currentAccountId(), false)) else phase = "archive"
                        }
                        if (!post.archived) {
                            PostMenuAction(if (post.pinnedAtMillis != null) R.string.post_unpin else R.string.post_pin) {
                                finish(store.setPinned(postId, currentAccountId(), post.pinnedAtMillis == null))
                            }
                        }
                        PostMenuAction(if (post.hideLikeCount) R.string.post_show_likes else R.string.post_hide_likes) {
                            finish(store.toggleLikeCount(postId, currentAccountId()))
                        }
                        PostMenuAction(if (post.commentsEnabled) R.string.post_disable_comments else R.string.post_enable_comments) {
                            finish(store.toggleComments(postId, currentAccountId()))
                        }
                    } else {
                        PostMenuAction(R.string.post_not_interested, enabled = signedIn) {
                            when (val result = store.hideFromHome(postId, currentAccountId())) {
                                is PostResult.Failure -> error = result.messageRes
                                is PostResult.Success -> onDismiss()
                            }
                        }
                        if (postId in account.reports) Text(stringResource(R.string.post_report_duplicate))
                        PostMenuAction(R.string.post_report_title, enabled = signedIn && postId !in account.reports) { phase = "report" }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostMenuAction(label: Int, enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) { Text(stringResource(label)) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCollectionSheet(
    posts: List<FeedPost>,
    archived: Boolean,
    onDismiss: () -> Unit,
    onOpen: (String) -> Unit,
    onRestore: (String) -> Unit = {},
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        contentWindowInsets = { WindowInsets.safeDrawing },
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(if (archived) R.string.post_archived_title else R.string.post_saved_title),
                style = MaterialTheme.typography.titleLarge,
            )
            if (posts.isEmpty()) Text(stringResource(if (archived) R.string.post_archived_empty else R.string.post_saved_empty))
            posts.forEach { post ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).clickable { onOpen(post.id) }.padding(vertical = 12.dp)) {
                        Text(post.authorName, style = MaterialTheme.typography.titleSmall)
                        Text(
                            post.content.ifBlank {
                                post.poll?.question.orEmpty()
                            }.ifBlank { stringResource(R.string.post_detail_title) },
                            maxLines = 3,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        )
                    }
                    if (archived) TextButton(onClick = { onRestore(post.id) }) { Text(stringResource(R.string.post_unarchive)) }
                }
            }
        }
    }
}
