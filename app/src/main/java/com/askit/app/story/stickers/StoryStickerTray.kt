package com.askit.app.story.stickers

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.explore.SearchAreaScreen
import com.askit.app.explore.ExploreLocationSource
import com.askit.app.explore.ExploreSearchArea
import com.askit.app.media.persistPhotoPickerReadAccess
import com.askit.app.story.StoryStickerKind
import com.askit.app.story.StoryViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StoryStickerTray(
    viewModel: StoryViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var showLocationPicker by remember { mutableStateOf(false) }
    var showMentionInput by remember { mutableStateOf(false) }
    var showHashtagInput by remember { mutableStateOf(false) }
    var showPollInput by remember { mutableStateOf(false) }
    var showQuestionInput by remember { mutableStateOf(false) }
    var showLinkInput by remember { mutableStateOf(false) }
    var mentionDraft by remember { mutableStateOf("") }
    var hashtagDraft by remember { mutableStateOf("") }
    var pollQuestion by remember { mutableStateOf("") }
    var pollOptionA by remember { mutableStateOf("") }
    var pollOptionB by remember { mutableStateOf("") }
    var questionDraft by remember { mutableStateOf("") }
    var linkUrl by remember { mutableStateOf("") }
    var linkLabel by remember { mutableStateOf("") }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            persistPhotoPickerReadAccess(context.contentResolver, listOf(uri))
            viewModel.addSticker(
                kind = StoryStickerKind.Photo,
                label = context.getString(R.string.story_sticker_photo),
                payload = uri.toString(),
            )
            onDismiss()
        },
    )

    if (showLocationPicker) {
        SearchAreaScreen(
            confirmedArea = ExploreSearchArea(
                placeId = null,
                displayName = stringResource(R.string.story_sticker_location),
                supportingText = null,
                latitude = null,
                longitude = null,
                radiusKm = 10,
                source = ExploreLocationSource.SAVED,
            ),
            onBack = { showLocationPicker = false },
            titleRes = R.string.story_sticker_location,
            showFilterControls = false,
            onApply = { area, _ ->
                if (area.isUsable) {
                    viewModel.addSticker(
                        kind = StoryStickerKind.Location,
                        label = area.displayName,
                        payload = area.placeId.orEmpty(),
                    )
                    onDismiss()
                }
                showLocationPicker = false
            },
        )
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SearchBar(
                inputField = {
                    androidx.compose.material3.SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = { },
                        expanded = false,
                        onExpandedChange = { },
                        placeholder = { Text(stringResource(R.string.story_sticker_search)) },
                    )
                },
                expanded = false,
                onExpandedChange = { },
                modifier = Modifier.fillMaxWidth(),
            ) { }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StickerChip(R.string.story_sticker_location) { showLocationPicker = true }
                StickerChip(R.string.story_sticker_mention) { showMentionInput = true }
                StickerChip(R.string.story_sticker_hashtag) { showHashtagInput = true }
                StickerChip(R.string.story_sticker_poll) { showPollInput = true }
                StickerChip(R.string.story_sticker_question) { showQuestionInput = true }
                StickerChip(R.string.story_sticker_link) { showLinkInput = true }
                StickerChip(R.string.story_sticker_photo) {
                    photoLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                }
                listOf("😀", "👍", "✨", "🔥").forEach { emoji ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            viewModel.addSticker(
                                kind = StoryStickerKind.Emoji,
                                label = emoji,
                            )
                            onDismiss()
                        },
                        label = { Text(emoji) },
                    )
                }
            }
        }
    }

    if (showMentionInput) {
        StickerInputDialog(
            title = stringResource(R.string.story_sticker_mention),
            value = mentionDraft,
            onValueChange = { mentionDraft = it },
            onDismiss = { showMentionInput = false },
            onConfirm = {
                viewModel.addSticker(
                    kind = StoryStickerKind.Mention,
                    label = "@$mentionDraft",
                    payload = mentionDraft,
                )
                showMentionInput = false
                onDismiss()
            },
        )
    }

    if (showHashtagInput) {
        StickerInputDialog(
            title = stringResource(R.string.story_sticker_hashtag),
            value = hashtagDraft,
            onValueChange = { hashtagDraft = it },
            onDismiss = { showHashtagInput = false },
            onConfirm = {
                viewModel.addSticker(
                    kind = StoryStickerKind.Hashtag,
                    label = "#$hashtagDraft",
                    payload = hashtagDraft,
                )
                showHashtagInput = false
                onDismiss()
            },
        )
    }

    if (showPollInput) {
        StickerMultiInputDialog(
            title = stringResource(R.string.story_sticker_poll),
            fields = listOf(pollQuestion, pollOptionA, pollOptionB),
            onDismiss = { showPollInput = false },
            onConfirm = { values ->
                pollQuestion = values[0]
                pollOptionA = values[1]
                pollOptionB = values[2]
                viewModel.addSticker(
                    kind = StoryStickerKind.Poll,
                    label = pollQuestion,
                    payload = "$pollOptionA|$pollOptionB",
                )
                showPollInput = false
                onDismiss()
            },
        )
    }

    if (showQuestionInput) {
        StickerInputDialog(
            title = stringResource(R.string.story_sticker_question),
            value = questionDraft,
            onValueChange = { questionDraft = it },
            onDismiss = { showQuestionInput = false },
            onConfirm = {
                viewModel.addSticker(
                    kind = StoryStickerKind.Question,
                    label = questionDraft,
                )
                showQuestionInput = false
                onDismiss()
            },
        )
    }

    if (showLinkInput) {
        StickerLinkDialog(
            url = linkUrl,
            label = linkLabel,
            onUrlChange = { linkUrl = it },
            onLabelChange = { linkLabel = it },
            onDismiss = { showLinkInput = false },
            onConfirm = {
                viewModel.addSticker(
                    kind = StoryStickerKind.Link,
                    label = linkLabel.ifBlank { linkUrl },
                    payload = linkUrl,
                )
                showLinkInput = false
                onDismiss()
            },
        )
    }
}

@Composable
private fun StickerChip(
    titleRes: Int,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(stringResource(titleRes)) },
    )
}

@Composable
private fun StickerInputDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.story_add))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.story_cancel))
            }
        },
    )
}

@Composable
private fun StickerMultiInputDialog(
    title: String,
    fields: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit,
) {
    var values by remember(fields) { mutableStateOf(fields) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                values.forEachIndexed { index, value ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                            values = values.toMutableList().also { it[index] = newValue }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = { onConfirm(values) }) {
                Text(stringResource(R.string.story_add))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.story_cancel))
            }
        },
    )
}

@Composable
private fun StickerLinkDialog(
    url: String,
    label: String,
    onUrlChange: (String) -> Unit,
    onLabelChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.story_sticker_link)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = url,
                    onValueChange = onUrlChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.story_link_url)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = onLabelChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.story_link_label)) },
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.story_add))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.story_cancel))
            }
        },
    )
}
