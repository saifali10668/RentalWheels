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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.rentalwheels.UiState
import com.example.rentalwheels.data.Booking
import com.example.rentalwheels.ui.theme.Danger
import com.example.rentalwheels.ui.theme.Emerald

@Composable
fun BookingsScreen(
    state: UiState,
    onApprove: (id: Int) -> Unit,
    onCancel: (id: Int) -> Unit,
    onOpenCheckin: (booking: Booking) -> Unit,
    onOpenDispute: (booking: Booking) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Pending", "Approved", "Completed")

    val filteredBookings = remember(state.bookings, selectedTab) {
        when (selectedTab) {
            1 -> state.bookings.filter { it.status.uppercase() == "PENDING" }
            2 -> state.bookings.filter { it.status.uppercase() == "APPROVED" }
            3 -> state.bookings.filter { it.status.uppercase() == "COMPLETED" }
            else -> state.bookings
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        LoadingRow(state.loading)

        if (filteredBookings.isEmpty() && !state.loading) {
            EmptyHint("No bookings found in this category.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredBookings, key = { it.id }) { booking ->
                    BookingCard(
                        booking = booking,
                        currentUser = state.profile?.username ?: "",
                        isHost = state.profile?.role in listOf("OWNER", "BOTH"),
                        onApprove = { onApprove(booking.id) },
                        onCancel = { onCancel(booking.id) },
                        onCheckin = { onOpenCheckin(booking) },
                        onDispute = { onOpenDispute(booking) }
                    )
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    currentUser: String,
    isHost: Boolean,
    onApprove: () -> Unit,
    onCancel: () -> Unit,
    onCheckin: () -> Unit,
    onDispute: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = booking.vehicle_details?.let { "${it.year} ${it.make} ${it.model}" } ?: "Vehicle #${booking.vehicle}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusChip(booking.status)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.padding(end = 6.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "${booking.start_date} → ${booking.end_date}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total: $${booking.total_amount}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Payment: ${booking.payment_status}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = "Renter: ${booking.renter_username.ifBlank { "User" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (booking.status.uppercase() == "PENDING" && isHost) {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Approve")
                    }
                    Spacer(Modifier.width(8.dp))
                }

                if (booking.status.uppercase() in listOf("PENDING", "APPROVED")) {
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                }

                if (booking.status.uppercase() in listOf("APPROVED", "COMPLETED")) {
                    OutlinedButton(onClick = onCheckin) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Inspection")
                    }
                    Spacer(Modifier.width(8.dp))
                }

                OutlinedButton(onClick = onDispute) {
                    Icon(Icons.Default.Gavel, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Dispute")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckinDialog(
    booking: Booking,
    state: UiState,
    onDismiss: () -> Unit,
    onSubmit: (type: String, odo: String, lat: Double, lng: Double, front: Uri, rear: Uri, left: Uri, right: Uri) -> Unit
) {
    var checkinType by remember { mutableStateOf("PICKUP") }
    var odometer by remember { mutableStateOf("50000") }
    var lat by remember { mutableStateOf("33.6844") }
    var lng by remember { mutableStateOf("73.0479") }
    var expanded by remember { mutableStateOf(false) }

    var frontUri by remember { mutableStateOf<Uri?>(null) }
    var rearUri by remember { mutableStateOf<Uri?>(null) }
    var leftUri by remember { mutableStateOf<Uri?>(null) }
    var rightUri by remember { mutableStateOf<Uri?>(null) }

    val frontLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { frontUri = it }
    val rearLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { rearUri = it }
    val leftLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { leftUri = it }
    val rightLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { rightUri = it }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Vehicle Inspection Check-In", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                StatusBanner(state.error, state.message)

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = checkinType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Inspection Stage") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("PICKUP (Pick-Up Inspection)") }, onClick = {
                            checkinType = "PICKUP"
                            expanded = false
                        })
                        DropdownMenuItem(text = { Text("RETURN (Drop-Off Inspection)") }, onClick = {
                            checkinType = "RETURN"
                            expanded = false
                        })
                    }
                }

                OutlinedTextField(
                    value = odometer,
                    onValueChange = { odometer = it },
                    label = { Text("Odometer Reading (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = lat,
                        onValueChange = { lat = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lng,
                        onValueChange = { lng = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Attach 4-Side Inspection Photos:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PhotoPickTile("Front", frontUri != null, Modifier.weight(1f)) { frontLauncher.launch("image/*") }
                    PhotoPickTile("Rear", rearUri != null, Modifier.weight(1f)) { rearLauncher.launch("image/*") }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PhotoPickTile("Left", leftUri != null, Modifier.weight(1f)) { leftLauncher.launch("image/*") }
                    PhotoPickTile("Right", rightUri != null, Modifier.weight(1f)) { rightLauncher.launch("image/*") }
                }

                val allPhotosAttached = frontUri != null && rearUri != null && leftUri != null && rightUri != null

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val latitude = lat.toDoubleOrNull() ?: 0.0
                            val longitude = lng.toDoubleOrNull() ?: 0.0
                            if (frontUri != null && rearUri != null && leftUri != null && rightUri != null) {
                                onSubmit(checkinType, odometer, latitude, longitude, frontUri!!, rearUri!!, leftUri!!, rightUri!!)
                            }
                        },
                        enabled = !state.loading && allPhotosAttached
                    ) {
                        Text(if (state.loading) "Saving..." else "Submit Inspection")
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoPickTile(label: String, attached: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (attached) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = if (attached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(if (attached) "$label ✓" else label, style = MaterialTheme.typography.bodySmall, fontWeight = if (attached) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
fun DisputeDialog(
    booking: Booking,
    state: UiState,
    onDismiss: () -> Unit,
    onSubmit: (description: String, evidence: Uri?) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var evidenceUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { evidenceUri = it }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Open Dispute for Booking #${booking.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                StatusBanner(state.error, state.message)

                AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Describe issue / damage",
                    singleLine = false
                )

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { launcher.launch("image/*") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(if (evidenceUri != null) "Evidence Attached ✓" else "Attach Evidence Photo", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onSubmit(description, evidenceUri) },
                        enabled = !state.loading && description.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Danger)
                    ) {
                        Text(if (state.loading) "Opening..." else "Submit Dispute")
                    }
                }
            }
        }
    }
}
