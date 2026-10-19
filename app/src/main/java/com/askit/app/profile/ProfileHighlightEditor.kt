package com.askit.app.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.format.appLocale
import com.askit.app.format.formatInstantDateTime
import com.askit.app.home.model.Story
import com.askit.app.story.StoryLayer
import com.askit.designsystem.inputs.AskITTextField
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import com.askit.designsystem.R as DsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HighlightEditor(
    ownerId: String,
    original: ProfileHighlight?,
    availableStories: List<Story>,
    onDismiss: () -> Unit,
    onSave: (ProfileHighlight) -> Boolean,
    onDelete: (String) -> Unit,
) {
    val id = remember(ownerId, original?.id) { original?.id ?: UUID.randomUUID().toString() }
    val candidates = (original?.stories.orEmpty() + availableStories)
        .filter { it.authorId == ownerId && !it.isDeleted }.distinctBy { it.id }
    var name by rememberSaveable(id) { mutableStateOf(original?.name.orEmpty()) }
    var selected by rememberSaveable(id) { mutableStateOf(original?.stories?.map { it.id }.orEmpty()) }
    var coverId by rememberSaveable(id) { mutableStateOf(original?.coverStoryId) }
    var choosingCover by rememberSaveable(id) { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val members = candidates.filter { it.id in selected }
    val cover = members.firstOrNull { it.id == coverId } ?: members.firstOrNull()
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = { if (!busy) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.fillMaxHeight(),
        dragHandle = null,
        sheetGesturesEnabled = !busy,
    ) {
        Column(Modifier.fillMaxSize().imePadding().testTag("highlight_editor")) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp).heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(enabled = !busy, onClick = { if (choosingCover) choosingCover = false else onDismiss() }) {
                    Icon(painterResource(DsR.drawable.ic_close), stringResource(R.string.profile_cancel))
                }
                Text(
                    stringResource(
                        if (choosingCover) {
                            R.string.profile_highlight_choose_cover
                        } else if (original == null) {
                            R.string.profile_highlight_new
                        } else {
                            R.string.profile_highlight_edit
                        },
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(enabled = name.isNotBlank() && members.isNotEmpty() && !busy && !choosingCover, onClick = {
                    scope.launch {
                        busy = true
                        val collection = ProfileHighlight(id, ownerId, name.trim(), members, cover?.id)
                        val saved = persistHighlightChange { onSave(collection) }
                        busy = false
                        if (saved) onDismiss() else failed = true
                    }
                }) {
                    if (busy) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.profile_save))
                    }
                }
            }
            HorizontalDivider()
            LazyVerticalGrid(
                columns = GridCells.Adaptive(96.dp),
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = 24.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HighlightCover(
                            cover,
                            Modifier.size(88.dp).clip(CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                        )
                        if (!choosingCover) {
                            TextButton(enabled = members.isNotEmpty() && !busy, onClick = { choosingCover = true }) {
                                Text(stringResource(R.string.profile_highlight_edit_cover))
                            }
                            AskITTextField(
                                name,
                                {
                                    if (it.length <= 60) {
                                        name = it
                                        failed = false
                                    }
                                },
                                enabled = !busy,
                                singleLine = true,
                                label = { Text(stringResource(R.string.profile_highlight_name)) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        val hint = if (choosingCover) {
                            stringResource(R.string.profile_highlight_choose_cover_hint)
                        } else {
                            pluralStringResource(R.plurals.profile_highlight_selection_count, members.size, members.size)
                        }
                        Text(
                            hint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        )
                        if (failed) Text(stringResource(R.string.profile_save_unavailable), color = MaterialTheme.colorScheme.error)
                        if (candidates.isEmpty()) {
                            Text(
                                stringResource(R.string.profile_highlight_no_stories),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 16.dp),
                            )
                        }
                    }
                }
                items(if (choosingCover) members else candidates, key = { it.id }) { story ->
                    val checked = if (choosingCover) story.id == cover?.id else story.id in selected
                    val label = story.content.caption.ifBlank {
                        story.content.layers.filterIsInstance<StoryLayer.Text>().joinToString(" ") { it.text }
                    }.ifBlank { story.authorName }
                    val date = formatInstantDateTime(story.createdAtMillis, appLocale())
                    val description = "$label, $date"
                    val select = {
                        failed = false
                        if (choosingCover) {
                            coverId = story.id
                            choosingCover = false
                        } else {
                            selected = if (checked) selected - story.id else selected + story.id
                            if (checked && coverId == story.id) coverId = null
                        }
                    }
                    val interaction = if (choosingCover) {
                        Modifier.selectable(checked, enabled = !busy, role = Role.RadioButton, onClick = select)
                    } else {
                        Modifier.toggleable(checked, enabled = !busy, role = Role.Checkbox, onValueChange = { select() })
                    }
                    Box(
                        Modifier.aspectRatio(3f / 4f).clip(RoundedCornerShape(8.dp))
                            .then(interaction).semantics { contentDescription = description }
                            .testTag("highlight_select_${story.id}"),
                    ) {
                        HighlightCover(story, Modifier.fillMaxSize())
                        Box(
                            Modifier.padding(8.dp).align(Alignment.TopEnd).size(24.dp).clip(CircleShape)
                                .background(
                                    if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = .8f),
                                )
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (checked) {
                                Icon(
                                    painterResource(DsR.drawable.ic_check),
                                    null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
                if (original != null && !choosingCover) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        TextButton(enabled = !busy, onClick = { onDelete(id) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.profile_highlight_delete), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

internal suspend fun persistHighlightChange(action: () -> Boolean): Boolean = withContext(Dispatchers.IO) {
    try {
        action()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        false
    }
}
