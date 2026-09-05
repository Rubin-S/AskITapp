package com.askit.app.home.stories

import android.app.Activity
import android.text.format.DateUtils
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.askit.app.home.model.Story
import com.askit.app.story.StoryReshareCardStyle
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
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onViewPost: ((String) -> Unit)? = null,
    onAddStory: (() -> Unit)? = null,
    onStorySeen: ((String) -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.currentStory?.id) {
        state.currentStory?.id?.let { storyId ->
            onStorySeen?.invoke(storyId)
        }
    }

    StoryViewerScreen(
        state = state,
        onNext = { if (!viewModel.advanceToNext()) onDismiss() },
        onPrevious = viewModel::backToPrevious,
        onPauseChanged = viewModel::setPaused,
        onLikeClick = viewModel::toggleLike,
        onSelectGroup = viewModel::selectGroup,
        onDismiss = onDismiss,
        modifier = modifier,
        onViewPost = onViewPost,
        onAddStory = onAddStory,
    )
}

@Composable
internal fun StoryViewerScreen(
    state: StoryViewerUiState,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPauseChanged: (Boolean) -> Unit,
    onLikeClick: () -> Unit,
    onSelectGroup: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onViewPost: ((String) -> Unit)? = null,
    onAddStory: (() -> Unit)? = null,
) {
    ImmersiveStatusBarEffect()

    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var showViewPostTooltip by remember(state.groupIndex, state.itemIndex) { mutableStateOf(false) }
    val density = LocalDensity.current
    val dismissThresholdPx = with(density) { 160.dp.toPx() }
    val scope = rememberCoroutineScope()

    val restartKey = "${state.groupIndex}:${state.itemIndex}"
    val effectivePaused = state.paused || showViewPostTooltip
    val itemProgressFraction =
        if (state.isLoading || state.groups.isEmpty()) 0f else rememberStoryItemProgress(
            restartKey = restartKey,
            paused = effectivePaused,
            onFinished = onNext,
        )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .graphicsLayer { translationY = dragOffsetY },
    ) {
        if (state.isLoading || state.groups.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize())
        } else {
            val pagerState = rememberPagerState(
                initialPage = state.groupIndex,
                pageCount = { state.groups.size },
            )

            SyncPagerWithState(pagerState = pagerState, groupIndex = state.groupIndex, onSelectGroup = onSelectGroup)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                StoryPageImage(
                    group = state.groups[page],
                    itemIndex = if (page == state.groupIndex) state.itemIndex else 0,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .storyTapGestures(
                        onPrevious = {
                            if (showViewPostTooltip) {
                                showViewPostTooltip = false
                            } else {
                                onPrevious()
                            }
                        },
                        onNext = {
                            if (showViewPostTooltip) {
                                showViewPostTooltip = false
                            } else {
                                onNext()
                            }
                        },
                        onHoldChanged = onPauseChanged,
                    )
                    .storyVerticalDrag(
                        onDragDeltaY = { delta ->
                            dragOffsetY = (dragOffsetY + delta).coerceAtLeast(-80f)
                        },
                        onSettle = {
                            if (dragOffsetY >= dismissThresholdPx) {
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
            )

            // Reshared post sticker on top of viewer
            state.currentStory?.sharedPost?.let { sharedPost ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    PostReshareSticker(
                        post = sharedPost,
                        cardStyle = StoryReshareCardStyle.FullCard,
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

            TopAndBottomScrims()

            val isOwn = state.currentStory?.isOwn == true || state.currentGroup?.authorName == "You"
            state.currentGroup?.let { group ->
                StoryTopOverlay(
                    group = group,
                    itemIndex = state.itemIndex,
                    itemProgressFraction = itemProgressFraction,
                    timeLabel = relativeTimeLabel(state.currentStory),
                    onClose = onDismiss,
                    modifier = Modifier.align(Alignment.TopCenter),
                    isOwnStory = isOwn,
                    onAddStory = onAddStory,
                )
                StoryBottomBar(
                    isLiked = state.isCurrentStoryLiked,
                    onLikeClick = onLikeClick,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    isOwnStory = isOwn,
                )
            }
        }
    }
}

@Composable
private fun StoryPageImage(
    group: StoryGroup,
    itemIndex: Int,
) {
    val story = group.stories[itemIndex.coerceIn(group.stories.indices)]
    if (story.sharedPost != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2C3E50),
                            Color(0xFF1E272E),
                            Color(0xFF0A0F14),
                        ),
                    ),
                ),
        )
    } else {
        AsyncImage(
            model = story.mediaUrl,
            contentDescription = story.caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** Keeps the pager page and the view model's selected group in sync both ways. */
@Composable
private fun SyncPagerWithState(
    pagerState: PagerState,
    groupIndex: Int,
    onSelectGroup: (Int) -> Unit,
) {
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
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

private fun relativeTimeLabel(story: Story?): String = story?.let {
    DateUtils.getRelativeTimeSpanString(
        it.createdAtMillis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
    ).toString()
} ?: ""

/**
 * Hides the status bar while the viewer is visible and restores it when it goes away. Safe under
 * Robolectric/local previews where the context is not an activity window.
 */
@Composable
private fun ImmersiveStatusBarEffect() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.statusBars())
        onDispose {
            controller?.show(WindowInsetsCompat.Type.statusBars())
        }
    }
}
