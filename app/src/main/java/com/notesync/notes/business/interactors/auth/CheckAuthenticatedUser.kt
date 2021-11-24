package com.notesync.notes.business.interactors.auth

import android.content.SharedPreferences
import android.util.Log

import com.notesync.notes.business.data.cache.CacheResponseHandler
import com.notesync.notes.business.data.cache.abstraction.AuthCacheDataSource
import com.notesync.notes.business.data.util.safeCacheCall
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.framework.dataSource.preferences.PreferenceKeys
import com.notesync.notes.framework.presentation.auth.state.AuthViewState
import com.notesync.notes.util.Constants.TAG
import com.notesync.notes.util.printLogD
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CheckAuthenticatedUser(
    private val cacheDataSource: AuthCacheDataSource,
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        const val NO_USER_FOUND = "No user found."
        const val USER_FOUND = "User found"
    }

    fun checkPreviousAuthUser(stateEvent: StateEvent?): Flow<DataState<AuthViewState>?> = flow {
        val previousAuthUserEmail: String? =
            sharedPreferences.getString(PreferenceKeys.USER_PREFERENCES, null)

        val deviceId: String? = sharedPreferences.getString(PreferenceKeys.DEVICE_PREFERENCES, null)
        val sk: String? =
            sharedPreferences.getString(PreferenceKeys.ENCRYPT_DECRYPT_PREFERENCES, null)
        printLogD("CHECK PREVIOUS AUTH USER","$sk")

        if (previousAuthUserEmail.isNullOrBlank() || deviceId.isNullOrBlank() || sk.isNullOrBlank()) {
            Log.d(TAG, "checkPreviousAuthUser: No previously authenticated user found.")
            emit(returnNoTokenFound(stateEvent))
        } else {
            printLogD("CHECK PREVIOUS AUTH USER","$previousAuthUserEmail")
            val cacheResult = safeCacheCall(IO) {
                cacheDataSource.retrieveUser(previousAuthUserEmail)
            }
            printLogD("CHECK PREVIOUS AUTH USER","$cacheResult")
            emit(
                object : CacheResponseHandler<AuthViewState, User?>(cacheResult, stateEvent) {
                    override suspend fun handleSuccess(resultObj: User?): DataState<AuthViewState>? {
                        if (resultObj != null) {
                            val result = User(
                                email = resultObj.email,
                                id = resultObj.id,
                                deviceId = deviceId,
                                sk=sk
                            )
                            return DataState.data(
                                Response(USER_FOUND, UIComponentType.None(), MessageType.Success()),
                                AuthViewState(user = result), stateEvent
                            )
                        } else {
                            return returnNoTokenFound(stateEvent)
                        }
                    }


                }.getResult()
            )
        }

    }

    private fun returnNoTokenFound(
        stateEvent: StateEvent?
    ): DataState<AuthViewState> {

        return DataState.data(
            response = Response(
                NO_USER_FOUND,
                UIComponentType.None(),
                MessageType.Error()
            ), null,
            stateEvent = stateEvent
        )
    }
}