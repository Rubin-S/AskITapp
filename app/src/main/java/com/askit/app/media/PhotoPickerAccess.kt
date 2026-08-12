package com.askit.app.media

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri

/**
 * Keeps Photo Picker read access for every selected URI without making one bad URI discard the
 * rest of a multi-selection.
 */
internal fun persistPhotoPickerReadAccess(
    contentResolver: ContentResolver,
    uris: Iterable<Uri>,
) {
    uris.forEach { uri ->
        runCatching {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
    }
}
