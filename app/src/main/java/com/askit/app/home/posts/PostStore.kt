package com.askit.app.home.posts

import com.askit.app.R
import com.askit.app.createpost.PostDraft
import com.askit.app.createpost.PostMediaLayout
import com.askit.app.createpost.isValidPostDraft
import com.askit.app.createpost.photoRatio
import com.askit.app.home.model.FeedItem
import com.askit.app.home.model.FeedPost
import com.askit.app.home.model.PostMedia
import com.askit.app.home.model.PostPoll
import com.askit.app.session.SessionProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class PostReportReason(val labelRes: Int) {
    SPAM(R.string.post_report_spam),
    INAPPROPRIATE(R.string.post_report_inappropriate),
    MISLEADING(R.string.post_report_misleading),
    OTHER(R.string.post_report_other),
}

data class PostAccountState(
    val accountId: String? = null,
    val saved: Set<String> = emptySet(),
    val liked: Set<String> = emptySet(),
    val reports: Map<String, PostReportReason> = emptyMap(),
    val hiddenFromHome: Set<String> = emptySet(),
)

sealed interface PostResult {
    data class Success(val postId: String) : PostResult
    data class Failure(val messageRes: Int) : PostResult
}

data class PostHideFeedback(val accountId: String, val postId: String, val id: String = UUID.randomUUID().toString())

/** Local frontend repository. Public posts and private editor snapshots are kept separately. */
class PostStore(initialPosts: List<FeedPost>) {
    private val mutablePosts = MutableStateFlow(initialPosts.associateBy { it.id })
    val posts = mutablePosts.asStateFlow()
    private val accounts = mutableMapOf<String, PostAccountState>()
    private val mutableAccount = MutableStateFlow(PostAccountState())
    val account = mutableAccount.asStateFlow()
    private val mutableHideFeedback = MutableStateFlow<PostHideFeedback?>(null)
    val hideFeedback = mutableHideFeedback.asStateFlow()
    private val drafts = mutableMapOf<String, PostDraft>()

    fun activateAccount(id: String?) {
        val next = id?.takeIf(String::isNotBlank)
        if (next == account.value.accountId) return
        mutableHideFeedback.value = null
        mutableAccount.value = next?.let { accounts[it] ?: PostAccountState(accountId = it) } ?: PostAccountState()
    }

    fun isCurrentAccount(id: String?) = !id.isNullOrBlank() && id == account.value.accountId
    fun isOwner(post: FeedPost, id: String?) = isCurrentAccount(id) && post.authorId == id
    fun getPost(id: String): FeedPost? = posts.value[id]?.takeIf { !it.archived || isOwner(it, account.value.accountId) }
    fun editableDraft(postId: String, id: String?): PostDraft? = posts.value[postId]?.takeIf { isOwner(it, id) }?.let { drafts[postId] }

    fun project(items: List<FeedItem>): List<FeedItem> = items.mapNotNull { item ->
        if (item is FeedItem.PostItem) {
            posts.value[item.post.id]?.takeUnless {
                it.archived || it.id in account.value.hiddenFromHome
            }?.let(FeedItem::PostItem)
        } else {
            item
        }
    }

    fun publishedPosts(): List<FeedPost> = posts.value.values.filter { it.id in drafts && !it.archived }.reversed()

    fun setPinned(postId: String, id: String?, pinned: Boolean): PostResult {
        val post = ownerPost(postId, id) ?: return rejection(postId, id)
        if (post.archived) return PostResult.Failure(R.string.post_unavailable)
        put(post.copy(pinnedAtMillis = if (pinned) post.pinnedAtMillis ?: System.currentTimeMillis() else null))
        return PostResult.Success(postId)
    }

    fun hideFromHome(postId: String, id: String?): PostResult {
        if (!isCurrentAccount(id)) return PostResult.Failure(R.string.post_action_sign_in)
        val post = getPost(postId) ?: return PostResult.Failure(R.string.post_unavailable)
        if (isOwner(post, id)) return PostResult.Failure(R.string.post_action_not_allowed)
        if (postId in account.value.hiddenFromHome) return PostResult.Success(postId)
        commit(account.value.copy(hiddenFromHome = account.value.hiddenFromHome + postId))
        mutableHideFeedback.value = PostHideFeedback(requireNotNull(id), postId)
        return PostResult.Success(postId)
    }

    fun undoHidden(id: String?, feedback: PostHideFeedback): PostResult {
        if (!isCurrentAccount(id) || id != feedback.accountId) return PostResult.Failure(R.string.post_action_sign_in)
        if (getPost(feedback.postId) == null) return PostResult.Failure(R.string.post_unavailable)
        commit(account.value.copy(hiddenFromHome = account.value.hiddenFromHome - feedback.postId))
        return PostResult.Success(feedback.postId)
    }

    fun consumeHideFeedback(id: String) {
        if (mutableHideFeedback.value?.id == id) mutableHideFeedback.value = null
    }

    fun archivedPosts(): List<FeedPost> = posts.value.values.filter { it.archived && isOwner(it, account.value.accountId) }
    fun savedPosts(): List<FeedPost> = account.value.saved.mapNotNull(::getPost)

