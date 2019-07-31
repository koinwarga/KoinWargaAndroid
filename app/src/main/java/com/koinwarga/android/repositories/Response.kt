package com.koinwarga.android.repositories

sealed class Response<out T> {

    data class Success<out T>(val body: T): Response<T>()
    data class Error<out T>(val code: ErrorCode, val message: String): Response<T>()

    enum class ErrorCode {
        ERROR_EMPTY,
        ERROR_CONNECTION
    }

}