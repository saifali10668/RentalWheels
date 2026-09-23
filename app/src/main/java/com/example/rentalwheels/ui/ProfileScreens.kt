package com.example.rentalwheels.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.rentalwheels.UiState
import com.example.rentalwheels.ui.theme.Danger
import com.example.rentalwheels.ui.theme.Emerald

@Composable
fun ProfileScreen(
    state: UiState,
    onLogout: () -> Unit,
    onSaveApi: (String) -> Unit,
    onOpenVerify: () -> Unit
) {
    var apiInput by remember(state.apiBase) { mutableStateOf(state.apiBase) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatusBanner(state.error, state.message)

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = state.profile?.username ?: "Guest User",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text("Role: ${state.profile?.role ?: "RENTER"}", style = MaterialTheme.typography.bodyMedium)
                Text("Email: ${state.profile?.email ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                Text("Phone: ${state.profile?.phone_number ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (state.profile?.is_id_verified == true) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Identity Verification", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    StatusChip(state.profile?.verification_status ?: "UNVERIFIED")
                }

                if (state.profile?.is_id_verified == true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald)
                        Spacer(Modifier.width(6.dp))
                        Text("Government ID & Facial Biometrics Verified ✓", style = MaterialTheme.typography.bodyMedium, color = Emerald, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Text("Complete AI ID verification to unlock vehicle listings and rentals.", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = onOpenVerify) {
                        Icon(Icons.Default.Badge, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Verify Government ID")
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dns, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("API Server Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                OutlinedTextField(
                    value = apiInput,
                    onValueChange = { apiInput = it },
                    label = { Text("Base URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = { onSaveApi(apiInput) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save Connection")
                }
            }
        }

        if (state.disputes.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = Danger)
                        Spacer(Modifier.width(8.dp))
                        Text("Active Disputes (${state.disputes.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    state.disputes.forEach { dispute ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text("Dispute #${dispute.id} - Booking #${dispute.booking}", fontWeight = FontWeight.Bold)
                            Text(dispute.description, style = MaterialTheme.typography.bodySmall)
                            StatusChip(dispute.status)
                        }
                    }
                }
            }
        }

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Danger),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Sign Out")
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun VerificationDialog(
    state: UiState,
    onDismiss: () -> Unit,
    onSubmit: (cnic: String, license: String, idDoc: Uri, selfie: Uri) -> Unit
) {
    var cnic by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }
    var idDocUri by remember { mutableStateOf<Uri?>(null) }
    var selfieUri by remember { mutableStateOf<Uri?>(null) }

    val idLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { idDocUri = it }
    val selfieLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selfieUri = it }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("AI Identity & License Verification", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Upload document & selfie for automated face match.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                StatusBanner(state.error, state.message)

                AppTextField(cnic, { cnic = it }, "CNIC / National ID Number")
                AppTextField(license, { license = it }, "Driver's License Number")

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PhotoPickTile("ID Card / License Doc", idDocUri != null, Modifier.weight(1f)) { idLauncher.launch("image/*") }
                    PhotoPickTile("Live Selfie Photo", selfieUri != null, Modifier.weight(1f)) { selfieLauncher.launch("image/*") }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (idDocUri != null && selfieUri != null) {
                                onSubmit(cnic, license, idDocUri!!, selfieUri!!)
                            }
                        },
                        enabled = !state.loading && cnic.isNotBlank() && license.isNotBlank() && idDocUri != null && selfieUri != null
                    ) {
                        Text(if (state.loading) "Verifying..." else "Submit for AI Verification")
                    }
                }
            }
        }
    }
}
