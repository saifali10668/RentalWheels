package com.example.rentalwheels.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @POST("auth/login/")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @POST("auth/signup/")
    suspend fun signup(@Body body: SignupRequest): UserProfile

    @GET("auth/profile/")
    suspend fun profile(): UserProfile

    @Multipart
    @POST("auth/verify-id/")
    suspend fun verifyId(
        @Part("cnic_number") cnic: RequestBody,
        @Part("license_number") license: RequestBody,
        @Part idDocument: MultipartBody.Part,
        @Part selfiePhoto: MultipartBody.Part
    ): Response<VerifyIdResponse>

    @GET("vehicles/")
    suspend fun vehicles(): List<Vehicle>

    @GET("vehicles/{id}/")
    suspend fun vehicle(@Path("id") id: Int): Vehicle

    @Multipart
    @POST("vehicles/")
    suspend fun createVehicle(
        @Part("make") make: RequestBody,
        @Part("model") model: RequestBody,
        @Part("year") year: RequestBody,
        @Part("plate_number") plate: RequestBody,
        @Part("price_per_day") price: RequestBody,
        @Part("location") location: RequestBody,
        @Part photo: MultipartBody.Part?
    ): Vehicle

    @GET("bookings/")
    suspend fun bookings(): List<Booking>

    @POST("bookings/")
    suspend fun createBooking(@Body body: CreateBookingRequest): Booking

    @PATCH("bookings/{id}/approve/")
    suspend fun approveBooking(@Path("id") id: Int): Booking

    @PATCH("bookings/{id}/cancel/")
    suspend fun cancelBooking(@Path("id") id: Int): Booking

    @Multipart
    @POST("checkins/")
    suspend fun createCheckin(
        @Part("booking") booking: RequestBody,
        @Part("checkin_type") type: RequestBody,
        @Part("odometer_reading") odometer: RequestBody,
        @Part("gps_latitude") lat: RequestBody,
        @Part("gps_longitude") lng: RequestBody,
        @Part front: MultipartBody.Part,
        @Part rear: MultipartBody.Part,
        @Part left: MultipartBody.Part,
        @Part right: MultipartBody.Part
    ): Response<Unit>

    @GET("disputes/")
    suspend fun disputes(): List<Dispute>

    @Multipart
    @POST("disputes/")
    suspend fun createDispute(
        @Part("booking") booking: RequestBody,
        @Part("description") description: RequestBody,
        @Part evidence: MultipartBody.Part?
    ): Dispute
}
