package com.example.rentalwheels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentalwheels.data.Booking
import com.example.rentalwheels.data.CreateBookingRequest
import com.example.rentalwheels.data.Dispute
import com.example.rentalwheels.data.LoginRequest
import com.example.rentalwheels.data.SignupRequest
import com.example.rentalwheels.data.UserProfile
import com.example.rentalwheels.data.Vehicle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.util.concurrent.TimeUnit
import java.util.Calendar

data class UiState(
    val ready: Boolean = false,
    val loggedIn: Boolean = false,
    val loading: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val apiBase: String = "http://10.0.2.2:8000/api/v1/",
    val profile: UserProfile? = null,
    val vehicles: List<Vehicle> = emptyList(),
    val selectedVehicle: Vehicle? = null,
    val bookings: List<Booking> = emptyList(),
    val disputes: List<Dispute> = emptyList(),
    val locationQuery: String = "",
    val makeQuery: String = ""
)

class RentalViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val apiBase = SessionStore.apiBase()
            val token = SessionStore.accessToken()
            _state.update { it.copy(apiBase = apiBase, ready = true, loggedIn = !token.isNullOrBlank()) }
            if (!token.isNullOrBlank()) {
                refreshAll()
            } else {
                runCatching { ApiFactory.api().vehicles() }
                    .onSuccess { list -> _state.update { it.copy(vehicles = list) } }
            }
        }
    }

    fun clearFlash() {
        _state.update { it.copy(error = null, message = null) }
    }

    fun setQueries(location: String, make: String) {
        _state.update { it.copy(locationQuery = location, makeQuery = make) }
    }

    fun saveApiBase(url: String) {
        viewModelScope.launch {
            SessionStore.saveApiBase(url)
            _state.update { it.copy(apiBase = SessionStore.apiBase(), message = "API server saved") }
        }
    }

    fun login(username: String, password: String) = launchOp {
        val tokens = ApiFactory.api().login(LoginRequest(username.trim(), password))
        SessionStore.saveTokens(tokens.access, tokens.refresh)
        val profile = ApiFactory.api().profile()
        _state.update { it.copy(loggedIn = true, profile = profile) }
        refreshAll()
    }

    fun signup(
        username: String,
        email: String,
        phone: String,
        password: String,
        role: String
    ) = launchOp {
        ApiFactory.api().signup(
            SignupRequest(
                username = username.trim(),
                email = email.trim(),
                phone_number = phone.trim(),
                password = password,
                role = role
            )
        )
        login(username, password)
    }

    fun logout() {
        viewModelScope.launch {
            SessionStore.clearTokens()
            _state.update {
                it.copy(
                    loggedIn = false,
                    profile = null,
                    bookings = emptyList(),
                    disputes = emptyList(),
                    selectedVehicle = null
                )
            }
            loadVehicles()
        }
    }

    fun loadVehicles() = launchOp(showLoading = _state.value.vehicles.isEmpty()) {
        val list = ApiFactory.api().vehicles()
        _state.update { it.copy(vehicles = list) }
    }

    fun loadVehicle(id: Int) = launchOp {
        val vehicle = ApiFactory.api().vehicle(id)
        _state.update { it.copy(selectedVehicle = vehicle) }
    }

    fun refreshAll() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        try {
            val vehicles = ApiFactory.api().vehicles()
            val profile = runCatching { ApiFactory.api().profile() }.getOrNull()
            val bookings = runCatching { ApiFactory.api().bookings() }.getOrDefault(emptyList())
            val disputes = runCatching { ApiFactory.api().disputes() }.getOrDefault(emptyList())
            _state.update {
                it.copy(
                    loading = false,
                    vehicles = vehicles,
                    profile = profile ?: it.profile,
                    bookings = bookings,
                    disputes = disputes,
                    loggedIn = profile != null || it.loggedIn
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(loading = false, error = e.userMessage()) }
        }
    }

    fun bookVehicle(start: String, end: String) = launchOp {
        val vehicle = _state.value.selectedVehicle ?: error("Select a vehicle first")
        val days = rentalDays(start, end)
        val total = (vehicle.price_per_day.toDoubleOrNull() ?: 0.0) * days
        ApiFactory.api().createBooking(
            CreateBookingRequest(
                vehicle = vehicle.id,
                start_date = start,
                end_date = end,
                total_amount = "%.2f".format(total)
            )
        )
        _state.update { it.copy(message = "Booking requested. Waiting for host approval.") }
        refreshAll()
    }

    fun approve(id: Int) = launchOp {
        ApiFactory.api().approveBooking(id)
        _state.update { it.copy(message = "Booking approved and escrow hold placed.") }
        refreshAll()
    }

    fun cancel(id: Int) = launchOp {
        ApiFactory.api().cancelBooking(id)
        _state.update { it.copy(message = "Booking cancelled.") }
        refreshAll()
    }

    fun listVehicle(
        make: String,
        model: String,
        year: String,
        plate: String,
        price: String,
        location: String,
        photo: Uri?
    ) = launchOp {
        val ctx = getApplication<Application>()
        val text = "text/plain".toMediaType()
        ApiFactory.api().createVehicle(
            make = make.toRequestBody(text),
            model = model.toRequestBody(text),
            year = year.toRequestBody(text),
            plate = plate.toRequestBody(text),
            price = price.toRequestBody(text),
            location = location.toRequestBody(text),
            photo = photo?.let { ctx.uriToPart("vehicle_photo", it, "vehicle.jpg") }
        )
        _state.update { it.copy(message = "Vehicle listed. ANPR check runs if a photo was attached.") }
        refreshAll()
    }

    fun verifyId(cnic: String, license: String, idDoc: Uri, selfie: Uri) = launchOp {
        val ctx = getApplication<Application>()
        val text = "text/plain".toMediaType()
        val response = ApiFactory.api().verifyId(
            cnic = cnic.toRequestBody(text),
            license = license.toRequestBody(text),
            idDocument = ctx.uriToPart("id_document", idDoc, "id.jpg"),
            selfiePhoto = ctx.uriToPart("selfie_photo", selfie, "selfie.jpg")
        )
        val body = response.body()
        val msg = body?.message ?: parseApiError(response.errorBody()?.string())
        if (response.isSuccessful) {
            _state.update { it.copy(message = msg) }
            refreshAll()
        } else {
            error(msg)
        }
    }

    fun submitCheckin(
        bookingId: Int,
        type: String,
        odometer: String,
        lat: Double,
        lng: Double,
        front: Uri,
        rear: Uri,
        left: Uri,
        right: Uri
    ) = launchOp {
        val ctx = getApplication<Application>()
        val text = "text/plain".toMediaType()
        val response = ApiFactory.api().createCheckin(
            booking = bookingId.toString().toRequestBody(text),
            type = type.toRequestBody(text),
            odometer = odometer.toRequestBody(text),
            lat = "%.6f".format(lat).toRequestBody(text),
            lng = "%.6f".format(lng).toRequestBody(text),
            front = ctx.uriToPart("front_photo", front, "front.jpg"),
            rear = ctx.uriToPart("rear_photo", rear, "rear.jpg"),
            left = ctx.uriToPart("left_photo", left, "left.jpg"),
            right = ctx.uriToPart("right_photo", right, "right.jpg")
        )
        if (response.isSuccessful) {
            _state.update { it.copy(message = "$type inspection saved.") }
            refreshAll()
        } else {
            error(parseApiError(response.errorBody()?.string()))
        }
    }

    fun submitDispute(bookingId: Int, description: String, evidence: Uri?) = launchOp {
        val ctx = getApplication<Application>()
        val text = "text/plain".toMediaType()
        ApiFactory.api().createDispute(
            booking = bookingId.toString().toRequestBody(text),
            description = description.toRequestBody(text),
            evidence = evidence?.let { ctx.uriToPart("evidence_photo", it, "evidence.jpg") }
        )
        _state.update { it.copy(message = "Dispute opened.") }
        refreshAll()
    }

    private fun launchOp(showLoading: Boolean = true, block: suspend () -> Unit) {
        viewModelScope.launch {
            if (showLoading) _state.update { it.copy(loading = true, error = null, message = null) }
            try {
                block()
                _state.update { it.copy(loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.userMessage()) }
            }
        }
    }

    private fun rentalDays(start: String, end: String): Long {
        return try {
            val s = start.split("-").map { it.toInt() }
            val e = end.split("-").map { it.toInt() }
            val startCal = Calendar.getInstance().apply { set(s[0], s[1] - 1, s[2], 0, 0, 0) }
            val endCal = Calendar.getInstance().apply { set(e[0], e[1] - 1, e[2], 0, 0, 0) }
            TimeUnit.MILLISECONDS.toDays(endCal.timeInMillis - startCal.timeInMillis).coerceAtLeast(1)
        } catch (_: Exception) {
            1
        }
    }
}

private fun Exception.userMessage(): String {
    return when (this) {
        is HttpException -> parseApiError(response()?.errorBody()?.string())
        else -> message ?: "Something went wrong. Is the Django API running?"
    }
}
