package com.askit.app.auth

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.askit.app.R
import com.askit.app.media.persistPhotoPickerReadAccess
import com.askit.designsystem.components.AskITPrimaryButton
import com.askit.designsystem.profile.ProfileAvatar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormAScreen(
    phoneNumber: String,
    onBack: () -> Unit,
    onSubmit: suspend (SignupDraft) -> SignupResult,
    modifier: Modifier = Modifier,
    signupAvailable: Boolean = true,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val initialLanguage = com.askit.app.session.AppLanguage.fromLanguageTag(LocalConfiguration.current.locales[0].language).storedValue

    var editablePhone by rememberSaveable(phoneNumber) { mutableStateOf(phoneNumber) }
    var avatarUri by rememberSaveable { mutableStateOf<String?>(null) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var preferredLanguage by rememberSaveable { mutableStateOf(initialLanguage) }
    var area by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var pincode by rememberSaveable { mutableStateOf("") }
    var photoTouched by rememberSaveable { mutableStateOf(false) }
    var isLocating by remember { mutableStateOf(false) }
    var isLocationDetected by rememberSaveable { mutableStateOf(false) }
    var locationFailed by rememberSaveable { mutableStateOf(false) }
    var submissionResult by rememberSaveable { mutableStateOf<SignupResult?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var photoCheckVersion by remember { mutableIntStateOf(0) }
    var photoReadable by remember(avatarUri, photoCheckVersion) { mutableStateOf(false) }
    var photoCheckComplete by remember(avatarUri, photoCheckVersion) { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { photoCheckVersion++ }
    LaunchedEffect(avatarUri, photoCheckVersion) {
        photoReadable = withContext(Dispatchers.IO) {
            isSignupPhotoReadable(context.contentResolver, avatarUri)
        }
        photoCheckComplete = true
    }

    val draft = SignupDraft(
        fullName = fullName,
        username = username,
        city = city,
        area = area,
        pincode = pincode,
        phoneNumber = editablePhone,
        avatarUri = avatarUri,
        preferredLanguage = preferredLanguage,
        email = email,
    )
    val validation = draft.validation(photoReadable)
    val isFormValid = signupAvailable && validation.isValid && !isSubmitting &&
        (com.askit.app.BuildConfig.CLIENT_PREVIEW || editablePhone == phoneNumber)

    fun submit() {
        if (!isFormValid || isSubmitting) return
        isSubmitting = true
        submissionResult = null
        scope.launch {
            try {
                val result = onSubmit(draft)
                submissionResult = result.takeUnless { it == SignupResult.Saved }
                if (result != SignupResult.Saved) photoCheckVersion++
            } finally {
                isSubmitting = false
            }
        }
    }

    fun detectLocation() {
        isLocating = true
        isLocationDetected = false
        locationFailed = false
        val originalLocation = Triple(area, city, pincode)
        scope.launch {
            try {
                LocationHelper.fetchCurrentLocation(context)
                    .onSuccess { geo ->
                        if (Triple(area, city, pincode) == originalLocation && !isSubmitting) {
                            area = geo.area
                            city = geo.city
                            pincode = geo.pincode
                            isLocationDetected = true
                        }
                    }
                    .onFailure { locationFailed = true }
            } finally {
                isLocating = false
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        photoTouched = true
        if (uri != null) {
            persistPhotoPickerReadAccess(context.contentResolver, listOf(uri))
            avatarUri = uri.toString()
        }
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            detectLocation()
        } else {
            isLocationDetected = false
            locationFailed = true
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
                        contentDescription = stringResource(R.string.explore_back),
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
                        text = stringResource(R.string.form_a_step),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!signupAvailable) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth().testTag("form_a_signup_unavailable"),
                ) {
                    Text(
                        text = stringResource(R.string.form_a_signup_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Profile Avatar Picker (Human trust anchor)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                ProfileAvatar(
                    avatarUrl = avatarUri,
                    cameraContentDescription = stringResource(R.string.form_a_add_photo),
                    onCamera = if (isSubmitting) {
                        null
                    } else {
                        {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        }
                    },
                    modifier = Modifier.testTag("form_a_avatar"),
                )
            }

            Text(
                text = stringResource(R.string.form_a_photo_required),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (avatarUri != null && !photoCheckComplete) {
                Text(stringResource(R.string.form_a_photo_checking), style = MaterialTheme.typography.bodySmall)
            } else if ((photoTouched || avatarUri != null) && !validation.photoValid) {
                Text(
                    text = stringResource(R.string.form_a_photo_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
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

            // Location Quick Action Card (GPS Auto-detect)
            OutlinedCard(
                enabled = !isLocating && !isSubmitting,
                onClick = {
                    if (!isLocating && !isSubmitting) {
                        if (LocationHelper.hasLocationPermission(context)) {
                            detectLocation()
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
                                text = if (locationFailed) {
                                    stringResource(R.string.form_a_location_failed)
                                } else if (isLocationDetected && city.isNotBlank()) {
                                    "${if (area.isNotBlank()) "$area, " else ""}$city ${if (pincode.isNotBlank()) "· $pincode" else ""}"
                                } else {
                                    stringResource(R.string.form_a_location_hint)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (isLocationDetected) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = stringResource(R.string.form_a_detected),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AccountFormFields(draft, onChange = { updated ->
                fullName = updated.fullName
                username = updated.username
                email = updated.email.orEmpty()
                city = updated.city
                pincode = updated.pincode
                preferredLanguage = updated.preferredLanguage
                editablePhone = updated.phoneNumber
                isLocationDetected = false
            }, enabled = !isSubmitting, tag = "form_a_input")

            if (!com.askit.app.BuildConfig.CLIENT_PREVIEW && editablePhone != phoneNumber) {
                Text(stringResource(R.string.profile_phone_verification_needed))
            }
            submissionResult?.let { result ->
                Text(
                    text = stringResource(
                        if (result == SignupResult.Unavailable) R.string.form_a_signup_unavailable else R.string.form_a_save_failed,
                    ),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("form_a_save_error"),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            AskITPrimaryButton(
                onClick = ::submit,
                enabled = isFormValid,
                modifier = Modifier.testTag("form_a_btn_submit"),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = stringResource(R.string.form_a_submit_btn),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
