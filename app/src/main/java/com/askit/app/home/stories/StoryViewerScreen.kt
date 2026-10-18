package com.askit.app.home.stories

import android.text.format.DateUtils
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import com.askit.app.home.model.Story
import com.askit.app.home.model.isShareable
import com.askit.app.story.StoryMediaType
import com.askit.app.story.editor.StoryCanvas
import com.askit.app.story.editor.StoryFrame
import com.askit.app.story.editor.StoryStage
import com.askit.app.story.stickers.PostReshareSticker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

/**
 * Full-screen, immersive Instagram-style story player: swipe horizontally to change author,
 * tap left/right or hold to pause, drag down to dismiss.
 */
@Composable
fun StoryViewerRoute(
    viewModel: StoryViewerViewModel,
    startStoryId: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onViewPost: ((String) -> Unit)? = null,
    onAddStory: (() -> Unit)? = null,
    onStorySeen: ((String) -> Unit)? = null,
    currentAccountId: () -> String? = { null },
    shareStoryId: String? = null,
    onShareStory: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val social by viewModel.social.state.collectAsStateWithLifecycle()
    // Wait for this navigation entry before starting playback or marking anything seen.
    if (state.entryStoryId != startStoryId) return
    BackHandler(enabled = state.menuStoryId == null && shareStoryId == null, onBack = onDismiss)
    LaunchedEffect(state.shouldClose) { if (state.shouldClose) onDismiss() }

    LaunchedEffect(state.currentStory?.id, state.currentStory?.createdAtMillis) {
        state.currentStory?.let { story ->
            kotlinx.coroutines.delay((story.createdAtMillis + 24 * 60 * 60 * 1000L - System.currentTimeMillis()).coerceAtLeast(0))
            viewModel.refreshAvailability()
        }
    }
    LaunchedEffect(state.currentStory?.id) {
        state.currentStory?.id?.takeUnless { state.shouldClose }?.let { storyId ->
            onStorySeen?.invoke(storyId)
        }
    }

    StoryViewerScreen(
        state = state,
        onNext = { if (!viewModel.advanceToNext()) onDismiss() },
        onPrevious = viewModel::backToPrevious,
        onPauseChanged = viewModel::setPaused,
        onLikeClick = { viewModel.toggleLike(currentAccountId()) },
        onShareClick = { state.currentStory?.id?.let(onShareStory) },
        socialLikes = social.likedStoryIds,
        onSelectGroup = viewModel::selectGroup,
        onDismiss = onDismiss,
        modifier = modifier,
        onViewPost = onViewPost,
        onAddStory = onAddStory,
        actionsOpen = state.menuStoryId != null || shareStoryId != null,
        showFollow = state.currentStory?.let { !it.isOwn && it.authorId !in social.following } == true,
        canFollow = viewModel.social.canAct(social.accountId, state.currentStory?.authorId),
        onFollow = { viewModel.follow(currentAccountId()) },
        onMore = viewModel::openActions,
    )
    StoryActionSheet(viewModel, state, social, currentAccountId)
}

