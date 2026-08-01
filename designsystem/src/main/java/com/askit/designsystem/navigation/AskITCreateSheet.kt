package com.askit.designsystem.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.askit.designsystem.R
import com.askit.designsystem.theme.AskITTheme

enum class AskITCreateAction {
    PostTask,
    AddService,
    ShareWork,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskITCreateSheet(
    onDismiss: () -> Unit,
    onActionClick: (AskITCreateAction) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var handled by remember { mutableStateOf(false) }

    fun select(action: AskITCreateAction) {
        if (handled) return
        handled = true
        onDismiss()
        onActionClick(action)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            CreateRow(
                iconRes = R.drawable.ic_task,
                titleRes = R.string.create_post_task_title,
                supportingRes = R.string.create_post_task_supporting,
                onClick = { select(AskITCreateAction.PostTask) },
            )
            CreateRow(
                iconRes = R.drawable.ic_service,
                titleRes = R.string.create_add_service_title,
                supportingRes = R.string.create_add_service_supporting,
                onClick = { select(AskITCreateAction.AddService) },
            )
            CreateRow(
                iconRes = R.drawable.ic_share_work,
                titleRes = R.string.create_share_work_title,
                supportingRes = R.string.create_share_work_supporting,
                onClick = { select(AskITCreateAction.ShareWork) },
            )
        }
    }
}

@Composable
private fun CreateRow(
    @DrawableRes iconRes: Int,
    @StringRes titleRes: Int,
    @StringRes supportingRes: Int,
    onClick: () -> Unit,
) {
    val title = stringResource(titleRes)
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        supportingContent = {
            Text(
                text = stringResource(supportingRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingContent = {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClickLabel = title, onClick = onClick),
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PreviewCreateSheet() {
    AskITTheme(darkTheme = false) {
        AskITCreateSheet(onDismiss = {}, onActionClick = {})
    }
}
