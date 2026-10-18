package com.askit.app.createpost

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import java.io.File
import java.util.UUID
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

internal enum class PostPhotoRatio(val x: Int, val y: Int) {
    LANDSCAPE(4, 3), PORTRAIT(3, 4);
    val value: Float get() = x.toFloat() / y
    companion object {
        fun fromSize(width: Int, height: Int): PostPhotoRatio? =
            entries.firstOrNull { width > 0 && height > 0 && abs(width.toLong() * it.y - height.toLong() * it.x) <= maxOf(it.x, it.y) }
    }
}

internal val PostMediaDraft.photoRatio: PostPhotoRatio?
    get() = PostPhotoRatio.fromSize(width, height)

internal data class PostCropSession(
    val sources: List<String>,
    val completed: List<PostMediaDraft> = emptyList(),
    val targetIndex: Int? = null,
    val ratio: PostPhotoRatio? = null,
) {
    val source: String get() = sources[completed.size]
}

/** Decode a bounded, orientation-correct working image, never a full camera-sized bitmap. */
internal fun loadCropBitmap(context: Context, source: String): Bitmap {
    val uri = Uri.parse(source)
    if (Build.VERSION.SDK_INT >= 28) {
        return ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
            val scale = min(1f, 3072f / maxOf(info.size.width, info.size.height))
            decoder.setTargetSize(maxOf(1, (info.size.width * scale).roundToInt()), maxOf(1, (info.size.height * scale).roundToInt()))
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    require(bounds.outWidth > 0 && bounds.outHeight > 0)
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 3072) sample *= 2
    val bitmap = resolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
    } ?: error("Unreadable image")
    val orientation = runCatching {
        resolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }
    }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL
    val matrix = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { setRotate(90f); postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> { setRotate(-90f); postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(-90f)
        }
    }
    if (matrix.isIdentity) return bitmap
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
        if (it !== bitmap) bitmap.recycle()
    }
}

internal data class CropWindow(val left: Float, val top: Float, val width: Float, val height: Float)
internal fun cropWindow(bitmap: Bitmap, ratio: PostPhotoRatio, zoom: Float, centerX: Float, centerY: Float): CropWindow {
    val width = min(bitmap.width.toFloat(), bitmap.height * ratio.value) / zoom.coerceIn(1f, 6f)
    val height = width / ratio.value
    return CropWindow(
        (centerX * bitmap.width - width / 2).coerceIn(0f, bitmap.width - width),
        (centerY * bitmap.height - height / 2).coerceIn(0f, bitmap.height - height),
        width, height,
    )
}

/** Export the same crop used by the preview, with exact integer 4:3 or 3:4 dimensions. */
internal fun savePostCrop(context: Context, bitmap: Bitmap, ratio: PostPhotoRatio, window: CropWindow): PostMediaDraft {
    val units = min(2048f / maxOf(ratio.x, ratio.y), min(window.width / ratio.x, window.height / ratio.y)).toInt().coerceAtLeast(1)
    val output = Bitmap.createBitmap(units * ratio.x, units * ratio.y, Bitmap.Config.ARGB_8888)
    val file = File(File(context.filesDir, "post_photos").apply { mkdirs() }, "${UUID.randomUUID()}.jpg")
    try {
        val canvas = Canvas(output)
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.scale(output.width / window.width, output.height / window.height)
        canvas.translate(-window.left, -window.top)
        canvas.drawBitmap(bitmap, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        file.outputStream().use { check(output.compress(Bitmap.CompressFormat.JPEG, 92, it)) }
        return PostMediaDraft(uri = Uri.fromFile(file).toString(), width = output.width, height = output.height)
    } catch (e: Exception) {
        file.delete()
        throw e
    } finally {
        output.recycle()
    }
}

internal fun deletePendingCrop(photo: PostMediaDraft) {
    val uri = photo.uri?.let(Uri::parse) ?: return
    if (uri.scheme == "file") {
        val file = uri.path?.let(::File) ?: return
        if (file.parentFile?.name == "post_photos") file.delete()
    }
}
