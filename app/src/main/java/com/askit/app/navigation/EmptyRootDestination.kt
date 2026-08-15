package com.askit.app

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.askit.designsystem.empty.AskITEmptyState

@Composable
internal fun EmptyRootDestination(
    @DrawableRes iconRes: Int,
    @StringRes titleRes: Int,
    @StringRes supportingRes: Int,
    @StringRes actionRes: Int? = null,
    onAction: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        AskITEmptyState(
            iconRes = iconRes,
            title = stringResource(titleRes),
            supporting = stringResource(supportingRes),
            actionLabel = actionRes?.let { stringResource(it) },
            onAction = onAction,
        )
    }
}
