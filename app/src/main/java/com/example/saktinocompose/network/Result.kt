// 6. Result Wrapper untuk handle success/error
// File: app/src/main/java/com/example/saktinocompose/network/Result.kt

package com.example.saktinocompose.network

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception, val message: String? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// Extension function untuk convert Result
fun <T> Result<T>.getDataOrNull(): T? = when (this) {
    is Result.Success -> data
    else -> null
}

fun <T> Result<T>.getErrorOrNull(): String? = when (this) {
    is Result.Error -> message ?: exception.message
    else -> null
}