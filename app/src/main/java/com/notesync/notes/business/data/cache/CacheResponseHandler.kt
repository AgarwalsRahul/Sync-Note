package com.notesync.notes.business.data.cache

import com.notesync.notes.business.domain.state.*
import com.notesync.notes.util.printLogD


abstract class CacheResponseHandler<ViewState, Data>(
    private val response: CacheResult<Data?>,
    private val stateEvent: StateEvent?,
) {

    suspend fun getResult(): DataState<ViewState>? {
        return when (response) {
            is CacheResult.GenericError -> {

                DataState.error<ViewState>(
                    response = Response(
                        "${stateEvent?.errorInfo()}\n\nReason: ${response.errorMessage}",
                        uiComponentType = UIComponentType.Dialog(),
                        messageType = MessageType.Error()
                    ),
                    stateEvent
                )
            }
            is CacheResult.Success -> {
                if (response.value == null) {
                    DataState.error<ViewState>(
                        response = Response(
                            "${stateEvent?.errorInfo()}\n\nReason: ${CacheErrors.CACHE_DATA_NULL}",
                            uiComponentType = UIComponentType.Dialog(),
                            messageType = MessageType.Error()
                        ),
                        stateEvent
                    )
                } else {
                    handleSuccess(response.value)
                }

            }
        }
    }

    abstract suspend fun handleSuccess(resultObj: Data): DataState<ViewState>?
}