package pember.qq.petugasunramhub.utils.location

import android.location.Location

data class LocationWithProvider(
    val location: Location,
    val provider: String
)

interface LocationProvider {
    fun isLocationEnabled(): Boolean
    suspend fun getCurrentLocation(): Result<LocationWithProvider>
}
