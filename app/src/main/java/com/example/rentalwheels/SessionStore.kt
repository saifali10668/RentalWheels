package com.example.rentalwheels

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore("rental_wheels_session")

object SessionStore {
    private const val DEFAULT_API = "http://10.0.2.2:8000/api/v1/"
    private val ACCESS = stringPreferencesKey("access")
    private val REFRESH = stringPreferencesKey("refresh")
    private val API_BASE = stringPreferencesKey("api_base")

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val apiBaseFlow: Flow<String>
        get() = appContext.dataStore.data.map { it[API_BASE] ?: DEFAULT_API }

    suspend fun apiBase(): String =
        appContext.dataStore.data.first()[API_BASE] ?: DEFAULT_API

    fun apiBaseBlocking(): String = runBlocking { apiBase() }

    suspend fun accessToken(): String? =
        appContext.dataStore.data.first()[ACCESS]

    fun accessTokenBlocking(): String? = runBlocking { accessToken() }

    suspend fun refreshToken(): String? =
        appContext.dataStore.data.first()[REFRESH]

    fun refreshTokenBlocking(): String? = runBlocking { refreshToken() }

    suspend fun saveApiBase(url: String) {
        var raw = url.trim()
        if (raw.isBlank()) raw = DEFAULT_API
        raw = raw.replace(".8000", ":8000")
        if (!raw.startsWith("http://") && !raw.startsWith("https://")) {
            raw = "http://$raw"
        }
        if (!raw.endsWith("/")) {
            raw = "$raw/"
        }
        if (!raw.contains("/api/v1/")) {
            raw = if (raw.endsWith("api/")) "${raw}v1/" else "${raw}api/v1/"
        }
        appContext.dataStore.edit { it[API_BASE] = raw }
        ApiFactory.invalidate()
    }

    suspend fun saveTokens(access: String, refresh: String?) {
        appContext.dataStore.edit { prefs ->
            prefs[ACCESS] = access
            if (!refresh.isNullOrBlank()) prefs[REFRESH] = refresh
        }
    }

    suspend fun clearTokens() {
        appContext.dataStore.edit {
            it.remove(ACCESS)
            it.remove(REFRESH)
        }
    }
}