    fun publish(draft: PostDraft, id: String?, profile: SessionProfile): PostResult {
        if (!isCurrentAccount(id)) return PostResult.Failure(R.string.post_action_sign_in)
        if (!draft.isValidPostDraft()) return PostResult.Failure(R.string.post_invalid_draft)
        val postId = UUID.randomUUID().toString()
        val post = FeedPost(
            id = postId,
            authorId = id,
            authorName = profile.displayName,
            authorAvatarUrl = profile.avatarUrl,
            content = "",
        ).withDraft(draft)
        drafts[postId] = draft
        mutablePosts.value = posts.value + (postId to post)
        return PostResult.Success(postId)
    }

    fun update(postId: String, draft: PostDraft, id: String?): PostResult {
        val post = ownerPost(postId, id) ?: return rejection(postId, id)
        if (drafts[postId] == null) return PostResult.Failure(R.string.post_edit_unavailable)
        if (!draft.isValidPostDraft()) return PostResult.Failure(R.string.post_invalid_draft)
        drafts[postId] = draft
        put(post.withDraft(draft))
        return PostResult.Success(postId)
    }

    fun toggleSaved(postId: String, id: String?): PostResult {
        if (!isCurrentAccount(id)) return PostResult.Failure(R.string.post_action_sign_in)
        if (getPost(postId) == null) return PostResult.Failure(R.string.post_unavailable)
        val state = account.value
        commit(state.copy(saved = if (postId in state.saved) state.saved - postId else state.saved + postId))
        return PostResult.Success(postId)
    }

    fun toggleLiked(postId: String, id: String?): PostResult {
        if (!isCurrentAccount(id)) return PostResult.Failure(R.string.post_action_sign_in)
        val post = getPost(postId) ?: return PostResult.Failure(R.string.post_unavailable)
        val state = account.value
        val liked = postId in state.liked
        commit(state.copy(liked = if (liked) state.liked - postId else state.liked + postId))
        put(post.copy(likesCount = (post.likesCount + if (liked) -1 else 1).coerceAtLeast(0)))
        return PostResult.Success(postId)
    }

    fun setArchived(postId: String, id: String?, archived: Boolean): PostResult = changeOwner(postId, id) {
        copy(archived = archived, pinnedAtMillis = if (archived) null else pinnedAtMillis)
    }
    fun toggleLikeCount(postId: String, id: String?): PostResult = changeOwner(postId, id) { copy(hideLikeCount = !hideLikeCount) }
    fun toggleComments(postId: String, id: String?): PostResult = changeOwner(postId, id) { copy(commentsEnabled = !commentsEnabled) }

    fun report(postId: String, id: String?, reason: PostReportReason): PostResult {
        if (!isCurrentAccount(id)) return PostResult.Failure(R.string.post_action_sign_in)
        val post = getPost(postId) ?: return PostResult.Failure(R.string.post_unavailable)
        if (isOwner(post, id)) return PostResult.Failure(R.string.post_action_not_allowed)
        if (postId in account.value.reports) return PostResult.Failure(R.string.post_report_duplicate)
        commit(account.value.copy(reports = account.value.reports + (postId to reason)))
        return PostResult.Success(postId)
    }

    private fun changeOwner(postId: String, id: String?, change: FeedPost.() -> FeedPost): PostResult {
        val post = ownerPost(postId, id) ?: return rejection(postId, id)
        put(post.change())
        return PostResult.Success(postId)
    }
    private fun ownerPost(postId: String, id: String?) = posts.value[postId]?.takeIf { isOwner(it, id) }
    private fun rejection(postId: String, id: String?) = PostResult.Failure(
        when {
            !isCurrentAccount(id) -> R.string.post_action_sign_in
            posts.value[postId] == null -> R.string.post_unavailable
            else -> R.string.post_action_not_allowed
        },
    )
    private fun put(post: FeedPost) {
        mutablePosts.value = posts.value + (post.id to post)
    }
    private fun commit(state: PostAccountState) {
        accounts[requireNotNull(state.accountId)] = state
        mutableAccount.value = state
    }
}

private fun FeedPost.withDraft(draft: PostDraft): FeedPost {
    val photos = draft.photos.mapNotNull { it.uri?.trim()?.takeIf(String::isNotEmpty) }
    val ratio = draft.photos.firstOrNull()?.photoRatio?.value ?: 4f / 3f
    return copy(
        content = draft.body.trim(),
        locationLabel = draft.location?.publicAreaLabel,
        media = when {
            draft.mediaLayout == PostMediaLayout.BEFORE_AFTER && photos.size >= 2 -> PostMedia.BeforeAfter(photos[0], photos[1], ratio)
            photos.size == 1 -> PostMedia.SinglePhoto(photos[0], draft.photos.first().imageDescription.takeIf(String::isNotBlank), ratio)
            photos.size > 1 -> PostMedia.Carousel(photos, ratio)
            else -> null
        },
        poll = draft.poll?.let { PostPoll(it.question.trim(), it.options.map(String::trim)) },
    )
}

/** Pinning changes profile order only; hidden Home posts remain accessible here. */
fun orderedProfilePosts(posts: Collection<FeedPost>, authorId: String?): List<FeedPost> = if (authorId.isNullOrBlank()) {
    emptyList()
} else {
    posts.filter { it.authorId == authorId && !it.archived }
        .sortedWith(
            compareByDescending<FeedPost> { it.pinnedAtMillis != null }
                .thenByDescending { it.pinnedAtMillis ?: 0L }.thenByDescending { it.createdAtMillis },
        )
}
