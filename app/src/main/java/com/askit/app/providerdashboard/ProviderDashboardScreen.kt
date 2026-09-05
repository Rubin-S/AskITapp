package com.askit.app.providerdashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.askit.app.R
import com.askit.app.providerdashboard.components.ActiveJobCard
import com.askit.app.providerdashboard.components.IncomingDispatchCard
import com.askit.app.providerdashboard.components.ProviderAlertsCard
import com.askit.app.providerdashboard.components.ProviderAvailabilityHeader
import com.askit.app.providerdashboard.components.ProviderKpiGrid
import com.askit.app.providerdashboard.components.ProviderProBanner
import com.askit.app.providerdashboard.components.ProviderTrustCard

@Composable
fun ProviderDashboardRoute(
    viewModel: ProviderDashboardViewModel,
    onBack: () -> Unit,
    onOpenJob: (String) -> Unit = {},
    onOpenChat: (String) -> Unit = {},
    onEditProfile: () -> Unit = {},
    onUploadWork: () -> Unit = {},
    onManageAlerts: () -> Unit = {},
    onUpgradePro: () -> Unit = {},
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProviderDashboardScreen(
        uiState = uiState,
        onBack = onBack,
        onToggleAvailability = viewModel::toggleAvailability,
        onAcceptRequest = viewModel::acceptRequest,
        onDeclineRequest = viewModel::declineRequest,
        onDismissAlert = viewModel::dismissAlert,
        onClearSnackbar = viewModel::clearAcceptedJobMessage,
        onOpenJob = onOpenJob,
        onOpenChat = onOpenChat,
        onEditProfile = onEditProfile,
        onUploadWork = onUploadWork,
        onManageAlerts = onManageAlerts,
        onUpgradePro = onUpgradePro,
        modifier = modifier,
        lazyListState = lazyListState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDashboardScreen(
    uiState: ProviderDashboardUiState,
    onBack: () -> Unit,
    onToggleAvailability: () -> Unit,
    onAcceptRequest: (String) -> Unit,
    onDeclineRequest: (String) -> Unit,
    onDismissAlert: (String) -> Unit,
    onClearSnackbar: () -> Unit,
    onOpenJob: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onEditProfile: () -> Unit,
    onUploadWork: () -> Unit,
    onManageAlerts: () -> Unit,
    onUpgradePro: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.acceptedJobMessage) {
        uiState.acceptedJobMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onClearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("provider_dashboard_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.provider_dashboard_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("provider_dashboard_back_button"),
                    ) {
                        Icon(
                            painter = painterResource(com.askit.designsystem.R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.provider_dashboard_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("provider_dashboard_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Availability Status Header
            item(key = "availability_header") {
                ProviderAvailabilityHeader(
                    businessName = uiState.businessName,
                    tradeTitle = uiState.tradeTitle,
                    isVerified = uiState.isVerified,
                    availability = uiState.availability,
                    onToggleAvailability = onToggleAvailability,
                )
            }

            // 2. Incoming Dispatch Requests
            if (uiState.availability == ProviderAvailabilityStatus.Online && uiState.incomingRequests.isNotEmpty()) {
                item(key = "section_incoming_title") {
                    SectionHeader(
                        title = stringResource(R.string.provider_section_incoming),
                        badgeText = stringResource(R.string.provider_incoming_count, uiState.incomingRequests.size),
                    )
                }
                items(
                    items = uiState.incomingRequests,
                    key = { "req_${it.id}" },
                ) { request ->
                    IncomingDispatchCard(
                        request = request,
                        onAccept = onAcceptRequest,
                        onDecline = onDeclineRequest,
                    )
                }
            }

            // 3. Active Jobs in Progress
            if (uiState.activeJobs.isNotEmpty()) {
                item(key = "section_active_jobs_title") {
                    SectionHeader(
                        title = stringResource(R.string.provider_section_active_jobs),
                        badgeText = stringResource(R.string.provider_active_jobs_count, uiState.activeJobs.size),
                    )
                }
                items(
                    items = uiState.activeJobs,
                    key = { "job_${it.id}" },
                ) { job ->
                    ActiveJobCard(
                        job = job,
                        onOpenJob = onOpenJob,
                        onOpenChat = onOpenChat,
                    )
                }
            }

            // 4. Operational KPIs Grid
            item(key = "section_kpis_title") {
                SectionHeader(
                    title = stringResource(R.string.provider_section_kpis),
                )
            }
            item(key = "kpi_grid") {
                ProviderKpiGrid(kpis = uiState.kpis)
            }

            // 5. Trust Score & Tier Progress
            item(key = "section_trust_title") {
                SectionHeader(
                    title = stringResource(R.string.provider_section_trust),
                )
            }
            item(key = "trust_card") {
                ProviderTrustCard(
                    trustScore = uiState.trustScore,
                    tierProgress = uiState.tierProgress,
                    onEditProfile = onEditProfile,
                    onUploadWork = onUploadWork,
                )
            }

            // 6. Priority Alerts
            if (uiState.alerts.isNotEmpty()) {
                item(key = "alerts_card") {
                    ProviderAlertsCard(
                        alerts = uiState.alerts,
                        onDismissAlert = onDismissAlert,
                        onManageAlerts = onManageAlerts,
                    )
                }
            }

            // 7. Pro Boost Banner
            if (uiState.showProUpgrade) {
                item(key = "pro_banner") {
                    ProviderProBanner(
                        onUpgradeClick = onUpgradePro,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    badgeText: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (badgeText != null) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
    }
}
