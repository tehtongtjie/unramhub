package pember.qq.petugasunramhub.utils.error

sealed interface AppError {
    val message: String
    val technicalCause: Throwable?

    data class ValidationError(override val message: String, override val technicalCause: Throwable? = null) : AppError
    data class NetworkError(override val message: String, override val technicalCause: Throwable? = null) : AppError
    data class AuthenticationError(override val message: String, override val technicalCause: Throwable? = null) : AppError
    data class AuthorizationError(override val message: String, override val technicalCause: Throwable? = null) : AppError
    data class UploadError(override val message: String, override val technicalCause: Throwable? = null) : AppError
    data class ConfigurationError(override val message: String, override val technicalCause: Throwable? = null) : AppError
    data class LocationError(override val message: String, override val technicalCause: Throwable? = null) : AppError
    data class UnknownError(override val message: String, override val technicalCause: Throwable? = null) : AppError
}

class AppException(val error: AppError) : Exception(error.message, error.technicalCause)
