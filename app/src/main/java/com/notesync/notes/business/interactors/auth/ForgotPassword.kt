package com.notesync.notes.business.interactors.auth

import com.notesync.notes.business.data.network.ApiResponseHandler
import com.notesync.notes.business.data.network.abstraction.AuthNetworkDataSource
import com.notesync.notes.business.data.util.safeApiCall
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.framework.presentation.auth.state.AuthViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ForgotPassword(
    private val authNetworkDataSource: AuthNetworkDataSource
) {
    companion object {
        const val FORGOT_PASSWORD_SUCCESS = "Reset password link is sent to your registered email."
    }

    fun forgotPassword(email: String, stateEvent: StateEvent?): Flow<DataState<AuthViewState>?> =
        flow {
            val apiResult = safeApiCall(IO) {
                authNetworkDataSource.forgotPassword(email)
            }

            val response = object : ApiResponseHandler<AuthViewState, Unit>(apiResult, stateEvent) {
                override suspend fun handleSuccess(resultObj: Unit): DataState<AuthViewState> {
                    return DataState.data(
                        Response(
                            FORGOT_PASSWORD_SUCCESS,
                            UIComponentType.SnackBar(),
                            MessageType.Success()
                        ), null, stateEvent
                    )
                }

            }.getResult()

            emit(response)

        }
}