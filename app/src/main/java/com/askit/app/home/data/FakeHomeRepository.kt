package com.askit.app.home.data

import com.askit.app.home.model.FeedItem
import com.askit.app.home.model.FeedPost
import com.askit.app.home.model.PersonPreview
import com.askit.app.home.model.PostMedia
import com.askit.app.home.model.PostPoll
import com.askit.app.home.model.ServicePreview
import com.askit.app.home.model.Story
import com.askit.app.task.InMemoryTaskRepository
import com.askit.app.task.TaskRepository
import com.askit.app.task.toTaskPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FakeHomeRepository(
    private val taskRepository: TaskRepository = InMemoryTaskRepository(),
    initialStoriesList: List<Story> = defaultStories,
) : HomeRepository {

    companion object {
        val defaultStories = listOf(
            Story(
                id = "story-1",
                authorName = "faraan_141",
                authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                mediaUrl = "https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=800",
                caption = "Finished full home electrical rewiring",
                createdAtMillis = System.currentTimeMillis() - 3600_000,
                isSeen = false,
            ),
            Story(
                id = "story-2",
                authorName = "nitpuducherry",
                authorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
                mediaUrl = "https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=800",
                caption = "Installing custom oak kitchen cabinets",
                createdAtMillis = System.currentTimeMillis() - 7200_000,
                isSeen = false,
            ),
            Story(
                id = "story-3",
                authorName = "amy_deliy",
                authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300",
                mediaUrl = "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=800",
                caption = "HVAC diagnostic and seasonal tune-up",
                createdAtMillis = System.currentTimeMillis() - 10800_000,
                isSeen = false,
            ),
            Story(
                id = "story-4",
                authorName = "alex_electric",
                authorAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300",
                mediaUrl = "https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=800",
                caption = "Commercial panel upgrade",
                createdAtMillis = System.currentTimeMillis() - 14400_000,
                isSeen = true,
            ),
            Story(
                id = "story-5",
                authorName = "chloe_craft",
                authorAvatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=300",
                mediaUrl = "https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=800",
                caption = "Modern walnut furniture showcase",
                createdAtMillis = System.currentTimeMillis() - 18000_000,
                isSeen = true,
            ),
        )
    }

    private val storiesFlow = MutableStateFlow(initialStoriesList)

    override fun getStories(): Flow<List<Story>> = storiesFlow

    override fun addStory(story: Story) {
        storiesFlow.value = listOf(story) + storiesFlow.value.filter { it.id != story.id }
    }

    override fun markStorySeen(storyId: String) {
        storiesFlow.value = storiesFlow.value.map { story ->
            if (story.id == storyId) story.copy(isSeen = true) else story
        }
    }

    private val post1 = FeedItem.PostItem(
        post = FeedPost(
            id = "post-1",
            authorName = "Elena Vance",
            authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300",
            locationLabel = "Seattle, WA",
            content = "Completed master bathroom tiling renovation ahead of schedule. Always inspect waterproofing membrane and seals before laying grout!",
            media = PostMedia.SinglePhoto(
                url = "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=800",
                contentDescription = "Modern renovated master bathroom with glass shower",
            ),
            createdAtMillis = System.currentTimeMillis() - 1800_000,
            likesCount = 24,
            commentsCount = 5,
        ),
    )

    private val post2 = FeedItem.PostItem(
        post = FeedPost(
            id = "post-2",
            authorName = "Marcus Reed",
            authorAvatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=300",
            locationLabel = "Portland, OR",
            content = "Custom live-edge dining table built from local black walnut. Here is the step-by-step milling, joinery, and natural beeswax finish.",
            media = PostMedia.Carousel(
                urls = listOf(
                    "https://images.unsplash.com/photo-1533090161767-e6ffed986c88?w=800",
                    "https://images.unsplash.com/photo-1540574163026-643ea20ade25?w=800",
                    "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?w=800",
                ),
            ),
            createdAtMillis = System.currentTimeMillis() - 7200_000,
            likesCount = 48,
            commentsCount = 12,
        ),
    )

    private val post3 = FeedItem.PostItem(
        post = FeedPost(
            id = "post-3",
            authorName = "Sarah Jenkins",
            authorAvatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=300",
            locationLabel = "Denver, CO",
            content = "Swipe the slider to see the kitchen makeover! Replaced dated laminate cabinets with painted shaker doors and quartz counters.",
            media = PostMedia.BeforeAfter(
                beforeUrl = "https://images.unsplash.com/photo-1484154218962-a197022b5858?w=800",
                afterUrl = "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=800",
            ),
            createdAtMillis = System.currentTimeMillis() - 14400_000,
            likesCount = 89,
            commentsCount = 19,
        ),
    )

    private val serviceSection = FeedItem.ServiceSection(
        id = "popular-services",
        services = listOf(
            ServicePreview(
                id = "srv-1",
                title = "Emergency Plumbing & Drain Cleaning",
                category = "Plumbing",
                providerName = "Apex Plumbing Co.",
                startingPriceLabel = "From $75",
                rating = 4.9,
                providerAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
            ),
            ServicePreview(
                id = "srv-2",
                title = "Residential Electrical Repairs & Panels",
                category = "Electrical",
                providerName = "Bright Solutions",
                startingPriceLabel = "From $90",
                rating = 4.8,
                providerAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300",
            ),
            ServicePreview(
                id = "srv-3",
                title = "Custom Cabinetry & Woodworking",
                category = "Carpentry",
                providerName = "Heritage Woodworks",
                startingPriceLabel = "From $150",
                rating = 4.95,
                providerAvatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=300",
            ),
        ),
    )

    private val page2Items = listOf(
        FeedItem.PostItem(
            post = FeedPost(
                id = "post-4",
                authorName = "David Miller",
                authorAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300",
                locationLabel = "Austin, TX",
                content = "Question for fellow woodworkers and deck builders: what is your go-to outdoor wood protection for hot and humid climates?",
                poll = PostPoll(
                    question = "Best outdoor wood finish for high-humidity climates?",
                    options = listOf(
                        "Marine Spar Varnish",
                        "Penetrating Danish Oil",
                        "100% Pure Tung Oil",
                        "Water-based Acrylic Sealer",
                    ),
                    closingSummary = "Poll closes in 24h • 52 responses",
                ),
                createdAtMillis = System.currentTimeMillis() - 21600_000,
                likesCount = 31,
                commentsCount = 14,
            ),
        ),
        FeedItem.PeopleSection(
            id = "top-providers",
            people = listOf(
                PersonPreview(
                    id = "pro-1",
                    name = "Lucas Scott",
                    trade = "Master Electrician",
                    rating = 4.95,
                    completedJobsCount = 142,
                    locationLabel = "North District",
                    avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300",
                ),
                PersonPreview(
                    id = "pro-2",
                    name = "Amara Chen",
                    trade = "Interior Architect",
                    rating = 4.92,
                    completedJobsCount = 88,
                    locationLabel = "Downtown",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                ),
                PersonPreview(
                    id = "pro-3",
                    name = "Carlos Rivera",
                    trade = "Master Plumber",
                    rating = 4.88,
                    completedJobsCount = 196,
                    locationLabel = "Westside",
                    avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
                ),
            ),
        ),
        FeedItem.PostItem(
            post = FeedPost(
                id = "post-5",
                authorName = "Liam O'Connor",
                authorAvatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=300",
                locationLabel = "Chicago, IL",
                content = "Installed high-efficiency hybrid heat pump water heater today. Cut energy consumption by over 60% compared to conventional tanks.",
                media = PostMedia.SinglePhoto(
                    url = "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=800",
                    contentDescription = "New heat pump installation in residential utility room",
                ),
                createdAtMillis = System.currentTimeMillis() - 28800_000,
                likesCount = 17,
                commentsCount = 4,
            ),
        ),
        FeedItem.PostItem(
            post = FeedPost(
                id = "post-6",
                authorName = "Chloe Bennett",
                authorAvatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=300",
                locationLabel = "San Francisco, CA",
                content = "Three-piece custom walnut credenza completed for an architect's studio. Clean brass hardware and soft-close under-mount slides.",
                media = PostMedia.Carousel(
                    urls = listOf(
                        "https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=800",
                        "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=800",
                        "https://images.unsplash.com/photo-1538688525198-9b88f6f53126?w=800",
                    ),
                ),
                createdAtMillis = System.currentTimeMillis() - 36000_000,
                likesCount = 63,
                commentsCount = 8,
            ),
        ),
    )

    private val page3Items = listOf(
        FeedItem.PostItem(
            post = FeedPost(
                id = "post-7",
                authorName = "Alex Vance",
                authorAvatarUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=300",
                locationLabel = "Boston, MA",
                content = "Living room hardwood floor refinishing before vs after. Sanded down 30-year-old scratches, stained natural oak, and sealed with 3 coats of polyurethane.",
                media = PostMedia.BeforeAfter(
                    beforeUrl = "https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=800",
                    afterUrl = "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800",
                ),
                createdAtMillis = System.currentTimeMillis() - 43200_000,
                likesCount = 104,
                commentsCount = 27,
            ),
        ),
        FeedItem.PostItem(
            post = FeedPost(
                id = "post-8",
                authorName = "Maya Lin",
                authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                locationLabel = "San Diego, CA",
                content = "Drought-tolerant backyard xeriscaping completed. Installed drip irrigation, native sage, and decomposed granite pathway.",
                media = PostMedia.SinglePhoto(
                    url = "https://images.unsplash.com/photo-1558904541-efa8c4a08931?w=800",
                    contentDescription = "Modern drought-tolerant landscaped garden",
                ),
                createdAtMillis = System.currentTimeMillis() - 50400_000,
                likesCount = 52,
                commentsCount = 9,
            ),
        ),
    )

    override fun getFeed(page: Int): Flow<List<FeedItem>> = when (page) {
        1 -> taskRepository.tasks.map { currentTasks ->
            val taskPreviews = currentTasks.map { it.toTaskPreview() }
            buildList {
                add(post1)
                if (taskPreviews.isNotEmpty()) {
                    add(FeedItem.TaskSection(id = "featured-tasks", tasks = taskPreviews))
                }
                add(post2)
                add(post3)
                add(serviceSection)
            }
        }
        2 -> flowOf(page2Items)
        3 -> flowOf(page3Items)
        else -> flowOf(emptyList())
    }
}

