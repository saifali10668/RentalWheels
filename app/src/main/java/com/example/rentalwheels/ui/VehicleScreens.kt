package com.example.rentalwheels.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.rentalwheels.ApiFactory
import com.example.rentalwheels.UiState
import com.example.rentalwheels.data.Vehicle
import com.example.rentalwheels.ui.theme.Emerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleListScreen(
    state: UiState,
    onSelectVehicle: (Vehicle) -> Unit,
    onRefresh: () -> Unit,
    onFilterChange: (location: String, make: String) -> Unit
) {
    var locationInput by remember(state.locationQuery) { mutableStateOf(state.locationQuery) }
    var makeInput by remember(state.makeQuery) { mutableStateOf(state.makeQuery) }

    val filteredVehicles = remember(state.vehicles, state.locationQuery, state.makeQuery) {
        state.vehicles.filter { v ->
            val matchLoc = state.locationQuery.isBlank() || v.location.contains(state.locationQuery, ignoreCase = true)
            val matchMake = state.makeQuery.isBlank() || v.make.contains(state.makeQuery, ignoreCase = true) || v.model.contains(state.makeQuery, ignoreCase = true)
            matchLoc && matchMake
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Search & Filter Vehicles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = locationInput,
                        onValueChange = {
                            locationInput = it
                            onFilterChange(locationInput, makeInput)
                        },
                        label = { Text("Location") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = makeInput,
                        onValueChange = {
                            makeInput = it
                            onFilterChange(locationInput, makeInput)
                        },
                        label = { Text("Make / Model") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        LoadingRow(state.loading)

        if (filteredVehicles.isEmpty() && !state.loading) {
            EmptyHint("No vehicles found matching your criteria.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredVehicles, key = { it.id }) { vehicle ->
                    VehicleCard(vehicle = vehicle, onClick = { onSelectVehicle(vehicle) })
                }
            }
        }
    }
}

@Composable
fun VehicleCard(
    vehicle: Vehicle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            val photoUrl = ApiFactory.rewriteMediaUrl(vehicle.vehicle_photo)
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "${vehicle.make} ${vehicle.model}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${vehicle.year} ${vehicle.make} ${vehicle.model}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    StatusChip(vehicle.status)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(vehicle.location.ifBlank { "Location unspecified" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (vehicle.is_verified) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "ANPR Verified", tint = Emerald, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("ANPR Verified", style = MaterialTheme.typography.bodySmall, color = Emerald, fontWeight = FontWeight.SemiBold)
                        } else {
                            Icon(Icons.Default.Shield, contentDescription = "Pending Verification", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Standard Listing", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    Text(
                        text = "$${vehicle.price_per_day}/day",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun VehicleDetailDialog(
    vehicle: Vehicle,
    state: UiState,
    onDismiss: () -> Unit,
    onBook: (startDate: String, endDate: String) -> Unit
) {
    var startDate by remember { mutableStateOf("2026-10-01") }
    var endDate by remember { mutableStateOf("2026-10-05") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val photoUrl = ApiFactory.rewriteMediaUrl(vehicle.vehicle_photo)
                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = "${vehicle.year} ${vehicle.make} ${vehicle.model}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text("Plate: ${vehicle.plate_number}", style = MaterialTheme.typography.bodyMedium)
                Text("Location: ${vehicle.location}", style = MaterialTheme.typography.bodyMedium)
                Text("Host: ${vehicle.owner_username.ifBlank { "Owner" }} ${if (vehicle.owner_verified) "✓ Verified" else ""}", style = MaterialTheme.typography.bodySmall)

                StatusBanner(state.error, state.message)

                Spacer(Modifier.height(8.dp))
                Text("Rental Dates (YYYY-MM-DD)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Text(
                    text = "Rate: $${vehicle.price_per_day} / day",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onBook(startDate, endDate) },
                        enabled = !state.loading && state.loggedIn && vehicle.status.uppercase() == "AVAILABLE"
                    ) {
                        Text(if (!state.loggedIn) "Log in to book" else if (state.loading) "Booking..." else "Request Booking")
                    }
                }
            }
        }
    }
}

@Composable
fun AddVehicleScreen(
    state: UiState,
    onSubmit: (make: String, model: String, year: String, plate: String, price: String, location: String, photo: Uri?) -> Unit
) {
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("2023") }
    var plate by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("50") }
    var location by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedPhotoUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("List a Vehicle for Rent", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Vehicle photo will be processed by ANPR for automated plate verification.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        StatusBanner(state.error, state.message)

        AppTextField(make, { make = it }, "Make (e.g. Toyota)")
        AppTextField(model, { model = it }, "Model (e.g. Corolla)")
        OutlinedTextField(
            value = year,
            onValueChange = { year = it },
            label = { Text("Year") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        AppTextField(plate, { plate = it }, "License Plate Number")
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Daily Rate ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        AppTextField(location, { location = it }, "City / Pickup Location")

        Card(
            modifier = Modifier.fillMaxWidth().clickable { launcher.launch("image/*") },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (selectedPhotoUri != null) "Photo Attached ✓" else "Attach Vehicle Photo (for ANPR)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selectedPhotoUri != null) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Button(
            onClick = { onSubmit(make, model, year, plate, price, location, selectedPhotoUri) },
            enabled = !state.loading && make.isNotBlank() && model.isNotBlank() && plate.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.loading) "Submitting Listing..." else "Publish Vehicle Listing")
        }
    }
}
