package com.example.rentalwheels.data

data class TokenResponse(
    val access: String = "",
    val refresh: String? = null
)

data class RefreshRequest(val refresh: String)

data class LoginRequest(
    val username: String,
    val password: String
)

data class SignupRequest(
    val username: String,
    val email: String,
    val phone_number: String,
    val password: String,
    val role: String
)

data class UserProfile(
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    val phone_number: String? = null,
    val role: String = "RENTER",
    val cnic_number: String? = null,
    val license_number: String? = null,
    val is_id_verified: Boolean = false,
    val verification_status: String = "UNVERIFIED",
    val verification_score: Double = 0.0,
    val id_document: String? = null,
    val selfie_photo: String? = null
)

data class VerifyIdResponse(
    val message: String? = null,
    val is_id_verified: Boolean? = null,
    val verification_status: String? = null,
    val verification_score: Double? = null,
    val detail: String? = null
)

data class Vehicle(
    val id: Int = 0,
    val owner: Int? = null,
    val owner_username: String = "",
    val owner_verified: Boolean = false,
    val make: String = "",
    val model: String = "",
    val year: Int = 0,
    val plate_number: String = "",
    val is_verified: Boolean = false,
    val is_flagged_stolen: Boolean = false,
    val registration_doc: String? = null,
    val vehicle_photo: String? = null,
    val price_per_day: String = "0",
    val location: String = "",
    val status: String = "AVAILABLE",
    val created_at: String? = null,
    val updated_at: String? = null
)

data class Booking(
    val id: Int = 0,
    val renter: Int? = null,
    val renter_username: String = "",
    val renter_verified: Boolean = false,
    val vehicle: Int = 0,
    val vehicle_details: Vehicle? = null,
    val start_date: String = "",
    val end_date: String = "",
    val total_amount: String = "0",
    val status: String = "PENDING",
    val payment_status: String = "UNPAID",
    val created_at: String? = null,
    val updated_at: String? = null
)

data class CreateBookingRequest(
    val vehicle: Int,
    val start_date: String,
    val end_date: String,
    val total_amount: String
)

data class Dispute(
    val id: Int = 0,
    val booking: Int = 0,
    val raised_by: Int? = null,
    val raised_by_username: String = "",
    val description: String = "",
    val evidence_photo: String? = null,
    val status: String = "OPEN",
    val admin_notes: String? = null,
    val created_at: String? = null
)
