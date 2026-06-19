package pember.qq.petugasunramhub.data.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pember.qq.petugasunramhub.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {
    companion object {
        private const val TAG = "RetrofitClient"

        @Volatile
        private var _instance: SupabaseApi? = null

        val instance: SupabaseApi
            get() = _instance ?: synchronized(this) {
                _instance ?: createInstance().also { _instance = it }
            }

        private fun createInstance(): SupabaseApi {
            return try {
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }

                val httpClient = OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val key = try { BuildConfig.SUPABASE_KEY } catch (e: Throwable) { "missing-key" }
                        val request = chain.request().newBuilder()
                            .addHeader("apikey", key)
                            .addHeader("Authorization", "Bearer $key")
                            .addHeader("Content-Type", "application/json")
                            .build()
                        chain.proceed(request)
                    }
                    .addInterceptor(loggingInterceptor)
                    .build()

                val rawUrl = try { BuildConfig.SUPABASE_URL } catch (e: Throwable) { null }
                val baseUrl = when {
                    rawUrl.isNullOrBlank() || rawUrl == "null" -> "https://placeholder.supabase.co/"
                    rawUrl.endsWith("/") -> rawUrl
                    else -> "$rawUrl/"
                }

                Log.d(TAG, "Creating Retrofit instance with baseUrl: ${baseUrl}rest/v1/")

                Retrofit.Builder()
                    .baseUrl("${baseUrl}rest/v1/")
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(SupabaseApi::class.java)
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to create SupabaseApi instance", e)
                throw RuntimeException("Retrofit initialization failed: ${e.message}", e)
            }
        }
    }
}
