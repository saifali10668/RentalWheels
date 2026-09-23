package com.example.rentalwheels.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.rentalwheels.ui.theme.Amber
import com.example.rentalwheels.ui.theme.Danger
import com.example.rentalwheels.ui.theme.Emerald

@Composable
fun StatusBanner(error: String?, message: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        if (!error.isNullOrBlank()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))) {
                Text(error, color = Danger, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
        if (!message.isNullOrBlank()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5))) {
                Text(message, color = Emerald, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun LoadingRow(loading: Boolean) {
    if (loading) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine
    )
}

@Composable
fun StatusChip(text: String) {
    val color = when (text.uppercase()) {
        "AVAILABLE", "APPROVED", "VERIFIED", "COMPLETED", "RELEASED" -> Emerald
        "PENDING", "UNVERIFIED", "UNPAID", "ESCROW_HOLD" -> Amber
        "ACTIVE" -> MaterialTheme.colorScheme.primary
        "CANCELLED", "DISPUTED", "REJECTED", "SUSPENDED" -> Danger
        else -> MaterialTheme.colorScheme.outline
    }
    AssistChip(
        onClick = {},
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = color,
            leadingIconContentColor = color
        )
    )
}

@Composable
fun EmptyHint(text: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
