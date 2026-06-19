package pember.qq.petugasunramhub.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pember.qq.petugasunramhub.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val httpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
        
        // Add Logging Interceptor safely
        try {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(logging)
        } catch (e: Throwable) {
            android.util.Log.e("RetrofitClient", "Failed to add logging interceptor", e)
        }

        builder.addInterceptor { chain ->
            // Mengambil API Key secara lebih aman
            val key = try {
                BuildConfig.SUPABASE_KEY
            } catch (e: Throwable) {
                android.util.Log.e("RetrofitClient", "Failed to access SUPABASE_KEY", e)
                ""
            }
            
            val request = chain.request().newBuilder()
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        
        builder.build()
    }

    val instance: SupabaseApi by lazy {
        val rawUrl = try {
            BuildConfig.SUPABASE_URL
        } catch (e: Throwable) {
            android.util.Log.e("RetrofitClient", "Failed to access SUPABASE_URL", e)
            ""
        }
        
        val baseUrl = when {
            rawUrl.isNullOrBlank() || rawUrl == "null" -> "https://placeholder.supabase.co/"
            rawUrl.endsWith("/") -> rawUrl
            else -> "$rawUrl/"
        }

        try {
            Retrofit.Builder()
                .baseUrl("${baseUrl}rest/v1/")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SupabaseApi::class.java)
        } catch (e: Throwable) {
            android.util.Log.e("RetrofitClient", "Failed to create SupabaseApi", e)
            // Fallback empty implementation or rethrow as a clearer exception
            throw RuntimeException("Retrofit initialization failed: ${e.message}", e)
        }
    }
}
