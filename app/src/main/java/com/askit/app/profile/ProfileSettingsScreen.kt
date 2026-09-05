package com.askit.app.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.app.R
import com.askit.app.session.ProfileAvailability
import com.askit.app.session.SessionProfile
import com.askit.designsystem.people.AskITAvatar
import com.askit.designsystem.R as DsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    profile: SessionProfile,
    onBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToListService: () -> Unit = {},
    onNavigateToCreatePost: () -> Unit = {},
    onNavigateToJobRequests: () -> Unit = {},
    onNavigateToSupportChat: () -> Unit = {},
    onUpdatePhoneNumber: (String) -> Unit = {},
    onUpdatePushNotifications: (Boolean) -> Unit = {},
    onUpdateJobAlerts: (Boolean) -> Unit = {},
    onUpdateLanguage: (String) -> Unit = {},
    onUpdateLocationServices: (Boolean) -> Unit = {},
    onUpdateWhoCanMessage: (String) -> Unit = {},
    onSaveAvailability: (ProfileAvailability) -> Unit = {},
    onLogout: () -> Unit = {},
    onClearAppData: () -> Unit = {},
) {
    var showPhoneDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageSheet by rememberSaveable { mutableStateOf(false) }
    var showMessagePrivacySheet by rememberSaveable { mutableStateOf(false) }
    var showBlockedAccountsSheet by rememberSaveable { mutableStateOf(false) }
    var showAvailabilitySheet by rememberSaveable { mutableStateOf(false) }
    var showSavedSheet by rememberSaveable { mutableStateOf(false) }
    var showHistorySheet by rememberSaveable { mutableStateOf(false) }
    var showHelpCenterDialog by rememberSaveable { mutableStateOf(false) }
    var showTermsDialog by rememberSaveable { mutableStateOf(false) }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showClearDataDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_settings_screen"),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
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
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(40.dp),
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("settings_back"),
                        ) {
                            Icon(
                                painter = painterResource(DsR.drawable.ic_arrow_back),
                                contentDescription = stringResource(R.string.explore_back),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(48.dp),
                    ) {
                        Icon(
                            painter = painterResource(DsR.drawable.ic_more),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // Profile Summary Header Card
            SettingsProfileHeaderCard(
                displayName = profile.displayName.ifBlank { "Provider" },
                username = profile.username,
                avatarUrl = profile.avatarUrl,
                onClick = onNavigateToEditProfile,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1. ACCOUNT Section
            SettingsSectionHeader(title = stringResource(R.string.settings_section_account))
            SettingsCardContainer {
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_edit_profile),
                    icon = painterResource(DsR.drawable.ic_edit),
                    iconBgColor = Color(0xFF1E232A),
                    iconTint = Color(0xFF7CE605),
                    testTag = "settings_row_edit_profile",
                    onClick = onNavigateToEditProfile,
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_phone_number),
                    icon = painterResource(DsR.drawable.ic_phone),
                    iconBgColor = Color(0xFF1E88E5),
                    value = profile.maskedPhoneNumber,
                    testTag = "settings_row_phone_number",
                    onClick = { showPhoneDialog = true },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. PREFERENCES Section
            SettingsSectionHeader(title = stringResource(R.string.settings_section_preferences))
            SettingsCardContainer {
                SettingsToggleRow(
                    title = stringResource(R.string.settings_push_notifications),
                    icon = painterResource(DsR.drawable.ic_notifications),
                    iconBgColor = Color(0xFFE65100),
                    checked = profile.pushNotificationsEnabled,
                    testTag = "settings_toggle_push",
                    onCheckedChange = onUpdatePushNotifications,
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = stringResource(R.string.settings_job_service_alerts),
                    icon = painterResource(DsR.drawable.ic_bolt),
                    iconBgColor = Color(0xFF7CB342),
                    checked = profile.jobAlertsEnabled,
                    testTag = "settings_toggle_alerts",
                    onCheckedChange = onUpdateJobAlerts,
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_language),
                    icon = painterResource(DsR.drawable.ic_sparkle),
                    iconBgColor = Color(0xFF7C4DFF),
                    value = profile.selectedLanguage,
                    testTag = "settings_row_language",
                    onClick = { showLanguageSheet = true },
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_location_services),
                    icon = painterResource(DsR.drawable.ic_location),
                    iconBgColor = Color(0xFF00897B),
                    value = if (profile.locationServicesEnabled) stringResource(R.string.settings_status_on) else stringResource(R.string.settings_status_off),
                    testTag = "settings_row_location",
                    onClick = { onUpdateLocationServices(!profile.locationServicesEnabled) },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. PRIVACY & SECURITY Section
            SettingsSectionHeader(title = stringResource(R.string.settings_section_privacy_security))
            SettingsCardContainer {
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_who_can_message),
                    icon = painterResource(DsR.drawable.ic_lock),
                    iconBgColor = Color(0xFF546E7A),
                    value = profile.whoCanMessage,
                    testTag = "settings_row_privacy_message",
                    onClick = { showMessagePrivacySheet = true },
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_blocked_accounts),
                    icon = painterResource(DsR.drawable.ic_shield),
                    iconBgColor = Color(0xFF1976D2),
                    value = profile.blockedAccountsCount.toString(),
                    testTag = "settings_row_blocked_accounts",
                    onClick = { showBlockedAccountsSheet = true },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. PROVIDER Section
            SettingsSectionHeader(title = stringResource(R.string.settings_section_provider))
            SettingsCardContainer {
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_service_listing),
                    icon = painterResource(DsR.drawable.ic_work),
                    iconBgColor = Color(0xFF7CB342),
                    value = if (profile.hasListedService) stringResource(R.string.settings_status_live) else "Manage",
                    testTag = "settings_row_service_listing",
                    onClick = onNavigateToListService,
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_availability),
                    icon = painterResource(DsR.drawable.ic_calendar),
                    iconBgColor = Color(0xFF00897B),
                    value = stringResource(R.string.settings_status_manage),
                    testTag = "settings_row_availability",
                    onClick = { showAvailabilitySheet = true },
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_creator_hub),
                    icon = painterResource(DsR.drawable.ic_sparkle),
                    iconBgColor = Color(0xFF7C4DFF),
                    testTag = "settings_row_creator_hub",
                    onClick = onNavigateToCreatePost,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. SEEKER Section
            SettingsSectionHeader(title = stringResource(R.string.settings_section_seeker))
            SettingsCardContainer {
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_saved),
                    icon = painterResource(DsR.drawable.ic_favorite),
                    iconBgColor = Color(0xFFE91E63),
                    testTag = "settings_row_saved",
                    onClick = { showSavedSheet = true },
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_history),
                    icon = painterResource(DsR.drawable.ic_history),
                    iconBgColor = Color(0xFF546E7A),
                    testTag = "settings_row_history",
                    onClick = { showHistorySheet = true },
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_my_job_requests),
                    icon = painterResource(DsR.drawable.ic_wrench),
                    iconBgColor = Color(0xFFFB8C00),
                    testTag = "settings_row_my_jobs",
                    onClick = onNavigateToJobRequests,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. SUPPORT Section
            SettingsSectionHeader(title = stringResource(R.string.settings_section_support))
            SettingsCardContainer {
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_help_center),
                    icon = painterResource(DsR.drawable.ic_sparkle),
                    iconBgColor = Color(0xFF7C4DFF),
                    testTag = "settings_row_help",
                    onClick = { showHelpCenterDialog = true },
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_contact_support),
                    icon = painterResource(DsR.drawable.ic_chat),
                    iconBgColor = Color(0xFF1E88E5),
                    testTag = "settings_row_contact_support",
                    onClick = onNavigateToSupportChat,
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = stringResource(R.string.settings_terms_privacy),
                    icon = painterResource(DsR.drawable.ic_shield),
                    iconBgColor = Color(0xFF546E7A),
                    testTag = "settings_row_terms",
                    onClick = { showTermsDialog = true },
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bottom Buttons
            // Log Out Button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable { showLogoutDialog = true }
                    .testTag("settings_btn_logout"),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.settings_logout),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Clear App Data Button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFEBEE),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable { showClearDataDialog = true }
                    .testTag("settings_btn_clear_data"),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.settings_clear_app_data),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        ),
                        color = Color(0xFFD32F2F),
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Dialogs and Bottom Sheets
    if (showPhoneDialog) {
        var phoneInput by remember { mutableStateOf(profile.phoneNumber) }
        AlertDialog(
            onDismissRequest = { showPhoneDialog = false },
            title = { Text(stringResource(R.string.settings_phone_number)) },
            text = {
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdatePhoneNumber(phoneInput)
                    showPhoneDialog = false
                }) {
                    Text(stringResource(R.string.profile_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhoneDialog = false }) {
                    Text(stringResource(R.string.profile_cancel))
                }
            },
        )
    }

    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
        ) {
            LanguageSelectionContent(
                selectedLanguage = profile.selectedLanguage,
                onSelectLanguage = { lang ->
                    onUpdateLanguage(lang)
                    showLanguageSheet = false
                },
            )
        }
    }

    if (showMessagePrivacySheet) {
        ModalBottomSheet(
            onDismissRequest = { showMessagePrivacySheet = false },
        ) {
            MessagePrivacyContent(
                currentOption = profile.whoCanMessage,
                onSelectOption = { opt ->
                    onUpdateWhoCanMessage(opt)
                    showMessagePrivacySheet = false
                },
            )
        }
    }

    if (showBlockedAccountsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBlockedAccountsSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.settings_blocked_accounts),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No blocked accounts. Accounts you block won't be able to message or view your listings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showAvailabilitySheet) {
        AvailabilitySheet(
            availability = profile.availability,
            onDismiss = { showAvailabilitySheet = false },
            onSave = { updated: ProfileAvailability ->
                onSaveAvailability(updated)
                showAvailabilitySheet = false
            },
        )
    }

    if (showSavedSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSavedSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.settings_saved),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "You haven't saved any tasks or professionals yet. Tap the bookmark or heart icon on listings to save them.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.settings_history),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your completed service history and past task applications will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showHelpCenterDialog) {
        AlertDialog(
            onDismissRequest = { showHelpCenterDialog = false },
            title = { Text(stringResource(R.string.settings_help_center)) },
            text = {
                Text("Need help with AskIT? Explore guides on posting tasks, listing services, receiving verified payouts, and contacting local specialists.")
            },
            confirmButton = {
                TextButton(onClick = { showHelpCenterDialog = false }) {
                    Text("OK")
                }
            },
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text(stringResource(R.string.settings_terms_privacy)) },
            text = {
                Text("AskIT connects verified local service providers and task seekers. All community interactions adhere to our community safety standards, data encryption, and transparent fee policies.")
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Close")
                }
            },
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.settings_logout_confirm_title)) },
            text = { Text(stringResource(R.string.settings_logout_confirm_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text(stringResource(R.string.settings_logout), color = Color(0xFFD32F2F))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.profile_cancel))
                }
            },
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text(stringResource(R.string.settings_clear_data_confirm_title)) },
            text = { Text(stringResource(R.string.settings_clear_data_confirm_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    showClearDataDialog = false
                    onClearAppData()
                }) {
                    Text(stringResource(R.string.settings_clear_data_action), color = Color(0xFFD32F2F))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text(stringResource(R.string.profile_cancel))
                }
            },
        )
    }
}

