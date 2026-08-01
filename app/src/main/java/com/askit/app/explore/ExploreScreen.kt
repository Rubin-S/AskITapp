package com.askit.app.explore

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.askit.app.R

@Composable
fun ExploreRoute(
    viewModel: ExploreViewModel,
    onSearchAreaClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ExploreScreen(
        query = uiState.query,
        searchArea = uiState.searchArea,
        onQueryChanged = viewModel::onQueryChanged,
        onQueryCleared = viewModel::onQueryCleared,
        onSearchAreaClick = onSearchAreaClick,
    )
}

@Composable
fun ExploreScreen(
    query: String,
    searchArea: ExploreSearchArea,
    onQueryChanged: (String) -> Unit,
    onQueryCleared: () -> Unit,
    onSearchAreaClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("explore_search_field"),
            placeholder = { Text(stringResource(R.string.explore_search_placeholder)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                )
            },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = onQueryCleared) {
                        Icon(
                            painter = painterResource(R.drawable.ic_clear),
                            contentDescription = stringResource(R.string.explore_clear_search),
                        )
                    }
                }
            } else {
                null
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus(force = true)
                },
            ),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
        )
        SearchAreaRow(
            searchArea = searchArea,
            onClick = onSearchAreaClick,
        )
    }
}

@Composable
private fun SearchAreaRow(
    searchArea: ExploreSearchArea,
    onClick: () -> Unit,
) {
    val rowDescription = stringResource(
        R.string.explore_search_area_content_description,
        stringResource(R.string.explore_near, searchArea.displayName),
        stringResource(R.string.explore_within_km, searchArea.radiusKm),
    )

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = stringResource(R.string.explore_search_area_action),
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) {
                contentDescription = rowDescription
            },
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_location_on),
                contentDescription = null,
            )
        },
        headlineContent = {
            Text(stringResource(R.string.explore_near, searchArea.displayName))
        },
        supportingContent = {
            Text(stringResource(R.string.explore_within_km, searchArea.radiusKm))
        },
        trailingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
