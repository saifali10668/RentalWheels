package com.example.rentalwheels

import android.app.Application

class RentalWheelsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionStore.init(this)
    }
}
