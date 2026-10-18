package com.askit.app.story

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryDownloadTest {
    @Test fun onlyAndroid8And9NeedTheLegacyStoragePermission() {
        assertTrue(storyDownloadNeedsLegacyPermission(26))
        assertTrue(storyDownloadNeedsLegacyPermission(28))
        assertFalse(storyDownloadNeedsLegacyPermission(29))
        assertFalse(storyDownloadNeedsLegacyPermission(35))
    }

    @Test fun galleryMimeTypeKeepsAResolvedMediaTypeAndFallsBackToTheDraftType() {
        assertEquals("image/png", storyGalleryMimeType("image/png", StoryMediaType.Video))
        assertEquals("video/webm", storyGalleryMimeType("video/webm", StoryMediaType.Photo))
        // Camera captures are file:// URIs, for which the resolver returns null.
        assertEquals("video/mp4", storyGalleryMimeType(null, StoryMediaType.Video))
        assertEquals("image/jpeg", storyGalleryMimeType(null, StoryMediaType.Photo))
        assertEquals("image/jpeg", storyGalleryMimeType("", null))
        // A non-media type is never written into the gallery as-is.
        assertEquals("video/mp4", storyGalleryMimeType("application/octet-stream", StoryMediaType.Video))
    }
}
