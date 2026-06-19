package pember.qq.petugasunramhub.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pember.qq.petugasunramhub.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                // Mengambil API Key dari BuildConfig secara aman
                val key = try { BuildConfig.SUPABASE_KEY } catch (e: Throwable) { "" }
                val request = chain.request().newBuilder()
                    .addHeader("apikey", key)
                    .addHeader("Authorization", "Bearer $key")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    val instance: SupabaseApi by lazy {
        // Mengambil Base URL dan memastikan diakhiri dengan slash agar Retrofit tidak crash
        val rawUrl = try { BuildConfig.SUPABASE_URL } catch (e: Throwable) { "" }
        val baseUrl = when {
            rawUrl.isNullOrBlank() || rawUrl == "null" -> "https://placeholder.supabase.co/"
            rawUrl.endsWith("/") -> rawUrl
            else -> "$rawUrl/"
        }

        Retrofit.Builder()
            .baseUrl("${baseUrl}rest/v1/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApi::class.java)
    }
}