@Composable
internal fun StoryViewerScreen(
    state: StoryViewerUiState,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPauseChanged: (Boolean) -> Unit,
    onLikeClick: () -> Unit,
    onShareClick: () -> Unit,
    socialLikes: Set<String>,
    onSelectGroup: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onViewPost: ((String) -> Unit)? = null,
    onAddStory: (() -> Unit)? = null,
    actionsOpen: Boolean = false,
    showFollow: Boolean = false,
    canFollow: Boolean = false,
    onFollow: () -> Unit = {},
    onMore: () -> Unit = {},
) {
    val retainedPlayback = state.playbackContext is StoryPlaybackContext.Highlight
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var showViewPostTooltip by remember(state.groupIndex, state.itemIndex) { mutableStateOf(false) }
    val density = LocalDensity.current
    val dismissThresholdPx = with(density) { 160.dp.toPx() }
    val scope = rememberCoroutineScope()

    val restartKey = state.currentStory?.id.orEmpty()
    val pagerState = rememberPagerState(
        initialPage = state.groupIndex,
        pageCount = { state.groups.size },
    )
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val effectivePaused = actionsOpen || state.paused || showViewPostTooltip || pagerState.isScrollInProgress ||
        !lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
    var videoProgress by remember(restartKey) { mutableFloatStateOf(0f) }
    // A reader, not a value: the progress bar reads it while drawing, so playback ticks no
    // longer recompose the whole viewer every frame.
    val itemProgressFraction: () -> Float =
        if (state.isLoading || state.groups.isEmpty()) {
            NO_PROGRESS
        } else if (state.currentStory?.content?.mediaType == StoryMediaType.Video) {
            remember(restartKey) { { videoProgress } }
        } else {
            rememberStoryItemProgress(
                restartKey = restartKey,
                paused = effectivePaused,
                onFinished = onNext,
            )
        }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .graphicsLayer { translationY = dragOffsetY }
            .safeDrawingPadding(),
    ) {
        StoryStage {
            if (state.isLoading || state.groups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize())
            } else {
                val groupKeys = remember(state.groups) { state.groups.map { it.authorId ?: it.stories.first().id } }
                SyncPagerWithState(
                    pagerState = pagerState,
                    groupIndex = state.groupIndex,
                    groupKeys = groupKeys,
                    onSelectGroup = onSelectGroup,
                )

                HorizontalPager(
                    state = pagerState,
                    key = { page -> state.groups[page].authorId ?: state.groups[page].stories.first().id },
                    userScrollEnabled = !actionsOpen,
                    modifier = Modifier
                        .fillMaxSize()
                        .storyTapGestures(
                            onPrevious = {
                                if (actionsOpen) return@storyTapGestures
                                if (showViewPostTooltip) {
                                    showViewPostTooltip = false
                                } else {
                                    onPrevious()
                                }
                            },
                            onNext = {
                                if (actionsOpen) return@storyTapGestures
                                if (showViewPostTooltip) {
                                    showViewPostTooltip = false
                                } else {
                                    onNext()
                                }
                            },
                            onHoldChanged = { if (!actionsOpen) onPauseChanged(it) },
                        )
                        .storyVerticalDrag(
                            onDragDeltaY = { delta ->
                                if (!actionsOpen) dragOffsetY = (dragOffsetY + delta).coerceAtLeast(-80f)
                            },
                            onSettle = {
                                if (!actionsOpen && dragOffsetY >= dismissThresholdPx) {
                                    onDismiss()
                                } else {
                                    scope.launch {
                                        animate(
                                            initialValue = dragOffsetY,
                                            targetValue = 0f,
                                            animationSpec = spring(),
                                        ) { value, _ -> dragOffsetY = value }
                                    }
                                }
                            },
                        ),
                ) { page ->
                    StoryPageContent(
                        group = state.groups[page],
                        itemIndex = if (page == state.groupIndex) state.itemIndex else 0,
                        active = page == state.groupIndex,
                        playing = !effectivePaused,
                        onVideoProgress = { if (page == state.groupIndex) videoProgress = it },
                        onVideoFinished = onNext,
                    )
                }

                val currentContent = state.currentStory?.content
                currentContent?.sharedPost?.let { sharedPost ->
                    StoryFrame(currentContent.viewport) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            PostReshareSticker(
                                post = sharedPost,
                                cardStyle = currentContent.reshareCardStyle,
                                onClick = {
                                    showViewPostTooltip = !showViewPostTooltip
                                },
                                showViewPostTooltip = showViewPostTooltip,
                                onViewPostClick = {
                                    showViewPostTooltip = false
                                    onViewPost?.invoke(sharedPost.id)
                                },
                            )
                        }
                    }
                }

                TopAndBottomScrims()

                val isOwn = state.currentStory?.isOwn == true
                state.currentGroup?.let { group ->
                    StoryTopOverlay(
                        group = group,
                        itemIndex = state.itemIndex,
                        itemProgressFraction = itemProgressFraction,
                        timeLabel = if (retainedPlayback) {
                            stringResource(
                                com.askit.app.R.string.profile_highlight_label,
                            )
                        } else {
                            relativeTimeLabel(state.currentStory)
                        },
                        onClose = onDismiss,
                        modifier = Modifier.align(Alignment.TopCenter),
                        isOwnStory = isOwn && !retainedPlayback,
                        showActions = !retainedPlayback,
                        onAddStory = onAddStory,
                        showFollow = showFollow, canFollow = canFollow, onFollow = onFollow, onMore = onMore,
                    )
                    if (!retainedPlayback) {
                        StoryBottomBar(
                            isLiked = state.currentStory?.id in socialLikes,
                            onLikeClick = onLikeClick,
                            onShareClick = onShareClick,
                            modifier = Modifier.align(Alignment.BottomCenter),
                            isOwnStory = isOwn,
                            canShare = state.currentStory?.isShareable == true,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryPageContent(
    group: StoryGroup,
    itemIndex: Int,
    active: Boolean,
    playing: Boolean,
    onVideoProgress: (Float) -> Unit,
    onVideoFinished: () -> Unit,
) {
    val story = group.stories[itemIndex.coerceIn(group.stories.indices)]
    val content = story.content
    key(story.id) {
        StoryFrame(content.viewport) {
            StoryCanvas(
                mediaUri = content.mediaUri,
                mediaType = content.mediaType,
                solidBackgroundArgb = content.solidBackgroundArgb,
                durationMs = content.durationMs,
                transform = content.transform,
                layers = content.layers,
                readOnly = true,
                trimStartMs = content.trimStartMs,
                trimEndMs = content.trimEndMs,
                videoActive = active,
                videoPlaying = playing,
                videoLoop = false,
                onVideoProgress = onVideoProgress,
                onVideoFinished = onVideoFinished,
                modifier = Modifier.fillMaxSize().semantics {
                    if (content.caption.isNotBlank()) contentDescription = content.caption
                },
            )
        }
    }
}

/** Keeps the pager page and the view model's selected group in sync both ways. */
@Composable
private fun SyncPagerWithState(pagerState: PagerState, groupIndex: Int, groupKeys: List<String>, onSelectGroup: (Int) -> Unit) {
    LaunchedEffect(pagerState, groupKeys) {
        snapshotFlow { pagerState.settledPage }
            .drop(1)
            .collectLatest(onSelectGroup)
    }
    LaunchedEffect(groupIndex) {
        if (pagerState.currentPage != groupIndex && !pagerState.isScrollInProgress) {
            pagerState.scrollToPage(groupIndex)
        }
    }
}

@Composable
private fun TopAndBottomScrims() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent),
                        startY = 0f,
                        endY = size.height * 0.18f,
                    ),
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                        startY = size.height * 0.82f,
                        endY = size.height,
                    ),
                )
            },
    )
}

private val NO_PROGRESS: () -> Float = { 0f }

private fun relativeTimeLabel(story: Story?): String = story?.let {
    DateUtils.getRelativeTimeSpanString(
        it.createdAtMillis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
    ).toString()
} ?: ""
