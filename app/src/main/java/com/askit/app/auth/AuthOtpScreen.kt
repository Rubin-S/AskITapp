package com.askit.app.auth

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.app.R
import com.askit.designsystem.components.AskITPrimaryButton
import com.askit.designsystem.jobs.OtpBoxes

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthOtpScreen(
    phoneNumber: String,
    onBack: () -> Unit,
    onEditPhone: () -> Unit,
    onVerify: (String) -> Unit,
    modifier: Modifier = Modifier,
    resendCountdownSeconds: Long = 30,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onResend: () -> Unit = {},
    challengeId: String? = null,
    isResending: Boolean = false,
) {
    // Saveable so a rotation or dark-mode switch keeps what the user typed; a new challenge resets it.
    var otpCode by rememberSaveable(phoneNumber, challengeId) { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()
    val isComplete = otpCode.length == 6

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("auth_otp_screen"),
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
            // Upper Content: Top Nav + Brand Emblem + Headline + Phone edit + 6-digit OTP boxes + Resend Timer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                // Top Navigation Bar (48dp accessible touch target)
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
                            .testTag("auth_otp_back"),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.explore_back),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                        .testTag("auth_otp_logo"),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.ic_askit_launcher),
                        contentDescription = stringResource(R.string.auth_logo),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Display Headline
                Text(
                    text = stringResource(R.string.auth_otp_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        letterSpacing = (-0.5).sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("auth_otp_headline"),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Keep the edit action below the destination, independent of number length.
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.auth_otp_subtitle, phoneNumber),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("auth_otp_subtitle"),
                    )
                    TextButton(
                        onClick = onEditPhone,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("auth_otp_edit_phone_btn"),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.auth_otp_edit_phone),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF7CE605) else MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 6-Digit Segmented OTP Input (Single BasicTextField Engine + Tabular Numerals + Blinking Caret)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    OtpBoxes(
                        value = otpCode,
                        onValueChange = { incoming ->
                            otpCode = incoming
                        },
                        digitCount = 6,
                        isError = errorMessage != null,
                        enabled = !isSubmitting,
                        autoFocus = true,
                        onComplete = { code ->
                            if (!isSubmitting) onVerify(code)
                        },
                        modifier = Modifier.testTag("auth_otp_input_boxes"),
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp)
                            .semantics { liveRegion = LiveRegionMode.Polite }
                            .testTag("auth_otp_error"),
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))

                // Resend Timer / Action directly beneath OTP input boxes
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (resendCountdownSeconds > 0) {
                        Text(
                            text = stringResource(R.string.auth_otp_resend_countdown, resendCountdownSeconds),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("auth_otp_countdown"),
                        )
                    } else {
                        TextButton(
                            onClick = onResend,
                            modifier = Modifier.testTag("auth_otp_resend_btn"),
                            enabled = !isSubmitting,
                        ) {
                            Text(
                                text = stringResource(R.string.auth_otp_resend_action),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                ),
                                color = if (isDark) Color(0xFF7CE605) else MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Pinned Bottom Actions Area (Smoothly lifts directly above keyboard via imePadding)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Primary CTA Button (Light = Black, Dark = Lime Green)
                AskITPrimaryButton(
                    onClick = {
                        if (isComplete && !isSubmitting) onVerify(otpCode)
                    },
                    enabled = isComplete && !isSubmitting,
                    modifier = Modifier.testTag("auth_otp_btn_verify"),
                ) {
                    Text(
                        text = stringResource(
                            when {
                                isResending -> R.string.auth_phone_requesting
                                isSubmitting -> R.string.auth_otp_verifying
                                else -> R.string.auth_otp_verify_btn
                            },
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        ),
                    )
                }
            }
        }
    }
}
