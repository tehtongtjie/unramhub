package pember.qq.petugasunramhub.utils.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import pember.qq.petugasunramhub.utils.error.AppError
import pember.qq.petugasunramhub.utils.error.AppException
import java.util.concurrent.Executor
import kotlin.coroutines.resume

class GPSLocationProvider(private val context: Context) : LocationProvider {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<LocationWithProvider> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }

        if (provider == null) {
            return@withContext Result.failure(
                AppException(AppError.LocationError("Layanan lokasi tidak aktif. Aktifkan GPS atau lokasi jaringan."))
            )
        }

        kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            val cancellationSignal = CancellationSignal()
            
            continuation.invokeOnCancellation {
                cancellationSignal.cancel()
            }

            try {
                val executor = Executor { command ->
                    if (!continuation.isCancelled) {
                        command.run()
                    }
                }

                LocationManagerCompat.getCurrentLocation(
                    locationManager,
                    provider,
                    cancellationSignal,
                    executor
                ) { location ->
                    if (continuation.isActive) {
                        if (location != null) {
                            continuation.resume(Result.success(LocationWithProvider(location, provider)))
                        } else {
                            continuation.resume(
                                Result.failure(AppException(AppError.LocationError("Lokasi tidak tersedia. Coba lagi dalam beberapa saat.")))
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resume(
                        Result.failure(AppException(pember.qq.petugasunramhub.utils.error.ErrorMapper.map(e)))
                    )
                }
            }
        }
    }
}
