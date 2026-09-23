package com.example.rentalwheels

import android.content.Context
import android.net.Uri
import com.example.rentalwheels.data.ApiService
import com.example.rentalwheels.data.RefreshRequest
import com.example.rentalwheels.data.TokenResponse
import com.google.gson.Gson
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

object ApiFactory {
    private val serviceRef = AtomicReference<ApiService?>(null)
    private val gson = Gson()

    fun api(): ApiService {
        serviceRef.get()?.let { return it }
        synchronized(this) {
            serviceRef.get()?.let { return it }
            val created = build(SessionStore.apiBaseBlocking())
            serviceRef.set(created)
            return created
        }
    }

    fun invalidate() {
        serviceRef.set(null)
    }

    fun rewriteMediaUrl(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val origin = SessionStore.apiBaseBlocking()
            .removeSuffix("/")
            .removeSuffix("/api/v1")
            .trimEnd('/')
        return if (raw.startsWith("/")) {
            "$origin$raw"
        } else {
            raw
                .replace("http://127.0.0.1:8000", origin)
                .replace("http://localhost:8000", origin)
                .replace("http://127.0.0.1", origin)
                .replace("http://localhost", origin)
        }
    }

    private fun build(baseUrl: String): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor())
            .authenticator(TokenAuthenticator(gson))
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}

private class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = SessionStore.accessTokenBlocking()
        val builder = original.newBuilder()
        if (!token.isNullOrBlank() && original.header("Authorization") == null) {
            builder.header("Authorization", "Bearer $token")
        }
        return chain.proceed(builder.build())
    }
}

private class TokenAuthenticator(private val gson: Gson) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null
        val refresh = SessionStore.refreshTokenBlocking() ?: return null
        val base = SessionStore.apiBaseBlocking()
        val refreshUrl = "${base.trimEnd('/')}/auth/refresh/"
        val body = gson.toJson(RefreshRequest(refresh))
            .toRequestBody("application/json; charset=utf-8".toMediaType())
        val refreshRequest = Request.Builder()
            .url(refreshUrl)
            .post(body)
            .build()
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .build()
        client.newCall(refreshRequest).execute().use { refreshResponse ->
            if (!refreshResponse.isSuccessful) return null
            val payload = gson.fromJson(refreshResponse.body?.string(), TokenResponse::class.java)
            if (payload.access.isBlank()) return null
            kotlinx.coroutines.runBlocking {
                SessionStore.saveTokens(payload.access, payload.refresh)
            }
            return response.request.newBuilder()
                .header("Authorization", "Bearer ${payload.access}")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}

fun Context.uriToPart(field: String, uri: Uri, filename: String = "upload.jpg"): MultipartBody.Part {
    val type = contentResolver.getType(uri) ?: "image/jpeg"
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
    val body = bytes.toRequestBody(type.toMediaType())
    return MultipartBody.Part.createFormData(field, filename, body)
}

fun parseApiError(raw: String?): String {
    if (raw.isNullOrBlank()) return "Request failed"
    return try {
        val obj = com.google.gson.JsonParser.parseString(raw).asJsonObject
        when {
            obj.has("detail") -> obj.get("detail").asString
            obj.has("message") -> obj.get("message").asString
            else -> obj.entrySet().joinToString("\n") { (k, v) -> "$k: $v" }
        }
    } catch (_: Exception) {
        raw
    }
}
