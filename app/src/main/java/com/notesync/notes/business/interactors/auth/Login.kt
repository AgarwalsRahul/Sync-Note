package com.notesync.notes.business.interactors.auth

import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.notesync.notes.business.data.cache.abstraction.AuthCacheDataSource
import com.notesync.notes.business.data.network.ApiResponseHandler
import com.notesync.notes.business.data.network.abstraction.AuthNetworkDataSource
import com.notesync.notes.business.data.util.safeApiCall
import com.notesync.notes.business.domain.model.User
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.framework.dataSource.network.implementation.NoteFirestoreServiceImpl.Companion.DEVICES_COLLECTION
import com.notesync.notes.framework.dataSource.network.implementation.NoteFirestoreServiceImpl.Companion.USERS_COLLECTION
import com.notesync.notes.framework.dataSource.preferences.PreferenceKeys
import com.notesync.notes.framework.presentation.auth.state.AuthViewState
import com.notesync.notes.framework.presentation.auth.state.LoginFields
import com.notesync.notes.util.printLogD
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.*

class Login(
    private val authCacheDataSource: AuthCacheDataSource,
    private val authNetworkDataSource: AuthNetworkDataSource,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
) {
    companion object {
        const val LOGIN_FAILURE = "Unable to login. Please try again."
        const val LOGIN_SUCCESS = "Successfully logged in"
    }

    @ExperimentalCoroutinesApi
    fun login(
        email: String,
        password: String,
        stateEvent: StateEvent
    ): Flow<DataState<AuthViewState>?> = flow {
        val loginFieldErrors = LoginFields(email, password).isValidForLogin()
        if (loginFieldErrors == LoginFields.LoginError.none()) {
            val apiResult = safeApiCall(IO) {
                authNetworkDataSource.login(email, password)
            }

            emit(
                object : ApiResponseHandler<AuthViewState, User?>(apiResult, stateEvent) {
                    override suspend fun handleSuccess(resultObj: User?): DataState<AuthViewState> {
                        if (resultObj == null) {
                            return DataState.data(
                                response = Response(
                                    LOGIN_FAILURE,
                                    UIComponentType.SnackBar(),
                                    MessageType.Error()
                                ), null, stateEvent
                            )
                        }
                        val cacheResult =
                            authCacheDataSource.setUser(User(resultObj.email, resultObj.id))

                        if (cacheResult < 0) {
                            return DataState.error(
                                Response(
                                    LOGIN_FAILURE,
                                    UIComponentType.SnackBar(),
                                    MessageType.Error()
                                ), stateEvent
                            )
                        }
                        saveAuthenticatedUserToPrefs(resultObj.email)
                        val sk = saveSecretKeyToPrefs(password, resultObj.id)
                        val deviceId = saveDeviceIdToPrefs(authNetworkDataSource)
                        safeApiCall(IO) {
                            FirebaseFirestore.getInstance().collection(USERS_COLLECTION)
                                .document(resultObj.id).collection(DEVICES_COLLECTION)
                                .document(deviceId!!).set({ }).await()
                        }
                        val result =
                            User(email = resultObj.email, id = resultObj.id, deviceId, sk = sk)
                        return DataState.data(
                            response = Response(
                                LOGIN_SUCCESS,
                                UIComponentType.None(),
                                MessageType.Success()
                            ), AuthViewState(user = result), stateEvent
                        )
                    }

                }.getResult()
            )
        } else {
            printLogD("Login", "Login Error $loginFieldErrors")
            emit(
                buildError(
                    loginFieldErrors,
                    UIComponentType.Toast(),
                    MessageType.Error(),
                    stateEvent
                )
            )
        }
    }

    private fun saveSecretKeyToPrefs(password: String, userId: String): String {
        val secretKeyCharArray = (password + userId).toCharArray()
        var sk: String = ""
        for (c in secretKeyCharArray) {
            sk += (c.code).toString()
            if (sk.length > 10) {
                break
            }
        }
        val uuid = UUID(sk.substring(0, 6).toLong(), sk.substring(6, 10).toLong()).toString()
        sharedPrefsEditor.putString(PreferenceKeys.ENCRYPT_DECRYPT_PREFERENCES, uuid)
        sharedPrefsEditor.apply()
        return uuid
    }


    private fun <ViewState> buildError(
        message: String,
        uiComponentType: UIComponentType,
        messageType: MessageType,
        stateEvent: StateEvent?
    ): DataState<ViewState> {
        return DataState.error(
            response = Response(
                message, uiComponentType, messageType
            ), stateEvent
        )
    }

    private fun saveAuthenticatedUserToPrefs(email: String) {
        sharedPrefsEditor.putString(PreferenceKeys.USER_PREFERENCES, email)
        sharedPrefsEditor.apply()
    }

    private suspend fun saveDeviceIdToPrefs(authNetworkDataSource: AuthNetworkDataSource): String? {
        val deviceId = safeApiCall(IO) {
            authNetworkDataSource.getFirebaseInstallationId()
        }
        val response = object : ApiResponseHandler<String, String>(deviceId, null) {
            override suspend fun handleSuccess(resultObj: String): DataState<String> {
                return DataState.data(null, resultObj, null)
            }

        }.getResult()
        sharedPrefsEditor.putString(PreferenceKeys.DEVICE_PREFERENCES, response.data)
        sharedPrefsEditor.apply()
        return response.data
    }
}