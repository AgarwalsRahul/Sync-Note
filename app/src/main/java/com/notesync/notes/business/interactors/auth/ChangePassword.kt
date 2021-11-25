package com.notesync.notes.business.interactors.auth

import com.notesync.notes.business.data.network.ApiResponseHandler
import com.notesync.notes.business.data.network.abstraction.AuthNetworkDataSource
import com.notesync.notes.business.data.util.safeApiCall
import com.notesync.notes.business.domain.state.*
import com.notesync.notes.framework.presentation.changePassword.state.ChangePasswordViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ChangePassword(
    private val authNetworkDataSource: AuthNetworkDataSource
) {
    companion object {
        const val CHANGE_PASSWORD_SUCCESS = "Password changed successfully."
    }

    fun changePassword(
        oldPassword: String,
        newPassword: String,
        stateEvent: StateEvent?
    ): Flow<DataState<ChangePasswordViewState>?> =
        flow {
            val apiResult = safeApiCall(Dispatchers.IO) {
                authNetworkDataSource.changePassword(oldPassword, newPassword)
            }

            val response = object : ApiResponseHandler<ChangePasswordViewState, Unit>(apiResult, stateEvent) {
                override suspend fun handleSuccess(resultObj: Unit): DataState<ChangePasswordViewState> {
                    return DataState.data(
                        Response(
                            CHANGE_PASSWORD_SUCCESS,
                            UIComponentType.SnackBar(),
                            MessageType.Success()
                        ), null, stateEvent
                    )
                }

            }.getResult()

            emit(response)

        }
}