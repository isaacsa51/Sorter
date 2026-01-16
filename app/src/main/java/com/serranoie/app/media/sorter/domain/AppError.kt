package com.serranoie.app.media.sorter.domain

/**
 * Sealed class representing application errors with user-friendly messages.
 * Each error type has a specific message and optional details.
 */
sealed class AppError {
    abstract val message: String
    abstract val details: String?

    data class MediaLoadError(
        override val message: String = "Failed to load media files",
        override val details: String? = null,
        val cause: Throwable? = null
    ) : AppError()

    data class PermissionError(
        override val message: String = "Permission required",
        override val details: String? = "Please grant storage permissions to access media files"
    ) : AppError()

    data class NoMediaFoundError(
        override val message: String = "No media files found",
        override val details: String? = "Your device doesn't have any images or videos"
    ) : AppError()

    data class NetworkError(
        override val message: String = "Network error",
        override val details: String? = null
    ) : AppError()

    data class UnknownError(
        override val message: String = "An unexpected error occurred",
        override val details: String? = null,
        val cause: Throwable? = null
    ) : AppError()

    data class StorageAccessError(
        override val message: String = "Cannot access storage",
        override val details: String? = "Unable to read from device storage"
    ) : AppError()

    fun getFullMessage(): String {
        return if (details != null) {
            "$message: $details"
        } else {
            message
        }
    }
    
    companion object {
        fun fromThrowable(throwable: Throwable): AppError {
            return when (throwable) {
                is SecurityException -> PermissionError(
                    details = throwable.message
                )
                is IllegalStateException -> StorageAccessError(
                    details = throwable.message
                )
                else -> UnknownError(
                    details = throwable.message,
                    cause = throwable
                )
            }
        }
    }
}
