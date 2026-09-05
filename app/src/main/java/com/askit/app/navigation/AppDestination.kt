package com.askit.app.navigation

import androidx.navigation3.runtime.NavKey
import com.askit.designsystem.navigation.AskITDestination
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppDestination : NavKey {
    val bottomBarDestination: AskITDestination

    @Serializable
    data object Entry : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data object AuthPhone : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data class AuthOtp(val phoneNumber: String) : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data class FormA(val phoneNumber: String) : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data object Home : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data object Explore : AppDestination {
        override val bottomBarDestination = AskITDestination.Explore
    }

    @Serializable
    data object Inbox : AppDestination {
        override val bottomBarDestination = AskITDestination.Inbox
    }

    @Serializable
    data object Profile : AppDestination {
        override val bottomBarDestination = AskITDestination.Profile
    }

    @Serializable
    data object EditProfile : AppDestination {
        override val bottomBarDestination = AskITDestination.Profile
    }

    @Serializable
    data object ProfileSettings : AppDestination {
        override val bottomBarDestination = AskITDestination.Profile
    }

    @Serializable
    data object SearchAreaDestination : AppDestination {
        override val bottomBarDestination = AskITDestination.Explore
    }

    @Serializable
    data object PostTask : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data object ListService : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data object CreatePost : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data object CreatorDashboard : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data object ProviderDashboard : AppDestination {
        override val bottomBarDestination = AskITDestination.Profile
    }

    @Serializable
    data object NewMessage : AppDestination {
        override val bottomBarDestination = AskITDestination.Inbox
    }

    @Serializable
    data class ChatThread(val conversationId: String) : AppDestination {
        override val bottomBarDestination = AskITDestination.Inbox
    }

    @Serializable
    data class JobDetail(val jobId: String) : AppDestination {
        override val bottomBarDestination = AskITDestination.Inbox
    }

    @Serializable
    data class JobVerifyShare(val jobId: String) : AppDestination {
        override val bottomBarDestination = AskITDestination.Inbox
    }

    @Serializable
    data class JobVerifyEnter(val jobId: String) : AppDestination {
        override val bottomBarDestination = AskITDestination.Inbox
    }

    @Serializable
    data class JobReview(val jobId: String) : AppDestination {
        override val bottomBarDestination = AskITDestination.Inbox
    }

    @Serializable
    data class TaskDetail(val taskId: String) : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data class ServiceDetail(val serviceId: String) : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data class UserProfile(val userId: String) : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data object Story : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data class StoryViewer(val startStoryId: String) : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    @Serializable
    data class PostDetail(val postId: String) : AppDestination {
        override val bottomBarDestination = AskITDestination.Home
    }

    companion object {
        fun fromBottomBarDestination(destination: AskITDestination): AppDestination =
            when (destination) {
                AskITDestination.Home -> Home
                AskITDestination.Explore -> Explore
                AskITDestination.Inbox -> Inbox
                AskITDestination.Profile -> Profile
            }
    }
}

internal val TOP_LEVEL_ROUTES = listOf(
    AppDestination.Home,
    AppDestination.Explore,
    AppDestination.Inbox,
    AppDestination.Profile,
)
