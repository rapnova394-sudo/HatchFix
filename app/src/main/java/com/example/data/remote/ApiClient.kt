package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Modular API client provider for WatchFlix.
 * Allows easy switching of the Node.js server base URL without recompiling.
 * Communicates strictly over HTTPS. Never holds or leaks Supabase private keys.
 */
object ApiClient {

  // Default placeholder URL for the user's Node.js server
  const val DEFAULT_BASE_URL = "https://watchflix-api.internal/"

  @Volatile
  private var currentBaseUrl: String = DEFAULT_BASE_URL

  private val moshi: Moshi by lazy {
    Moshi.Builder()
      .addLast(KotlinJsonAdapterFactory())
      .build()
  }

  private val okHttpClient: OkHttpClient by lazy {
    val logging = HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BASIC
    }

    OkHttpClient.Builder()
      // Generous timeouts for smartwatch Bluetooth tethering / low-power Wi-Fi
      .connectTimeout(15, TimeUnit.SECONDS)
      .readTimeout(20, TimeUnit.SECONDS)
      .writeTimeout(15, TimeUnit.SECONDS)
      .addInterceptor(logging)
      .addInterceptor { chain ->
        // Standard user-agent identifying Wear OS client without leaking sensitive tokens
        val request = chain.request().newBuilder()
          .header("User-Agent", "WatchFlix-WearOS/1.0 (PixelWatch)")
          .header("Accept", "application/json")
          .build()
        chain.proceed(request)
      }
      .build()
  }

  @Volatile
  private var apiServiceInstance: WatchFlixApiService? = null

  fun getService(): WatchFlixApiService {
    return apiServiceInstance ?: synchronized(this) {
      apiServiceInstance ?: buildService(currentBaseUrl).also { apiServiceInstance = it }
    }
  }

  fun updateBaseUrl(newUrl: String) {
    var formatted = newUrl.trim()
    if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
      formatted = "https://$formatted"
    }
    if (!formatted.endsWith("/")) {
      formatted = "$formatted/"
    }
    synchronized(this) {
      currentBaseUrl = formatted
      apiServiceInstance = buildService(currentBaseUrl)
    }
  }

  fun getBaseUrl(): String = currentBaseUrl

  private fun buildService(url: String): WatchFlixApiService {
    val sanitizedUrl = if (url.endsWith("/")) url else "$url/"
    return Retrofit.Builder()
      .baseUrl(sanitizedUrl)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(WatchFlixApiService::class.java)
  }
}
