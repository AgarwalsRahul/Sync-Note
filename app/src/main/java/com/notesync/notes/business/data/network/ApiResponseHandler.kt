package com.notesync.notes.business.data.network

import com.notesync.notes.business.data.cache.CacheErrors
import com.notesync.notes.business.data.cache.CacheResult
import com.notesync.notes.business.data.network.NetworkErrors.NETWORK_DATA_NULL
import com.notesync.notes.business.data.network.NetworkErrors.NETWORK_ERROR
import com.notesync.notes.business.domain.state.*


abstract class ApiResponseHandler<ViewState, Data>(
    private val response: ApiResult<Data?>,
    private val stateEvent: StateEvent?,
) {

    suspend fun getResult(): DataState<ViewState> {
        return when (response) {
            is ApiResult.GenericError -> {
                DataState.error<ViewState>(
                    response = Response(
                        "${stateEvent?.errorInfo()}\n\nReason: ${response.errorMessage}",
                        uiComponentType = UIComponentType.SnackBar(),
                        messageType = MessageType.Error()
                    ),
                    stateEvent
                )
            }
            is ApiResult.Success -> {
                if (response.value == null) {
                    DataState.error<ViewState>(
                        response = Response(
                            "${stateEvent?.errorInfo()}\n\nReason: $NETWORK_DATA_NULL",
                            uiComponentType = UIComponentType.SnackBar(),
                            messageType = MessageType.Error()
                        ),
                        stateEvent
                    )
                } else {
                    handleSuccess(response.value)
                }

            }

            is ApiResult.FirebaseError -> {
                DataState.error(
                    Response(
                        response.errorMessage,
                        UIComponentType.SnackBar(),
                        MessageType.Error()
                    ), stateEvent
                )
            }

            is ApiResult.NetworkError -> {
                DataState.error<ViewState>(
                    response = Response(
                        "${stateEvent?.errorInfo()}\n\nReason: $NETWORK_ERROR",
                        uiComponentType = UIComponentType.Dialog(),
                        messageType = MessageType.Error()
                    ),
                    stateEvent
                )
            }
        }
    }

    abstract suspend fun handleSuccess(resultObj: Data): DataState<ViewState>
}