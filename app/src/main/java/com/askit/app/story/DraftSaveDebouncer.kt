package com.askit.app.story

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Coalesces draft saves: [submit] replaces any save still waiting, and the latest one runs once
 * [delayMillis] passes without another submit. [flush] runs the waiting save now.
 *
 * Main-thread only, like the view model that owns it.
 */
class DraftSaveDebouncer internal constructor(
    private val delayMillis: Long,
    private val schedule: (delayMillis: Long, action: () -> Unit) -> Cancellable,
) {
    fun interface Cancellable {
        fun cancel()
    }

    private var pending: (() -> Unit)? = null
    private var timer: Cancellable? = null

    val hasPendingSave: Boolean get() = pending != null

    fun submit(save: () -> Unit) {
        pending = save
        timer?.cancel()
        timer = schedule(delayMillis) { flush() }
    }

    fun flush() {
        timer?.cancel()
        timer = null
        val save = pending ?: return
        pending = null
        save()
    }

    companion object {
        /** Schedules on [scope] (the view model's) with a coroutine delay. */
        internal fun coroutineScheduler(scope: CoroutineScope): (Long, () -> Unit) -> Cancellable = { delayMillis, action ->
            val job: Job = scope.launch {
                delay(delayMillis)
                action()
            }
            Cancellable { job.cancel() }
        }
    }
}

/** Long enough to skip per-frame drag/pinch updates, short enough that a pause saves. */
internal const val STORY_DRAFT_SAVE_DEBOUNCE_MS = 400L
