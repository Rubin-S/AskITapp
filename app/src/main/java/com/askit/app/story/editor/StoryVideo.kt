package com.askit.app.story.editor

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import com.askit.app.R
import com.askit.app.story.STORY_MAX_DURATION_MS
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
internal fun StoryVideo(
    uri: String?,
    trimStartMs: Long,
    trimEndMs: Long?,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    active: Boolean = true,
    playing: Boolean = true,
    loop: Boolean = true,
    onProgress: (Float) -> Unit = {},
    onFinished: () -> Unit = {},
    playerFactory: (Context) -> Player = { context ->
        ExoPlayer.Builder(context)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    },
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleState by lifecycle.currentStateAsState()
    val foreground = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
    val latestPlaying by rememberUpdatedState(playing)
    val latestLoop by rememberUpdatedState(loop)
    val latestProgress by rememberUpdatedState(onProgress)
    val latestFinished by rememberUpdatedState(onFinished)
    val latestFactory by rememberUpdatedState(playerFactory)
    var player by remember { mutableStateOf<Player?>(null) }
    var retry by remember(uri, trimStartMs, trimEndMs) { mutableIntStateOf(0) }
    var position by rememberSaveable(
        uri, trimStartMs, trimEndMs, active,
        saver = Saver<MutableLongState, Long>(
            save = { player?.currentPosition ?: it.longValue },
            restore = { mutableLongStateOf(it) },
        ),
    ) { mutableLongStateOf(0L) }
    var loading by remember(uri, trimStartMs, trimEndMs, retry) { mutableStateOf(true) }
    var failed by remember(uri, trimStartMs, trimEndMs, retry) { mutableStateOf(false) }
    var ended by remember(uri, trimStartMs, trimEndMs, active, retry) { mutableStateOf(false) }
    var completionDelivered by remember(uri, trimStartMs, trimEndMs, active, retry) { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    DisposableEffect(lifecycle, uri, trimStartMs, trimEndMs, active, retry) {
        var disposed = false
        var listener: Player.Listener? = null

        fun release() {
            val current = player ?: return
            position = current.currentPosition
            listener?.let(current::removeListener)
            player = null
            isPlaying = false
            current.release()
        }

        fun prepare() {
            if (!active || player != null || disposed || failed) return
            if (uri.isNullOrBlank()) {
                failed = true
                loading = false
                return
            }
            loading = true
            try {
                val current = latestFactory(context)
                player = current
                val events = object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (disposed || player !== current) return
                        loading = playbackState == Player.STATE_IDLE || playbackState == Player.STATE_BUFFERING
                        ended = playbackState == Player.STATE_ENDED
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        if (disposed || player !== current) return
                        failed = true
                        loading = false
                    }

                    override fun onIsPlayingChanged(playing: Boolean) {
                        if (!disposed && player === current) isPlaying = playing
                    }
                }
                listener = events
                current.addListener(events)
                val start = trimStartMs.coerceAtLeast(0)
                val end = (trimEndMs ?: (start + STORY_MAX_DURATION_MS))
                    .coerceIn(start + 1, start + STORY_MAX_DURATION_MS)
                current.setMediaItem(MediaItem.Builder().setUri(uri).setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(start).setEndPositionMs(end).build(),
                ).build())
                current.seekTo(position)
                current.repeatMode = if (latestLoop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                current.playWhenReady = latestPlaying && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
                current.prepare()
            } catch (_: Exception) {
                failed = true
                loading = false
                release()
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> prepare()
                Lifecycle.Event.ON_RESUME -> player?.playWhenReady = latestPlaying && !failed
                Lifecycle.Event.ON_PAUSE -> player?.pause()
                Lifecycle.Event.ON_STOP -> release()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) prepare()
        onDispose {
            disposed = true
            lifecycle.removeObserver(observer)
            release()
        }
    }

    LaunchedEffect(player, loop) {
        player?.repeatMode = if (loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }
    LaunchedEffect(player, active, playing, foreground, failed) {
        player?.playWhenReady = active && playing && foreground && !failed
    }
    LaunchedEffect(player) {
        val current = player ?: return@LaunchedEffect
        while (isActive && player === current) {
            val duration = current.duration
            latestProgress(if (duration > 0) (current.currentPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f)
            delay(100)
        }
    }
    LaunchedEffect(ended, active, playing, foreground, loop) {
        if (ended && active && playing && foreground && !loop && !completionDelivered) {
            completionDelivered = true
            latestProgress(1f)
            latestFinished()
        }
    }

    Box(
        modifier.fillMaxSize().background(Color.Black)
            .then(if (isPlaying && active && playing && foreground) Modifier.keepScreenOn() else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        ContentFrame(
            player = player,
            modifier = contentModifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            // The authored canvas is scaled inside previews and the editor's crop transform.
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
        )
        if (failed) {
            Column(
                Modifier.padding(24.dp).testTag("story_video_error"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(stringResource(R.string.story_video_unavailable), color = Color.White)
                TextButton(onClick = { retry++ }) {
                    Text(stringResource(R.string.explore_retry), color = Color.White)
                }
            }
        } else if (active && loading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.testTag("story_video_loading"))
        }
    }
}
