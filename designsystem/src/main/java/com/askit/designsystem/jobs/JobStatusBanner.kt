package com.askit.designsystem.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.askit.designsystem.R

enum class JobBannerTone {
    Neutral,
    Progress,
    Warning,
}

@Composable
fun JobStatusBanner(
    title: String,
    supporting: String,
    tone: JobBannerTone,
    modifier: Modifier = Modifier,
) {
    val colors = when (tone) {
        JobBannerTone.Neutral -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        JobBannerTone.Progress -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        JobBannerTone.Warning -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
    val iconRes = when (tone) {
        JobBannerTone.Neutral -> R.drawable.ic_radio_checked
        JobBannerTone.Progress -> R.drawable.ic_check
        JobBannerTone.Warning -> R.drawable.ic_radio_unchecked
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = colors,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = supporting, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
