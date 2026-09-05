package com.askit.app.providerdashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.askit.app.R
import com.askit.app.providerdashboard.ProviderOperatingKpis

@Composable
fun ProviderKpiGrid(
    kpis: ProviderOperatingKpis,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("provider_kpi_grid"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ProviderSingleKpiCard(
                headline = stringResource(R.string.provider_kpi_todays_jobs),
                value = kpis.todayJobsCount.toString(),
                supporting = stringResource(R.string.provider_kpi_todays_jobs_delta, kpis.todayJobsDelta),
                testTag = "provider_kpi_todays_jobs",
                modifier = Modifier.weight(1f),
            )
            ProviderSingleKpiCard(
                headline = stringResource(R.string.provider_kpi_pending_live),
                value = kpis.pendingLiveCount.toString(),
                supporting = stringResource(R.string.provider_kpi_pending_live_sub),
                testTag = "provider_kpi_pending_live",
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ProviderSingleKpiCard(
                headline = stringResource(R.string.provider_kpi_response_rate),
                value = "${kpis.responseRatePercent}%",
                supporting = stringResource(R.string.provider_kpi_response_rate_sub),
                testTag = "provider_kpi_response_rate",
                modifier = Modifier.weight(1f),
            )
            ProviderSingleKpiCard(
                headline = stringResource(R.string.provider_kpi_avg_response),
                value = stringResource(R.string.provider_kpi_avg_response_value, kpis.avgResponseMinutes),
                supporting = "⚡ Instant reply",
                testTag = "provider_kpi_avg_response",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ProviderSingleKpiCard(
    headline: String,
    value: String,
    supporting: String,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = headline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = supporting,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
