package com.askit.app.auth

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.app.R
import com.askit.designsystem.components.AskITPrimaryButton
import com.askit.designsystem.profile.ProfileAvatar
import kotlinx.coroutines.launch
import com.askit.designsystem.R as DsR

val defaultServiceInterests = listOf(
    "Home repairs",
    "Plumbing",
    "Cleaning",
    "Electrical",
    "Tutoring",
    "Design",
    "IT support",
    "Other",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormAScreen(
    phoneNumber: String,
    onBack: () -> Unit,
    onSubmitSuccess: (fullName: String, city: String, pincode: String, interests: List<String>) -> Unit,
    modifier: Modifier = Modifier,
    initialInterests: List<String> = listOf("Home repairs", "Plumbing", "Cleaning"),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    var avatarUri by remember { mutableStateOf<String?>(null) }
    var fullName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var customInterest by remember { mutableStateOf("") }
    var isLocating by remember { mutableStateOf(false) }
    var isLocationDetected by remember { mutableStateOf(false) }

    val selectedInterests = remember {
        mutableStateListOf<String>().apply { addAll(initialInterests) }
    }

    val isFormValid = fullName.isNotBlank() && city.isNotBlank()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            avatarUri = uri.toString()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            isLocating = true
            scope.launch {
                val result = LocationHelper.fetchCurrentLocation(context)
                isLocating = false
                result.onSuccess { geo ->
                    if (geo.area.isNotBlank()) area = geo.area
                    city = geo.city
                    pincode = geo.pincode
                    isLocationDetected = true
                }
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("form_a_screen"),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .imeNestedScroll()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("form_a_back"),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                ) {
                    Text(
                        text = "Step 2 of 2",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Profile Avatar Picker (Human trust anchor)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                ProfileAvatar(
                    avatarUrl = avatarUri,
                    cameraContentDescription = "Add profile photo",
                    onCamera = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    modifier = Modifier.testTag("form_a_avatar"),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Headline & Subtitle
            Text(
                text = stringResource(R.string.form_a_headline),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.testTag("form_a_headline"),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.form_a_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("form_a_subtitle"),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Full Name Input
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full name") },
                placeholder = { Text(stringResource(R.string.form_a_full_name_placeholder)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(DsR.drawable.ic_person),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingIcon = if (fullName.isNotEmpty()) {
                    {
                        IconButton(onClick = { fullName = "" }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_clear),
                                contentDescription = "Clear",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_a_input_name"),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // One-line Bio / Headline (Optional)
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio (Optional)") },
                placeholder = { Text("e.g. Homeowner in R.S. Puram, or Electrician") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(DsR.drawable.ic_edit),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingIcon = if (bio.isNotEmpty()) {
                    {
                        IconButton(onClick = { bio = "" }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_clear),
                                contentDescription = "Clear",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_a_input_bio"),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Location Quick Action Card (GPS Auto-detect)
            OutlinedCard(
                onClick = {
                    if (!isLocating) {
                        if (LocationHelper.hasLocationPermission(context)) {
                            isLocating = true
                            scope.launch {
                                val result = LocationHelper.fetchCurrentLocation(context)
                                isLocating = false
                                result.onSuccess { geo ->
                                    if (geo.area.isNotBlank()) area = geo.area
                                    city = geo.city
                                    pincode = geo.pincode
                                    isLocationDetected = true
                                }
                            }
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                ),
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isLocationDetected) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_a_location_btn"),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.form_a_location_locating),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_my_location),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (isLocationDetected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isLocationDetected) {
                                    stringResource(R.string.form_a_location_located)
                                } else {
                                    stringResource(R.string.form_a_location_use_current)
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = if (isLocationDetected && city.isNotBlank()) {
                                    "${if (area.isNotBlank()) "$area, " else ""}$city ${if (pincode.isNotBlank()) "· $pincode" else ""}"
                                } else {
                                    "Auto-detects your locality and city via GPS"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (isLocationDetected) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = "Detected",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Area / Neighborhood (Hyperlocal anchor)
            OutlinedTextField(
                value = area,
                onValueChange = { area = it },
                label = { Text("Area / Neighborhood") },
                placeholder = { Text("e.g. R.S. Puram, Gandhipuram") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(DsR.drawable.ic_location),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingIcon = if (area.isNotEmpty()) {
                    {
                        IconButton(onClick = { area = "" }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_clear),
                                contentDescription = "Clear",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_a_input_area"),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Compact 2-Column City & Pincode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    placeholder = { Text(stringResource(R.string.form_a_city_placeholder)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(DsR.drawable.ic_location),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailingIcon = if (city.isNotEmpty()) {
                        {
                            IconButton(onClick = { city = "" }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_clear),
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("form_a_input_city"),
                )

                OutlinedTextField(
                    value = pincode,
                    onValueChange = { pincode = it.filter(Char::isDigit).take(10) },
                    label = { Text(stringResource(R.string.form_a_pincode_label)) },
                    placeholder = { Text(stringResource(R.string.form_a_pincode_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("form_a_input_pincode"),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Areas of interest section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(R.string.form_a_interests_label),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Choose topics you want to explore, offer, or request",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_a_interests_flow_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                defaultServiceInterests.forEach { interest ->
                    val isSelected = selectedInterests.contains(interest)

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                selectedInterests.remove(interest)
                            } else {
                                selectedInterests.add(interest)
                            }
                        },
                        label = {
                            Text(
                                text = interest,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                ),
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        } else null,
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = if (isDark) {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            selectedLabelColor = if (isDark) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            },
                            selectedLeadingIconColor = if (isDark) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            },
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) {
                                if (isDark) MaterialTheme.colorScheme.outline else Color.Transparent
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                        ),
                        modifier = Modifier.testTag("chip_$interest"),
                    )
                }
            }

            AnimatedVisibility(
                visible = selectedInterests.contains("Other"),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    OutlinedTextField(
                        value = customInterest,
                        onValueChange = { customInterest = it },
                        placeholder = { Text(stringResource(R.string.form_a_custom_category_placeholder)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_a_input_custom_interest"),
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // CTA Submit Button
            AskITPrimaryButton(
                onClick = {
                    if (isFormValid) {
                        val finalInterests = selectedInterests.toMutableList()
                        if (customInterest.isNotBlank() && finalInterests.contains("Other")) {
                            finalInterests.remove("Other")
                            finalInterests.add(customInterest.trim())
                        }
                        val submittedCity = if (area.isNotBlank() && !city.contains(area, ignoreCase = true)) {
                            "$area, $city"
                        } else {
                            city
                        }
                        onSubmitSuccess(fullName.trim(), submittedCity.trim(), pincode.trim(), finalInterests)
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.testTag("form_a_btn_submit"),
            ) {
                Text(
                    text = stringResource(R.string.form_a_submit_btn),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
