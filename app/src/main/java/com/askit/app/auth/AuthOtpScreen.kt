package com.askit.app.auth

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.app.R
import com.askit.designsystem.components.AskITPrimaryButton
import com.askit.designsystem.jobs.OtpBoxes
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthOtpScreen(
    phoneNumber: String,
    onBack: () -> Unit,
    onEditPhone: () -> Unit,
    onVerifySuccess: () -> Unit,
    modifier: Modifier = Modifier,
    initialCountdownSeconds: Int = 30,
) {
    var otpCode by remember { mutableStateOf("") }
    var countdown by remember { mutableIntStateOf(initialCountdownSeconds) }
    var isError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()
    val isComplete = otpCode.length == 6

    // Resend Countdown Timer
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000L)
            countdown -= 1
        }
    }

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
                            contentDescription = "Back",
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
                        contentDescription = "AskIT Logo",
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

                // Subtitle with Phone and inline Edit button with accessible touch target
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
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
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(
                        onClick = onEditPhone,
                        modifier = Modifier.testTag("auth_otp_edit_phone_btn"),
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
                            isError = false
                        },
                        digitCount = 6,
                        isError = isError,
                        autoFocus = true,
                        onComplete = {
                            onVerifySuccess()
                        },
                        modifier = Modifier.testTag("auth_otp_input_boxes"),
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Resend Timer / Action directly beneath OTP input boxes
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (countdown > 0) {
                        Text(
                            text = stringResource(R.string.auth_otp_resend_countdown, countdown),
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
                            onClick = {
                                countdown = initialCountdownSeconds
                                otpCode = ""
                                isError = false
                            },
                            modifier = Modifier.testTag("auth_otp_resend_btn"),
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
                        if (isComplete) {
                            onVerifySuccess()
                        } else {
                            isError = true
                        }
                    },
                    enabled = isComplete,
                    modifier = Modifier.testTag("auth_otp_btn_verify"),
                ) {
                    Text(
                        text = stringResource(R.string.auth_otp_verify_btn),
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
