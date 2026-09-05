package com.askit.app.story.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.story.StoryEditorTool
import com.askit.app.story.StoryFormState
import com.askit.app.story.StoryLayer
import com.askit.app.story.StoryMediaType
import com.askit.app.story.StoryViewModel
import com.askit.app.story.stickers.PostReshareSticker
import com.askit.designsystem.dialogs.AskITDiscardDialog
import com.askit.designsystem.navigation.AskITVerticalToolColumn
import com.askit.designsystem.navigation.AskITVerticalToolItem

@Composable
fun StoryEditorScreen(
    viewModel: StoryViewModel,
    state: StoryFormState,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDiscardConfirm by remember { mutableStateOf(false) }
    var moreExpanded by remember { mutableStateOf(false) }
    val editingTextLayer = state.layers
        .filterIsInstance<StoryLayer.Text>()
        .firstOrNull { it.id == state.editingTextLayerId }
    val showBottomChrome = editingTextLayer == null && state.activeTool != StoryEditorTool.Draw

    fun requestBack() {
        if (viewModel.isDirty) {
            showDiscardConfirm = true
        } else {
            onBack()
        }
    }

    BackHandler(onBack = ::requestBack)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .imePadding(),
    ) {
        StoryCanvas(
            mediaUri = state.mediaUri,
            mediaType = state.mediaType,
            solidBackgroundArgb = if (state.mediaType == StoryMediaType.SolidBackground) {
                viewModel.solidBackgroundArgb()
            } else {
                null
            },
            durationMs = state.durationMs,
            transform = state.transform,
            layers = state.layers,
            selectedLayerId = state.selectedLayerId,
            drawMode = state.activeTool == StoryEditorTool.Draw,
            onTransformChanged = viewModel::updateTransform,
            onLayerTransformChanged = viewModel::updateLayerTransform,
            onLayerSelected = viewModel::selectLayer,
            onDrawPoint = viewModel::addDrawPoint,
            onDrawFinished = viewModel::finishDrawStroke,
            editingTextLayerId = state.editingTextLayerId,
            onTextChanged = { id, text -> viewModel.updateTextLayer(id, text = text) },
            modifier = Modifier.fillMaxSize(),
        )

        state.sharedPost?.let { post ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                PostReshareSticker(
                    post = post,
                    cardStyle = state.reshareCardStyle,
                    onClick = { viewModel.toggleReshareCardStyle() },
                )
            }
        }

        IconButton(
            onClick = ::requestBack,
            modifier = Modifier
                .statusBarsPadding()
                .align(Alignment.TopStart)
                .padding(4.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.story_back),
                tint = Color.White,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .statusBarsPadding()
                .padding(end = 4.dp),
        ) {
            val tools = buildList {
                add(
                    AskITVerticalToolItem(
                        icon = Icons.Default.TextFields,
                        labelRes = R.string.story_tool_text,
                        selected = state.activeTool == StoryEditorTool.Text ||
                            editingTextLayer != null,
                        onClick = {
                            val existing = state.layers.filterIsInstance<StoryLayer.Text>().lastOrNull()
                            if (existing == null) {
                                viewModel.addTextLayer()
                            } else {
                                viewModel.startTextEditing(existing.id)
                            }
                        },
                        testTag = "story_tool_text",
                    ),
                )
                add(
                    AskITVerticalToolItem(
                        icon = Icons.Outlined.Mood,
                        labelRes = R.string.story_tool_stickers,
                        selected = state.activeTool == StoryEditorTool.Stickers,
                        onClick = { viewModel.openStickerTray() },
                        testTag = "story_tool_stickers",
                    ),
                )
                add(
                    AskITVerticalToolItem(
                        icon = Icons.Default.Brush,
                        labelRes = R.string.story_tool_draw,
                        selected = state.activeTool == StoryEditorTool.Draw,
                        onClick = { viewModel.setActiveTool(StoryEditorTool.Draw) },
                        testTag = "story_tool_draw",
                    ),
                )
                add(
                    AskITVerticalToolItem(
                        icon = Icons.Default.Download,
                        labelRes = R.string.story_tool_download,
                        onClick = onDownload,
                        testTag = "story_tool_download",
                    ),
                )
                if (state.mediaType == StoryMediaType.SolidBackground) {
                    add(
                        AskITVerticalToolItem(
                            icon = Icons.Default.FormatColorFill,
                            labelRes = R.string.story_tool_background,
                            selected = state.activeTool == StoryEditorTool.Background,
                            onClick = {
                                viewModel.cycleSolidBackground()
                                viewModel.setActiveTool(StoryEditorTool.Background)
                            },
                            testTag = "story_tool_background",
                        ),
                    )
                }
                add(
                    AskITVerticalToolItem(
                        icon = Icons.Default.MoreVert,
                        labelRes = R.string.story_tool_more,
                        onClick = { moreExpanded = true },
                        testTag = "story_tool_more",
                    ),
                )
            }
            AskITVerticalToolColumn(
                items = tools,
                overlay = true,
            )
            DropdownMenu(
                expanded = moreExpanded,
                onDismissRequest = { moreExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.story_keep_draft)) },
                    onClick = {
                        viewModel.markDraftSaved()
                        moreExpanded = false
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(com.askit.designsystem.R.string.discard_dialog_discard)) },
                    onClick = {
                        moreExpanded = false
                        showDiscardConfirm = true
                    },
                )
            }
        }

        if (showBottomChrome) {
            StoryEditorBottomChrome(
                viewModel = viewModel,
                state = state,
                onNext = onNext,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        if (state.activeTool == StoryEditorTool.Draw) {
            StoryDrawPad(
                viewModel = viewModel,
                selectedColorArgb = state.drawStrokeColorArgb,
                onDone = { viewModel.setActiveTool(null) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        editingTextLayer?.let { layer ->
            StoryTextEditor(
                viewModel = viewModel,
                layer = layer,
                onDone = viewModel::finishTextEditing,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    if (showDiscardConfirm) {
        AskITDiscardDialog(
            onDismissRequest = { showDiscardConfirm = false },
            onDiscard = {
                showDiscardConfirm = false
                onBack()
            },
        )
    }
}
