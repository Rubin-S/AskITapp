package com.askit.app.preview

import org.junit.Assert.assertEquals
import org.junit.Test

/** Preview storage lives in the debug/preview-only demo source set, so its tests run on debug only. */
class PreviewProfileStorageTest {
    @Test fun cameraCacheMediaIsCopiedBeforeTheSourceCanBeEvicted() {
        val directory = java.nio.file.Files.createTempDirectory("highlight-test").toFile()
        try {
            val source = java.io.File(directory, "camera.jpg").apply { writeText("photo bytes") }
            val retained = retainPreviewFile(source, java.io.File(directory, "durable"))
            source.delete()
            assertEquals("photo bytes", retained.readText())
            assertEquals(retained, retainPreviewFile(retained, retained.parentFile!!))
        } finally { directory.deleteRecursively() }
    }
}
