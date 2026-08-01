package com.askit.designsystem.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.askit.designsystem.R
import com.askit.designsystem.people.AskITAvatar
import com.askit.designsystem.theme.AskITTheme

enum class AskITDestination {
    Home,
    Explore,
    Inbox,
    Profile,
}

@Composable
fun AskITBottomBar(
    selectedDestination: AskITDestination,
    avatarUrl: String?,
    unreadCount: Int,
    onDestinationClick: (AskITDestination) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val unread = unreadCount.coerceAtLeast(0)

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
    ) {
        IconDestination(
            selected = selectedDestination == AskITDestination.Home,
            labelRes = R.string.nav_home,
            selectedIcon = R.drawable.ic_home_filled,
            unselectedIcon = R.drawable.ic_home_outlined,
            onClick = { onDestinationClick(AskITDestination.Home) },
        )
        IconDestination(
            selected = selectedDestination == AskITDestination.Explore,
            labelRes = R.string.nav_explore,
            selectedIcon = R.drawable.ic_explore_filled,
            unselectedIcon = R.drawable.ic_explore_outlined,
            onClick = { onDestinationClick(AskITDestination.Explore) },
        )
        CreateSlot(onClick = onCreateClick)
        InboxDestination(
            selected = selectedDestination == AskITDestination.Inbox,
            unread = unread,
            onClick = { onDestinationClick(AskITDestination.Inbox) },
        )
        ProfileDestination(
            selected = selectedDestination == AskITDestination.Profile,
            avatarUrl = avatarUrl,
            onClick = { onDestinationClick(AskITDestination.Profile) },
        )
    }
}

@Composable
private fun RowScope.NavSlot(
    selected: Boolean,
    label: String,
    contentDescription: String = label,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { LongPressTooltip(label, content = icon) },
        label = null,
        colors = destinationColors(),
        modifier = Modifier.semantics { this.contentDescription = contentDescription },
    )
}

@Composable
private fun RowScope.IconDestination(
    selected: Boolean,
    @StringRes labelRes: Int,
    @DrawableRes selectedIcon: Int,
    @DrawableRes unselectedIcon: Int,
    onClick: () -> Unit,
) {
    val label = stringResource(labelRes)
    NavSlot(selected = selected, label = label, onClick = onClick) {
        Icon(
            painter = painterResource(if (selected) selectedIcon else unselectedIcon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun RowScope.InboxDestination(
    selected: Boolean,
    unread: Int,
    onClick: () -> Unit,
) {
    val label = stringResource(R.string.nav_inbox)
    val description = if (unread > 0) {
        pluralStringResource(R.plurals.nav_inbox_unread, unread, unread)
    } else {
        label
    }
    val badgeText = when {
        unread <= 0 -> null
        unread > 99 -> "99+"
        else -> unread.toString()
    }
    val iconRes = if (selected) R.drawable.ic_inbox_filled else R.drawable.ic_inbox_outlined

    NavSlot(
        selected = selected,
        label = label,
        contentDescription = description,
        onClick = onClick,
    ) {
        val icon = @Composable {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
        if (badgeText != null) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface,
                    ) {
                        Text(text = badgeText)
                    }
                },
            ) {
                icon()
            }
        } else {
            icon()
        }
    }
}

@Composable
private fun RowScope.ProfileDestination(
    selected: Boolean,
    avatarUrl: String?,
    onClick: () -> Unit,
) {
    val label = stringResource(R.string.nav_profile)
    NavSlot(selected = selected, label = label, onClick = onClick) {
        ProfileAvatar(avatarUrl = avatarUrl, selected = selected)
    }
}

@Composable
private fun ProfileAvatar(avatarUrl: String?, selected: Boolean) {
    val avatar = @Composable {
        AskITAvatar(
            avatarUrl = avatarUrl,
            avatarSize = 28.dp,
            fallbackIconSize = 28.dp,
        )
    }

    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    .padding(2.dp),
                contentAlignment = Alignment.Center,
                content = { avatar() },
            )
        } else {
            avatar()
        }
    }
}

@Composable
private fun RowScope.CreateSlot(onClick: () -> Unit) {
    val label = stringResource(R.string.nav_create)
    Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.Center,
    ) {
        LongPressTooltip(label) {
            SmallFloatingActionButton(
                onClick = onClick,
                modifier = Modifier
                    .offset(y = (-4).dp)
                    .size(48.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = label
                    },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp,
                ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun destinationColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.onSurface,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    indicatorColor = Color.Transparent,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LongPressTooltip(label: String, content: @Composable () -> Unit) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above,
        ),
        tooltip = { PlainTooltip { Text(text = label) } },
        state = rememberTooltipState(),
        content = content,
    )
}

@Preview(showBackground = true, widthDp = 320)
@Preview(showBackground = true, widthDp = 360)
@Preview(showBackground = true, widthDp = 412)
@Composable
private fun PreviewBottomBar() {
    AskITTheme(darkTheme = false) {
        AskITBottomBar(
            selectedDestination = AskITDestination.Home,
            avatarUrl = null,
            unreadCount = 2,
            onDestinationClick = {},
            onCreateClick = {},
        )
    }
}
