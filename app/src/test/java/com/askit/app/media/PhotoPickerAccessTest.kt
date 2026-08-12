package com.askit.app.media

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PhotoPickerAccessTest {

    @Test
    fun persistsReadAccessForEveryUri_andContinuesAfterOneFailure() {
        val first = Uri.parse("content://photos/first")
        val rejected = Uri.parse("content://photos/rejected")
        val last = Uri.parse("content://photos/last")
        val resolver = RecordingContentResolver(failingUri = rejected)

        persistPhotoPickerReadAccess(resolver, listOf(first, rejected, last))

        assertEquals(listOf(first, rejected, last), resolver.attemptedUris)
        assertEquals(
            listOf(
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            ),
            resolver.attemptedFlags,
        )
    }

    private class RecordingContentResolver(
        private val failingUri: Uri,
    ) : ContentResolver(null) {
        val attemptedUris = mutableListOf<Uri>()
        val attemptedFlags = mutableListOf<Int>()

        override fun takePersistableUriPermission(uri: Uri, modeFlags: Int) {
            attemptedUris += uri
            attemptedFlags += modeFlags
            if (uri == failingUri) {
                throw SecurityException("test rejection")
            }
        }
    }
}
