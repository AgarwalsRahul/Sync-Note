package com.notesync.notes.business.data.util

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.notesync.notes.business.data.cache.CacheConstants.CACHE_TIMEOUT
import com.notesync.notes.business.data.cache.CacheErrors.CACHE_ERROR_TIMEOUT
import com.notesync.notes.business.data.cache.CacheErrors.CACHE_ERROR_UNKNOWN
import com.notesync.notes.business.data.cache.CacheResult
import com.notesync.notes.business.data.network.ApiResult
import com.notesync.notes.business.data.network.NetworkConstants.NETWORK_TIMEOUT
import com.notesync.notes.business.data.network.NetworkErrors.NETWORK_ERROR_TIMEOUT
import com.notesync.notes.business.data.network.NetworkErrors.NETWORK_ERROR_UNKNOWN
import com.notesync.notes.business.data.util.GenericErrors.ERROR_UNKNOWN
import com.notesync.notes.util.cLog
import com.notesync.notes.util.printLogD
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException


suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T?
): ApiResult<T?> {
    return withContext(dispatcher) {
        try {
            // throws TimeoutCancellationException
            withTimeout(NETWORK_TIMEOUT) {
                ApiResult.Success(apiCall.invoke())
            }
        } catch (throwable: Throwable) {
            cLog(throwable.message)
            throwable.printStackTrace()
            when (throwable) {
                is TimeoutCancellationException -> {
                    val code = 408 // timeout error code
                    ApiResult.GenericError(code, NETWORK_ERROR_TIMEOUT)
                }
                is IOException -> {
                    ApiResult.NetworkError
                }
                is FirebaseAuthException -> {
                    val errorMessage = when (throwable.errorCode) {
                        "ERROR_INVALID_EMAIL" -> FirebaseErrors.ERROR_INVALID_EMAIL
                        "ERROR_WRONG_PASSWORD" -> FirebaseErrors.ERROR_WRONG_PASSWORD
                        "ERROR_USER_NOT_FOUND" -> FirebaseErrors.ERROR_USER_NOT_FOUND
                        "ERROR_USER_DISABLED" -> FirebaseErrors.ERROR_USER_DISABLED
                        "ERROR_TOO_MANY_REQUESTS" -> FirebaseErrors.ERROR_TOO_MANY_REQUESTS
                        "ERROR_OPERATION_NOT_ALLOWED" -> FirebaseErrors.ERROR_OPERATION_NOT_ALLOWED
                        "ERROR_WEAK_PASSWORD" -> FirebaseErrors.ERROR_WEAK_PASSWORD
                        "ERROR_EMAIL_ALREADY_IN_USE" -> FirebaseErrors.ERROR_EMAIL_ALREADY_IN_USE
                        "ERROR_INVALID_CREDENTIAL" -> FirebaseErrors.ERROR_INVALID_CREDENTIAL
                        else -> {
                            if (!throwable.message.isNullOrBlank()) {
                                throwable.message
                            } else {
                                ERROR_UNKNOWN
                            }
                        }
                    }
                    ApiResult.FirebaseError(throwable.errorCode, errorMessage)
                }
                is FirebaseException -> {
                    val errorMessage =
                        if (!throwable.message.isNullOrEmpty()) throwable.message else ERROR_UNKNOWN
                    ApiResult.FirebaseError(null, errorMessage)
                }
                is FirebaseFirestoreException->{
                    val errorMessage =
                        if (!throwable.message.isNullOrEmpty()) throwable.message else ERROR_UNKNOWN
                    ApiResult.FirebaseError(null, errorMessage)
                }
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = convertErrorBody(throwable)
                    ApiResult.GenericError(
                        code,
                        errorResponse
                    )
                }
                else -> {
                    ApiResult.GenericError(
                        null,
                        NETWORK_ERROR_UNKNOWN
                    )
                }
            }
        }
    }
}

suspend fun <T> safeCacheCall(
    dispatcher: CoroutineDispatcher,
    cacheCall: suspend () -> T?
): CacheResult<T?> {
    return withContext(dispatcher) {
        try {
            // throws TimeoutCancellationException
            withTimeout(CACHE_TIMEOUT) {
                CacheResult.Success(cacheCall.invoke())
            }
        } catch (throwable: Throwable) {
            cLog(throwable.message)
            printLogD("CacheResult", throwable.message!!)
            throwable.printStackTrace()
            when (throwable) {

                is TimeoutCancellationException -> {
                    CacheResult.GenericError(CACHE_ERROR_TIMEOUT)
                }
                else -> {
                    CacheResult.GenericError(CACHE_ERROR_UNKNOWN)
                }
            }
        }
    }
}

private fun convertErrorBody(throwable: HttpException): String? {
    return try {
        throwable.response()?.errorBody()?.string()
    } catch (exception: Exception) {
        ERROR_UNKNOWN
    }
}


