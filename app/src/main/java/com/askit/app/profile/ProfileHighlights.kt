package com.askit.app.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.askit.app.R
import com.askit.app.home.model.Story
import com.askit.app.story.StoryLayer
import com.askit.app.story.StoryMediaType
import kotlinx.coroutines.launch
import com.askit.designsystem.R as DsR

@Composable
internal fun ProfileHighlights(
    ownerId: String,
    viewerId: String?,
    collections: List<ProfileHighlight>,
    stories: List<Story>,
    canManage: Boolean,
    authorizedPostIds: Set<String> = emptySet(),
    onSave: (ProfileHighlight) -> Boolean,
    onDelete: (String) -> Boolean,
    onOpenPost: (String) -> Unit,
) {
    var editingId by rememberSaveable(ownerId, viewerId) { mutableStateOf<String?>(null) }
    var viewingId by rememberSaveable(ownerId, viewerId) { mutableStateOf<String?>(null) }
    var menuId by rememberSaveable(ownerId, viewerId) { mutableStateOf<String?>(null) }
    var deletingId by rememberSaveable(ownerId, viewerId) { mutableStateOf<String?>(null) }
    val owned = collections.filter { it.ownerId == ownerId }
    val highlightsLabel = stringResource(R.string.profile_highlights)
    Column(Modifier.fillMaxWidth().testTag("profile_highlights")) {
        LazyRow(modifier = Modifier.semantics { contentDescription = highlightsLabel }, horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
            if (canManage) item(key = "new_highlight") {
                HighlightItem(stringResource(R.string.profile_highlight_new_label),
                    onClick = { editingId = "new" }, modifier = Modifier.testTag("highlight_new")) {
                    Icon(painterResource(DsR.drawable.ic_add), stringResource(R.string.profile_highlight_new), modifier = Modifier.size(28.dp))
                }
            }
            items(owned, key = { it.id }) { collection ->
                val visible = collection.visibleStories(viewerId, authorizedPostIds)
                val cover = visible.firstOrNull { it.id == collection.coverStoryId } ?: visible.firstOrNull()
                Box {
                    HighlightItem(collection.name, onClick = { viewingId = collection.id },
                        onLongClick = if (canManage) ({ menuId = collection.id }) else null,
                        longClickLabel = stringResource(R.string.profile_highlight_manage, collection.name),
                        modifier = Modifier.testTag("highlight_${collection.id}")) {
                        HighlightCover(cover, Modifier.fillMaxSize())
                    }
                    DropdownMenu(expanded = canManage && menuId == collection.id, onDismissRequest = { menuId = null }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.profile_highlight_edit)) }, onClick = {
                            menuId = null; editingId = collection.id
                        })
                        DropdownMenuItem(text = { Text(stringResource(R.string.profile_highlight_delete), color = MaterialTheme.colorScheme.error) }, onClick = {
                            menuId = null; deletingId = collection.id
                        })
                    }
                }
            }
        }
        if (owned.isEmpty() || canManage) Text(
            stringResource(if (owned.isEmpty()) R.string.profile_highlights_empty else R.string.profile_highlight_manage_hint),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 4.dp))
    }
    if (canManage && editingId != null) {
        HighlightEditor(ownerId, owned.firstOrNull { it.id == editingId }, stories,
            onDismiss = { editingId = null }, onSave = onSave,
            onDelete = { id -> editingId = null; deletingId = id })
    }
    if (canManage) owned.firstOrNull { it.id == deletingId }?.let { collection ->
        HighlightDeleteDialog(collection, onDismiss = { deletingId = null }, onDelete = onDelete)
    }
    owned.firstOrNull { it.id == viewingId }?.let { collection ->
        HighlightPlayback(collection, viewerId, authorizedPostIds, onDismiss = { viewingId = null },
            onOpenPost = { postId -> viewingId = null; onOpenPost(postId) })
    }
}

@Composable
private fun HighlightItem(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    longClickLabel: String? = null,
    cover: @Composable BoxScope.() -> Unit,
) {
    Column(modifier.width(80.dp).combinedClickable(onClick = onClick, onClickLabel = name,
        onLongClick = onLongClick, onLongClickLabel = longClickLabel), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(72.dp).border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), CircleShape)
            .padding(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center, content = cover)
        Text(name, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp))
    }
}

/** Static covers never start video players or expose stories outside the caller's audience filter. */
@Composable
internal fun HighlightCover(story: Story?, modifier: Modifier = Modifier) {
    val content = story?.content
    val text = content?.layers?.filterIsInstance<StoryLayer.Text>()?.firstOrNull()
    var failed by remember(content?.mediaUri) { mutableStateOf(false) }
    val background = content?.solidBackgroundArgb?.let { Color(it) } ?: MaterialTheme.colorScheme.surfaceContainerHigh
    val foreground = if (background.luminance() < .5f) Color.White else Color.Black
    Box(modifier.background(background)
        .clearAndSetSemantics { }, contentAlignment = Alignment.Center) {
        when {
            content?.mediaType == StoryMediaType.Photo && !content.mediaUri.isNullOrBlank() && !failed ->
                AsyncImage(content.mediaUri, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(), onError = { failed = true })
            content?.mediaType == StoryMediaType.Video ->
                Icon(Icons.Outlined.PlayArrow, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(28.dp))
            !failed && (!text?.text.isNullOrBlank() || !content?.caption.isNullOrBlank()) -> {
                // Covers are artwork; labels outside them still follow accessibility text size.
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1f)) {
                    Text(text?.text?.takeIf { it.isNotBlank() } ?: content.caption.orEmpty(),
                        color = text?.colorArgb?.let { Color(it) } ?: foreground,
                        fontSize = 11.sp, lineHeight = 14.sp, textAlign = TextAlign.Center, maxLines = 3,
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(6.dp)
                            .background(if (text?.hasBackground == true) Color(text.backgroundArgb) else Color.Transparent, MaterialTheme.shapes.extraSmall)
                            .padding(2.dp))
                }
            }
            else -> Icon(painterResource(DsR.drawable.ic_photo), null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun HighlightDeleteDialog(collection: ProfileHighlight, onDismiss: () -> Unit, onDelete: (String) -> Boolean) {
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(stringResource(R.string.profile_highlight_delete_title, collection.name)) },
        text = { Text(stringResource(if (failed) R.string.profile_save_unavailable else R.string.profile_highlight_delete_hint)) },
        confirmButton = { TextButton(enabled = !busy, onClick = {
            scope.launch {
                busy = true
                val deleted = persistHighlightChange { onDelete(collection.id) }
                busy = false
                if (deleted) onDismiss() else failed = true
            }
        }) { Text(stringResource(R.string.profile_highlight_delete), color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text(stringResource(R.string.profile_cancel)) } })
}