fun getFeedPostById(postId: String): FeedPost {
    val allPosts = listOf(
        FeedPost(
            id = "post-1",
            authorName = "Elena Vance",
            authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300",
            locationLabel = "Seattle, WA",
            content = "Completed master bathroom tiling renovation ahead of schedule. Always inspect waterproofing membrane and seals before laying grout!",
            media = PostMedia.SinglePhoto(
                url = "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=800",
                contentDescription = "Modern renovated master bathroom with glass shower",
            ),
            createdAtMillis = System.currentTimeMillis() - 1800_000,
            likesCount = 24,
            commentsCount = 5,
        ),
        FeedPost(
            id = "post-2",
            authorName = "Marcus Reed",
            authorAvatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=300",
            locationLabel = "Portland, OR",
            content = "Custom live-edge dining table built from local black walnut. Here is the step-by-step milling, joinery, and natural beeswax finish.",
            media = PostMedia.Carousel(
                urls = listOf(
                    "https://images.unsplash.com/photo-1533090161767-e6ffed986c88?w=800",
                    "https://images.unsplash.com/photo-1540574163026-643ea20ade25?w=800",
                    "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?w=800",
                ),
            ),
            createdAtMillis = System.currentTimeMillis() - 7200_000,
            likesCount = 48,
            commentsCount = 12,
        ),
        FeedPost(
            id = "post-3",
            authorName = "Sarah Jenkins",
            authorAvatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=300",
            locationLabel = "Denver, CO",
            content = "Swipe the slider to see the kitchen makeover! Replaced dated laminate cabinets with painted shaker doors and quartz counters.",
            media = PostMedia.BeforeAfter(
                beforeUrl = "https://images.unsplash.com/photo-1484154218962-a197022b5858?w=800",
                afterUrl = "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=800",
            ),
            createdAtMillis = System.currentTimeMillis() - 14400_000,
            likesCount = 89,
            commentsCount = 19,
        ),
    )
    return allPosts.find { it.id == postId } ?: allPosts.first()
}
