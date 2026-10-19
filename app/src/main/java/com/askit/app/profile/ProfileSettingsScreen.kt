package com.askit.app.profile

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.format.appLocale
import com.askit.app.format.formatCalendarDate
import com.askit.app.home.model.FeedPost
import com.askit.app.home.posts.PostCollectionSheet
import com.askit.app.home.stories.MutedStoriesSheet
import com.askit.app.home.stories.StoryAuthor
import com.askit.app.session.AppLanguage
import com.askit.app.session.AvailabilityStatus
import com.askit.app.session.MessagingAudience
import com.askit.app.session.ProfileAvailability
import com.askit.app.session.SessionProfile
import com.askit.app.session.statusOn
import java.time.LocalDate
import com.askit.designsystem.R as DsR

/** The app language: the device locale, which is the single source of truth for every language label. */
@Composable
@ReadOnlyComposable
internal fun currentAppLanguage(): AppLanguage = AppLanguage.fromLanguageTag(LocalConfiguration.current.locales[0].language)

private enum class SettingsSheet { Language, MessagePrivacy, Muted, Blocked, Availability, Saved, Archived, Terms, Logout, ClearData }

/**
 * Settings. Rows are data: each section lists only what this build can do (see [ProfileCapabilities]),
 * so release never shows a switch or form whose change can't be saved or honoured.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    profile: SessionProfile,
    capabilities: ProfileCapabilities,
    locationServicesEnabled: Boolean,
    mutedStoryAuthors: List<StoryAuthor>,
    blockedAccounts: List<String>,
    savedPosts: List<FeedPost>,
    archivedPosts: List<FeedPost>,
    supportLink: String,
    helpCenterLink: String,
    termsLink: String,
    privacyPolicyLink: String,
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onEditListing: () -> Unit,
    onOpenCreatorHub: (() -> Unit)?,
    onOpenJobRequests: () -> Unit,
    onOpenLink: (String) -> Unit,
    onUpdatePushNotifications: (Boolean) -> Unit,
    onUpdateJobAlerts: (Boolean) -> Unit,
    onUpdateLanguage: (String) -> Unit,
    onUpdateLocationServices: (Boolean) -> Unit,
    onUpdateWhoCanMessage: (String) -> Unit,
    onSaveAvailability: (ProfileAvailability) -> Unit,
    onUnmuteStoryAuthor: (String) -> Unit,
    onOpenPost: (String) -> Unit,
    onRestorePost: (String) -> Unit,
    onLogout: () -> Unit,
    onClearAppData: () -> Unit,
    modifier: Modifier = Modifier,
    previewControls: (@Composable () -> Unit)? = null,
) {
    var sheet by rememberSaveable { mutableStateOf<SettingsSheet?>(null) }
    val edit = capabilities.editProfile
    val language = currentAppLanguage()
    val audience = MessagingAudience.fromStoredValue(profile.whoCanMessage) ?: MessagingAudience.Everyone
    val sections = listOf(
        R.string.settings_section_account to listOfNotNull(
            SettingsRow(R.string.settings_edit_profile, DsR.drawable.ic_edit, EditAccent, "settings_row_edit_profile", tint = LimeTint) {
                onEditProfile()
            }.takeIf { edit },
            // Read-only where the number can't be changed here.
            SettingsRow(
                R.string.settings_phone_number,
                DsR.drawable.ic_phone,
                Blue,
                "settings_row_phone_number",
                value = profile.maskedPhoneNumber,
                onClick = onEditProfile.takeIf { edit },
            ),
        ),
        R.string.settings_section_preferences to listOfNotNull(
            // Nothing posts notifications yet, so these are preview-only until the account API can store them.
            SettingsRow(
                R.string.settings_push_notifications,
                DsR.drawable.ic_notifications,
                Orange,
                "settings_toggle_push",
                checked = profile.pushNotificationsEnabled,
            ) { onUpdatePushNotifications(!profile.pushNotificationsEnabled) }.takeIf { edit },
            SettingsRow(
                R.string.settings_job_service_alerts,
                DsR.drawable.ic_bolt,
                Green,
                "settings_toggle_alerts",
                checked = profile.jobAlertsEnabled,
            ) { onUpdateJobAlerts(!profile.jobAlertsEnabled) }.takeIf { edit },
            SettingsRow(
                R.string.settings_language,
                DsR.drawable.ic_sparkle,
                Violet,
                "settings_row_language",
                stringResource(language.labelRes),
            ) {
                sheet = SettingsSheet.Language
            },
            SettingsRow(
                R.string.settings_location_services,
                DsR.drawable.ic_location,
                Teal,
                "settings_row_location",
                checked = locationServicesEnabled,
            ) { onUpdateLocationServices(!locationServicesEnabled) },
        ),
        R.string.settings_section_privacy_security to listOfNotNull(
            SettingsRow(
                R.string.settings_who_can_message,
                DsR.drawable.ic_lock,
                Slate,
                "settings_row_privacy_message",
                stringResource(audience.labelRes),
            ) {
                sheet = SettingsSheet.MessagePrivacy
            }.takeIf { edit },
            SettingsRow(
                R.string.story_muted_settings,
                DsR.drawable.ic_shield,
                Slate,
                "settings_muted_stories",
                mutedStoryAuthors.size.toString(),
            ) {
                sheet = SettingsSheet.Muted
            },
            SettingsRow(
                R.string.settings_blocked_accounts,
                DsR.drawable.ic_shield,
                DarkBlue,
                "settings_row_blocked_accounts",
                maxOf(blockedAccounts.size, profile.blockedAccountsCount).toString(),
            ) { sheet = SettingsSheet.Blocked },
        ),
        R.string.settings_section_provider to listOfNotNull(
            SettingsRow(
                R.string.settings_service_listing,
                DsR.drawable.ic_work,
                Green,
                "settings_row_service_listing",
                stringResource(if (profile.hasListedService) R.string.settings_status_live else R.string.settings_status_manage),
            ) { onEditListing() }.takeIf { edit },
            SettingsRow(
                R.string.settings_availability,
                DsR.drawable.ic_calendar,
                Teal,
                "settings_row_availability",
                availabilityLabel(profile.availability),
            ) { sheet = SettingsSheet.Availability }.takeIf { edit && profile.listing != null },
            onOpenCreatorHub?.let {
                SettingsRow(R.string.settings_creator_hub, DsR.drawable.ic_sparkle, Violet, "settings_row_creator_hub", onClick = it)
            },
        ),
        R.string.settings_section_seeker to listOf(
            SettingsRow(R.string.settings_saved, DsR.drawable.ic_favorite, Pink, "settings_row_saved") { sheet = SettingsSheet.Saved },
            SettingsRow(R.string.post_archived_title, DsR.drawable.ic_work, Slate, "settings_archived_posts") {
                sheet =
                    SettingsSheet.Archived
            },
            SettingsRow(
                R.string.settings_my_job_requests,
                DsR.drawable.ic_wrench,
                Amber,
                "settings_row_my_jobs",
                onClick = onOpenJobRequests,
            ),
        ),
        R.string.settings_section_support to listOf(
            SettingsRow(R.string.settings_help_center, DsR.drawable.ic_sparkle, Violet, "settings_row_help") { onOpenLink(helpCenterLink) },
            SettingsRow(R.string.settings_contact_support, DsR.drawable.ic_chat, Blue, "settings_row_contact_support") {
                onOpenLink(supportLink)
            },
            SettingsRow(R.string.settings_terms_privacy, DsR.drawable.ic_shield, Slate, "settings_row_terms") {
                sheet = SettingsSheet.Terms
            },
        ),
    )

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("profile_settings_screen"),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { SettingsTopBar(onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            previewControls?.invoke()
            SettingsProfileHeaderCard(
                displayName = profile.displayName.ifBlank { profile.username.removePrefix("@") },
                username = profile.username,
                avatarUrl = profile.avatarUrl,
                onClick = onEditProfile.takeIf { edit },
            )
            sections.filter { it.second.isNotEmpty() }.forEach { (title, rows) -> SettingsSection(stringResource(title), rows) }
            SettingsAccountActions(onLogoutClick = { sheet = SettingsSheet.Logout }, onClearDataClick = { sheet = SettingsSheet.ClearData })
            Spacer(Modifier.height(12.dp))
        }
    }

    val dismiss = { sheet = null }
    when (sheet) {
        SettingsSheet.Language -> SettingsChoiceSheet(
            title = stringResource(R.string.settings_language),
            options = AppLanguage.entries.map { it.storedValue to stringResource(it.labelRes) },
            selected = language.storedValue,
            onSelect = { value ->
                dismiss()
                if (value != language.storedValue) onUpdateLanguage(value)
            },
            onDismiss = dismiss,
        )

        SettingsSheet.MessagePrivacy -> SettingsChoiceSheet(
            title = stringResource(R.string.settings_who_can_message),
            hint = stringResource(R.string.settings_message_privacy_hint),
            options = MessagingAudience.entries.map { it.storedValue to stringResource(it.labelRes) },
            selected = audience.storedValue,
            onSelect = { value ->
                dismiss()
                onUpdateWhoCanMessage(value)
            },
            onDismiss = dismiss,
        )

        SettingsSheet.Muted -> MutedStoriesSheet(mutedStoryAuthors, onDismiss = dismiss, onUnmute = onUnmuteStoryAuthor)

        SettingsSheet.Blocked -> SettingsListSheet(
            title = stringResource(R.string.settings_blocked_accounts),
            items = blockedAccounts,
            emptyMessage = stringResource(
                if (profile.blockedAccountsCount >
                    blockedAccounts.size
                ) {
                    R.string.settings_blocked_unavailable
                } else {
                    R.string.settings_blocked_empty
                },
            ),
            onDismiss = dismiss,
        )

        SettingsSheet.Availability -> AvailabilitySheet(profile.availability, onDismiss = dismiss, onSave = { updated ->
            dismiss()
            onSaveAvailability(updated)
        })

        SettingsSheet.Saved -> PostCollectionSheet(savedPosts, archived = false, onDismiss = dismiss, onOpen = { id ->
            dismiss()
            onOpenPost(id)
        })

        SettingsSheet.Archived -> PostCollectionSheet(archivedPosts, archived = true, onDismiss = dismiss, onOpen = { id ->
            dismiss()
            onOpenPost(id)
        }, onRestore = onRestorePost)

        SettingsSheet.Terms -> SettingsTermsDialog(
            onOpenTerms = { onOpenLink(termsLink) },
            onOpenPrivacyPolicy = { onOpenLink(privacyPolicyLink) },
            onDismiss = dismiss,
        )

        SettingsSheet.Logout -> SettingsConfirmDialog(
            titleRes = R.string.settings_logout_confirm_title,
            messageRes = R.string.settings_logout_confirm_msg,
            confirmRes = R.string.settings_logout,
            confirmTestTag = "settings_confirm_logout",
            onConfirm = onLogout,
            onDismiss = dismiss,
        )

        SettingsSheet.ClearData -> SettingsConfirmDialog(
            titleRes = R.string.settings_clear_data_confirm_title,
            messageRes = R.string.settings_clear_data_confirm_msg,
            confirmRes = R.string.settings_clear_data_action,
            confirmTestTag = "settings_confirm_clear_data",
            onConfirm = onClearAppData,
            onDismiss = dismiss,
        )

        null -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        navigationIcon = {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 1.dp,
                modifier = Modifier.padding(start = 12.dp).size(40.dp),
            ) {
                IconButton(onClick = onBack, modifier = Modifier.fillMaxSize().testTag("settings_back")) {
                    Icon(
                        painter = painterResource(DsR.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.explore_back),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    )
}

/** "Available", "Away from 3 Nov" (scheduled) or "Away until 12 Nov", computed for today. */
@Composable
private fun availabilityLabel(availability: ProfileAvailability): String {
    val locale = appLocale()
    fun day(date: LocalDate) = formatCalendarDate(date, locale)
    return when (val status = availability.statusOn(LocalDate.now())) {
        AvailabilityStatus.Available -> stringResource(R.string.settings_availability_available)

        is AvailabilityStatus.Scheduled -> stringResource(R.string.settings_availability_scheduled, day(status.from))

        is AvailabilityStatus.Away -> status.until?.let { stringResource(R.string.settings_availability_away_until, day(it)) }
            ?: stringResource(R.string.settings_availability_away)
    }
}

/** One settings row. [checked] makes it a switch row; a null [onClick] makes it read-only (no chevron). */
internal class SettingsRow(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val accent: Color,
    val tag: String,
    val value: String? = null,
    val checked: Boolean? = null,
    val tint: Color = Color.White,
    val onClick: (() -> Unit)? = null,
)

// Fixed category accents (white glyphs on mid-tone fills read in light and dark themes alike).
private val EditAccent = Color(0xFF1E232A)
private val LimeTint = Color(0xFF7CE605)
private val Blue = Color(0xFF1E88E5)
private val DarkBlue = Color(0xFF1976D2)
private val Orange = Color(0xFFE65100)
private val Amber = Color(0xFFFB8C00)
private val Green = Color(0xFF7CB342)
private val Teal = Color(0xFF00897B)
private val Violet = Color(0xFF7C4DFF)
private val Slate = Color(0xFF546E7A)
private val Pink = Color(0xFFE91E63)
