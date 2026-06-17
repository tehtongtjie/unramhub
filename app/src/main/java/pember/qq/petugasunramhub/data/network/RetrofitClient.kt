package pember.qq.petugasunramhub.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pember.qq.petugasunramhub.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    val instance: SupabaseApi by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                    .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .build()

        val rawUrl = BuildConfig.SUPABASE_URL
        val baseUrl = if (rawUrl.isNullOrBlank() || rawUrl == "null") {
            "https://placeholder.supabase.co/"
        } else if (rawUrl.endsWith("/")) {
            rawUrl
        } else {
            "$rawUrl/"
        }

        Retrofit.Builder()
            .baseUrl("${baseUrl}rest/v1/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApi::class.java)
    }
}