@Composable
private fun SettingsProfileHeaderCard(
    displayName: String,
    username: String,
    avatarUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("settings_profile_header_card"),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            AskITAvatar(
                avatarUrl = avatarUrl,
                avatarSize = 56.dp,
                fallbackIconSize = 48.dp,
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "@${username.removePrefix("@")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                painter = painterResource(DsR.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 12.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsCardContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    icon: Painter,
    iconBgColor: Color,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.White,
    value: String? = null,
    testTag: String? = null,
    onClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconBgColor, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (!value.isNullOrBlank()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 6.dp),
            )
        }
        Icon(
            painter = painterResource(DsR.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    icon: Painter,
    iconBgColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.White,
    testTag: String? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconBgColor, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF76D700),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        )
    }
}

@Composable
private fun SettingsDivider(
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(
        modifier = modifier.padding(start = 66.dp),
        thickness = 0.75.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    )
}

@Composable
private fun LanguageSelectionContent(
    selectedLanguage: String,
    onSelectLanguage: (String) -> Unit,
) {
    val languages = listOf("English", "Tamil (தமிழ்)", "Hindi (हिन्दी)", "Telugu (తెలుగు)", "Kannada (ಕನ್ನಡ)")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_language),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp),
        )
        languages.forEach { lang ->
            val simpleName = lang.substringBefore(" (")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectLanguage(simpleName) }
                    .padding(vertical = 12.dp),
            ) {
                RadioButton(
                    selected = selectedLanguage.startsWith(simpleName, ignoreCase = true),
                    onClick = { onSelectLanguage(simpleName) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = lang,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MessagePrivacyContent(
    currentOption: String,
    onSelectOption: (String) -> Unit,
) {
    val options = listOf("Everyone", "Contacts only", "Verified users only")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_who_can_message),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp),
        )
        options.forEach { opt ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectOption(opt) }
                    .padding(vertical = 12.dp),
            ) {
                RadioButton(
                    selected = currentOption.equals(opt, ignoreCase = true),
                    onClick = { onSelectOption(opt) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = opt,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
