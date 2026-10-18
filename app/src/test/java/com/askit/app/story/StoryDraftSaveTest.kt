package com.askit.app.story

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryDraftSaveTest {
    /** Manual clock: records scheduled saves and runs them when the test says time passed. */
    private class FakeScheduler {
        val scheduled = mutableListOf<Pair<Long, () -> Unit>>()
        var cancelled = 0
        fun schedule(delayMillis: Long, action: () -> Unit): DraftSaveDebouncer.Cancellable {
            val entry = delayMillis to action
            scheduled += entry
            return DraftSaveDebouncer.Cancellable {
                if (scheduled.remove(entry)) cancelled++
            }
        }
        fun elapse() {
            val due = scheduled.toList()
            scheduled.clear()
            due.forEach { it.second() }
        }
    }

    @Test fun aBurstOfSubmitsRunsOnlyTheLastSave() {
        val scheduler = FakeScheduler()
        val debouncer = DraftSaveDebouncer(400, scheduler::schedule)
        val saved = mutableListOf<Int>()
        repeat(5) { i -> debouncer.submit { saved += i } }
        assertEquals(1, scheduler.scheduled.size)
        assertEquals(4, scheduler.cancelled)
        assertEquals(400L, scheduler.scheduled.single().first)
        assertTrue(saved.isEmpty())
        scheduler.elapse()
        assertEquals(listOf(4), saved)
        assertFalse(debouncer.hasPendingSave)
    }

    @Test fun flushSavesImmediatelyAndOnlyOnce() {
        val scheduler = FakeScheduler()
        val debouncer = DraftSaveDebouncer(400, scheduler::schedule)
        var saves = 0
        debouncer.submit { saves++ }
        debouncer.flush()
        debouncer.flush()
        scheduler.elapse()
        assertEquals(1, saves)
    }

    @Test fun dragFramesDoNotRewriteTheSavedDraftUntilThePause() {
        val scheduler = FakeScheduler()
        val handle = SavedStateHandle()
        val viewModel = StoryViewModel(handle, scheduler::schedule)
        repeat(30) { frame -> viewModel.updateTransform(frame.toFloat(), 0f, 1f) }
        assertNull(handle.get<String>("story_transform"))
        scheduler.elapse()
        val saved = handle.get<String>("story_transform")
        assertNotNull(saved)
        assertTrue(saved!!.contains("29"))
    }

    @Test fun flushingWritesTheLatestDraftSoARestoredEditorMatches() {
        val scheduler = FakeScheduler()
        val handle = SavedStateHandle()
        val viewModel = StoryViewModel(handle, scheduler::schedule)
        viewModel.updateCreateText("Hello")
        viewModel.setAudience(StoryAudience.entries.last())
        viewModel.flushDraftSave()
        val restored = StoryViewModel(handle, scheduler::schedule).formState.value
        assertEquals("Hello", restored.createTextDraft)
        assertEquals(StoryAudience.entries.last(), restored.audience)
    }
}
