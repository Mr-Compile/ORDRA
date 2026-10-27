package com.ordra.core.common

/** Domain/app error categories per the Error & Validation Contract. */
sealed interface AppError {
    data class ValidationError(val field: String, val message: String) : AppError
    data class NotFound(val entityId: String) : AppError
    data class DatabaseFailure(val cause: Throwable) : AppError
    data class FileAccessFailure(val cause: Throwable? = null) : AppError
    data class PermissionRequired(val permission: String) : AppError
    data class OcrFailure(val cause: Throwable? = null) : AppError
    data object NetworkUnavailable : AppError
    data class GroqFailure(val cause: Throwable? = null) : AppError
    data object RateLimited : AppError
    data object InvalidAiResponse : AppError
    data class BackupInvalid(val reason: String? = null) : AppError
    data class UnknownFailure(val cause: Throwable? = null) : AppError
}
