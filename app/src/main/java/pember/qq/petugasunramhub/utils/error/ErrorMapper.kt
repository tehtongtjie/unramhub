package pember.qq.petugasunramhub.utils.error

import java.io.IOException
import java.net.SocketTimeoutException

object ErrorMapper {
    fun map(throwable: Throwable): AppError {
        return when (throwable) {
            is AppException -> throwable.error
            is SocketTimeoutException -> AppError.NetworkError("Koneksi timeout. Silakan coba lagi.", throwable)
            is IOException -> AppError.NetworkError("Gagal terhubung ke server. Periksa koneksi internet Anda.", throwable)
            is SecurityException -> AppError.AuthorizationError("Izin ditolak atau masalah keamanan sistem.", throwable)
            is IllegalArgumentException -> AppError.ValidationError("Format data tidak valid: ${throwable.message}", throwable)
            else -> AppError.UnknownError(throwable.message ?: "Terjadi kesalahan yang tidak diketahui.", throwable)
        }
    }
}
