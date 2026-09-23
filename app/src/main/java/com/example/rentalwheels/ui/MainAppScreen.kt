package com.example.rentalwheels.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.rentalwheels.RentalViewModel
import com.example.rentalwheels.UiState
import com.example.rentalwheels.data.Booking
import com.example.rentalwheels.data.Vehicle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    state: UiState,
    viewModel: RentalViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    var selectedVehicleForDetail by remember { mutableStateOf<Vehicle?>(null) }
    var selectedBookingForCheckin by remember { mutableStateOf<Booking?>(null) }
    var selectedBookingForDispute by remember { mutableStateOf<Booking?>(null) }
    var showVerificationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "Explore Vehicles"
                            1 -> "My Bookings"
                            2 -> "List Vehicle"
                            else -> "My Profile"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshAll() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                    label = { Text("Vehicles") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
                    label = { Text("Bookings") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.AddCircleOutline, contentDescription = null) },
                    label = { Text("List Car") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> VehicleListScreen(
                    state = state,
                    onSelectVehicle = { vehicle -> selectedVehicleForDetail = vehicle },
                    onRefresh = { viewModel.loadVehicles() },
                    onFilterChange = { loc, make -> viewModel.setQueries(loc, make) }
                )
                1 -> BookingsScreen(
                    state = state,
                    onApprove = { id -> viewModel.approve(id) },
                    onCancel = { id -> viewModel.cancel(id) },
                    onOpenCheckin = { booking -> selectedBookingForCheckin = booking },
                    onOpenDispute = { booking -> selectedBookingForDispute = booking }
                )
                2 -> AddVehicleScreen(
                    state = state,
                    onSubmit = { make, model, year, plate, price, location, photo ->
                        viewModel.listVehicle(make, model, year, plate, price, location, photo)
                        selectedTab = 0
                    }
                )
                else -> ProfileScreen(
                    state = state,
                    onLogout = { viewModel.logout() },
                    onSaveApi = { url -> viewModel.saveApiBase(url) },
                    onOpenVerify = { showVerificationDialog = true }
                )
            }
        }
    }

    // Modal Dialogs
    selectedVehicleForDetail?.let { vehicle ->
        VehicleDetailDialog(
            vehicle = vehicle,
            state = state,
            onDismiss = { selectedVehicleForDetail = null },
            onBook = { startDate, endDate ->
                viewModel.loadVehicle(vehicle.id)
                viewModel.bookVehicle(startDate, endDate)
                selectedVehicleForDetail = null
                selectedTab = 1
            }
        )
    }

    selectedBookingForCheckin?.let { booking ->
        CheckinDialog(
            booking = booking,
            state = state,
            onDismiss = { selectedBookingForCheckin = null },
            onSubmit = { type, odo, lat, lng, front, rear, left, right ->
                viewModel.submitCheckin(booking.id, type, odo, lat, lng, front, rear, left, right)
                selectedBookingForCheckin = null
            }
        )
    }

    selectedBookingForDispute?.let { booking ->
        DisputeDialog(
            booking = booking,
            state = state,
            onDismiss = { selectedBookingForDispute = null },
            onSubmit = { desc, evidence ->
                viewModel.submitDispute(booking.id, desc, evidence)
                selectedBookingForDispute = null
            }
        )
    }

    if (showVerificationDialog) {
        VerificationDialog(
            state = state,
            onDismiss = { showVerificationDialog = false },
            onSubmit = { cnic, license, idDoc, selfie ->
                viewModel.verifyId(cnic, license, idDoc, selfie)
                showVerificationDialog = false
            }
        )
    }
}
