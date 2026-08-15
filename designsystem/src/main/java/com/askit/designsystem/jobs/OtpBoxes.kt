package com.askit.designsystem.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun OtpBoxes(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val digits = value.padEnd(4, ' ').take(4)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(4) { index ->
            OutlinedTextField(
                value = digits[index].toString().trim(),
                onValueChange = { incoming ->
                    val digit = incoming.filter(Char::isDigit).takeLast(1)
                    val chars = value.padEnd(4, ' ').toCharArray()
                    chars[index] = digit.singleOrNull() ?: ' '
                    onValueChange(chars.concatToString().replace(" ", "").take(4))
                },
                enabled = enabled,
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    textAlign = TextAlign.Center,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.extraSmall,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("otp_box_$index"),
            )
        }
    }
}
