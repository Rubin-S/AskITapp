package com.askit.app.preview

import android.content.Context
import android.net.Uri
import com.askit.app.BuildConfig
import com.askit.app.listservice.toServiceListing
import com.askit.app.session.SessionProfile
import com.askit.app.story.StoryLayer
import com.askit.app.story.StoryStickerKind
import com.askit.app.variant.ProfileSnapshotStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest

/** Preview-only durable snapshots and retained media, isolated by stable account ID. */
internal class PreviewProfileStorage(private val context: Context) : ProfileSnapshotStorage {
    private val preferences = context.getSharedPreferences("preview_profiles_v1", Context.MODE_PRIVATE)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun loadRelationships(): Map<String, Set<String>> {
        check(BuildConfig.CLIENT_PREVIEW)
        return context.getSharedPreferences("preview_relationships_v1", Context.MODE_PRIVATE).getString("relationships", null)
            ?.let { json.decodeFromString<Map<String, Set<String>>>(it) }.orEmpty()
    }

    // KTX edit returns Unit; retain the checked commit result so failures never publish success.
    @android.annotation.SuppressLint("UseKtx")
    override fun saveRelationships(relationships: Map<String, Set<String>>) {
        check(BuildConfig.CLIENT_PREVIEW)
        check(
            context.getSharedPreferences("preview_relationships_v1", Context.MODE_PRIVATE).edit()
                .putString("relationships", json.encodeToString(relationships)).commit(),
        )
    }

    override fun load(accountId: String): SessionProfile? {
        check(BuildConfig.CLIENT_PREVIEW)
        return preferences.getString(accountId, null)?.let { saved ->
            json.decodeFromString<SessionProfile>(saved).also { require(it.accountId == accountId) }.let { profile ->
                profile.copy(
                    listing = profile.listingDraft?.toServiceListing(profile.listing?.category ?: profile.listingDraft.categoryId)
                        ?.copy(live = profile.listing?.live ?: profile.availability.available) ?: profile.listing,
                )
            }
        }
    }

    @android.annotation.SuppressLint("UseKtx")
    override fun save(profile: SessionProfile): SessionProfile {
        check(BuildConfig.CLIENT_PREVIEW)
        if (profile.accountId.isBlank()) return profile
        val durable = profile.copy(
            avatarUrl = retain(profile.accountId, profile.avatarUrl),
            bannerUrl = retain(profile.accountId, profile.bannerUrl),
            highlights = profile.highlights.map { collection ->
                collection.copy(
                    stories = collection.stories.map { story ->
                        story.copy(
                            content = story.content.copy(
                                mediaUri = retain(profile.accountId, story.content.mediaUri),
                                layers = story.content.layers.map { layer ->
                                    if (layer is StoryLayer.Sticker && layer.kind == StoryStickerKind.Photo) {
                                        layer.copy(payload = retain(profile.accountId, layer.payload).orEmpty())
                                    } else {
                                        layer
                                    }
                                },
                            ),
                        )
                    },
                )
            },
        )
        check(preferences.edit().putString(profile.accountId, json.encodeToString(durable)).commit())
        val retained = buildSet {
            addAll(listOfNotNull(durable.avatarUrl, durable.bannerUrl))
            durable.highlights.flatMap { it.stories }.forEach { story ->
                story.content.mediaUri?.let(::add)
                story.content.layers.filterIsInstance<StoryLayer.Sticker>()
                    .filter { it.kind == StoryStickerKind.Photo }.forEach { add(it.payload) }
            }
        }
        File(context.filesDir, "preview_profile_media/${hash(profile.accountId)}").listFiles()
            ?.filter { Uri.fromFile(it).toString() !in retained }?.forEach { it.delete() }
        return durable
    }

    @android.annotation.SuppressLint("UseKtx")
    override fun clearAll() {
        check(BuildConfig.CLIENT_PREVIEW)
        check(preferences.edit().clear().commit())
        check(context.getSharedPreferences("preview_relationships_v1", Context.MODE_PRIVATE).edit().clear().commit())
        File(context.filesDir, "preview_profile_media").deleteRecursively()
    }

    private fun retain(accountId: String, value: String?): String? {
        val uri = value?.let(Uri::parse) ?: return null
        if (uri.scheme !in setOf("content", "file")) return value
        val directory = File(context.filesDir, "preview_profile_media/${hash(accountId)}").apply { check(isDirectory || mkdirs()) }
        if (uri.scheme == "file") return Uri.fromFile(retainPreviewFile(File(requireNotNull(uri.path)), directory)).toString()
        val target = File(
            directory,
            hash(value) + if (context.contentResolver.getType(uri)?.startsWith("video/") == true) ".mp4" else ".image",
        )
        if (!target.exists()) {
            val temporary = File(directory, target.name + ".tmp")
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    temporary.outputStream().use { output -> input.copyTo(output) }
                } ?: error("Media unavailable")
                check(temporary.length() > 0 && temporary.renameTo(target))
            } finally {
                temporary.delete()
            }
        }
        return Uri.fromFile(target).toString()
    }

    private fun hash(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}

/** Camera captures start in cache; retained stories must survive cache eviction. */
internal fun retainPreviewFile(source: File, directory: File): File {
    check(source.isFile && source.length() > 0)
    check(directory.isDirectory || directory.mkdirs())
    if (source.canonicalFile.parentFile == directory.canonicalFile) return source
    val name = MessageDigest.getInstance("SHA-256").digest(source.canonicalPath.toByteArray())
        .joinToString("") { "%02x".format(it) }
    val target = File(directory, name + "." + source.extension.ifBlank { "media" })
    if (!target.exists()) {
        val temporary = File(directory, target.name + ".tmp")
        try {
            source.inputStream().use { input -> temporary.outputStream().use { input.copyTo(it) } }
            check(temporary.length() > 0 && temporary.renameTo(target))
        } finally {
            temporary.delete()
        }
    }
    return target
}
