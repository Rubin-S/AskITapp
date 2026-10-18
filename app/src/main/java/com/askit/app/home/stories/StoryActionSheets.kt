package com.askit.app.home.stories

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.askit.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryActionSheet(
    viewModel: StoryViewerViewModel,
    state: StoryViewerUiState,
    social: StorySocialState,
    currentAccountId: () -> String?,
) {
    if (state.menuStoryId == null) return
    val story = viewModel.menuStory() ?: return
    val enabled = viewModel.social.canAct(social.accountId, story.authorId)
    val reported = story.id in social.reports
    ModalBottomSheet(
        onDismissRequest = viewModel::closeActions,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        contentWindowInsets = { WindowInsets.safeDrawing },
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(if (state.reporting) R.string.story_report_title else R.string.story_actions_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(story.authorName, style = MaterialTheme.typography.bodyMedium)
            if (!enabled) {
                Text(
                    stringResource(
                        if (social.accountId ==
                            null
                        ) {
                            R.string.story_action_sign_in
                        } else {
                            R.string.story_action_unavailable
                        },
                    ),
                )
            }
            state.actionError?.let { Text(stringResource(it.messageRes), color = MaterialTheme.colorScheme.error) }
            if (state.reporting) {
                Text(stringResource(R.string.story_report_notice))
                Column(Modifier.selectableGroup()) {
                    StoryReportReason.entries.forEach { reason ->
                        Row(
                            Modifier.fillMaxWidth().selectable(
                                state.reportReason == reason,
                                enabled = enabled,
                                role = Role.RadioButton,
                                onClick = {
                                    viewModel.selectReportReason(reason)
                                },
                            ).padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(state.reportReason == reason, onClick = null, enabled = enabled)
                            Text(stringResource(reason.labelRes), modifier = Modifier.padding(start = 10.dp))
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = viewModel::closeActions) { Text(stringResource(R.string.story_cancel)) }
                    TextButton(
                        onClick = { viewModel.report(currentAccountId()) },
                        enabled =
                            enabled && !reported && state.reportReason != null,
                    ) {
                        Text(stringResource(R.string.story_report_confirm))
                    }
                }
            } else {
                TextButton(onClick = viewModel::openReport, enabled = enabled && !reported, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(if (reported) R.string.story_reported else R.string.story_report_title))
                }
                TextButton(onClick = { viewModel.mute(currentAccountId()) }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.story_mute_author))
                }
                TextButton(onClick = { viewModel.hide(currentAccountId()) }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.story_not_interested))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MutedStoriesSheet(authors: List<StoryAuthor>, onDismiss: () -> Unit, onUnmute: (String) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        contentWindowInsets = { WindowInsets.safeDrawing },
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.story_muted_settings), style = MaterialTheme.typography.titleLarge)
            if (authors.isEmpty()) Text(stringResource(R.string.story_muted_empty))
            authors.forEach { author ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(author.name, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onUnmute(author.id) }) { Text(stringResource(R.string.story_unmute)) }
                }
            }
        }
    }
}
