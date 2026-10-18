package com.askit.app.auth

import androidx.compose.runtime.setValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import com.askit.designsystem.inputs.AskITInputDefaults
import com.askit.designsystem.inputs.AskITTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.app.R
import com.askit.designsystem.components.AskITPrimaryButton
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AuthPhoneScreen(
    onBack: () -> Unit,
    onGetOtp: (String) -> Unit,
    modifier: Modifier = Modifier,
    availableCountries: List<CountryCode> = defaultCountryCodes,
    initialCountry: CountryCode = defaultCountryCodes.first(),
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onOpenTerms: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
) {
    var selectedCountryIso by rememberSaveable { mutableStateOf(initialCountry.isoCode) }
    val selectedCountry = availableCountries.firstOrNull { it.isoCode == selectedCountryIso } ?: initialCountry
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var showValidationError by rememberSaveable { mutableStateOf(false) }
    var phoneFieldFocused by remember { mutableStateOf(false) }
    var isCountryPickerOpen by remember { mutableStateOf(false) }
    var countrySearchQuery by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val validation = remember(phoneNumber, selectedCountry.isoCode) {
        PhoneNumberValidator.validate(phoneNumber, selectedCountry.isoCode)
    }
    val validatedPhone = validation as? PhoneValidation.Valid
    val isValidPhone = validatedPhone != null
    val validationError = if (showValidationError && phoneNumber.isNotBlank() && !isValidPhone) {
        stringResource(when {
            validation == PhoneValidation.CountryMismatch -> R.string.auth_phone_country_mismatch
            selectedCountry.isoCode == "IN" -> R.string.auth_phone_invalid_india
            else -> R.string.auth_phone_invalid_mobile
        })
    } else null
    val displayedError = validationError ?: errorMessage

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("auth_phone_screen"),
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Upper Content: Top Nav + Brand Header + Headline + Phone Input
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                // Top Navigation Bar with 48dp Touch Target
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
                            .testTag("auth_phone_back"),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.explore_back),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mini AskIT Emblem with Soft Floating Lift Shadow
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(
                            elevation = if (isDark) 10.dp else 14.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = if (isDark) Color(0xFF7CE605).copy(alpha = 0.25f) else Color(0xFF0F172A).copy(alpha = 0.12f),
                            spotColor = if (isDark) Color(0xFF7CE605).copy(alpha = 0.38f) else Color(0xFF0F172A).copy(alpha = 0.22f),
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .testTag("auth_phone_logo"),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.ic_askit_launcher),
                        contentDescription = stringResource(R.string.auth_logo),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Display Headline
                Text(
                    text = stringResource(R.string.auth_phone_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        letterSpacing = (-0.5).sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("auth_phone_headline"),
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subtitle
                Text(
                    text = stringResource(R.string.auth_phone_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("auth_phone_subtitle"),
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Input Label
                Text(
                    text = stringResource(R.string.auth_phone_label),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 2.dp, bottom = 8.dp),
                )

                // High-End Phone Input Box
                Surface(
                    shape = AskITInputDefaults.Shape,
                    color = AskITInputDefaults.containerColor,
                    border = BorderStroke(
                        width = if (phoneFieldFocused) 2.dp else 1.dp,
                        color = when {
                            validationError != null -> MaterialTheme.colorScheme.error
                            phoneFieldFocused -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline
                        },
                    ),
                    shadowElevation = if (isDark) 2.dp else 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = AskITInputDefaults.MinimumHeight)
                        .testTag("auth_phone_input_container"),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Interactive Country Code Badge
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = !isSubmitting) { isCountryPickerOpen = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("auth_phone_country_picker_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = selectedCountry.flagEmoji,
                                fontSize = 18.sp,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${selectedCountry.isoCode} ${selectedCountry.dialCode}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .width(1.dp)
                                .height(24.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant),
                        )

                        // Clean Numeric Phone Text Field
                        BasicTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            singleLine = true,
                            enabled = !isSubmitting,
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp,
                                letterSpacing = 0.5.sp,
                            ),
                            cursorBrush = SolidColor(if (isDark) Color(0xFF7CE605) else Color(0xFF121418)),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (validatedPhone != null && !isSubmitting) {
                                        onGetOtp(validatedPhone.e164)
                                    } else if (!isSubmitting) {
                                        showValidationError = true
                                    }
                                },
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    if (phoneNumber.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.auth_phone_placeholder),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = MaterialTheme.colorScheme.outline,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 17.sp,
                                            ),
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged {
                                    if (phoneFieldFocused && !it.isFocused) showValidationError = true
                                    phoneFieldFocused = it.isFocused
                                }
                                .testTag("auth_phone_text_field"),
                        )

                        // Clear Button
                        if (phoneNumber.isNotEmpty()) {
                            IconButton(
                                onClick = { phoneNumber = "" },
                                enabled = !isSubmitting,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("auth_phone_clear_btn"),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_clear),
                                    contentDescription = stringResource(R.string.auth_clear_phone),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }

                if (displayedError != null) {
                    Text(
                        text = displayedError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp)
                            .semantics { liveRegion = LiveRegionMode.Polite }
                            .testTag("auth_phone_error"),
                    )
                }
                Spacer(modifier = Modifier.height(36.dp))
            }

            // Pinned Bottom Actions Area (Strictly adheres to Light=Black, Dark=Green)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Primary CTA Button
                AskITPrimaryButton(
                    onClick = { if (validatedPhone != null && !isSubmitting) onGetOtp(validatedPhone.e164) },
                    enabled = isValidPhone && !isSubmitting,
                    modifier = Modifier.testTag("auth_phone_btn_get_otp"),
                ) {
                    Text(
                        text = stringResource(if (isSubmitting) R.string.auth_phone_requesting else R.string.auth_phone_get_otp),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LegalDisclaimer(
                    textRes = R.string.auth_phone_disclaimer,
                    onOpenTerms = onOpenTerms,
                    onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("auth_phone_disclaimer"),
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }

    // Country Code Selection Modal Bottom Sheet
    if (isCountryPickerOpen) {
        val locale = LocalConfiguration.current.locales[0]
        val countryNames = remember(availableCountries, locale) {
            availableCountries.associate { country ->
                country.isoCode to Locale.Builder().setRegion(country.isoCode).build()
                    .getDisplayCountry(locale)
            }
        }
        val filteredCountries = remember(countrySearchQuery, availableCountries, countryNames) {
            if (countrySearchQuery.isBlank()) {
                availableCountries
            } else {
                val q = countrySearchQuery.trim().lowercase()
                availableCountries.filter {
                    countryNames.getValue(it.isoCode).contains(q, ignoreCase = true) ||
                        it.countryName.lowercase().contains(q) ||
                        it.isoCode.lowercase().contains(q) ||
                        it.dialCode.contains(q)
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = { isCountryPickerOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.testTag("auth_country_picker_sheet"),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(
                    text = stringResource(R.string.auth_country_picker_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                AskITTextField(
                    value = countrySearchQuery,
                    onValueChange = { countrySearchQuery = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.auth_country_search_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_country_search_input"),
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                ) {
                    items(filteredCountries, key = { it.isoCode }) { country ->
                        val isSelected = country.isoCode == selectedCountry.isoCode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent)
                                .clickable {
                                    selectedCountryIso = country.isoCode
                                    showValidationError = true
                                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                                        isCountryPickerOpen = false
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 14.dp)
                                .testTag("country_item_${country.isoCode}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = country.flagEmoji,
                                    fontSize = 22.sp,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = countryNames.getValue(country.isoCode),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = country.isoCode,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Text(
                                text = country.dialCode,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = if (isSelected) Color(0xFF7CE605) else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
